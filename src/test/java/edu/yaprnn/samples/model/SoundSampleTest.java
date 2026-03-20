package edu.yaprnn.samples.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.File;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class SoundSampleTest {

  static SoundSample create(float[] original) {
    return new SoundSample(new File("test.aiff"), "test", "A",
        new float[]{1f, 0f, 0f, 0f, 0f}, original, original);
  }

  @Nested
  class SubSample {

    @Test
    void shouldProduceRequestedResolution() {
      var original = new float[100];
      for (var i = 0; i < original.length; i++) {
        original[i] = i / 100f;
      }
      var sample = create(original);

      var result = sample.subSample(10, 0f);

      assertThat(result.getInput()).hasSize(10);
    }

    @Test
    void shouldPreserveOriginal() {
      var original = new float[]{1f, 2f, 3f, 4f, 5f};
      var sample = create(original);

      var result = sample.subSample(3, 0f);

      assertThat(result.getOriginal()).isSameAs(original);
    }

    @Test
    void shouldProduceNonNegativeValues() {
      var original = new float[50];
      for (var i = 0; i < original.length; i++) {
        original[i] = (float) Math.abs(Math.sin(i));
      }
      var sample = create(original);

      var result = sample.subSample(10, 0.1f);

      for (var value : result.getInput()) {
        assertThat(value).isGreaterThanOrEqualTo(0f);
      }
    }

    @Test
    void shouldPreserveMetadata() {
      var sample = create(new float[]{1f, 2f, 3f, 4f, 5f});

      var result = sample.subSample(3, 0f);

      assertThat(result.getName()).isEqualTo("test");
      assertThat(result.getLabel()).isEqualTo("A");
      assertThat(result.getFile()).isEqualTo(new File("test.aiff"));
    }

    @Test
    void shouldHandleNarrowWindow() {
      // Very short original with high resolution triggers rightIndex - leftIndex < 2 branch
      var sample = create(new float[]{5f});

      var result = sample.subSample(1, 0f);

      assertThat(result.getInput()).hasSize(1);
    }

    @Test
    void shouldSubSampleWithExplicitLambda() {
      var original = new float[100];
      for (var i = 0; i < original.length; i++) {
        original[i] = i / 100f;
      }
      var sample = create(original);

      var result = sample.subSample(10, 0.1f, 1.01f);

      assertThat(result.getInput()).hasSize(10);
    }

    @Test
    void shouldDelegateToThreeArgSubSample() {
      var sample = create(new float[]{1f, 2f, 3f, 4f, 5f});

      // two-arg delegates to three-arg with lambda=1.005
      var result = sample.subSample(3, 0f);

      assertThat(result.getInput()).hasSize(3);
    }

    @ParameterizedTest
    @ValueSource(floats = {-0.1f, 1.0f, 1.5f})
    void shouldRejectInvalidOverlap(float overlap) {
      var sample = create(new float[]{1f, 2f, 3f, 4f, 5f});

      assertThatThrownBy(() -> sample.subSample(3, overlap, 1.005f))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("Overlap");
    }

    @ParameterizedTest
    @ValueSource(floats = {0.5f, 1.0f, 0.0f, -1.0f})
    void shouldRejectInvalidLambda(float lambda) {
      var sample = create(new float[]{1f, 2f, 3f, 4f, 5f});

      assertThatThrownBy(() -> sample.subSample(3, 0f, lambda))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("Lambda");
    }

    @Test
    void shouldAcceptBoundaryOverlapZero() {
      var sample = create(new float[]{1f, 2f, 3f, 4f, 5f});

      var result = sample.subSample(3, 0f, 1.005f);

      assertThat(result.getInput()).hasSize(3);
    }

    @Test
    void shouldAcceptOverlapJustBelowOne() {
      var sample = create(new float[]{1f, 2f, 3f, 4f, 5f});

      var result = sample.subSample(3, 0.99f, 1.005f);

      assertThat(result.getInput()).hasSize(3);
    }
  }

  @Nested
  class Accessors {

    final SoundSample sample = create(new float[]{1f, 2f, 3f});

    @Test
    void shouldReturnLabels() {
      assertThat(sample.getLabels()).containsExactly("A", "E", "I", "O", "U");
    }

    @Test
    void shouldReturnCachedLabelsArray() {
      assertThat(sample.getLabels()).isSameAs(sample.getLabels());
    }

    @Test
    void shouldCreatePreviewFromOriginal() {
      var image = sample.createPreviewFromOriginal();

      assertThat(image).isNotNull();
      assertThat(image.getWidth(null)).isEqualTo(3);
    }

    @Test
    void shouldCreatePreviewFromInput() {
      var image = sample.createPreviewFromInput();

      assertThat(image).isNotNull();
      assertThat(image.getWidth(null)).isEqualTo(3);
    }

    @Test
    void shouldFormatMetaDescription() {
      var meta = sample.getMetaDescription();

      assertThat(meta).contains("test.aiff");
      assertThat(meta).contains("test");
      assertThat(meta).contains("A");
    }

    @Test
    void shouldFormatToString() {
      assertThat(sample.toString()).isEqualTo("test (A)");
    }
  }
}
