package edu.yaprnn.networks.functions;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import edu.yaprnn.networks.WeightsDimension;
import java.util.Comparator;
import java.util.Random;
import org.apache.commons.math3.random.GaussianRandomGenerator;
import org.apache.commons.math3.random.RandomGeneratorFactory;
import org.apache.commons.math3.random.UniformRandomGenerator;

/**
 * Scales values of an output of a layer so that differences to excessively big values are mitigated
 * and do not dominate classification.
 *
 * <p>Denotation:
 * <ul>
 *   <li>{@code v} - outputs before activation and after matrix-transform by weights between two layers</li>
 *   <li>{@code h} - outputs after activation</li>
 * </ul>
 */
@JsonTypeInfo(use = Id.CLASS)
public interface ActivationFunction {

  Comparator<ActivationFunction> COMPARATOR = Comparator.comparing(ActivationFunction::toString);

  /**
   * @param v outputs before activation
   * @return {@code h} outputs after activation
   */
  float[] apply(float[] v);

  /**
   * @param h outputs after activation
   * @param v outputs before activation
   * @return derivative of outputs
   */
  float[] derivative(float[] h, float[] v);

  /**
   * @param v outputs before activation
   * @return derivative of outputs
   */
  float[] derivative(float[] v);

  float[] initialize(Random random, int count, int outputSize);

  class Initialization {

    private static UniformRandomGenerator createUniformRandomGenerator(Random random) {
      return new UniformRandomGenerator(RandomGeneratorFactory.createRandomGenerator(random));
    }

    private static GaussianRandomGenerator createGaussianRandomGenerator(Random random) {
      return new GaussianRandomGenerator(RandomGeneratorFactory.createRandomGenerator(random));
    }

    public static float[] shell(Random random, int count, int outputSize, Kernel kernel) {
      var weights = new float[count];
      var dimension = WeightsDimension.from(weights, outputSize);
      kernel.accept(random, weights, dimension.inputSize(), outputSize);
      return weights;
    }

    public static void uniform(Random random, float[] weights, int inputSize, int outputSize) {
      var generator = createUniformRandomGenerator(random);

      for (int row = 0, w = 0; row < inputSize; row++) {
        for (var col = 0; col < outputSize; col++, w++) {
          weights[w] = (float) generator.nextNormalizedDouble();
        }
      }
    }

    public static void gaussian(Random random, float[] weights, int inputSize, int outputSize) {
      var generator = createGaussianRandomGenerator(random);

      for (int row = 0, w = 0; row < inputSize; row++) {
        for (var col = 0; col < outputSize; col++, w++) {
          weights[w] = (float) generator.nextNormalizedDouble();
        }
      }
    }

    public static void xavierUniform(Random random, float[] weights, int inputSize,
        int outputSize) {
      var range = (float) Math.sqrt(6d / (inputSize + outputSize));

      for (int row = 0, w = 0; row < inputSize; row++) {
        for (var col = 0; col < outputSize; col++, w++) {
          weights[w] = random.nextFloat(-range, range);
        }
      }
    }

    public static void xavierNormal(Random random, float[] weights, int inputSize, int outputSize) {
      var generator = createGaussianRandomGenerator(random);
      var xavier = 2f / (inputSize + outputSize);

      for (int row = 0, w = 0; row < inputSize; row++) {
        for (var col = 0; col < outputSize; col++, w++) {
          weights[w] = xavier * (float) generator.nextNormalizedDouble();
        }
      }
    }

    public static void heNormal(Random random, float[] weights, int inputSize, int outputSize) {
      var generator = createGaussianRandomGenerator(random);
      var xavier = 2f / inputSize;

      for (int row = 0, w = 0; row < inputSize; row++) {
        for (var col = 0; col < outputSize; col++, w++) {
          weights[w] = xavier * (float) generator.nextNormalizedDouble();
        }
      }
    }

    public static void heUniform(Random random, float[] weights, int inputSize, int outputSize) {
      var range = (float) Math.sqrt(6d / inputSize);

      for (int row = 0, w = 0; row < inputSize; row++) {
        for (var col = 0; col < outputSize; col++, w++) {
          weights[w] = random.nextFloat(-range, range);
        }
      }
    }

    public interface Kernel {

      void accept(Random random, float[] weights, int inputSize, int outputSize);
    }
  }
}
