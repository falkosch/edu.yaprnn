package edu.yaprnn.networks.weights;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Random;
import org.assertj.core.data.Offset;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class WeightInitializerTest {

  static void assertAllFinite(float[] values) {
    for (var v : values) {
      assertThat(v).isFinite();
    }
  }

  static float computeStd(float[] values) {
    var mean = 0.0;
    for (var v : values) {
      mean += v;
    }
    mean /= values.length;

    var variance = 0.0;
    for (var v : values) {
      variance += (v - mean) * (v - mean);
    }
    variance /= values.length;
    return (float) Math.sqrt(variance);
  }

  static void assertAnyNonZero(float[] values) {
    var hasNonZero = false;
    for (var v : values) {
      if (v != 0f) {
        hasNonZero = true;
        break;
      }
    }
    assertThat(hasNonZero).isTrue();
  }

  @Nested
  class GaussianCase {

    @Test
    void shouldFillAllWeightsWithGaussian() {
      var weights = new float[6];
      GaussianInitializer.gaussian(new Random(42L), weights, 3, 2);

      assertAllFinite(weights);
      assertAnyNonZero(weights);
    }

    @Test
    void shouldFillAllWeightsWithXavier() {
      var weights = new float[6];
      GaussianInitializer.xavier(new Random(42L), weights, 3, 2);

      assertAllFinite(weights);
      assertAnyNonZero(weights);
    }

    @Test
    void shouldFillAllWeightsWithHe() {
      var weights = new float[6];
      GaussianInitializer.he(new Random(42L), weights, 3, 2);

      assertAllFinite(weights);
      assertAnyNonZero(weights);
    }

    @Test
    void shouldProduceDeterministicResults() {
      var w1 = new float[6];
      var w2 = new float[6];
      GaussianInitializer.gaussian(new Random(42L), w1, 3, 2);
      GaussianInitializer.gaussian(new Random(42L), w2, 3, 2);

      assertThat(w1).containsExactly(w2);
    }

    @Test
    void shouldScaleXavierWithCorrectStandardDeviation() {
      var inputSize = 100;
      var outputSize = 50;
      var weights = new float[inputSize * outputSize];
      GaussianInitializer.xavier(new Random(42L), weights, inputSize, outputSize);

      var expectedStd = (float) Math.sqrt(2.0 / (inputSize + outputSize));
      var empiricalStd = computeStd(weights);

      assertThat(empiricalStd).isCloseTo(expectedStd, Offset.offset(0.01f));
    }

    @Test
    void shouldScaleHeWithCorrectStandardDeviation() {
      var inputSize = 100;
      var outputSize = 50;
      var weights = new float[inputSize * outputSize];
      GaussianInitializer.he(new Random(42L), weights, inputSize, outputSize);

      var expectedStd = (float) Math.sqrt(2.0 / inputSize);
      var empiricalStd = computeStd(weights);

      assertThat(empiricalStd).isCloseTo(expectedStd, Offset.offset(0.01f));
    }
  }

  @Nested
  class UniformCase {

    @Test
    void shouldFillAllWeightsWithNormalized() {
      var weights = new float[6];
      UniformInitializer.normalized(new Random(42L), weights, 3, 2);

      assertAllFinite(weights);
      assertAnyNonZero(weights);
    }

    @Test
    void shouldFillAllWeightsWithXavier() {
      var weights = new float[6];
      UniformInitializer.xavier(new Random(42L), weights, 3, 2);

      var range = (float) Math.sqrt(6d / (3 + 2));
      for (var w : weights) {
        assertThat(w).isBetween(-range, range);
      }
    }

    @Test
    void shouldFillAllWeightsWithHe() {
      var weights = new float[6];
      UniformInitializer.he(new Random(42L), weights, 3, 2);

      var range = (float) Math.sqrt(6d / 3);
      for (var w : weights) {
        assertThat(w).isBetween(-range, range);
      }
    }

    @Test
    void shouldProduceDeterministicResults() {
      var w1 = new float[6];
      var w2 = new float[6];
      UniformInitializer.xavier(new Random(42L), w1, 3, 2);
      UniformInitializer.xavier(new Random(42L), w2, 3, 2);

      assertThat(w1).containsExactly(w2);
    }
  }

  @Nested
  class InitializationCase {

    @Test
    void shouldCreateArrayOfCorrectSize() {
      var w = Initialization.shell(new Random(42L), 6, 2, UniformInitializer::normalized);
      assertThat(w).hasSize(6);
    }

    @Test
    void shouldDelegateToInitializer() {
      var w = Initialization.shell(new Random(42L), 6, 2, UniformInitializer::xavier);
      assertAnyNonZero(w);
    }
  }
}
