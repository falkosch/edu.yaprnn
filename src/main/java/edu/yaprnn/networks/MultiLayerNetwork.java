package edu.yaprnn.networks;

import com.google.common.collect.Lists;
import edu.yaprnn.functions.ActivationFunction;
import edu.yaprnn.networks.templates.MultiLayerNetworkTemplate;
import edu.yaprnn.samples.model.Sample;
import edu.yaprnn.training.DataSelector;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@Builder
@Getter
@RequiredArgsConstructor
public class MultiLayerNetwork {

  @Deprecated
  private final MultiLayerNetworkTemplate template;

  private final int[] layerSizes;
  private final ActivationFunction[] activationFunctions;
  private final float bias;

  @Setter
  private String name;

  // [layerSizes.length - 1][each l : (layerSizes[l] + 1) x layerSizes[l + 1]]
  // rows -> from input layer nodes with 1 bias node
  // cols -> to output layer nodes without 1 bias node
  private float[][] layerWeights;
  private float[][] previousLayerGradients;

  public void resetLayerWeights(GradientMatrixService gradientMatrixService) {
    layerWeights = gradientMatrixService.resetLayerWeights(layerSizes, activationFunctions);
    previousLayerGradients = gradientMatrixService.zeroMatrices(layerSizes);
  }

  /**
   * It is hard to parallelize online learning, since weights are immediately updated after each
   * visited sample. Computation of gradients would be affected by "outdated" error vectors, thus
   * may overshoot, undershoot, or even nullify weight changes. This strongly depends on the chosen
   * activation function though, e.g. the identity function is very sensible to this.
   */
  public void learnOnlineParallelized(GradientMatrixService gradientMatrixService,
      List<? extends Sample> trainingSamples, DataSelector dataSelector, float learningRate,
      float momentum, float decayL1, float decayL2) {

    trainingSamples.parallelStream().forEach(sample -> {
      var layerGradients = gradientMatrixService.zeroMatrices(layerSizes);
      var layerWeights = gradientMatrixService.copyMatrices(this.layerWeights);
      var input = dataSelector.input(sample);
      var target = dataSelector.target(sample);
      computeGradients(layerGradients, layerWeights, input, target);
      applyGradients(layerGradients, layerWeights, learningRate, momentum, decayL1, decayL2);
    });
  }

  private void computeGradients(float[][] layerGradients, float[][] layerWeights, float[] input,
      float[] target) {
    var layers = feedForward(input, layerWeights);
    var last = Layer.outputIndex(layers);
    var outputError = computeOutputError(target, layers[last], activationFunctions[last]);
    backPropagate(layerGradients, layerWeights, layers, outputError);
  }

  private void applyGradients(float[][] learnedLayerGradients, float[][] layerWeights,
      float learningRate, float momentum, float decayL1, float decayL2) {
    for (var lw = 0; lw < layerWeights.length; lw++) {
      var weights = layerWeights[lw];
      var gradients = learnedLayerGradients[lw];
      var thisWeights = this.layerWeights[lw];
      var previousGradients = previousLayerGradients[lw];

      for (var w = 0; w < weights.length; w++) {
        var decay = decayL1 * Math.signum(weights[w]) + decayL2 * 2f * weights[w];
        var gradient = momentum * previousGradients[w] - learningRate * (gradients[w] + decay);
        thisWeights[w] += (1f + momentum) * gradient - momentum * previousGradients[w];
        previousGradients[w] = gradient;
      }
    }
  }

  private Layer[] feedForward(float[] input, float[][] layerWeights) {
    var v = Arrays.copyOf(input, layerSizes[0]);
    var layers = new Layer[layerSizes.length];
    layers[0] = new Layer(v, activationFunctions[0].apply(v));
    for (int i = 1, k = 0; i < layers.length; i++, k++) {
      layers[i] = feedForward(layers[k], layerWeights[k], i);
    }
    return layers;
  }

  private float[] computeOutputError(float[] target, Layer output,
      ActivationFunction activationFunction) {
    var h = output.h();
    var t = Arrays.copyOf(target, h.length);
    var error = activationFunction.derivative(h, output.v());
    for (var i = 0; i < h.length; i++) {
      error[i] *= h[i] - t[i];
    }
    return error;
  }

