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
      newLayerWeights[w] = activationFunctions[l].randomWeights(random, newLayerWeights[w].length,
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

  public float[][] copyMatrices(float[][] source) {
    var result = new float[source.length][];
    for (var i = 0; i < source.length; i++) {
      result[i] = source[i].clone();
    }
    return result;
  }
}
