package edu.yaprnn.networks;

import edu.yaprnn.functions.ActivationFunction;
import edu.yaprnn.functions.TangentHyperbolicActivationFunction;
import edu.yaprnn.networks.templates.LayerTemplate;
import edu.yaprnn.networks.templates.MultiLayerNetworkTemplate;
import edu.yaprnn.samples.model.Sample;
import edu.yaprnn.samples.model.SimpleSample;
import edu.yaprnn.training.ClassifierDataSelector;
import edu.yaprnn.training.DataSelector;
import java.security.SecureRandom;
import java.util.List;
import java.util.Random;
import org.assertj.core.api.Assertions;
import org.assertj.core.data.Offset;
import org.assertj.core.data.Percentage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class MultiLayerNetworkTest {

  final ActivationFunction tanh = new TangentHyperbolicActivationFunction();
  final DataSelector dataSelector = new ClassifierDataSelector();
  MultiLayerNetwork network;
  List<SimpleSample> samples;
  Random random;

  float train(int iterations) {
    for (var i = 0; i < iterations; i++) {
      var learningRate = (float) (0.1 * Math.pow(0.98, i));
      network.learnOnlineParallelized(new TestGradientMatrixService(), samples, dataSelector,
          learningRate, 0.1f, 0f, 0f);
    }
    return network.computeNetworkError(samples, dataSelector);
  }

  @BeforeEach
  void createRandom() {
    random = new SecureRandom(new byte[42]);
  }

  @Nested
  class NotCase {

    @BeforeEach
    void createNetwork() {
      samples = List.of(
          SimpleSample.builder().input(new float[]{-1f}).target(new float[]{1f}).build(),
          SimpleSample.builder().input(new float[]{1f}).target(new float[]{-1f}).build());

      var template = new MultiLayerNetworkTemplate();
      template.addLayer(LayerTemplate.builder().size(1).activationFunction(tanh).build());
      template.addLayer(LayerTemplate.builder().size(1).activationFunction(tanh).build());

      network = MultiLayerNetwork.builder().template(template).bias(1f)
          .activationFunctions(template.collectActivationFunctions())
          .layerSizes(template.collectLayerSizes()).build();
      network.resetLayerWeights(new TestGradientMatrixService());
    }

    @Test
    void shouldMinimizeTrainingErrorWhenTrained() {
      var firstError = network.computeNetworkError(samples, dataSelector);

      var after500 = train(500);
      Assertions.assertThat(after500).isLessThan(firstError);

      Assertions.assertThat(train(2500)).isLessThan(after500);
    }

    @Test
    void shouldConvergeAfterNSteps() {
      var firstError = network.computeNetworkError(samples, dataSelector);
      var after3000 = train(3000);
      Assertions.assertThat(after3000).isNotCloseTo(firstError, Percentage.withPercentage(5));
      Assertions.assertThat(after3000).isCloseTo(train(1), Percentage.withPercentage(5));
    }

    @Test
    void shouldClassifySamplesWithLowError() {
      train(1000);

      for (Sample sample : samples) {
        var layers = network.feedForward(sample, dataSelector);

        Assertions.assertThat(layers[layers.length - 1].h())
            .containsExactly(sample.getTarget(), Offset.offset(0.5f));
      }
    }
  }

  @Nested
  class AndCase {

    @BeforeEach
    void createNetwork() {
      samples = List.of(
          SimpleSample.builder().input(new float[]{-1f, -1f}).target(new float[]{-1f}).build(),
          SimpleSample.builder().input(new float[]{1f, -1f}).target(new float[]{-1f}).build(),
          SimpleSample.builder().input(new float[]{-1f, 1f}).target(new float[]{-1f}).build(),
          SimpleSample.builder().input(new float[]{1f, 1f}).target(new float[]{1f}).build());

      var template = new MultiLayerNetworkTemplate();
      template.addLayer(LayerTemplate.builder().size(2).activationFunction(tanh).build());
      template.addLayer(LayerTemplate.builder().size(1).activationFunction(tanh).build());

      network = MultiLayerNetwork.builder().template(template).bias(1f)
          .activationFunctions(template.collectActivationFunctions())
          .layerSizes(template.collectLayerSizes()).build();
      network.resetLayerWeights(new TestGradientMatrixService());
    }

    @Test
    void shouldMinimizeTrainingErrorWhenTrained() {
      var firstError = network.computeNetworkError(samples, dataSelector);

      var after500 = train(500);
      Assertions.assertThat(after500).isLessThan(firstError);

      Assertions.assertThat(train(2500)).isLessThan(after500);
    }

    @Test
    void shouldConvergeAfterNSteps() {
      var firstError = network.computeNetworkError(samples, dataSelector);
      var after3000 = train(3000);
      Assertions.assertThat(after3000).isNotCloseTo(firstError, Percentage.withPercentage(5));
      Assertions.assertThat(after3000).isCloseTo(train(1), Percentage.withPercentage(5));
    }

    @Test
    void shouldClassifySamplesWithLowError() {
      train(1000);

      for (Sample sample : samples) {
        var layers = network.feedForward(sample, dataSelector);

        Assertions.assertThat(layers[layers.length - 1].h())
            .containsExactly(sample.getTarget(), Offset.offset(0.5f));
      }
    }
  }

  @Nested
  class OrCase {

    @BeforeEach
    void createNetwork() {
      samples = List.of(
          SimpleSample.builder().input(new float[]{-1f, -1f}).target(new float[]{-1f}).build(),
          SimpleSample.builder().input(new float[]{1f, -1f}).target(new float[]{1f}).build(),
          SimpleSample.builder().input(new float[]{-1f, 1f}).target(new float[]{1f}).build(),
          SimpleSample.builder().input(new float[]{1f, 1f}).target(new float[]{1f}).build());

      var template = new MultiLayerNetworkTemplate();
      template.addLayer(LayerTemplate.builder().size(2).activationFunction(tanh).build());
      template.addLayer(LayerTemplate.builder().size(1).activationFunction(tanh).build());

      network = MultiLayerNetwork.builder().template(template).bias(1f)
          .activationFunctions(template.collectActivationFunctions())
          .layerSizes(template.collectLayerSizes()).build();
      network.resetLayerWeights(new TestGradientMatrixService());
    }

    @Test
    void shouldMinimizeTrainingErrorWhenTrained() {
      var firstError = network.computeNetworkError(samples, dataSelector);

      var after500 = train(500);
      Assertions.assertThat(after500).isLessThan(firstError);

      Assertions.assertThat(train(2500)).isLessThan(after500);
    }

    @Test
    void shouldConvergeAfterNSteps() {
      var firstError = network.computeNetworkError(samples, dataSelector);
      var after3000 = train(3000);
      Assertions.assertThat(after3000).isNotCloseTo(firstError, Percentage.withPercentage(5));
      Assertions.assertThat(after3000).isCloseTo(train(1), Percentage.withPercentage(5));
    }

    @Test
    void shouldClassifySamplesWithLowError() {
      train(1000);

      for (Sample sample : samples) {
        var layers = network.feedForward(sample, dataSelector);

        Assertions.assertThat(layers[layers.length - 1].h())
            .containsExactly(sample.getTarget(), Offset.offset(0.5f));
      }
    }
  }

  @Nested
  class XorCase {

    @BeforeEach
    void createNetwork() {
      samples = List.of(
          SimpleSample.builder().input(new float[]{-1f, -1f}).target(new float[]{-1f}).build(),
          SimpleSample.builder().input(new float[]{1f, -1f}).target(new float[]{1f}).build(),
          SimpleSample.builder().input(new float[]{-1f, 1f}).target(new float[]{1f}).build(),
          SimpleSample.builder().input(new float[]{1f, 1f}).target(new float[]{-1f}).build());

      var template = new MultiLayerNetworkTemplate();
      template.addLayer(LayerTemplate.builder().size(2).activationFunction(tanh).build());
      template.addLayer(LayerTemplate.builder().size(3).activationFunction(tanh).build());
      template.addLayer(LayerTemplate.builder().size(1).activationFunction(tanh).build());

      network = MultiLayerNetwork.builder().template(template).bias(1f)
          .activationFunctions(template.collectActivationFunctions())
          .layerSizes(template.collectLayerSizes()).build();
      network.resetLayerWeights(new TestGradientMatrixService());
    }

    @Test
    void shouldMinimizeTrainingErrorWhenTrained() {
      var firstError = network.computeNetworkError(samples, dataSelector);

      var after500 = train(500);
      Assertions.assertThat(after500).isLessThan(firstError);

      Assertions.assertThat(train(2500)).isLessThan(after500);
    }

    @Test
    void shouldConvergeAfterNSteps() {
      var firstError = network.computeNetworkError(samples, dataSelector);
      var after3000 = train(3000);
      Assertions.assertThat(after3000).isNotCloseTo(firstError, Percentage.withPercentage(5));
      Assertions.assertThat(after3000).isCloseTo(train(1), Percentage.withPercentage(5));
    }

    @Test
    void shouldClassifySamplesWithLowError() {
      train(10000);

      for (Sample sample : samples) {
        var layers = network.feedForward(sample, dataSelector);

        Assertions.assertThat(layers[layers.length - 1].h())
            .containsExactly(sample.getTarget(), Offset.offset(0.5f));
      }
    }
  }

  class TestGradientMatrixService extends GradientMatrixService {

    @Override
    public float[][] resetLayerWeights(int[] layerSizes, ActivationFunction[] activationFunctions) {
      return super.resetLayerWeights(random, layerSizes, activationFunctions);
    }
  }
}
