package edu.yaprnn.networks;

import static org.assertj.core.api.Assertions.assertThat;

import org.assertj.core.data.Offset;
import org.junit.jupiter.api.Test;

class AccuracyResultTest {

  static final Offset<Float> PRECISION = Offset.offset(0.001f);

  @Test
  void shouldCountHitWhenArgMaxMatches() {
    var h = new float[]{0.1f, 0.9f};
    var target = new float[]{0f, 1f};

    var result = AccuracyResult.from(h, target, 0.5f);

    assertThat(result.hits()).isCloseTo(1f, PRECISION);
    assertThat(result.count()).isEqualTo(1);
    assertThat(result.error()).isCloseTo(0.5f, PRECISION);
  }

  @Test
  void shouldCountMissWhenArgMaxDiffers() {
    var h = new float[]{0.9f, 0.1f};
    var target = new float[]{0f, 1f};

    var result = AccuracyResult.from(h, target, 0.8f);

    assertThat(result.hits()).isCloseTo(0f, PRECISION);
  }

  @Test
  void shouldSumTwoResults() {
    var a = new AccuracyResult(0.5f, 1, 1f);
    var b = new AccuracyResult(0.3f, 1, 0f);

    var sum = AccuracyResult.sum(a, b);

    assertThat(sum.error()).isCloseTo(0.8f, PRECISION);
    assertThat(sum.count()).isEqualTo(2);
    assertThat(sum.hits()).isCloseTo(1f, PRECISION);
  }

  @Test
  void shouldAverageResult() {
    var result = new AccuracyResult(1.0f, 4, 3f);

    var avg = AccuracyResult.average(result);

    assertThat(avg.error()).isCloseTo(0.25f, PRECISION);
    assertThat(avg.count()).isEqualTo(1);
    assertThat(avg.hits()).isCloseTo(0.75f, PRECISION);
  }
}
