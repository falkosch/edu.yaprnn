package edu.yaprnn.networks;

import com.google.common.primitives.Floats;
import edu.yaprnn.functions.ActivationFunction;
import edu.yaprnn.functions.TangentHyperbolicActivationFunction;
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

  final ActivationFunction tanh = new TangentHyperbolicActivationFunction();
  MultiLayerNetwork network;
  List<SimpleSample> samples;
  Random random;

  @BeforeEach
  void createNetwork() {
    random = new SecureRandom(new byte[42]);

    samples = IntStream.range(0, 1000).mapToObj(
            i -> SimpleSample.builder().input(generateFloats(i)).target(generateFloats(999)).build())
        .toList();

    var template = new MultiLayerNetworkTemplate();
    template.addLayer(LayerTemplate.builder().size(999).activationFunction(tanh).build());
    template.addLayer(LayerTemplate.builder().size(999).activationFunction(tanh).build());

    network = MultiLayerNetwork.builder().template(template).bias(1f)
        .activationFunctions(template.collectActivationFunctions())
        .layerSizes(template.collectLayerSizes()).build();
    network.resetLayerWeights(new TestGradientMatrixService());
  }

  float[] generateFloats(int count) {
    return Floats.toArray(random.doubles(count).mapToObj(x -> (float) x).toList());
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
    return network.feedForward(sample, new ClassifierDataSelector());
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
    network.learnOnlineParallelized(new TestGradientMatrixService(), samples,
        new ClassifierDataSelector(), 0.001f, 0.5f, 0.001f, 0.001f);
  }

  class TestGradientMatrixService extends GradientMatrixService {

    @Override
    public float[][] resetLayerWeights(int[] layerSizes, ActivationFunction[] activationFunctions) {
      return super.resetLayerWeights(random, layerSizes, activationFunctions);
    }
  }
}
