package edu.yaprnn.networks;

import static java.util.concurrent.Executors.newFixedThreadPool;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import edu.yaprnn.networks.activation.ActivationFunction;
import edu.yaprnn.networks.loss.LossFunction;
import edu.yaprnn.samples.model.Sample;
import edu.yaprnn.training.selectors.DataSelector;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@JsonDeserialize(builder = MultiLayerNetwork.MultiLayerNetworkBuilder.class)
@Builder
@Getter
public final class MultiLayerNetwork {

  private final int[] layerSizes;
  private final ActivationFunction[] activationFunctions;
  private final float bias;
  private final LossFunction lossFunction;

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
  public void learnOnline(GradientMatrixService gradientMatrixService,
      List<? extends Sample> trainingSamples, DataSelector dataSelector, int maxParallelism,
      float learningRate, float momentum, float decayL1, float decayL2) {

    var tasks = trainingSamples.parallelStream().<Callable<Sample>>map(sample -> () -> {
      var input = dataSelector.input(sample);
      var target = dataSelector.target(sample, activationFunctions[activationFunctions.length - 1]);

      var layerGradients = gradientMatrixService.zeroMatrices(layerSizes);
      var layerWeights1 = gradientMatrixService.copyMatrices(this.layerWeights);
      computeGradients(layerGradients, layerWeights1, input, target);
      applyGradients(layerGradients, layerWeights1, 1, learningRate, momentum, decayL1, decayL2);
      return sample;
    }).toList();

    try (var executor = newFixedThreadPool(maxParallelism, Thread.ofVirtual().factory())) {
      executor.invokeAll(tasks);
    } catch (InterruptedException _) {
    }
  }

  private void computeGradients(float[][] layerGradients, float[][] layerWeights, float[] input,
      float[] target) {
    var layers = feedForward(input, layerWeights);
    var layerIndex = Layer.outputIndex(layers);

    var outputLayer = layers[layerIndex];
    var outputError = lossFunction.computeOutputError(outputLayer.v(), outputLayer.h(), target,
        outputLayer.activationFunction());

    for (layerIndex -= 1; layerIndex >= 0; layerIndex--) {
      var gradients = layerGradients[layerIndex];
      var inputLayer = layers[layerIndex];
      var h = inputLayer.h();
      var w = 0;

      for (var j = 0; j < h.length; j++, w += outputError.length) {
        SimdSupport.sumScalarMultiplicationLaneWise(gradients, outputError, h[j], w, 0,
            outputError.length);
      }
      SimdSupport.sumScalarMultiplicationLaneWise(gradients, outputError, bias, w, 0,
          outputError.length);

      if (layerIndex > 0) {
        outputError = SimdSupport.computeLayerError(inputLayer, outputError,
            layerWeights[layerIndex], activationFunctions[layerIndex]);
      }
    }
  }

  private void applyGradients(float[][] learnedLayerGradients, float[][] layerWeights,
      int batchSize, float learningRate, float momentum, float decayL1, float decayL2) {
    var miniLearningRate = learningRate / (float) Math.max(batchSize, 1);

    assert layerWeights.length == learnedLayerGradients.length;
    assert layerWeights.length == this.layerWeights.length;
    assert layerWeights.length == this.previousLayerGradients.length;

    for (var lw = 0; lw < layerWeights.length; lw++) {
      var weights = layerWeights[lw];
      var gradients = learnedLayerGradients[lw];
      var thisWeights = this.layerWeights[lw];
      var previousGradients = previousLayerGradients[lw];

      SimdSupport.applyGradients(weights, miniLearningRate, momentum, decayL1, decayL2, gradients,
          thisWeights, previousGradients);
    }
  }

  private Layer[] feedForward(float[] input, float[][] layerWeights) {
    var v = input.length == layerSizes[0] ? input : Arrays.copyOf(input, layerSizes[0]);
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

    return SimdSupport.computeMeanSquaredErrorGradient(t, h, v, outputActivationFunction);
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

    for (var row = 0; row < input.length; row++, w += v.length) {
      SimdSupport.sumScalarMultiplicationLaneWise(v, weights, input[row], 0, w,
          v.length);
    }
    SimdSupport.sumScalarMultiplicationLaneWise(v, weights, bias, 0, w, v.length);

    var nextLayerActivationFunction = activationFunctions[nextLayerIndex];
    return new Layer(nextLayerIndex, v, nextLayerActivationFunction.apply(v),
        nextLayerActivationFunction);
  }

  public void learnMiniBatch(GradientMatrixService gradientMatrixService,
      List<? extends Sample> trainingSamples, DataSelector dataSelector, int maxParallelism,
      int batchSize, float learningRate, float momentum, float decayL1, float decayL2) {

    try (var executor = newFixedThreadPool(maxParallelism, Thread.ofVirtual().factory())) {
      for (var batchStart = 0; batchStart < trainingSamples.size(); batchStart += batchSize) {
        var batchEnd = Math.min(batchStart + batchSize, trainingSamples.size());
        var batch = trainingSamples.subList(batchStart, batchEnd)
            .stream()
            .<Callable<float[][]>>map(sample -> () -> {
              var input = dataSelector.input(sample);
              var target = dataSelector.target(sample,
                  activationFunctions[activationFunctions.length - 1]);

              var layerGradients = gradientMatrixService.zeroMatrices(layerSizes);
              computeGradients(layerGradients, layerWeights, input, target);
              return layerGradients;
            })
            .toList();

        var batchLayerGradients = executor.invokeAll(batch)
            .parallelStream()
            .map(Future::resultNow)
            .reduce(gradientMatrixService.zeroMatrices(layerSizes),
                gradientMatrixService::accumulateGradients);

        applyGradients(batchLayerGradients, layerWeights, batchSize, learningRate, momentum,
            decayL1, decayL2);
      }
    } catch (InterruptedException _) {
    }
  }

  public AccuracyResult computeAccuracy(Collection<? extends Sample> samples,
      DataSelector dataSelector) {
    var outputActivationFunction = activationFunctions[activationFunctions.length - 1];

    return samples.parallelStream().map(sample -> {
      var layers = feedForward(dataSelector.input(sample), layerWeights);
      var h = Layer.output(layers).h();

      var target = dataSelector.target(sample, outputActivationFunction);
      var error = lossFunction.computeNetworkError(h, target);
      return AccuracyResult.from(h, target, error);
    }).reduce(AccuracyResult::sum).map(AccuracyResult::average).orElseThrow();
  }

  private float computeNetworkError(float[] h, float[] target) {
    var maxLength = Math.max(h.length, target.length);
    var safeH = Arrays.copyOf(h, maxLength);
    var safeT = Arrays.copyOf(target, maxLength);

    return 0.5f * SimdSupport.sumSquaredError(safeT, safeH);
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
