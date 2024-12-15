package edu.yaprnn.networks;

import static org.assertj.core.api.Assertions.assertThat;

import edu.yaprnn.networks.activation.ActivationFunction;
import edu.yaprnn.networks.activation.GeLUActivationFunction;
import edu.yaprnn.networks.activation.LinearActivationFunction;
import edu.yaprnn.networks.loss.HalfSquaredErrorLossFunction;
import edu.yaprnn.networks.loss.LossFunction;
import edu.yaprnn.networks.templates.LayerTemplate;
import edu.yaprnn.networks.templates.MultiLayerNetworkTemplate;
import edu.yaprnn.samples.model.Sample;
import edu.yaprnn.samples.model.SimpleSample;
import edu.yaprnn.training.ClassifierDataSelector;
import edu.yaprnn.training.DataSelector;
import java.security.SecureRandom;
import java.util.List;
import java.util.Random;
import org.assertj.core.data.Offset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class MultiLayerNetworkTest {

  final ActivationFunction nonlinear = new GeLUActivationFunction();
  final ActivationFunction linear = new LinearActivationFunction();
  final DataSelector dataSelector = new ClassifierDataSelector();
  final LossFunction lossFunction = new HalfSquaredErrorLossFunction();

  final TestGradientMatrixService gradientMatrixService = new TestGradientMatrixService();
  Random random;

  MultiLayerNetwork network;
  List<SimpleSample> samples;

  void train(int epochs) {
    for (var i = 0; i < epochs; i++) {
      network.learnMiniBatch(gradientMatrixService, samples, dataSelector, samples.size(), 0.2f,
          0.2f, 0f, 0f);
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
    final MultiLayerNetworkTemplate notModel = MultiLayerNetworkTemplate.builder()
        .lossFunction(lossFunction)
        .layers(List.of(LayerTemplate.builder().size(1).activationFunction(linear).build(),
            LayerTemplate.builder().size(1).activationFunction(linear).build()))
        .build();

    @BeforeEach
    void setupSamples() {
      samples = List.of(
          SimpleSample.builder().input(new float[]{0f}).target(new float[]{1f}).build(),
          SimpleSample.builder().input(new float[]{0.25f}).target(new float[]{1f}).build(),
          SimpleSample.builder().input(new float[]{0.75f}).target(new float[]{0f}).build(),
          SimpleSample.builder().input(new float[]{1f}).target(new float[]{0f}).build());
    }

    @BeforeEach
    void setupNetwork() {
      network = MultiLayerNetwork.builder()
          .bias(-1f)
          .activationFunctions(notModel.collectActivationFunctions())
          .layerSizes(notModel.collectLayerSizes())
          .lossFunction(notModel.getLossFunction())
          .build();
      network.resetLayerWeights(gradientMatrixService);
    }

    @Test
    void shouldMinimizeTrainingErrorWhenTrained() {
      var initial = network.computeAccuracy(samples, dataSelector);

      train(100);

      var actual = network.computeAccuracy(samples, dataSelector);
      assertThat(actual.error()).isLessThan(initial.error());
    }

    @Test
    void shouldClassifyAllSamplesAccurately() {
      train(100);

      var result = network.computeAccuracy(samples, dataSelector);
      assertThat(result.hits()).isGreaterThan(0.9f);
    }
  }

  @Nested
  class AndCase {

    /**
     * Optimal weights: [1, 1, 1.5], last is weight for bias=-1 (McCulloch and Pitts, 1943)
     */
    final MultiLayerNetworkTemplate andModel = MultiLayerNetworkTemplate.builder()
        .lossFunction(lossFunction)
        .layers(List.of(LayerTemplate.builder().size(2).activationFunction(linear).build(),
            LayerTemplate.builder().size(1).activationFunction(linear).build()))
        .build();

    @BeforeEach
    void setupSamples() {
      samples = List.of(
          SimpleSample.builder().input(new float[]{0f, 0f}).target(new float[]{0f}).build(),
          SimpleSample.builder().input(new float[]{0.25f, 0f}).target(new float[]{0f}).build(),
          SimpleSample.builder().input(new float[]{0.75f, 0f}).target(new float[]{0f}).build(),
          SimpleSample.builder().input(new float[]{1f, 0f}).target(new float[]{0f}).build(),

          SimpleSample.builder().input(new float[]{0f, 0.25f}).target(new float[]{0f}).build(),
          SimpleSample.builder().input(new float[]{0.25f, 0.25f}).target(new float[]{0f}).build(),
          SimpleSample.builder().input(new float[]{0.75f, 0.25f}).target(new float[]{0f}).build(),
          SimpleSample.builder().input(new float[]{1f, 0.25f}).target(new float[]{0f}).build(),

          SimpleSample.builder().input(new float[]{0f, 0.75f}).target(new float[]{0f}).build(),
          SimpleSample.builder().input(new float[]{0.25f, 0.75f}).target(new float[]{0f}).build(),
          SimpleSample.builder().input(new float[]{0.75f, 0.75f}).target(new float[]{1f}).build(),
          SimpleSample.builder().input(new float[]{1f, 0.75f}).target(new float[]{1f}).build(),

          SimpleSample.builder().input(new float[]{0f, 1f}).target(new float[]{0f}).build(),
          SimpleSample.builder().input(new float[]{0.25f, 1f}).target(new float[]{0f}).build(),
          SimpleSample.builder().input(new float[]{0.75f, 1f}).target(new float[]{1f}).build(),
          SimpleSample.builder().input(new float[]{1f, 1f}).target(new float[]{1f}).build());
    }

    @BeforeEach
    void setupNetwork() {
      network = MultiLayerNetwork.builder()
          .bias(-1f)
          .activationFunctions(andModel.collectActivationFunctions())
          .layerSizes(andModel.collectLayerSizes())
          .lossFunction(andModel.getLossFunction())
          .build();
      network.resetLayerWeights(gradientMatrixService);
    }

    @Test
    void shouldMinimizeTrainingErrorWhenTrained() {
      var initial = network.computeAccuracy(samples, dataSelector);

      train(100);

      var actual = network.computeAccuracy(samples, dataSelector);
      assertThat(actual.error()).isLessThan(initial.error());
    }

    @Test
    void shouldClassifySamplesWithLowError() {
      train(100);

      for (Sample sample : samples) {
        var layers = network.feedForward(sample, dataSelector);

        assertThat(Layer.output(layers).h()).containsExactly(sample.getTarget(),
            Offset.offset(0.499f));
      }
    }
  }

  @Nested
  class OrCase {

    /**
     * Optimal weights: [1, 1, 0.5], last is weight for bias=-1 (McCulloch and Pitts, 1943)
     */
    final MultiLayerNetworkTemplate orModel = MultiLayerNetworkTemplate.builder()
        .lossFunction(lossFunction)
        .layers(List.of(LayerTemplate.builder().size(2).activationFunction(linear).build(),
            LayerTemplate.builder().size(1).activationFunction(linear).build()))
        .build();

    @BeforeEach
    void setupSamples() {
      samples = List.of(
          SimpleSample.builder().input(new float[]{0f, 0f}).target(new float[]{0f}).build(),
          SimpleSample.builder().input(new float[]{0.25f, 0f}).target(new float[]{0f}).build(),
          SimpleSample.builder().input(new float[]{0.75f, 0f}).target(new float[]{1f}).build(),
          SimpleSample.builder().input(new float[]{1f, 0f}).target(new float[]{1f}).build(),

          SimpleSample.builder().input(new float[]{0f, 0.25f}).target(new float[]{0f}).build(),
          SimpleSample.builder().input(new float[]{0.25f, 0.25f}).target(new float[]{0f}).build(),
          SimpleSample.builder().input(new float[]{0.75f, 0.25f}).target(new float[]{1f}).build(),
          SimpleSample.builder().input(new float[]{1f, 0.25f}).target(new float[]{1f}).build(),

          SimpleSample.builder().input(new float[]{0f, 0.75f}).target(new float[]{1f}).build(),
          SimpleSample.builder().input(new float[]{0.25f, 0.75f}).target(new float[]{1f}).build(),
          SimpleSample.builder().input(new float[]{0.75f, 0.75f}).target(new float[]{1f}).build(),
          SimpleSample.builder().input(new float[]{1f, 0.75f}).target(new float[]{1f}).build(),

          SimpleSample.builder().input(new float[]{0f, 1f}).target(new float[]{1f}).build(),
          SimpleSample.builder().input(new float[]{0.25f, 1f}).target(new float[]{1f}).build(),
          SimpleSample.builder().input(new float[]{0.75f, 1f}).target(new float[]{1f}).build(),
          SimpleSample.builder().input(new float[]{1f, 1f}).target(new float[]{1f}).build());
    }

    @BeforeEach
    void setupNetwork() {
      network = MultiLayerNetwork.builder()
          .bias(-1f)
          .activationFunctions(orModel.collectActivationFunctions())
          .layerSizes(orModel.collectLayerSizes())
          .lossFunction(orModel.getLossFunction())
          .build();
      network.resetLayerWeights(gradientMatrixService);
    }

    @Test
    void shouldMinimizeTrainingErrorWhenTrained() {
      var initial = network.computeAccuracy(samples, dataSelector);

      train(100);

      var actual = network.computeAccuracy(samples, dataSelector);
      assertThat(actual.error()).isLessThan(initial.error());
    }

    @Test
    void shouldClassifySamplesWithLowError() {
      train(100);

      for (Sample sample : samples) {
        var layers = network.feedForward(sample, dataSelector);

        assertThat(Layer.output(layers).h()).containsExactly(sample.getTarget(),
            Offset.offset(0.499f));
      }
    }
  }

  @Nested
  class XorCase {

    @BeforeEach
    void setupSamples() {
      samples = List.of(
          SimpleSample.builder().input(new float[]{0f, 0f}).target(new float[]{0f}).build(),
          SimpleSample.builder().input(new float[]{0.25f, 0f}).target(new float[]{0f}).build(),
          SimpleSample.builder().input(new float[]{0.75f, 0f}).target(new float[]{1f}).build(),
          SimpleSample.builder().input(new float[]{1f, 0f}).target(new float[]{1f}).build(),

          SimpleSample.builder().input(new float[]{0f, 0.25f}).target(new float[]{0f}).build(),
          SimpleSample.builder().input(new float[]{0.25f, 0.25f}).target(new float[]{0f}).build(),
          SimpleSample.builder().input(new float[]{0.75f, 0.25f}).target(new float[]{1f}).build(),
          SimpleSample.builder().input(new float[]{1f, 0.25f}).target(new float[]{1f}).build(),

          SimpleSample.builder().input(new float[]{0f, 0.75f}).target(new float[]{1f}).build(),
          SimpleSample.builder().input(new float[]{0.25f, 0.75f}).target(new float[]{1f}).build(),
          SimpleSample.builder().input(new float[]{0.75f, 0.75f}).target(new float[]{0f}).build(),
          SimpleSample.builder().input(new float[]{1f, 0.75f}).target(new float[]{0f}).build(),

          SimpleSample.builder().input(new float[]{0f, 1f}).target(new float[]{1f}).build(),
          SimpleSample.builder().input(new float[]{0.25f, 1f}).target(new float[]{1f}).build(),
          SimpleSample.builder().input(new float[]{0.75f, 1f}).target(new float[]{0f}).build(),
          SimpleSample.builder().input(new float[]{1f, 1f}).target(new float[]{0f}).build());
    }

    @Nested
    class BadPerceptronLinearCase {

      final MultiLayerNetworkTemplate xorModel = MultiLayerNetworkTemplate.builder()
          .lossFunction(lossFunction)
          .layers(List.of(LayerTemplate.builder().size(2).activationFunction(linear).build(),
              LayerTemplate.builder().size(1).activationFunction(linear).build()))
          .build();

      @BeforeEach
      void setupNetwork() {
        network = MultiLayerNetwork.builder()
            .bias(-1f)
            .activationFunctions(xorModel.collectActivationFunctions())
            .layerSizes(xorModel.collectLayerSizes())
            .lossFunction(xorModel.getLossFunction())
            .build();
        network.resetLayerWeights(gradientMatrixService);
      }

      @Test
      void shouldMinimizeTrainingErrorWhenTrained() {
        var initial = network.computeAccuracy(samples, dataSelector);

        train(500);

        var actual = network.computeAccuracy(samples, dataSelector);
        assertThat(actual.error()).isLessThan(initial.error());
      }

      @Test
      void shouldClassifySamplesWithLowError() {
        train(500);

        for (Sample sample : samples) {
          var layers = network.feedForward(sample, dataSelector);

          assertThat(Layer.output(layers).h()).containsExactly(sample.getTarget(),
              Offset.offset(0.499f));
        }
      }
    }

    @Nested
    class BadPerceptronNonLinearCase {

      final MultiLayerNetworkTemplate xorModel = MultiLayerNetworkTemplate.builder()
          .lossFunction(lossFunction)
          .layers(List.of(LayerTemplate.builder().size(2).activationFunction(linear).build(),
              LayerTemplate.builder().size(1).activationFunction(nonlinear).build()))
          .build();

      @BeforeEach
      void setupNetwork() {
        network = MultiLayerNetwork.builder()
            .bias(-1f)
            .activationFunctions(xorModel.collectActivationFunctions())
            .layerSizes(xorModel.collectLayerSizes())
            .lossFunction(xorModel.getLossFunction())
            .build();
        network.resetLayerWeights(gradientMatrixService);
      }

      @Test
      void shouldMinimizeTrainingErrorWhenTrained() {
        var initial = network.computeAccuracy(samples, dataSelector);

        train(500);

        var actual = network.computeAccuracy(samples, dataSelector);
        assertThat(actual.error()).isLessThan(initial.error());
      }

      @Test
      void shouldClassifySamplesWithLowError() {
        train(500);

        for (Sample sample : samples) {
          var layers = network.feedForward(sample, dataSelector);

          assertThat(Layer.output(layers).h()).containsExactly(sample.getTarget(),
              Offset.offset(0.499f));
        }
      }
    }

    @Nested
    class BadMultiLayerLinearCase {

      final MultiLayerNetworkTemplate xorModel = MultiLayerNetworkTemplate.builder()
          .lossFunction(lossFunction)
          .layers(List.of(LayerTemplate.builder().size(2).activationFunction(linear).build(),
              LayerTemplate.builder().size(2).activationFunction(linear).build(),
              LayerTemplate.builder().size(1).activationFunction(linear).build()))
          .build();

      @BeforeEach
      void setupNetwork() {
        network = MultiLayerNetwork.builder()
            .bias(-1f)
            .activationFunctions(xorModel.collectActivationFunctions())
            .layerSizes(xorModel.collectLayerSizes())
            .lossFunction(xorModel.getLossFunction())
            .build();
        network.resetLayerWeights(gradientMatrixService);
      }

      @Test
      void shouldMinimizeTrainingErrorWhenTrained() {
        var initial = network.computeAccuracy(samples, dataSelector);

        train(500);

        var actual = network.computeAccuracy(samples, dataSelector);
        assertThat(actual.error()).isLessThan(initial.error());
      }

      @Test
      void shouldClassifySamplesWithLowError() {
        train(500);

        for (Sample sample : samples) {
          var layers = network.feedForward(sample, dataSelector);

          assertThat(Layer.output(layers).h()).containsExactly(sample.getTarget(),
              Offset.offset(0.499f));
        }
      }
    }

    @Nested
    class GoodMultiLayerNonLinearCase {

      final MultiLayerNetworkTemplate xorModel = MultiLayerNetworkTemplate.builder()
          .lossFunction(lossFunction)
          .layers(List.of(LayerTemplate.builder().size(2).activationFunction(linear).build(),
              LayerTemplate.builder().size(2).activationFunction(nonlinear).build(),
              LayerTemplate.builder().size(1).activationFunction(nonlinear).build()))
          .build();

      @BeforeEach
      void setupNetwork() {
        network = MultiLayerNetwork.builder()
            .bias(-1f)
            .activationFunctions(xorModel.collectActivationFunctions())
            .layerSizes(xorModel.collectLayerSizes())
            .lossFunction(xorModel.getLossFunction())
            .build();
        network.resetLayerWeights(gradientMatrixService);
      }

      @Test
      void shouldMinimizeTrainingErrorWhenTrained() {
        var initial = network.computeAccuracy(samples, dataSelector);

        train(500);

        var actual = network.computeAccuracy(samples, dataSelector);
        assertThat(actual.error()).isLessThan(initial.error());
      }

      @Test
      void shouldClassifySamplesWithLowError() {
        train(500);

        for (Sample sample : samples) {
          var layers = network.feedForward(sample, dataSelector);

          assertThat(Layer.output(layers).h()).containsExactly(sample.getTarget(),
              Offset.offset(0.499f));
        }
      }
    }
  }

  final class TestGradientMatrixService extends GradientMatrixService {

    @Override
    public float[][] resetLayerWeights(int[] layerSizes, ActivationFunction[] activationFunctions) {
      return super.resetLayerWeights(random, layerSizes, activationFunctions);
    }
  }
}
