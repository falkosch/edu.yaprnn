package edu.yaprnn.networks.weights;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Random;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class WeightInitializerTest {

  static void assertAllFinite(float[] values) {
    for (var v : values) {
      assertThat(v).isFinite();
    }
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
