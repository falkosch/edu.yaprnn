package edu.yaprnn.networks;

import com.google.common.collect.Lists;
import edu.yaprnn.functions.ActivationFunction;
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
    var layerIndex = Layer.outputIndex(layers);
    var outputError = computeOutputError(target, layers[layerIndex],
        activationFunctions[layerIndex]);

    for (layerIndex -= 1; layerIndex >= 0; layerIndex--) {
      var gradients = layerGradients[layerIndex];
      var inputLayer = layers[layerIndex];
      var h = inputLayer.h();
      var w = 0;

      for (var j = 0; j < h.length; j++) {
        var value = h[j];
        for (var i = 0; i < outputError.length; i++, w++) {
          gradients[w] += outputError[i] * value;
        }
      }
      for (var i = 0; i < outputError.length; i++, w++) {
        gradients[w] += outputError[i] * bias;
      }

      if (layerIndex > 0) {
        outputError = computeLayerError(inputLayer, outputError, layerWeights[layerIndex],
            activationFunctions[layerIndex]);
      }
    }
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

  private float[] computeOutputError(float[] target, Layer outputLayer,
      ActivationFunction outputActivationFunction) {
    var h = outputLayer.h();
    var v = outputLayer.v();
    var t = Arrays.copyOf(target, h.length);
    var error = outputActivationFunction.derivative(h, v);

    for (var i = 0; i < h.length; i++) {
      error[i] *= h[i] - t[i];
    }

    return error;
  }

  private Layer feedForward(Layer inputLayer, float[] weights, int outputLayerIndex) {
    var inputH = inputLayer.h();
    var outputV = new float[layerSizes[outputLayerIndex]];
    var w = 0;

    for (var row = 0; row < inputH.length; row++) {
      var value = inputH[row];
      for (var i = 0; i < outputV.length; i++, w++) {
        outputV[i] += weights[w] * value;
      }
    }

    for (var i = 0; i < outputV.length; i++, w++) {
      outputV[i] += weights[w] * bias;
    }

    return new Layer(outputV, activationFunctions[outputLayerIndex].apply(outputV));
  }

  private float[] computeLayerError(Layer inputLayer, float[] outputError, float[] weights,
      ActivationFunction inputActivationFunction) {
    var layerError = inputActivationFunction.derivative(inputLayer.h(), inputLayer.v());

    for (int j = 0, w = 0; j < layerError.length; j++) {
      var weightedErrorSum = 0f;
      for (var i = 0; i < outputError.length; i++, w++) {
        weightedErrorSum += weights[w] * outputError[i];
      }
      layerError[j] *= weightedErrorSum;
    }

    return layerError;
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
              gradientMatrixService::accumulateGradients);

      applyGradients(batchLayerGradients, layerWeights, learningRate, momentum, decayL1, decayL2);
    });
  }

  public AccuracyResult computeAccuracy(Collection<? extends Sample> samples,
      DataSelector dataSelector) {
    return samples.parallelStream().map(sample -> {
      var layers = feedForward(dataSelector.input(sample), layerWeights);
      var outputLayer = Layer.output(layers);
      var target = dataSelector.target(sample);
      var error = computeNetworkError(outputLayer, target);
      return AccuracyResult.from(outputLayer.h(), target, error);
    }).reduce(AccuracyResult::sum).map(AccuracyResult::average).orElseThrow();
  }

  private float computeNetworkError(Layer outputLayer, float[] target) {
    var h = outputLayer.h();
    var maxLength = Math.max(h.length, target.length);
    var safeH = Arrays.copyOf(h, maxLength);
    var safeT = Arrays.copyOf(target, maxLength);

    var sumSquaredError = 0f;
    for (var i = 0; i < maxLength; i++) {
      var residual = safeT[i] - safeH[i];
      sumSquaredError += residual * residual;
    }
    return 0.5f * sumSquaredError;
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
