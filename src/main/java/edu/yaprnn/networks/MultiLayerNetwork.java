package edu.yaprnn.networks;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.google.common.collect.Lists;
import edu.yaprnn.networks.functions.ActivationFunction;
import edu.yaprnn.samples.model.Sample;
import edu.yaprnn.training.DataSelector;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@JsonDeserialize(builder = MultiLayerNetwork.MultiLayerNetworkBuilder.class)
@Builder
@Getter
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

    var outputActivationFunction = activationFunctions[activationFunctions.length - 1];

    trainingSamples.parallelStream().forEach(sample -> {
      var input = dataSelector.input(sample);
      var target = dataSelector.target(sample, outputActivationFunction);

      var layerGradients = gradientMatrixService.zeroMatrices(layerSizes);
      var layerWeights = gradientMatrixService.copyMatrices(this.layerWeights);
      computeGradients(layerGradients, layerWeights, input, target);
      applyGradients(layerGradients, layerWeights, 1, learningRate, momentum, decayL1, decayL2);
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
      int batchSize, float learningRate, float momentum, float decayL1, float decayL2) {
    var miniLearningRate = learningRate / (float) Math.max(batchSize, 1);

    for (var lw = 0; lw < layerWeights.length; lw++) {
      var weights = layerWeights[lw];
      var gradients = learnedLayerGradients[lw];
      var thisWeights = this.layerWeights[lw];
      var previousGradients = previousLayerGradients[lw];

      for (var w = 0; w < weights.length; w++) {
        var decay = decayL1 * Math.signum(weights[w]) + decayL2 * 2f * weights[w];
        var gradient = momentum * previousGradients[w] - miniLearningRate * (gradients[w] + decay);
        thisWeights[w] += (1f + momentum) * gradient - momentum * previousGradients[w];
        previousGradients[w] = gradient;
      }
    }
  }

  private Layer[] feedForward(float[] input, float[][] layerWeights) {
    var v = Arrays.copyOf(input, layerSizes[0]);
    var layers = new Layer[layerSizes.length];
    var inputActivationFunction = activationFunctions[0];
    layers[0] = new Layer(0, v, inputActivationFunction.apply(v), inputActivationFunction);

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

  private Layer feedForward(Layer inputLayer, float[] weights, int nextLayerIndex) {
    var input = inputLayer.h();
    var v = new float[layerSizes[nextLayerIndex]];
    var w = 0;

    for (var row = 0; row < input.length; row++) {
      var value = input[row];
      for (var i = 0; i < v.length; i++, w++) {
        v[i] += weights[w] * value;
      }
    }

    for (var i = 0; i < v.length; i++, w++) {
      v[i] += weights[w] * bias;
    }

    var nextLayerActivationFunction = activationFunctions[nextLayerIndex];
    return new Layer(nextLayerIndex, v, nextLayerActivationFunction.apply(v),
        nextLayerActivationFunction);
  }

  public void learnMiniBatch(GradientMatrixService gradientMatrixService,
      List<? extends Sample> trainingSamples, DataSelector dataSelector, int batchSize,
      float learningRate, float momentum, float decayL1, float decayL2) {
    var outputActivationFunction = activationFunctions[activationFunctions.length - 1];

    Lists.partition(trainingSamples, batchSize).forEach(batch -> {
      var batchLayerGradients = batch.parallelStream()
          .map(sample -> {
            var input = dataSelector.input(sample);
            var target = dataSelector.target(sample, outputActivationFunction);

            var layerGradients = gradientMatrixService.zeroMatrices(layerSizes);
            computeGradients(layerGradients, layerWeights, input, target);
            return layerGradients;
          })
          .reduce(gradientMatrixService.zeroMatrices(layerSizes),
              gradientMatrixService::accumulateGradients);

      applyGradients(batchLayerGradients, layerWeights, batchSize, learningRate, momentum, decayL1,
          decayL2);
    });
  }

  public AccuracyResult computeAccuracy(Collection<? extends Sample> samples,
      DataSelector dataSelector) {
    var outputActivationFunction = activationFunctions[activationFunctions.length - 1];

    return samples.parallelStream().map(sample -> {
      var layers = feedForward(dataSelector.input(sample), layerWeights);
      var h = Layer.output(layers).h();

      var target = dataSelector.target(sample, outputActivationFunction);
      var error = computeNetworkError(h, target);
      return AccuracyResult.from(h, target, error);
    }).reduce(AccuracyResult::sum).map(AccuracyResult::average).orElseThrow();
  }

  private float computeNetworkError(float[] h, float[] target) {
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

  @JsonPOJOBuilder(withPrefix = "")
  public static class MultiLayerNetworkBuilder {
    // Make Jackson use the lombok builder for deserialization
    // https://stackoverflow.com/a/48801237
  }
}
