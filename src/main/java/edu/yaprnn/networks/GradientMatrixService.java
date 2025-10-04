package edu.yaprnn.networks;

import edu.yaprnn.functions.ActivationFunction;
import edu.yaprnn.support.RandomConfigurer;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import java.util.Random;

@Singleton
public class GradientMatrixService {

  @Named(RandomConfigurer.YAPRNN_RANDOM_BEAN)
  @Inject
  Instance<Random> randomInstance;

  public float[][] resetLayerWeights(int[] layerSizes, ActivationFunction[] activationFunctions) {
    return resetLayerWeights(randomInstance.get(), layerSizes, activationFunctions);
  }

  protected float[][] resetLayerWeights(Random random, int[] layerSizes,
      ActivationFunction[] activationFunctions) {
    assert layerSizes.length == activationFunctions.length;

    var newLayerWeights = zeroMatrices(layerSizes);
    for (int w = 0, l = 1; w < newLayerWeights.length; w++, l++) {
      newLayerWeights[w] = activationFunctions[l].initialize(random, newLayerWeights[w].length,
          layerSizes[l]);
    }
    return newLayerWeights;
  }

  public float[][] zeroMatrices(int[] layerSizes) {
    // layerSizes[i] + 1 -> adding bias weights to input size
    var result = new float[layerSizes.length - 1][];
    for (var i = 0; i < result.length; i++) {
      result[i] = new float[(layerSizes[i] + 1) * layerSizes[i + 1]];
    }
    return result;
  }

  public float[][] accumulateGradients(float[][] accumulator, float[][] layersGradients) {
    assert accumulator.length == layersGradients.length;

    var result = new float[accumulator.length][];
    for (var i = 0; i < accumulator.length; i++) {
      var accumulatorGradients = accumulator[i];
      var layerGradients = layersGradients[i];
      assert accumulatorGradients.length == layerGradients.length;

      var resultGradients = new float[accumulatorGradients.length];
      for (var w = 0; w < resultGradients.length; w++) {
        resultGradients[w] = accumulatorGradients[w] + layerGradients[w];
      }
      result[i] = resultGradients;
    }
    return result;
  }

  public float[][] copyMatrices(float[][] source) {
    var result = new float[source.length][];
    for (var i = 0; i < source.length; i++) {
      result[i] = source[i].clone();
    }
    return result;
  }
}
