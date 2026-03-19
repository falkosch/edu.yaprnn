package edu.yaprnn.support;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class FloatsTest {

  @Nested
  class IndexOf {

    @Test
    void shouldFindExactMatch() {
      assertThat(Floats.indexOf(new float[]{0f, 1f, 2f}, 1f)).isEqualTo(1);
    }

    @Test
    void shouldReturnFirstOccurrence() {
      assertThat(Floats.indexOf(new float[]{1f, 1f, 2f}, 1f)).isEqualTo(0);
    }

    @Test
    void shouldReturnMinusOneWhenNotFound() {
      assertThat(Floats.indexOf(new float[]{0f, 1f, 2f}, 3f)).isEqualTo(-1);
    }

    @Test
    void shouldReturnMinusOneForEmptyArray() {
      assertThat(Floats.indexOf(new float[]{}, 1f)).isEqualTo(-1);
    }
  }

  @Nested
  class ArgMax {

    @Test
    void shouldReturnZeroForSingleElement() {
      assertThat(Floats.argMax(new float[]{5f})).isEqualTo(0);
    }

    @Test
    void shouldReturnIndexOfMaximum() {
      assertThat(Floats.argMax(new float[]{1f, 3f, 2f})).isEqualTo(1);
    }

    @Test
    void shouldReturnFirstIndexOnDuplicateMax() {
      assertThat(Floats.argMax(new float[]{1f, 3f, 3f})).isEqualTo(1);
    }

    @Test
    void shouldHandleNegativeValues() {
      assertThat(Floats.argMax(new float[]{-3f, -1f, -2f})).isEqualTo(1);
    }
  }

  @Nested
  class Max {

    @Test
    void shouldReturnOnlyElement() {
      assertThat(Floats.max(new float[]{42f})).isEqualTo(42f);
    }

    @Test
    void shouldReturnMaximumValue() {
      assertThat(Floats.max(new float[]{1f, 5f, 3f})).isEqualTo(5f);
    }

    @Test
    void shouldHandleAllNegative() {
      assertThat(Floats.max(new float[]{-5f, -1f, -3f})).isEqualTo(-1f);
    }
  }

  @Nested
  class HaveMaxAtSameIndex {

    @Test
    void shouldReturnTrueWhenMaxAtSameIndex() {
      assertThat(Floats.haveMaxAtSameIndex(
          new float[]{0f, 1f, 0f},
          new float[]{0f, 1f, 0f}
      )).isTrue();
    }

    @Test
    void shouldReturnFalseWhenMaxAtDifferentIndex() {
      assertThat(Floats.haveMaxAtSameIndex(
          new float[]{1f, 0f, 0f},
          new float[]{0f, 0f, 1f}
      )).isFalse();
    }

    @Test
    void shouldReturnTrueForSingleElement() {
      assertThat(Floats.haveMaxAtSameIndex(
          new float[]{5f},
          new float[]{3f}
      )).isTrue();
    }
  }
}
