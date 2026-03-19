package edu.yaprnn.networks.loss;

import static org.assertj.core.api.Assertions.assertThat;

import edu.yaprnn.networks.activation.LinearActivationFunction;
import org.assertj.core.data.Offset;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class LossFunctionTest {

  static final Offset<Float> PRECISION = Offset.offset(0.001f);

  final LinearActivationFunction linear = new LinearActivationFunction();

  @Test
  void shouldSortLossFunctionsByToStringViaComparator() {
    var bce = new BinaryCrossEntropyLossFunction();
    var hse = new HalfSquaredErrorLossFunction();

    assertThat(LossFunction.COMPARATOR.compare(bce, hse)).isNotEqualTo(0);
    assertThat(LossFunction.COMPARATOR.compare(bce, bce)).isEqualTo(0);
  }

  @Nested
  class HalfSquaredErrorCase {

    final HalfSquaredErrorLossFunction loss = new HalfSquaredErrorLossFunction();

    @Test
    void shouldComputeZeroErrorForPerfectPrediction() {
      var h = new float[]{1f, 0f};
      var target = new float[]{1f, 0f};

      assertThat(loss.computeNetworkError(h, target)).isCloseTo(0f, PRECISION);
    }

    @Test
    void shouldComputeHalfSumSquaredResiduals() {
      // E = 0.5 * ((0.8-1)^2 + (0.2-0)^2) = 0.5 * (0.04 + 0.04) = 0.04
      var h = new float[]{0.8f, 0.2f};
      var target = new float[]{1f, 0f};

      assertThat(loss.computeNetworkError(h, target)).isCloseTo(0.04f, PRECISION);
    }

    @Test
    void shouldComputeOutputError() {
      var v = new float[]{0.5f, 0.3f};
      var h = new float[]{0.5f, 0.3f}; // linear: h == v
      var target = new float[]{1f, 0f};

      var error = loss.computeOutputError(v, h, target, linear);

      // derivative is 1 for linear, error[i] = (h[i] - target[i])
      assertThat(error[0]).isCloseTo(-0.5f, PRECISION);
      assertThat(error[1]).isCloseTo(0.3f, PRECISION);
    }

    @Test
    void shouldHandleMismatchedLengthsInOutputError() {
      var v = new float[]{0.5f, 0.3f, 0.1f};
      var h = new float[]{0.5f, 0.3f, 0.1f};
      var target = new float[]{1f};

      var error = loss.computeOutputError(v, h, target, linear);

      assertThat(error).hasSize(3);
      // First element: h[0] - target[0] = -0.5
      assertThat(error[0]).isCloseTo(-0.5f, PRECISION);
      // Remaining: h[i] - 0 (assume target=0)
      assertThat(error[1]).isCloseTo(0.3f, PRECISION);
      assertThat(error[2]).isCloseTo(0.1f, PRECISION);
    }

    @Test
    void shouldHandleMismatchedLengthsInNetworkError() {
      var h = new float[]{0.5f};
      var target = new float[]{1f, 0f};

      var error = loss.computeNetworkError(h, target);

      assertThat(error).isGreaterThan(0f);
    }

    @Test
    void shouldFormatToString() {
      assertThat(loss.toString()).contains("HalfSquaredError");
    }
  }

  @Nested
  class MeanSquaredErrorCase {

    final MeanSquaredErrorLossFunction loss = new MeanSquaredErrorLossFunction();

    @Test
    void shouldComputeZeroErrorForPerfectPrediction() {
      var h = new float[]{1f, 0f};
      var target = new float[]{1f, 0f};

      assertThat(loss.computeNetworkError(h, target)).isCloseTo(0f, PRECISION);
    }

    @Test
    void shouldComputeMeanSquaredResiduals() {
      // E = 1/2 * ((0.8-1)^2 + (0.2-0)^2) = 0.5 * 0.08 = 0.04
      var h = new float[]{0.8f, 0.2f};
      var target = new float[]{1f, 0f};

      assertThat(loss.computeNetworkError(h, target)).isCloseTo(0.04f, PRECISION);
    }

    @Test
    void shouldComputeOutputError() {
      var v = new float[]{0.5f, 0.3f};
      var h = new float[]{0.5f, 0.3f};
      var target = new float[]{1f, 0f};

      var error = loss.computeOutputError(v, h, target, linear);

      // normalization = 2/2 = 1, error[i] = (h[i] - target[i])
      assertThat(error[0]).isCloseTo(-0.5f, PRECISION);
      assertThat(error[1]).isCloseTo(0.3f, PRECISION);
    }

    @Test
    void shouldHandleMismatchedLengthsInOutputError() {
      var v = new float[]{0.5f, 0.3f};
      var h = new float[]{0.5f, 0.3f};
      var target = new float[]{1f};

      var error = loss.computeOutputError(v, h, target, linear);

      assertThat(error).hasSize(2);
    }

    @Test
    void shouldFormatToString() {
      assertThat(loss.toString()).contains("MeanSquaredError");
    }
  }

  @Nested
  class BinaryCrossEntropyCase {

    final BinaryCrossEntropyLossFunction loss = new BinaryCrossEntropyLossFunction();

    @Test
    void shouldComputeFiniteErrorForValidPredictions() {
      var h = new float[]{0.9f, 0.1f};
      var target = new float[]{1f, 0f};

      var error = loss.computeNetworkError(h, target);

      assertThat(error).isFinite();
      assertThat(error).isGreaterThanOrEqualTo(0f);
    }

    @Test
    void shouldReturnInfinityWhenLossIsInfinite() {
      // y=1, x=0: -1*log(0) = -(-Inf) = +Inf, isNaN(Inf)=false but Inf is returned
      var h = new float[]{0f};
      var target = new float[]{1f};

      assertThat(loss.computeNetworkError(h, target)).isEqualTo(Float.POSITIVE_INFINITY);
    }

    @Test
    void shouldReturnInfinityWhenLossIsNaN() {
      // y=0, x=0: 0*log(0) = 0*(-Inf) = NaN, isNaN check catches this
      var h = new float[]{0f};
      var target = new float[]{0f};

      assertThat(loss.computeNetworkError(h, target)).isEqualTo(Float.POSITIVE_INFINITY);
    }

    @Test
    void shouldComputeOutputError() {
      var v = new float[]{0.8f, 0.2f};
      var h = new float[]{0.8f, 0.2f};
      var target = new float[]{1f, 0f};

      var error = loss.computeOutputError(v, h, target, linear);

      assertThat(error).hasSize(2);
      assertThat(error[0]).isFinite();
      assertThat(error[1]).isFinite();
    }

    @Test
    void shouldHandleMismatchedLengthsInOutputError() {
      var v = new float[]{0.5f, 0.3f};
      var h = new float[]{0.5f, 0.3f};
      var target = new float[]{0.5f};

      var error = loss.computeOutputError(v, h, target, linear);

      assertThat(error).hasSize(2);
    }

    @Test
    void shouldHandleZeroDenominatorInOutputError() {
      // When x=0 or x=1, xMxx=0, so result should be 0
      var v = new float[]{0f, 1f};
      var h = new float[]{0f, 1f};
      var target = new float[]{0f, 1f};

      var error = loss.computeOutputError(v, h, target, linear);

      assertThat(error[0]).isCloseTo(0f, PRECISION);
      assertThat(error[1]).isCloseTo(0f, PRECISION);
    }

    @Test
    void shouldComputeNonZeroDenominatorInOutputError() {
      // xMxx != 0 branch: h values not 0 or 1
      var v = new float[]{0.8f, 0.2f};
      var h = new float[]{0.8f, 0.2f};
      var target = new float[]{1f, 0f};

      var error = loss.computeOutputError(v, h, target, linear);

      assertThat(error[0]).isFinite();
      assertThat(error[1]).isFinite();
      assertThat(error[0]).isNotEqualTo(0f);
    }

    @Test
    void shouldComputeNonZeroDenominatorInMismatchedOutputError() {
      // xMxx != 0 branch in the remainder loop (h longer than target)
      var v = new float[]{0.8f, 0.3f};
      var h = new float[]{0.8f, 0.3f};
      var target = new float[]{1f};

      var error = loss.computeOutputError(v, h, target, linear);

      assertThat(error[1]).isFinite();
      assertThat(error[1]).isNotEqualTo(0f);
    }

    @Test
    void shouldHandleZeroDenominatorInMismatchedOutputError() {
      // xMxx == 0 in the remainder loop (h=0 or h=1 beyond target length)
      var v = new float[]{0.5f, 0f, 1f};
      var h = new float[]{0.5f, 0f, 1f};
      var target = new float[]{0.5f};

      var error = loss.computeOutputError(v, h, target, linear);

      assertThat(error[1]).isCloseTo(0f, PRECISION);
      assertThat(error[2]).isCloseTo(0f, PRECISION);
    }

    @Test
    void shouldReturnFiniteLossForNonNanCase() {
      var h = new float[]{0.9f, 0.1f};
      var target = new float[]{1f, 0f};

      var result = loss.computeNetworkError(h, target);

      assertThat(result).isFinite();
      assertThat(result).isNotEqualTo(Float.POSITIVE_INFINITY);
    }

    @Test
    void shouldFormatToString() {
      assertThat(loss.toString()).contains("BinaryCrossEntropy");
    }
  }

  @Nested
  class MeanBinaryCrossEntropyCase {

    final MeanBinaryCrossEntropyLossFunction loss = new MeanBinaryCrossEntropyLossFunction();

    @Test
    void shouldComputeFiniteErrorForValidPredictions() {
      var h = new float[]{0.9f, 0.1f};
      var target = new float[]{1f, 0f};

      var error = loss.computeNetworkError(h, target);

      assertThat(error).isFinite();
      assertThat(error).isGreaterThanOrEqualTo(0f);
    }

    @Test
    void shouldBeSmallerThanBceForSameInput() {
      var bce = new BinaryCrossEntropyLossFunction();
      var h = new float[]{0.9f, 0.1f};
      var target = new float[]{1f, 0f};

      var bceError = bce.computeNetworkError(h, target);
      var meanBceError = loss.computeNetworkError(h, target);

      // Mean divides by n, so should be smaller
      assertThat(meanBceError).isLessThan(bceError);
    }

    @Test
    void shouldComputeOutputError() {
      var v = new float[]{0.8f, 0.2f};
      var h = new float[]{0.8f, 0.2f};
      var target = new float[]{1f, 0f};

      var error = loss.computeOutputError(v, h, target, linear);

      assertThat(error).hasSize(2);
    }

    @Test
    void shouldHandleMismatchedLengthsInOutputError() {
      var v = new float[]{0.5f, 0.3f};
      var h = new float[]{0.5f, 0.3f};
      var target = new float[]{0.5f};

      var error = loss.computeOutputError(v, h, target, linear);

      assertThat(error).hasSize(2);
    }

    @Test
    void shouldComputeNonZeroDenominatorInOutputError() {
      // xMxx != 0 branch with matched lengths
      var v = new float[]{0.8f, 0.2f};
      var h = new float[]{0.8f, 0.2f};
      var target = new float[]{1f, 0f};

      var error = loss.computeOutputError(v, h, target, linear);

      assertThat(error[0]).isFinite();
      assertThat(error[0]).isNotEqualTo(0f);
    }

    @Test
    void shouldComputeNonZeroDenominatorInMismatchedOutputError() {
      // xMxx != 0 branch in remainder loop
      var v = new float[]{0.8f, 0.3f};
      var h = new float[]{0.8f, 0.3f};
      var target = new float[]{1f};

      var error = loss.computeOutputError(v, h, target, linear);

      assertThat(error[1]).isFinite();
      assertThat(error[1]).isNotEqualTo(0f);
    }

    @Test
    void shouldHandleZeroDenominatorInOutputError() {
      var v = new float[]{0f, 1f};
      var h = new float[]{0f, 1f};
      var target = new float[]{0f, 1f};

      var error = loss.computeOutputError(v, h, target, linear);

      assertThat(error[0]).isCloseTo(0f, PRECISION);
      assertThat(error[1]).isCloseTo(0f, PRECISION);
    }

    @Test
    void shouldHandleZeroDenominatorInMismatchedOutputError() {
      // xMxx == 0 in the remainder loop
      var v = new float[]{0.5f, 0f, 1f};
      var h = new float[]{0.5f, 0f, 1f};
      var target = new float[]{0.5f};

      var error = loss.computeOutputError(v, h, target, linear);

      assertThat(error[1]).isCloseTo(0f, PRECISION);
      assertThat(error[2]).isCloseTo(0f, PRECISION);
    }

    @Test
    void shouldReturnInfinityWhenLossIsNaN() {
      // y=0, x=0: 0*log(0) = NaN
      var h = new float[]{0f};
      var target = new float[]{0f};

      assertThat(loss.computeNetworkError(h, target)).isEqualTo(Float.POSITIVE_INFINITY);
    }

    @Test
    void shouldReturnFiniteLossForNonNanCase() {
      var h = new float[]{0.9f, 0.1f};
      var target = new float[]{1f, 0f};

      var result = loss.computeNetworkError(h, target);

      assertThat(result).isFinite();
      assertThat(result).isNotEqualTo(Float.POSITIVE_INFINITY);
    }

    @Test
    void shouldFormatToString() {
      assertThat(loss.toString()).contains("MeanBinaryCrossEntropy");
    }
  }
}
