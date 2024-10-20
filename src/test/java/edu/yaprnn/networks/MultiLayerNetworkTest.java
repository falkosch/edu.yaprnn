package edu.yaprnn.networks;

import edu.yaprnn.functions.ActivationFunction;
import edu.yaprnn.functions.IdentityActivationFunction;
import edu.yaprnn.functions.SigmoidActivationFunction;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class MultiLayerNetworkTest {

  final ActivationFunction sigmoid = new SigmoidActivationFunction();
  final ActivationFunction linear = new IdentityActivationFunction();
  final DataSelector dataSelector = new ClassifierDataSelector();

  final TestGradientMatrixService gradientMatrixService = new TestGradientMatrixService();
  Random random;

  MultiLayerNetwork network;
  List<SimpleSample> samples;

  void train() {
    for (var i = 0; i < 500; i++) {
      var learningRate = 0.1f * (float) Math.pow(0.99, i);
      network.learnMiniBatch(gradientMatrixService, samples, dataSelector, samples.size(),
          learningRate, 0f, 0f, 0f);
    }
  }

  @BeforeEach
  void createRandom() {
    random = new SecureRandom(new byte[42]);
  }

  @Nested
  class NotCase {

    /**
     * Optimal weights: [-1,-0.5], last is weight for bias=-1 (McCulloch and Pitts, 1943)
     */
    MultiLayerNetworkTemplate notModel = MultiLayerNetworkTemplate.builder()
        .layers(List.of(LayerTemplate.builder().size(1).activationFunction(linear).build(),
            LayerTemplate.builder().size(1).activationFunction(linear).build()))
        .build();

    @BeforeEach
    void createNetwork() {
      samples = List.of(
          SimpleSample.builder().input(new float[]{0f}).target(new float[]{1f}).build(),
          SimpleSample.builder().input(new float[]{1f}).target(new float[]{0f}).build());
      network = MultiLayerNetwork.builder()
          .bias(-1f)
          .activationFunctions(notModel.collectActivationFunctions())
          .layerSizes(notModel.collectLayerSizes())
          .build();
      network.resetLayerWeights(gradientMatrixService);
    }

    @Test
    void shouldMinimizeTrainingErrorWhenTrained() {
      var initial = network.computeAccuracy(samples, dataSelector);

      train();

      var actual = network.computeAccuracy(samples, dataSelector);
      Assertions.assertThat(actual.error()).isLessThan(initial.error());
    }

    @Test
    void shouldClassifySamplesWithLowError() {
      train();

      for (Sample sample : samples) {
        var layers = network.feedForward(sample, dataSelector);

        Assertions.assertThat(Layer.output(layers).h())
            .containsExactly(sample.getTarget(), Offset.offset(0.499f));
      }
    }
  }

  @Nested
  class AndCase {

    /**
     * Optimal weights: [1, 1, 1.5], last is weight for bias=-1 (McCulloch and Pitts, 1943)
     */
    MultiLayerNetworkTemplate andModel = MultiLayerNetworkTemplate.builder()
        .layers(List.of(LayerTemplate.builder().size(2).activationFunction(linear).build(),
            LayerTemplate.builder().size(1).activationFunction(linear).build()))
        .build();

    @BeforeEach
    void createNetwork() {
      samples = List.of(
          SimpleSample.builder().input(new float[]{0f, 0f}).target(new float[]{0f}).build(),
          SimpleSample.builder().input(new float[]{1f, 0f}).target(new float[]{0f}).build(),
          SimpleSample.builder().input(new float[]{0f, 1f}).target(new float[]{0f}).build(),
          SimpleSample.builder().input(new float[]{1f, 1f}).target(new float[]{1f}).build());
      network = MultiLayerNetwork.builder()
          .bias(-1f)
          .activationFunctions(andModel.collectActivationFunctions())
          .layerSizes(andModel.collectLayerSizes())
          .build();
      network.resetLayerWeights(gradientMatrixService);
    }

    @Test
    void shouldMinimizeTrainingErrorWhenTrained() {
      var initial = network.computeAccuracy(samples, dataSelector);

      train();

      var actual = network.computeAccuracy(samples, dataSelector);
      Assertions.assertThat(actual.error()).isLessThan(initial.error());
    }

    @Test
    void shouldClassifySamplesWithLowError() {
      train();

      for (Sample sample : samples) {
        var layers = network.feedForward(sample, dataSelector);

        Assertions.assertThat(Layer.output(layers).h())
            .containsExactly(sample.getTarget(), Offset.offset(0.499f));
      }
    }
  }

  @Nested
  class OrCase {

    /**
     * Optimal weights: [1, 1, 0.5], last is weight for bias=-1 (McCulloch and Pitts, 1943)
     */
    MultiLayerNetworkTemplate orModel = MultiLayerNetworkTemplate.builder()
        .layers(List.of(LayerTemplate.builder().size(2).activationFunction(linear).build(),
            LayerTemplate.builder().size(1).activationFunction(linear).build()))
        .build();

    @BeforeEach
    void createNetwork() {
      samples = List.of(
          SimpleSample.builder().input(new float[]{0f, 0f}).target(new float[]{0f}).build(),
          SimpleSample.builder().input(new float[]{1f, 0f}).target(new float[]{1f}).build(),
          SimpleSample.builder().input(new float[]{0f, 1f}).target(new float[]{1f}).build(),
          SimpleSample.builder().input(new float[]{1f, 1f}).target(new float[]{1f}).build());
      network = MultiLayerNetwork.builder()
          .bias(-1f)
          .activationFunctions(orModel.collectActivationFunctions())
          .layerSizes(orModel.collectLayerSizes())
          .build();
      network.resetLayerWeights(gradientMatrixService);
    }

    @Test
    void shouldMinimizeTrainingErrorWhenTrained() {
      var initial = network.computeAccuracy(samples, dataSelector);

      train();

      var actual = network.computeAccuracy(samples, dataSelector);
      Assertions.assertThat(actual.error()).isLessThan(initial.error());
    }

    @Test
    void shouldClassifySamplesWithLowError() {
      train();

      for (Sample sample : samples) {
        var layers = network.feedForward(sample, dataSelector);

        Assertions.assertThat(Layer.output(layers).h())
            .containsExactly(sample.getTarget(), Offset.offset(0.499f));
      }
    }
  }

  @Nested
  class XorCase {

    @BeforeEach
    void setUpSamples() {
      samples = List.of(
          SimpleSample.builder().input(new float[]{0f, 0f}).target(new float[]{0f}).build(),
          SimpleSample.builder().input(new float[]{1f, 0f}).target(new float[]{1f}).build(),
          SimpleSample.builder().input(new float[]{0f, 1f}).target(new float[]{1f}).build(),
          SimpleSample.builder().input(new float[]{1f, 1f}).target(new float[]{0f}).build());
    }

    @Nested
    class BadPerceptronLinearCase {

      MultiLayerNetworkTemplate xorModel = MultiLayerNetworkTemplate.builder()
          .layers(List.of(LayerTemplate.builder().size(2).activationFunction(linear).build(),
              LayerTemplate.builder().size(1).activationFunction(linear).build()))
          .build();

      @BeforeEach
      void createNetwork() {
        network = MultiLayerNetwork.builder()
            .bias(-1f)
            .activationFunctions(xorModel.collectActivationFunctions())
            .layerSizes(xorModel.collectLayerSizes())
            .build();
        network.resetLayerWeights(gradientMatrixService);
      }

      @Test
      void shouldNotBeAbleToMinimizeTrainingErrorWhenTrained() {
        var initial = network.computeAccuracy(samples, dataSelector);

        train();

        var actual = network.computeAccuracy(samples, dataSelector);
        Assertions.assertThat(actual.error()).isGreaterThan(initial.error());
      }

      @Test
      void shouldNotClassifySamplesWithLowError() {
        train();

        for (Sample sample : samples) {
          var layers = network.feedForward(sample, dataSelector);

          Assertions.assertThat(Layer.output(layers).h())
              .doesNotContain(sample.getTarget(), Offset.offset(0.499f));
        }
      }
    }

    @Nested
    class BadPerceptronNonLinearCase {

      MultiLayerNetworkTemplate xorModel = MultiLayerNetworkTemplate.builder()
          .layers(List.of(LayerTemplate.builder().size(2).activationFunction(linear).build(),
              LayerTemplate.builder().size(1).activationFunction(sigmoid).build()))
          .build();

      @BeforeEach
      void createNetwork() {
        network = MultiLayerNetwork.builder()
            .bias(-1f)
            .activationFunctions(xorModel.collectActivationFunctions())
            .layerSizes(xorModel.collectLayerSizes())
            .build();
        network.resetLayerWeights(gradientMatrixService);
      }

      @Test
      void shouldNotBeAbleToMinimizeTrainingErrorWhenTrained() {
        var initial = network.computeAccuracy(samples, dataSelector);

        train();

        var actual = network.computeAccuracy(samples, dataSelector);
        Assertions.assertThat(actual.error()).isGreaterThan(initial.error());
      }

      @Test
      void shouldNotClassifySamplesWithLowError() {
        train();

        for (Sample sample : samples) {
          var layers = network.feedForward(sample, dataSelector);

          Assertions.assertThat(Layer.output(layers).h())
              .doesNotContain(sample.getTarget(), Offset.offset(0.499f));
        }
      }
    }

    @Nested
    class BadMultiLayerLinearCase {

      MultiLayerNetworkTemplate xorModel = MultiLayerNetworkTemplate.builder()
          .layers(List.of(LayerTemplate.builder().size(2).activationFunction(linear).build(),
              LayerTemplate.builder().size(2).activationFunction(linear).build(),
              LayerTemplate.builder().size(1).activationFunction(linear).build()))
          .build();

      @BeforeEach
      void createNetwork() {
        network = MultiLayerNetwork.builder()
            .bias(-1f)
            .activationFunctions(xorModel.collectActivationFunctions())
            .layerSizes(xorModel.collectLayerSizes())
            .build();
        network.resetLayerWeights(gradientMatrixService);
      }

      @Test
      void shouldNotBeAbleToMinimizeTrainingErrorWhenTrained() {
        var initial = network.computeAccuracy(samples, dataSelector);

        train();

        var actual = network.computeAccuracy(samples, dataSelector);
        Assertions.assertThat(actual.error()).isGreaterThan(initial.error());
      }

      @Test
      void shouldNotClassifySamplesWithLowError() {
        train();

        for (Sample sample : samples) {
          var layers = network.feedForward(sample, dataSelector);

          Assertions.assertThat(Layer.output(layers).h())
              .doesNotContain(sample.getTarget(), Offset.offset(0.499f));
        }
      }
    }

    @Nested
    class GoodMultiLayerNonLinearCase {

      MultiLayerNetworkTemplate xorModel = MultiLayerNetworkTemplate.builder()
          .layers(List.of(LayerTemplate.builder().size(2).activationFunction(linear).build(),
              LayerTemplate.builder().size(2).activationFunction(sigmoid).build(),
              LayerTemplate.builder().size(1).activationFunction(sigmoid).build()))
          .build();

      @BeforeEach
      void createNetwork() {
        network = MultiLayerNetwork.builder()
            .bias(-1f)
            .activationFunctions(xorModel.collectActivationFunctions())
            .layerSizes(xorModel.collectLayerSizes())
            .build();
        network.resetLayerWeights(gradientMatrixService);
      }

      @Test
      void shouldMinimizeTrainingErrorWhenTrained() {
        var initial = network.computeAccuracy(samples, dataSelector);

        train();

        var actual = network.computeAccuracy(samples, dataSelector);
        Assertions.assertThat(actual.error()).isLessThan(initial.error());
      }

      @Test
      void shouldClassifySamplesWithLowError() {
        train();

        for (Sample sample : samples) {
          var layers = network.feedForward(sample, dataSelector);

          Assertions.assertThat(Layer.output(layers).h())
              .containsExactly(sample.getTarget(), Offset.offset(0.499f));
        }
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
