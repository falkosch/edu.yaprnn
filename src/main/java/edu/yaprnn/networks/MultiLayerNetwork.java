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

      for (var j = 0; j < h.length; j++, w += outputError.length) {
        SimdSupport.sumScalarMultiplicationLaneWise(gradients, outputError, h[j], w, 0, outputError.length);
      }
      SimdSupport.sumScalarMultiplicationLaneWise(gradients, outputError, bias, w, 0, outputError.length);

      if (layerIndex > 0) {
        outputError = SimdSupport.computeLayerError(inputLayer, outputError, layerWeights[layerIndex], activationFunctions[layerIndex]);
      }
    }
  }

  private void applyGradients(float[][] learnedLayerGradients, float[][] layerWeights,
      float learningRate, float momentum, float decayL1, float decayL2) {
    assert layerWeights.length == learnedLayerGradients.length;
    assert layerWeights.length == this.layerWeights.length;
    assert layerWeights.length == this.previousLayerGradients.length;

    for (var i = 0; i < layerWeights.length; i++) {
      var weights = layerWeights[i];
      var gradients = learnedLayerGradients[i];
      var thisWeights = this.layerWeights[i];
      var thisPreviousGradients = this.previousLayerGradients[i];

      SimdSupport.applyGradients(weights, learningRate, momentum, decayL1, decayL2, gradients,
          thisWeights, thisPreviousGradients);
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
    var v = output.v();
    var t = Arrays.copyOf(target, h.length);

    return SimdSupport.computeMeanSquaredErrorGradient(t, h, v, activationFunction);
  }

  private Layer feedForward(Layer inputLayer, float[] weights, int outputLayerIndex) {
    var inputs = inputLayer.h();
    var outputs = new float[layerSizes[outputLayerIndex]];

    var w = 0;
    for (var i = 0; i < inputs.length; i++, w += outputs.length) {
      SimdSupport.sumScalarMultiplicationLaneWise(outputs, weights, inputs[i], 0, w,
          outputs.length);
    }
    SimdSupport.sumScalarMultiplicationLaneWise(outputs, weights, bias, 0, w, outputs.length);

    return new Layer(outputs, activationFunctions[outputLayerIndex].apply(outputs));
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
    var output = outputLayer.h();
    var compareLength = Math.max(output.length, target.length);
    var prediction = Arrays.copyOf(output, compareLength);
    var actual = Arrays.copyOf(target, compareLength);
    return 0.5f * SimdSupport.sumSquaredError(actual, prediction);
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
