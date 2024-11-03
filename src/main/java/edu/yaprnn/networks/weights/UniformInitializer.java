package edu.yaprnn.networks.weights;

import java.util.Random;
import lombok.experimental.UtilityClass;
import org.apache.commons.math3.random.RandomGeneratorFactory;
import org.apache.commons.math3.random.UniformRandomGenerator;

@UtilityClass
public final class UniformInitializer {

  private static UniformRandomGenerator createUniformRandomGenerator(Random random) {
    return new UniformRandomGenerator(RandomGeneratorFactory.createRandomGenerator(random));
  }

  public static void normalized(Random random, float[] weights, int inputSize, int outputSize) {
    var generator = createUniformRandomGenerator(random);

    for (int row = 0, w = 0; row < inputSize; row++) {
      for (var col = 0; col < outputSize; col++, w++) {
        weights[w] = (float) generator.nextNormalizedDouble();
      }
    }
  }

  public static void xavier(Random random, float[] weights, int inputSize, int outputSize) {
    var range = (float) Math.sqrt(6d / (inputSize + outputSize));

    for (int row = 0, w = 0; row < inputSize; row++) {
      for (var col = 0; col < outputSize; col++, w++) {
        weights[w] = random.nextFloat(-range, range);
      }
    }
  }

  public static void he(Random random, float[] weights, int inputSize, int outputSize) {
    var range = (float) Math.sqrt(6d / inputSize);

    for (int row = 0, w = 0; row < inputSize; row++) {
      for (var col = 0; col < outputSize; col++, w++) {
        weights[w] = random.nextFloat(-range, range);
      }
    }
  }
}
