package edu.yaprnn.networks;

import edu.yaprnn.functions.ActivationFunction;
import edu.yaprnn.support.RandomConfigurer;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import java.util.Arrays;
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
    for (var lw = 0; lw < newLayerWeights.length; lw++) {
      newLayerWeights[lw] = activationFunctions[lw].randomWeights(random,
          newLayerWeights[lw].length, layerSizes[lw + 1]);
    }
    return newLayerWeights;
  }

  public float[][] zeroMatrices(int[] layerSizes) {
    var matrices = new float[layerSizes.length - 1][];
    for (var lw = 0; lw < matrices.length; lw++) {
      matrices[lw] = zeroMatrix(layerSizes[lw], layerSizes[lw + 1]);
    }
    return matrices;
  }

  private float[] zeroMatrix(int inputSize, int outputSize) {
    // adding bias weights to input size
    return new float[(inputSize + 1) * outputSize];
  }

  public float[][] sumGradients(float[][] left, float[][] right) {
    assert left.length == right.length;

    var leftResult = copyMatrices(left);
    for (var lw = 0; lw < leftResult.length; lw++) {
      var leftGradients = leftResult[lw];
      var rightGradients = right[lw];

      assert leftGradients.length == rightGradients.length;
      for (var i = 0; i < leftGradients.length; i++) {
        leftGradients[i] += rightGradients[i];
      }
    }
    return leftResult;
  }

  public float[][] copyMatrices(float[][] layerWeights) {
    var copy = new float[layerWeights.length][];
    for (var lw = 0; lw < layerWeights.length; lw++) {
      var weights = layerWeights[lw];
      copy[lw] = Arrays.copyOf(weights, weights.length);
    }
    return copy;
  }
}
