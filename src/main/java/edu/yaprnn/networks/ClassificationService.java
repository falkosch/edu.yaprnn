package edu.yaprnn.networks;

import edu.yaprnn.samples.model.Sample;
import edu.yaprnn.training.selectors.DataSelector;
import jakarta.inject.Singleton;

/**
 * Performs feedforward classification on a network. Toolkit-agnostic.
 */
@Singleton
public class ClassificationService {

  /**
   * Runs feedforward classification and postprocesses the output.
   */
  public ClassificationResult classify(MultiLayerNetwork network, Sample sample,
      DataSelector dataSelector) {
    var layers = network.feedForward(sample, dataSelector);
    var outputLayer = Layer.output(layers);
    var output = dataSelector.postprocessOutput(outputLayer.v(), outputLayer.h(),
        outputLayer.activationFunction());
    var labels = sample.getLabels();
    return new ClassificationResult(layers, output, labels);
  }

  public record ClassificationResult(Layer[] layers, float[] output, String[] labels) {
  }
}
