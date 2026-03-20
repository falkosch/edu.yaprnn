package edu.yaprnn.networks;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import edu.yaprnn.networks.activation.ActivationFunction;
import edu.yaprnn.networks.loss.LossFunction;
import edu.yaprnn.samples.model.Sample;
import edu.yaprnn.training.selectors.DataSelector;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
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
    var v = input.length == layerSizes[0] ? input : Arrays.copyOf(input, layerSizes[0]);
    var layers = new Layer[layerSizes.length];
    var inputActivationFunction = activationFunctions[0];
    layers[0] = new Layer(0, v, inputActivationFunction.apply(v), inputActivationFunction);

    for (int i = 1; i < layers.length; i++) {
      layers[i] = feedForward(layers[i - 1], layerWeights[i - 1], i);
    }

    return layers;
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
      ExecutorService executor, List<? extends Sample> trainingSamples,
      DataSelector dataSelector, int maxParallelism, int batchSize, float learningRate,
      float momentum, float decayL1, float decayL2) {
    Objects.requireNonNull(gradientMatrixService, "gradientMatrixService");
    Objects.requireNonNull(executor, "executor");
    Objects.requireNonNull(trainingSamples, "trainingSamples");
    Objects.requireNonNull(dataSelector, "dataSelector");
    if (maxParallelism < 1) {
      throw new IllegalArgumentException("maxParallelism must be >= 1");
    }
    if (batchSize < 1) {
      throw new IllegalArgumentException("batchSize must be >= 1");
    }

    // Pre-allocate per-chunk gradient buffers (P3 fix)
    var chunkGradients = new float[maxParallelism][][];
    for (var t = 0; t < maxParallelism; t++) {
      chunkGradients[t] = gradientMatrixService.zeroMatrices(layerSizes);
    }

    try {
      for (var batchStart = 0; batchStart < trainingSamples.size(); batchStart += batchSize) {
        var batchEnd = Math.min(batchStart + batchSize, trainingSamples.size());
        var batchSamples = trainingSamples.subList(batchStart, batchEnd);

        // Each chunk processes multiple samples, accumulating gradients in one buffer
        var chunkCount = Math.min(maxParallelism, batchSamples.size());
        var chunkTasks = new ArrayList<Callable<float[][]>>(chunkCount);
        for (var c = 0; c < chunkCount; c++) {
          var chunkStart = c * batchSamples.size() / chunkCount;
          var chunkEnd = (c + 1) * batchSamples.size() / chunkCount;
          var gradients = chunkGradients[c];
          chunkTasks.add(() -> {
            gradientMatrixService.zeroFillMatrices(gradients);
            for (var s = chunkStart; s < chunkEnd; s++) {
              var sample = batchSamples.get(s);
              var input = dataSelector.input(sample);
              var target = dataSelector.target(sample,
                  activationFunctions[activationFunctions.length - 1]);
              computeGradients(gradients, layerWeights, input, target);
            }
            return gradients;
          });
        }

        executor.invokeAll(chunkTasks);

        // Merge chunk results — only maxParallelism buffers to reduce
        for (var c = 1; c < chunkCount; c++) {
          gradientMatrixService.accumulateGradientsInPlace(chunkGradients[0], chunkGradients[c]);
        }

        applyGradients(chunkGradients[0], layerWeights, batchSize, learningRate, momentum,
            decayL1, decayL2);
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new RuntimeException("Training interrupted", e);
    }
  }

  public AccuracyResult computeAccuracy(Collection<? extends Sample> samples,
      DataSelector dataSelector) {
    Objects.requireNonNull(samples, "samples");
    Objects.requireNonNull(dataSelector, "dataSelector");

    var outputActivationFunction = activationFunctions[activationFunctions.length - 1];

    return samples.stream().map(sample -> {
      var layers = feedForward(dataSelector.input(sample), layerWeights);
      var h = Layer.output(layers).h();

      var target = dataSelector.target(sample, outputActivationFunction);
      var error = lossFunction.computeNetworkError(h, target);
      return AccuracyResult.from(h, target, error);
    }).reduce(AccuracyResult::sum).map(AccuracyResult::average).orElseThrow();
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
