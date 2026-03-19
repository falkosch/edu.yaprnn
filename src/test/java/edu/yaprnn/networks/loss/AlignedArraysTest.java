package edu.yaprnn.networks.loss;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class AlignedArraysTest {

  @Test
  void shouldPreserveBothWhenEqualLength() {
    var result = AlignedArrays.of(new float[]{1f, 2f}, new float[]{3f, 4f});

    assertThat(result.h()).containsExactly(1f, 2f);
    assertThat(result.target()).containsExactly(3f, 4f);
    assertThat(result.length()).isEqualTo(2);
  }

  @Test
  void shouldPadTargetWithZerosWhenHIsLonger() {
    var result = AlignedArrays.of(new float[]{1f, 2f, 3f}, new float[]{4f});

    assertThat(result.h()).containsExactly(1f, 2f, 3f);
    assertThat(result.target()).containsExactly(4f, 0f, 0f);
    assertThat(result.length()).isEqualTo(3);
  }

  @Test
  void shouldPadHWithZerosWhenTargetIsLonger() {
    var result = AlignedArrays.of(new float[]{1f}, new float[]{2f, 3f, 4f});

    assertThat(result.h()).containsExactly(1f, 0f, 0f);
    assertThat(result.target()).containsExactly(2f, 3f, 4f);
    assertThat(result.length()).isEqualTo(3);
  }

  @Test
  void shouldNotMutateOriginalArrays() {
    var h = new float[]{1f, 2f};
    var target = new float[]{3f};

    AlignedArrays.of(h, target);

    assertThat(h).containsExactly(1f, 2f);
    assertThat(target).containsExactly(3f);
  }
}
