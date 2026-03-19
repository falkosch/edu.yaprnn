package edu.yaprnn.samples.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import org.assertj.core.data.Offset;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class ImageSampleTest {

  static final Offset<Float> PRECISION = Offset.offset(0.01f);

  static ImageSample create(int width, int height, float[] original) {
    return new ImageSample(new File("images.idx3"), new File("labels.idx1"), 0, "img_0", "3",
        new float[]{0f, 0f, 0f, 1f, 0f, 0f, 0f, 0f, 0f, 0f},
        width, height, original, width, height, original);
  }

  @Nested
  class SubSample {

    @Test
    void shouldDownsampleToRequestedResolution() {
      var original = new float[16];
      java.util.Arrays.fill(original, 0.5f);
      var sample = create(4, 4, original);

      var result = sample.subSample(4, 0f);

      assertThat(result.getInput()).hasSize(4);
    }

    @Test
    void shouldPreserveUniformValues() {
      var original = new float[16];
      java.util.Arrays.fill(original, 0.8f);
      var sample = create(4, 4, original);

      var result = sample.subSample(4, 0f);

      for (var value : result.getInput()) {
        assertThat(value).isCloseTo(0.8f, PRECISION);
      }
    }

    @Test
    void shouldPreserveOriginalDimensions() {
      var original = new float[16];
      java.util.Arrays.fill(original, 0.5f);
      var sample = create(4, 4, original);

      var result = sample.subSample(4, 0f);

      assertThat(result.getOriginalWidth()).isEqualTo(4);
      assertThat(result.getOriginalHeight()).isEqualTo(4);
      assertThat(result.getOriginal()).isSameAs(original);
    }

    @Test
    void shouldReturnSameResolutionWhenResolutionMatchesOriginal() {
      var original = new float[]{0.1f, 0.2f, 0.3f, 0.4f};
      var sample = create(2, 2, original);

      var result = sample.subSample(4, 0f);

      assertThat(result.getInput()).hasSize(4);
    }
  }

  @Nested
  class Accessors {

    final ImageSample sample = create(2, 2, new float[]{0.1f, 0.2f, 0.3f, 0.4f});

    @Test
    void shouldReturnImagesPackageFile() {
      assertThat(sample.getFile()).isEqualTo(new File("images.idx3"));
    }

    @Test
    void shouldReturnLabels() {
      assertThat(sample.getLabels()).containsExactly("0", "1", "2", "3", "4", "5", "6", "7", "8",
          "9");
    }

    @Test
    void shouldCreatePreviewFromOriginal() {
      var image = sample.createPreviewFromOriginal();

      assertThat(image).isNotNull();
      assertThat(image.getWidth(null)).isEqualTo(2);
      assertThat(image.getHeight(null)).isEqualTo(2);
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

      assertThat(meta).contains("images.idx3");
      assertThat(meta).contains("labels.idx1");
      assertThat(meta).contains("img_0");
      assertThat(meta).contains("3");
    }

    @Test
    void shouldFormatToString() {
      assertThat(sample.toString()).isEqualTo("img_0 (3)");
    }
  }
}
