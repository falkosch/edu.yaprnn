package edu.yaprnn.networks;

import edu.yaprnn.networks.functions.ActivationFunction;
import edu.yaprnn.networks.functions.LinearActivationFunction;
import edu.yaprnn.networks.functions.SigmoidActivationFunction;
import edu.yaprnn.networks.templates.LayerTemplate;
import edu.yaprnn.networks.templates.MultiLayerNetworkTemplate;
import edu.yaprnn.samples.model.SimpleSample;
import edu.yaprnn.training.ClassifierDataSelector;
import java.security.SecureRandom;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MultiLayerNetworkPerformanceTest {

  final ClassifierDataSelector dataSelector = new ClassifierDataSelector();
  final ActivationFunction linear = new LinearActivationFunction();
  final ActivationFunction sigmoid = new SigmoidActivationFunction();
  final MultiLayerNetworkTemplate model = MultiLayerNetworkTemplate.builder()
      .layers(List.of(LayerTemplate.builder().size(999).activationFunction(linear).build(),
          LayerTemplate.builder().size(999).activationFunction(sigmoid).build(),
          LayerTemplate.builder().size(999).activationFunction(sigmoid).build()))
      .build();
  final TestGradientMatrixService gradientMatrixService = new TestGradientMatrixService();

  Random random;
  List<SimpleSample> samples;
  MultiLayerNetwork network;

  @BeforeEach
  void createNetwork() {
    random = new SecureRandom(new byte[42]);
    samples = IntStream.range(0, 1000)
        .mapToObj(i -> SimpleSample.builder()
            .input(generateFloats(i))
            .target(generateFloats(999))
            .build())
        .toList();
    network = MultiLayerNetwork.builder()
        .bias(-1f)
        .activationFunctions(model.collectActivationFunctions())
        .layerSizes(model.collectLayerSizes())
        .build();
    network.resetLayerWeights(new TestGradientMatrixService());
  }

  float[] generateFloats(int count) {
    var floats = new float[count];
    for (int i = 0; i < count; i++) {
      floats[i] = random.nextFloat();
    }
    return floats;

  }

  @Test
  void measureFeedForward() {
    feedForward(samples.getFirst());
    feedForward(samples.get(300));
    feedForward(samples.get(600));

    var t = System.nanoTime();

    var s = 0;
    for (var sample : samples) {
      s += feedForward(sample) != null ? 1 : 0;
    }

    var d = System.nanoTime() - t;
    System.out.printf("Time delta: %s s -- %s%n", (d / 1_000_000_000.0), s);
  }

  Layer[] feedForward(SimpleSample sample) {
    return network.feedForward(sample, dataSelector);
  }

  @Test
  void measureLearnOnline() {
    learnOnline();

    var t = System.nanoTime();

    learnOnline();

    var d = System.nanoTime() - t;
    System.out.printf("Time delta: %s s", (d / 1_000_000_000.0));
  }

  void learnOnline() {
    network.learnMiniBatch(gradientMatrixService, samples, dataSelector, 1, 0.001f, 0.5f, 0.001f,
        0.001f);
  }

  final class TestGradientMatrixService extends GradientMatrixService {

    @Override
    public float[][] resetLayerWeights(int[] layerSizes, ActivationFunction[] activationFunctions) {
      return super.resetLayerWeights(random, layerSizes, activationFunctions);
    }
  }
}
