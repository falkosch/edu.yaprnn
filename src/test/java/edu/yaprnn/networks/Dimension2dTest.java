package edu.yaprnn.networks;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class Dimension2dTest {

  @Test
  void shouldComputeFromWeightsAndOutputSize() {
    var dim = Dimension2d.from(new float[12], 3);
    assertThat(dim.rows()).isEqualTo(4);
    assertThat(dim.cols()).isEqualTo(3);
  }

  @Test
  void shouldReturnInputSizeWithoutBias() {
    var dim = new Dimension2d(5, 3);
    assertThat(dim.inputSize()).isEqualTo(4);
  }

  @Test
  void shouldReturnInputSizeWithBias() {
    var dim = new Dimension2d(5, 3);
    assertThat(dim.inputSizeWithBias()).isEqualTo(5);
  }
}