  private void backPropagate(float[][] layerGradients, float[][] layerWeights, Layer[] layers,
      float[] outputError) {
    for (var in = layers.length - 2; in >= 0; in--) {
      var gradients = layerGradients[in];
      var inH = layers[in].h();

      var w = 0;
      for (var row = 0; row < inH.length; row++) {
        var inHRow = inH[row];
        for (var col = 0; col < outputError.length; col++, w++) {
          gradients[w] += inHRow * outputError[col];
        }
      }
      for (var biasCol = 0; biasCol < outputError.length; biasCol++, w++) {
        gradients[w] += outputError[biasCol];
      }

      if (in > 0) {
        outputError = computeLayerError(layers[in], outputError, layerWeights[in],
            activationFunctions[in]);
      }
    }
  }

  private Layer feedForward(Layer inLayer, float[] weights, int outLayer) {
    var inH = inLayer.h();
    var outV = new float[layerSizes[outLayer]];
    var w = 0;
    for (var row = 0; row < inH.length; row++) {
      var in = inH[row];
      for (var col = 0; col < outV.length; col++, w++) {
        outV[col] += in * weights[w];
      }
    }
    for (var biasCol = 0; biasCol < outV.length; biasCol++, w++) {
      outV[biasCol] += weights[w];
    }
    return new Layer(outV, activationFunctions[outLayer].apply(outV));
  }

  private float[] computeLayerError(Layer inLayer, float[] outError, float[] weights,
      ActivationFunction activationFunction) {
    var inError = activationFunction.derivative(inLayer.h(), inLayer.v());
    for (int row = 0, w = 0; row < inError.length; row++) {
      var error = 0f;
      for (var col = 0; col < outError.length; col++, w++) {
        error += weights[w] * outError[col];
      }
      inError[row] *= error;
    }
    return inError;
  }

  public void learnMiniBatch(GradientMatrixService gradientMatrixService,
      List<? extends Sample> trainingSamples, DataSelector dataSelector, int batchSize,
      float learningRate, float momentum, float decayL1, float decayL2) {
    Lists.partition(trainingSamples, batchSize).forEach(batch -> {
      var batchLayerGradients = batch.parallelStream()
          .map(sample -> {
            var layerGradients = gradientMatrixService.zeroMatrices(layerSizes);
            var input = dataSelector.input(sample);
            var target = dataSelector.target(sample);
            computeGradients(layerGradients, layerWeights, input, target);
            return layerGradients;
          })
          .reduce(gradientMatrixService.zeroMatrices(layerSizes),
              gradientMatrixService::sumGradients);

      applyGradients(batchLayerGradients, layerWeights, learningRate, momentum, decayL1, decayL2);
    });
  }

  public AccuracyResult computeAccuracy(Collection<? extends Sample> samples,
      DataSelector dataSelector) {
    return samples.parallelStream().map(sample -> {
      var layers = feedForward(dataSelector.input(sample), layerWeights);
      var output = Layer.output(layers);
      var target = dataSelector.target(sample);
      var error = computeNetworkError(output, target);
      return AccuracyResult.from(output.h(), target, error);
    }).reduce(AccuracyResult::sum).map(AccuracyResult::average).orElseThrow();
  }

  private float computeNetworkError(Layer outputLayer, float[] target) {
    var output = outputLayer.h();
    var maxLength = Math.max(output.length, target.length);
    var outH = Arrays.copyOf(output, maxLength);
    var t = Arrays.copyOf(target, maxLength);
    var error = 0f;
    for (var i = 0; i < maxLength; i++) {
      var d = t[i] - outH[i];
      error += d * d;
    }
    return 0.5f * error;
  }

  public Layer[] feedForward(Sample sample, DataSelector dataSelector) {
    return feedForward(dataSelector.input(sample), layerWeights);
  }

  @Override
  public String toString() {
    return "%s (%s)".formatted(name,
        Arrays.toString(Objects.requireNonNullElseGet(layerSizes, () -> new int[0])));
  }
}
