package edu.yaprnn.networks;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import edu.yaprnn.networks.activation.ActivationFunction;
import edu.yaprnn.networks.activation.GeLUActivationFunction;
import edu.yaprnn.networks.activation.LinearActivationFunction;
import edu.yaprnn.networks.loss.HalfSquaredErrorLossFunction;
import edu.yaprnn.networks.loss.LossFunction;
import edu.yaprnn.networks.templates.LayerTemplate;
import edu.yaprnn.networks.templates.MultiLayerNetworkTemplate;
import edu.yaprnn.samples.model.Sample;
import edu.yaprnn.samples.model.SimpleSample;
import edu.yaprnn.training.selectors.ClassifierDataSelector;
import edu.yaprnn.training.selectors.DataSelector;
import java.security.SecureRandom;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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
  final ExecutorService executor = Executors.newFixedThreadPool(1, Thread.ofVirtual().factory());
  Random random;

  MultiLayerNetwork network;
  List<SimpleSample> samples;

  void train(int epochs) {
    for (var i = 0; i < epochs; i++) {
      network.learnMiniBatch(gradientMatrixService, executor, samples, dataSelector, 1,
          samples.size(), 0.2f, 0.2f, 0f, 0f);
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
        .bias(-1f)
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
          .bias(notModel.getBias())
          .lossFunction(notModel.getLossFunction())
          .activationFunctions(notModel.collectActivationFunctions())
          .layerSizes(notModel.collectLayerSizes())
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
        .bias(-1f)
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
          .bias(andModel.getBias())
          .lossFunction(andModel.getLossFunction())
          .activationFunctions(andModel.collectActivationFunctions())
          .layerSizes(andModel.collectLayerSizes())
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
        .bias(-1f)
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
          .bias(orModel.getBias())
          .lossFunction(orModel.getLossFunction())
          .activationFunctions(orModel.collectActivationFunctions())
          .layerSizes(orModel.collectLayerSizes())
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

    /**
     * A single-layer linear perceptron cannot solve XOR because XOR is not linearly separable.
     * The network converges to outputting ~0.5 for all inputs (the mean target value).
     */
    @Nested
    class BadPerceptronLinearCase {

      final MultiLayerNetworkTemplate xorModel = MultiLayerNetworkTemplate.builder()
          .bias(-1f)
          .lossFunction(lossFunction)
          .layers(List.of(LayerTemplate.builder().size(2).activationFunction(linear).build(),
              LayerTemplate.builder().size(1).activationFunction(linear).build()))
          .build();

      @BeforeEach
      void setupNetwork() {
        network = MultiLayerNetwork.builder()
            .bias(xorModel.getBias())
            .lossFunction(xorModel.getLossFunction())
            .activationFunctions(xorModel.collectActivationFunctions())
            .layerSizes(xorModel.collectLayerSizes())
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
      void shouldNotClassifyAllSamplesCorrectly() {
        train(500);

        var misclassified = false;
        for (Sample sample : samples) {
          var layers = network.feedForward(sample, dataSelector);
          var output = Layer.output(layers).h()[0];
          var target = sample.getTarget()[0];
          if (Math.abs(output - target) > 0.499f) {
            misclassified = true;
            break;
          }
        }
        assertThat(misclassified)
            .as("Linear perceptron should not solve XOR (not linearly separable)")
            .isTrue();
      }
    }

    /**
     * A single-layer perceptron with nonlinear activation still cannot solve XOR because it has
     * only one decision boundary.
     */
    @Nested
    class BadPerceptronNonLinearCase {

      final MultiLayerNetworkTemplate xorModel = MultiLayerNetworkTemplate.builder()
          .bias(-1f)
          .lossFunction(lossFunction)
          .layers(List.of(LayerTemplate.builder().size(2).activationFunction(linear).build(),
              LayerTemplate.builder().size(1).activationFunction(nonlinear).build()))
          .build();

      @BeforeEach
      void setupNetwork() {
        network = MultiLayerNetwork.builder()
            .bias(xorModel.getBias())
            .lossFunction(xorModel.getLossFunction())
            .activationFunctions(xorModel.collectActivationFunctions())
            .layerSizes(xorModel.collectLayerSizes())
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
      void shouldNotClassifyAllSamplesCorrectly() {
        train(500);

        var misclassified = false;
        for (Sample sample : samples) {
          var layers = network.feedForward(sample, dataSelector);
          var output = Layer.output(layers).h()[0];
          var target = sample.getTarget()[0];
          if (Math.abs(output - target) > 0.499f) {
            misclassified = true;
            break;
          }
        }
        assertThat(misclassified)
            .as("Single-neuron output layer should not solve XOR")
            .isTrue();
      }
    }

    /**
     * A multi-layer network with all linear activations collapses to a single linear
     * transformation and thus cannot solve XOR.
     */
    @Nested
    class BadMultiLayerLinearCase {

      final MultiLayerNetworkTemplate xorModel = MultiLayerNetworkTemplate.builder()
          .bias(-1f)
          .lossFunction(lossFunction)
          .layers(List.of(LayerTemplate.builder().size(2).activationFunction(linear).build(),
              LayerTemplate.builder().size(2).activationFunction(linear).build(),
              LayerTemplate.builder().size(1).activationFunction(linear).build()))
          .build();

      @BeforeEach
      void setupNetwork() {
        network = MultiLayerNetwork.builder()
            .bias(xorModel.getBias())
            .lossFunction(xorModel.getLossFunction())
            .activationFunctions(xorModel.collectActivationFunctions())
            .layerSizes(xorModel.collectLayerSizes())
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
      void shouldNotClassifyAllSamplesCorrectly() {
        train(500);

        var misclassified = false;
        for (Sample sample : samples) {
          var layers = network.feedForward(sample, dataSelector);
          var output = Layer.output(layers).h()[0];
          var target = sample.getTarget()[0];
          if (Math.abs(output - target) > 0.499f) {
            misclassified = true;
            break;
          }
        }
        assertThat(misclassified)
            .as("All-linear multi-layer network should not solve XOR")
            .isTrue();
      }
    }

    /**
     * A multi-layer network with nonlinear hidden activations CAN solve XOR. The hidden layer
     * creates two decision boundaries that combine to separate the XOR regions.
     */
    @Nested
    class GoodMultiLayerNonLinearCase {

      final MultiLayerNetworkTemplate xorModel = MultiLayerNetworkTemplate.builder()
          .bias(-1f)
          .lossFunction(lossFunction)
          .layers(List.of(LayerTemplate.builder().size(2).activationFunction(linear).build(),
              LayerTemplate.builder().size(4).activationFunction(nonlinear).build(),
              LayerTemplate.builder().size(1).activationFunction(nonlinear).build()))
          .build();

      @BeforeEach
      void setupNetwork() {
        network = MultiLayerNetwork.builder()
            .bias(xorModel.getBias())
            .lossFunction(xorModel.getLossFunction())
            .activationFunctions(xorModel.collectActivationFunctions())
            .layerSizes(xorModel.collectLayerSizes())
            .build();
        network.resetLayerWeights(gradientMatrixService);
      }

      @Test
      void shouldMinimizeTrainingErrorWhenTrained() {
        var initial = network.computeAccuracy(samples, dataSelector);

        train(1000);

        var actual = network.computeAccuracy(samples, dataSelector);
        assertThat(actual.error()).isLessThan(initial.error());
      }

      @Test
      void shouldClassifySamplesWithLowError() {
        train(1000);

        for (Sample sample : samples) {
          var layers = network.feedForward(sample, dataSelector);

          assertThat(Layer.output(layers).h()).containsExactly(sample.getTarget(),
              Offset.offset(0.499f));
        }
      }
    }
  }

  @Nested
  class ToString {

    @Test
    void shouldFormatWithNameAndLayerSizes() {
      var net = MultiLayerNetwork.builder()
          .name("myNet")
          .layerSizes(new int[]{2, 3, 1})
          .activationFunctions(new ActivationFunction[]{linear, linear, linear})
          .bias(1f)
          .lossFunction(lossFunction)
          .build();

      assertThat(net.toString()).isEqualTo("myNet ([2, 3, 1])");
    }

    @Test
    void shouldHandleNullLayerSizes() {
      var net = MultiLayerNetwork.builder()
          .name("noLayers")
          .bias(1f)
          .lossFunction(lossFunction)
          .build();

      assertThat(net.toString()).isEqualTo("noLayers ([])");
    }
  }

  @Nested
  class LearnMiniBatchValidation {

    final MultiLayerNetwork validationNetwork = MultiLayerNetwork.builder()
        .layerSizes(new int[]{2, 1})
        .activationFunctions(new ActivationFunction[]{linear, linear})
        .bias(1f)
        .lossFunction(lossFunction)
        .build();

    @Test
    void shouldThrowOnNullGradientMatrixService() {
      assertThatNullPointerException()
          .isThrownBy(() -> validationNetwork.learnMiniBatch(null, executor, List.of(),
              dataSelector, 1, 10, 0.1f, 0f, 0f, 0f))
          .withMessageContaining("gradientMatrixService");
    }

    @Test
    void shouldThrowOnNullExecutor() {
      assertThatNullPointerException()
          .isThrownBy(() -> validationNetwork.learnMiniBatch(gradientMatrixService, null,
              List.of(), dataSelector, 1, 10, 0.1f, 0f, 0f, 0f))
          .withMessageContaining("executor");
    }

    @Test
    void shouldThrowOnNullTrainingSamples() {
      assertThatNullPointerException()
          .isThrownBy(() -> validationNetwork.learnMiniBatch(gradientMatrixService, executor,
              null, dataSelector, 1, 10, 0.1f, 0f, 0f, 0f))
          .withMessageContaining("trainingSamples");
    }

    @Test
    void shouldThrowOnNullDataSelector() {
      assertThatNullPointerException()
          .isThrownBy(() -> validationNetwork.learnMiniBatch(gradientMatrixService, executor,
              List.of(), null, 1, 10, 0.1f, 0f, 0f, 0f))
          .withMessageContaining("dataSelector");
    }

    @Test
    void shouldThrowOnInvalidMaxParallelism() {
      assertThatThrownBy(() -> validationNetwork.learnMiniBatch(gradientMatrixService, executor,
          List.of(), dataSelector, 0, 10, 0.1f, 0f, 0f, 0f))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("maxParallelism");
    }

    @Test
    void shouldThrowOnInvalidBatchSize() {
      assertThatThrownBy(() -> validationNetwork.learnMiniBatch(gradientMatrixService, executor,
          List.of(), dataSelector, 1, 0, 0.1f, 0f, 0f, 0f))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("batchSize");
    }
  }

  @Nested
  class ComputeAccuracyValidation {

    final MultiLayerNetwork validationNetwork = MultiLayerNetwork.builder()
        .layerSizes(new int[]{2, 1})
        .activationFunctions(new ActivationFunction[]{linear, linear})
        .bias(1f)
        .lossFunction(lossFunction)
        .build();

    @Test
    void shouldThrowOnNullSamples() {
      assertThatNullPointerException()
          .isThrownBy(() -> validationNetwork.computeAccuracy(null, dataSelector))
          .withMessageContaining("samples");
    }

    @Test
    void shouldThrowOnNullDataSelector() {
      assertThatNullPointerException()
          .isThrownBy(() -> validationNetwork.computeAccuracy(List.of(), null))
          .withMessageContaining("dataSelector");
    }
  }

  final class TestGradientMatrixService extends GradientMatrixService {

    @Override
    public float[][] resetLayerWeights(int[] layerSizes, ActivationFunction[] activationFunctions) {
      return super.resetLayerWeights(random, layerSizes, activationFunctions);
    }
  }
}
