package edu.yaprnn.networks;

import edu.yaprnn.functions.ActivationFunction;
import edu.yaprnn.support.RandomConfigurer;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import java.util.Arrays;
import java.util.Random;
import java.util.stream.IntStream;

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
    Arrays.setAll(newLayerWeights,
        i -> activationFunctions[i].randomWeights(random, newLayerWeights[i].length,
            layerSizes[i + 1]));
    return newLayerWeights;
  }

  public float[][] zeroMatrices(int[] layerSizes) {
    // layerSizes[i] + 1 -> adding bias weights to input size
    return IntStream.range(0, layerSizes.length - 1)
        .mapToObj(i -> new float[(layerSizes[i] + 1) * layerSizes[i + 1]]).toArray(float[][]::new);
  }

  public float[][] copyMatrices(float[][] source) {
    return Arrays.stream(source).map(float[]::clone).toArray(float[][]::new);
  }

  public float[][] addGradients(float[][] leftLayers, float[][] rightLayers) {
    assert leftLayers.length == rightLayers.length;
    return IntStream.range(0, leftLayers.length)
        .mapToObj(i -> SimdSupport.addElementWise(leftLayers[i], rightLayers[i]))
        .toArray(float[][]::new);
  }
}
