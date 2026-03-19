package edu.yaprnn.samples.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class SimpleSampleTest {

  final SimpleSample sample = SimpleSample.builder()
      .name("test")
      .labels(new String[]{"A", "B", "C"})
      .target(new float[]{0f, 1f, 0f})
      .input(new float[]{0.5f, 0.8f})
      .build();

  @Test
  void shouldReturnLabelAtOneHotIndex() {
    assertThat(sample.getLabel()).isEqualTo("B");
  }

  @Test
  void shouldReturnFirstLabelWhenFirstIsHot() {
    var s = SimpleSample.builder()
        .name("test")
        .labels(new String[]{"X", "Y"})
        .target(new float[]{1f, 0f})
        .input(new float[]{0.5f})
        .build();

    assertThat(s.getLabel()).isEqualTo("X");
  }

  @Test
  void shouldReturnOriginalSameAsInput() {
    assertThat(sample.getOriginal()).isSameAs(sample.getInput());
  }

  @Test
  void shouldReturnNullFile() {
    assertThat(sample.getFile()).isNull();
  }

  @Test
  void shouldCreatePreviewFromOriginal() {
    var image = sample.createPreviewFromOriginal();

    assertThat(image).isNotNull();
    assertThat(image.getWidth(null)).isEqualTo(2);
    assertThat(image.getHeight(null)).isEqualTo(1);
  }

  @Test
  void shouldCreatePreviewFromInput() {
    var image = sample.createPreviewFromInput();

    assertThat(image).isNotNull();
    assertThat(image.getWidth(null)).isEqualTo(2);
  }

  @Test
  void shouldFormatMetaDescription() {
    var meta = sample.getMetaDescription();

    assertThat(meta).contains("test");
    assertThat(meta).contains("B");
    assertThat(meta).contains("2");
  }
}
