package edu.yaprnn.networks.activation;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Random;
import org.assertj.core.data.Offset;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class ActivationFunctionTest {

  static final Offset<Float> PRECISION = Offset.offset(0.01f);

  @Test
  void shouldSortActivationFunctionsByToStringViaComparator() {
    var linear = new LinearActivationFunction();
    var sigmoid = new SigmoidActivationFunction();

    assertThat(ActivationFunction.COMPARATOR.compare(linear, sigmoid)).isNotEqualTo(0);
    assertThat(ActivationFunction.COMPARATOR.compare(linear, linear)).isEqualTo(0);
  }

  static void assertAllFinite(float[] values) {
    for (var v : values) {
      assertThat(v).isFinite();
    }
  }

  @Nested
  class LinearCase {

    final LinearActivationFunction fn = new LinearActivationFunction();

    @Test
    void shouldReturnCopyOfInput() {
      var v = new float[]{1f, -2f, 3f};
      var h = fn.apply(v);

      assertThat(h).containsExactly(v);
      assertThat(h).isNotSameAs(v);
    }

    @Test
    void shouldReturnAllOnesDerivative() {
      assertThat(fn.derivative(new float[]{5f, -3f})).containsExactly(1f, 1f);
    }

    @Test
    void shouldInitializeWeights() {
      var w = fn.initialize(new Random(1L), 6, 2);
      assertThat(w).hasSize(6);
      assertAllFinite(w);
    }

    @Test
    void shouldFormatToString() {
      assertThat(fn.toString()).contains("Linear");
    }
  }

  @Nested
  class SigmoidCase {

    final SigmoidActivationFunction fn = new SigmoidActivationFunction();

    @Test
    void shouldMapZeroToHalf() {
      assertThat(fn.apply(new float[]{0f})[0]).isCloseTo(0.5f, PRECISION);
    }

    @Test
    void shouldMapLargePositiveToNearOne() {
      assertThat(fn.apply(new float[]{10f})[0]).isCloseTo(1f, PRECISION);
    }

    @Test
    void shouldMapLargeNegativeToNearZero() {
      assertThat(fn.apply(new float[]{-10f})[0]).isCloseTo(0f, PRECISION);
    }

    @Test
    void shouldComputeDerivativeFromPreActivation() {
      var d = fn.derivative(new float[]{0f});
      assertThat(d[0]).isCloseTo(0.25f, PRECISION);
    }

    @Test
    void shouldComputeDerivativeFromPostActivation() {
      var h = new float[]{0.5f};
      var v = new float[]{0f};
      var d = fn.derivative(h, v);
      assertThat(d[0]).isCloseTo(0.25f, PRECISION);
    }

    @Test
    void shouldInitializeWeights() {
      var w = fn.initialize(new Random(1L), 6, 2);
      assertThat(w).hasSize(6);
      assertAllFinite(w);
    }

    @Test
    void shouldFormatToString() {
      assertThat(fn.toString()).contains("Sigmoid");
    }
  }

  @Nested
  class ReLUCase {

    final ReLUActivationFunction fn = new ReLUActivationFunction();

    @Test
    void shouldPassPositiveValues() {
      assertThat(fn.apply(new float[]{3f, 0.5f})).containsExactly(3f, 0.5f);
    }

    @Test
    void shouldClampNegativeToZero() {
      assertThat(fn.apply(new float[]{-1f, -0.5f})).containsExactly(0f, 0f);
    }

    @Test
    void shouldReturnOneForPositiveDerivative() {
      assertThat(fn.derivative(new float[]{1f, 0f, -1f})).containsExactly(1f, 1f, 0f);
    }

    @Test
    void shouldInitializeWeights() {
      var w = fn.initialize(new Random(1L), 6, 2);
      assertThat(w).hasSize(6);
      assertAllFinite(w);
    }

    @Test
    void shouldFormatToString() {
      assertThat(fn.toString()).contains("ReLU");
    }
  }

  @Nested
  class TanHCase {

    final TangentHyperbolicActivationFunction fn = new TangentHyperbolicActivationFunction();

    @Test
    void shouldMapZeroToZero() {
      assertThat(fn.apply(new float[]{0f})[0]).isCloseTo(0f, PRECISION);
    }

    @Test
    void shouldMapLargePositiveToNearOne() {
      assertThat(fn.apply(new float[]{5f})[0]).isCloseTo(1f, PRECISION);
    }

    @Test
    void shouldComputeDerivativeFromPreActivation() {
      assertThat(fn.derivative(new float[]{0f})[0]).isCloseTo(1f, PRECISION);
    }

    @Test
    void shouldComputeDerivativeFromPostActivation() {
      var h = new float[]{0f};
      var d = fn.derivative(h, new float[]{0f});
      assertThat(d[0]).isCloseTo(1f, PRECISION);
    }

    @Test
    void shouldInitializeWeights() {
      var w = fn.initialize(new Random(1L), 6, 2);
      assertThat(w).hasSize(6);
      assertAllFinite(w);
    }

    @Test
    void shouldFormatToString() {
      assertThat(fn.toString()).contains("TanH");
    }
  }

  @Nested
  class TanHHardCase {

    final TangentHyperbolicHardActivationFunction fn =
        new TangentHyperbolicHardActivationFunction();

    @Test
    void shouldClampToRange() {
      assertThat(fn.apply(new float[]{-5f, 0.5f, 5f}))
          .containsExactly(-1f, 0.5f, 1f);
    }

    @Test
    void shouldReturnOneInsideRangeDerivative() {
      assertThat(fn.derivative(new float[]{0.5f, -1f, 1f}))
          .containsExactly(1f, 0f, 0f);
    }

    @Test
    void shouldInitializeWeights() {
      var w = fn.initialize(new Random(1L), 6, 2);
      assertThat(w).hasSize(6);
      assertAllFinite(w);
    }

    @Test
    void shouldFormatToString() {
      assertThat(fn.toString()).contains("TanH Hard");
    }
  }

  @Nested
  class SoftMaxCase {

    final SoftMaxActivationFunction fn = new SoftMaxActivationFunction();

    @Test
    void shouldSumToOne() {
      var h = fn.apply(new float[]{1f, 2f, 3f});
      var sum = 0f;
      for (var v : h) {
        sum += v;
      }
      assertThat(sum).isCloseTo(1f, PRECISION);
    }

    @Test
    void shouldPreserveOrdering() {
      var h = fn.apply(new float[]{1f, 3f, 2f});
      assertThat(h[1]).isGreaterThan(h[2]);
      assertThat(h[2]).isGreaterThan(h[0]);
    }

    @Test
    void shouldComputeDerivativeFromPostActivation() {
      var h = fn.apply(new float[]{1f, 2f});
      var d = fn.derivative(h, new float[]{1f, 2f});
      for (var i = 0; i < d.length; i++) {
        assertThat(d[i]).isCloseTo(h[i] * (1f - h[i]), PRECISION);
      }
    }

    @Test
    void shouldComputeDerivativeFromPreActivation() {
      var d = fn.derivative(new float[]{1f, 2f});
      assertThat(d).hasSize(2);
      assertAllFinite(d);
    }

    @Test
    void shouldInitializeWeights() {
      var w = fn.initialize(new Random(1L), 6, 2);
      assertThat(w).hasSize(6);
      assertAllFinite(w);
    }

    @Test
    void shouldHandleLargeInputsWithoutOverflow() {
      var h = fn.apply(new float[]{100f, 200f, 300f});
      var sum = 0f;
      for (var v : h) {
        assertThat(v).isFinite();
        sum += v;
      }
      assertThat(sum).isCloseTo(1f, PRECISION);
      // Largest input should dominate
      assertThat(h[2]).isCloseTo(1f, PRECISION);
    }

    @Test
    void shouldHandleLargeNegativeInputs() {
      var h = fn.apply(new float[]{-100f, -200f, -300f});
      var sum = 0f;
      for (var v : h) {
        assertThat(v).isFinite();
        sum += v;
      }
      assertThat(sum).isCloseTo(1f, PRECISION);
    }

    @Test
    void shouldHandleIdenticalInputs() {
      var h = fn.apply(new float[]{5f, 5f, 5f});
      for (var v : h) {
        assertThat(v).isCloseTo(1f / 3f, PRECISION);
      }
    }

    @Test
    void shouldFormatToString() {
      assertThat(fn.toString()).contains("SoftMax");
    }
  }

  @Nested
  class BinaryStepCase {

    final BinaryStepActivationFunction fn = new BinaryStepActivationFunction();

    @Test
    void shouldReturnOneForNonNegative() {
      assertThat(fn.apply(new float[]{0f, 1f, 0.5f})).containsExactly(1f, 1f, 1f);
    }

    @Test
    void shouldReturnZeroForNegative() {
      assertThat(fn.apply(new float[]{-0.1f, -5f})).containsExactly(0f, 0f);
    }

    @Test
    void shouldReturnAllOnesDerivative() {
      assertThat(fn.derivative(new float[]{-1f, 0f, 1f})).containsExactly(1f, 1f, 1f);
    }

    @Test
    void shouldInitializeWeights() {
      var w = fn.initialize(new Random(1L), 6, 2);
      assertThat(w).hasSize(6);
      assertAllFinite(w);
    }

    @Test
    void shouldFormatToString() {
      assertThat(fn.toString()).contains("BinaryStep");
    }
  }

  @Nested
  class SignumCase {

    final SignumActivationFunction fn = new SignumActivationFunction();

    @Test
    void shouldReturnSignOfValues() {
      assertThat(fn.apply(new float[]{-5f, 0f, 3f})).containsExactly(-1f, 0f, 1f);
    }

    @Test
    void shouldReturnAllOnesDerivative() {
      assertThat(fn.derivative(new float[]{-1f, 0f, 1f})).containsExactly(1f, 1f, 1f);
    }

    @Test
    void shouldInitializeWeights() {
      var w = fn.initialize(new Random(1L), 6, 2);
      assertThat(w).hasSize(6);
      assertAllFinite(w);
    }

    @Test
    void shouldFormatToString() {
      assertThat(fn.toString()).contains("Signum");
    }
  }

  @Nested
  class ThresholdCase {

    final ThresholdActivationFunction fn = new ThresholdActivationFunction();

    @Test
    void shouldPassValuesAboveThreshold() {
      assertThat(fn.apply(new float[]{0.5f, 1f})).containsExactly(0.5f, 1f);
    }

    @Test
    void shouldReturnValueBelowThreshold() {
      assertThat(fn.apply(new float[]{0.05f, -1f})).containsExactly(0f, 0f);
    }

    @Test
    void shouldReturnOneAboveThresholdDerivative() {
      assertThat(fn.derivative(new float[]{0.5f, 0.05f})).containsExactly(1f, 0f);
    }

    @Test
    void shouldInitializeWeights() {
      var w = fn.initialize(new Random(1L), 6, 2);
      assertThat(w).hasSize(6);
      assertAllFinite(w);
    }

    @Test
    void shouldFormatToString() {
      assertThat(fn.toString()).contains("Threshold");
    }
  }

  @Nested
  class GeLUCase {

    final GeLUActivationFunction fn = new GeLUActivationFunction();

    @Test
    void shouldMapZeroToZero() {
      assertThat(fn.apply(new float[]{0f})[0]).isCloseTo(0f, PRECISION);
    }

    @Test
    void shouldMapPositiveToPositive() {
      assertThat(fn.apply(new float[]{2f})[0]).isGreaterThan(0f);
    }

    @Test
    void shouldMapNegativeToNearZero() {
      assertThat(fn.apply(new float[]{-3f})[0]).isCloseTo(0f, Offset.offset(0.1f));
    }

    @Test
    void shouldComputeFiniteDerivative() {
      var d = fn.derivative(new float[]{-1f, 0f, 1f});
      assertThat(d).hasSize(3);
      assertAllFinite(d);
    }

    @Test
    void shouldInitializeWeights() {
      var w = fn.initialize(new Random(1L), 6, 2);
      assertThat(w).hasSize(6);
      assertAllFinite(w);
    }

    @Test
    void shouldFormatToString() {
      assertThat(fn.toString()).contains("GeLU");
    }
  }

  @Nested
  class QuickGeLUCase {

    final QuickGeLUActivationFunction fn = new QuickGeLUActivationFunction();

    @Test
    void shouldMapZeroToZero() {
      assertThat(fn.apply(new float[]{0f})[0]).isCloseTo(0f, PRECISION);
    }

    @Test
    void shouldMapPositiveToPositive() {
      assertThat(fn.apply(new float[]{2f})[0]).isGreaterThan(0f);
    }

    @Test
    void shouldComputeDerivativeFromPreActivation() {
      var d = fn.derivative(new float[]{0f, 1f, -1f});
      assertThat(d).hasSize(3);
      assertAllFinite(d);
    }

    @Test
    void shouldComputeDerivativeFromPostActivation() {
      var v = new float[]{1f, -1f};
      var h = fn.apply(v);
      var d = fn.derivative(h, v);
      assertThat(d).hasSize(2);
      assertAllFinite(d);
    }

    @Test
    void shouldInitializeWeights() {
      var w = fn.initialize(new Random(1L), 6, 2);
      assertThat(w).hasSize(6);
      assertAllFinite(w);
    }

    @Test
    void shouldFormatToString() {
      assertThat(fn.toString()).contains("QuickGeLU");
    }
  }
}
