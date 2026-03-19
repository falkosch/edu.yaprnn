package edu.yaprnn.training.selectors;

import static org.assertj.core.api.Assertions.assertThat;

import edu.yaprnn.networks.activation.LinearActivationFunction;
import edu.yaprnn.samples.model.ImageSample;
import edu.yaprnn.samples.model.SimpleSample;
import java.io.File;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class DataSelectorTest {

  final LinearActivationFunction linear = new LinearActivationFunction();

  final SimpleSample sample = SimpleSample.builder()
      .name("test")
      .labels(new String[]{"A", "B"})
      .target(new float[]{0f, 1f})
      .input(new float[]{0.3f, 0.7f})
      .build();

  final ImageSample imageSample = new ImageSample(
      new File("img"), new File("lbl"), 0, "img_0", "1",
      new float[]{0f, 1f}, 4, 4, new float[16], 2, 2, new float[4]);

  @Nested
  class ClassifierDataSelectorCase {

    final ClassifierDataSelector selector = new ClassifierDataSelector();

    @Test
    void shouldReturnInputFromSample() {
      assertThat(selector.input(sample)).isSameAs(sample.getInput());
    }

    @Test
    void shouldApplyActivationToTarget() {
      var result = selector.target(sample, linear);

      assertThat(result).containsExactly(sample.getTarget());
    }

    @Test
    void shouldPostprocessToOneHotAtArgMax() {
      var h = new float[]{0.1f, 0.9f, 0.3f};

      var result = selector.postprocessOutput(null, h, linear);

      assertThat(result).containsExactly(0f, 1f, 0f);
    }

    @Test
    void shouldReturnOneForOutputWidth() {
      assertThat(selector.getOutputWidth(imageSample)).isEqualTo(1);
    }

    @Test
    void shouldFormatToString() {
      assertThat(selector.toString()).isEqualTo("ClassifierDataSelector");
    }
  }

  @Nested
  class OnlyInputDataSelectorCase {

    final OnlyInputDataSelector selector = new OnlyInputDataSelector();

    @Test
    void shouldReturnInputFromSample() {
      assertThat(selector.input(sample)).isSameAs(sample.getInput());
    }

    @Test
    void shouldUseInputAsTarget() {
      var result = selector.target(sample, linear);

      assertThat(result).containsExactly(sample.getInput());
    }

    @Test
    void shouldReturnInputUnchangedInPostprocess() {
      var v = new float[]{0.5f, 0.5f};
      var h = new float[]{0.3f, 0.7f};

      assertThat(selector.postprocessOutput(v, h, linear)).isSameAs(v);
    }

    @Test
    void shouldReturnInputWidthForOutputWidth() {
      assertThat(selector.getOutputWidth(imageSample)).isEqualTo(2);
    }

    @Test
    void shouldFormatToString() {
      assertThat(selector.toString()).isEqualTo("OnlyInputDataSelector");
    }
  }

  @Nested
  class TargetAsInputDataSelectorCase {

    final TargetAsInputDataSelector selector = new TargetAsInputDataSelector();

    @Test
    void shouldReturnTargetAsInput() {
      assertThat(selector.input(sample)).isSameAs(sample.getTarget());
    }

    @Test
    void shouldApplyActivationToInputAsTarget() {
      var result = selector.target(sample, linear);

      assertThat(result).containsExactly(sample.getInput());
    }

    @Test
    void shouldReturnInputWidthForOutputWidth() {
      assertThat(selector.getOutputWidth(imageSample)).isEqualTo(2);
    }

    @Test
    void shouldFormatToString() {
      assertThat(selector.toString()).isEqualTo("TargetAsInputDataSelector");
    }
  }

  @Nested
  class SuperResolutionDataSelectorCase {

    final SuperResolutionDataSelector selector = new SuperResolutionDataSelector();

    @Test
    void shouldReturnInputFromSample() {
      assertThat(selector.input(sample)).isSameAs(sample.getInput());
    }

    @Test
    void shouldApplyActivationToOriginalAsTarget() {
      var result = selector.target(sample, linear);

      // SimpleSample.getOriginal() returns input
      assertThat(result).containsExactly(sample.getOriginal());
    }

    @Test
    void shouldReturnOriginalWidthForOutputWidth() {
      assertThat(selector.getOutputWidth(imageSample)).isEqualTo(4);
    }

    @Test
    void shouldFormatToString() {
      assertThat(selector.toString()).isEqualTo("SuperResolutionDataSelector");
    }
  }
}
