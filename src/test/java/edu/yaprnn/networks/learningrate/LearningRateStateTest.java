package edu.yaprnn.networks.learningrate;

import static org.assertj.core.api.Assertions.assertThat;

import org.assertj.core.data.Offset;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class LearningRateStateTest {

  static final Offset<Float> PRECISION = Offset.offset(0.0001f);

  @Nested
  class ConstantCase {

    @Test
    void shouldReturnConfiguredRate() {
      var state = ConstantLearningRateState.from(0.01f);
      assertThat(state.current()).isCloseTo(0.01f, PRECISION);
    }

    @Test
    void shouldReturnSameRateAfterUpdate() {
      var state = ConstantLearningRateState.from(0.05f);
      var updated = state.updateRate(0.5f);

      assertThat(updated.current()).isCloseTo(0.05f, PRECISION);
    }

    @Test
    void shouldReturnSelfOnUpdate() {
      var state = ConstantLearningRateState.from(0.01f);
      assertThat(state.updateRate(1f)).isSameAs(state);
    }
  }

  @Nested
  class DynamicCase {

    @Test
    void shouldReturnInitialRate() {
      var state = DynamicLearningRateState.from(1.1f, 0.5f, 0.01f);
      assertThat(state.current()).isCloseTo(0.01f, PRECISION);
    }

    @Test
    void shouldIncreaseRateWhenErrorDecreases() {
      var state = DynamicLearningRateState.from(1.1f, 0.5f, 0.01f);
      // First update sets lastError. Initial lastError is MAX_VALUE, so any error < MAX_VALUE
      // means error decreased -> ascend
      var updated = state.updateRate(0.5f);
      assertThat(updated.current()).isCloseTo(0.01f * 1.1f, PRECISION);
    }

    @Test
    void shouldDecreaseRateWhenErrorIncreases() {
      var state = DynamicLearningRateState.from(1.1f, 0.5f, 0.01f);
      // First: error=0.5, lastError=MAX_VALUE -> decreasing -> ascend
      var after1 = state.updateRate(0.5f);
      // Second: error=0.8, lastError=0.5 -> increasing -> descend
      var after2 = after1.updateRate(0.8f);
      assertThat(after2.current()).isCloseTo(0.01f * 1.1f * 0.5f, PRECISION);
    }

    @Test
    void shouldKeepRateWhenErrorUnchanged() {
      var state = DynamicLearningRateState.from(1.1f, 0.5f, 0.01f);
      var after1 = state.updateRate(0.5f);
      var rate1 = after1.current();
      var after2 = after1.updateRate(0.5f);
      assertThat(after2.current()).isCloseTo(rate1, PRECISION);
    }
  }

  @Nested
  class EpochCase {

    @Test
    void shouldReturnInitialRate() {
      var state = EpochLearningRateState.from(5, 0.5f, 0.1f);
      assertThat(state.current()).isCloseTo(0.1f, PRECISION);
    }

    @Test
    void shouldNotChangeBeforeInterval() {
      var state = EpochLearningRateState.from(5, 0.5f, 0.1f);
      // iterations 0..3: no change (iterations=0 initially, nextRate checks iterations>0 && %5==0)
      var s = (LearningRateState) state;
      for (var i = 0; i < 4; i++) {
        s = s.updateRate(1f);
      }
      assertThat(s.current()).isCloseTo(0.1f, PRECISION);
    }

    @Test
    void shouldDecreaseAtInterval() {
      var state = EpochLearningRateState.from(5, 0.5f, 0.1f);
      // nextRate() reads iterations before increment; descent at iterations=5
      var s = (LearningRateState) state;
      for (var i = 0; i < 6; i++) {
        s = s.updateRate(1f);
      }
      assertThat(s.current()).isCloseTo(0.1f * 0.5f, PRECISION);
    }

    @Test
    void shouldDecreaseAgainAtNextInterval() {
      var state = EpochLearningRateState.from(5, 0.5f, 0.1f);
      var s = (LearningRateState) state;
      for (var i = 0; i < 11; i++) {
        s = s.updateRate(1f);
      }
      assertThat(s.current()).isCloseTo(0.1f * 0.5f * 0.5f, PRECISION);
    }
  }
}
