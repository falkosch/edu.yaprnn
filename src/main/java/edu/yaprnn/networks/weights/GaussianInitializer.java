package edu.yaprnn.networks.weights;

import java.util.Random;
import org.apache.commons.math3.random.GaussianRandomGenerator;
import org.apache.commons.math3.random.RandomGeneratorFactory;

public final class GaussianInitializer {

  private static GaussianRandomGenerator createGaussianRandomGenerator(Random random) {
    return new GaussianRandomGenerator(RandomGeneratorFactory.createRandomGenerator(random));
  }

  public static void gaussian(Random random, float[] weights, int inputSize, int outputSize) {
    var generator = createGaussianRandomGenerator(random);

    for (int row = 0, w = 0; row < inputSize; row++) {
      for (var col = 0; col < outputSize; col++, w++) {
        weights[w] = (float) generator.nextNormalizedDouble();
      }
    }
  }

  public static void xavier(Random random, float[] weights, int inputSize, int outputSize) {
    var generator = createGaussianRandomGenerator(random);
    var xavier = 2f / (inputSize + outputSize);

    for (int row = 0, w = 0; row < inputSize; row++) {
      for (var col = 0; col < outputSize; col++, w++) {
        weights[w] = xavier * (float) generator.nextNormalizedDouble();
      }
    }
  }

  public static void he(Random random, float[] weights, int inputSize, int outputSize) {
    var generator = createGaussianRandomGenerator(random);
    var xavier = 2f / inputSize;

    for (int row = 0, w = 0; row < inputSize; row++) {
      for (var col = 0; col < outputSize; col++, w++) {
        weights[w] = xavier * (float) generator.nextNormalizedDouble();
      }
    }
  }
}
