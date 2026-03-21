package edu.yaprnn.model;

import edu.yaprnn.gui.services.PersistenceService;
import edu.yaprnn.networks.MultiLayerNetwork;
import edu.yaprnn.networks.templates.MultiLayerNetworkTemplate;
import edu.yaprnn.samples.ImagesImportService;
import edu.yaprnn.samples.model.ImageSample;
import edu.yaprnn.training.TrainingData;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Loads predefined scenarios from classpath resources. Toolkit-agnostic.
 */
@Singleton
public class ScenarioService {

  @Inject
  ImagesImportService imagesImportService;
  @Inject
  PersistenceService persistenceService;

  /**
   * Loads the built-in digits scenario (classifiers, super-resolution, autoencoder).
   */
  public ScenarioData loadDigitsScenario() {
    var samples = imagesImportService.fromImagesLabelsPackage(
        getResource("/digits.idx3-ubyte"), getResource("/digits.idx1-ubyte"));

    var trainingDataList = new ArrayList<TrainingData>();
    var templates = new ArrayList<MultiLayerNetworkTemplate>();
    var networks = new ArrayList<MultiLayerNetwork>();

    // Classifier
    trainingDataList.add(persistenceService.loadTrainingData(
        getResource("/digits.yaprnn-training-data")));
    templates.add(persistenceService.loadMultiLayerNetworkTemplate(
        getResource("/digits.yaprnn-mln-template")));
    networks.add(persistenceService.loadMultiLayerNetwork(
        getResource("/digits.yaprnn-mln")));

    // Image-from-label
    trainingDataList.add(persistenceService.loadTrainingData(
        getResource("/digits-image-from-label.yaprnn-training-data")));
    templates.add(persistenceService.loadMultiLayerNetworkTemplate(
        getResource("/digits-image-from-label.yaprnn-mln-template")));
    networks.add(persistenceService.loadMultiLayerNetwork(
        getResource("/digits-image-from-label.yaprnn-mln")));

    // Input reconstruction autoencoder (6 layers, bottleneck 12)
    var inputReconstructionTrainingData = persistenceService.loadTrainingData(
        getResource("/digits-input-reconstruction.yaprnn-training-data"));
    trainingDataList.add(inputReconstructionTrainingData);
    templates.add(persistenceService.loadMultiLayerNetworkTemplate(
        getResource(
            "/digits-input-reconstruction-ae-layers-6-bottleneck-12.yaprnn-mln-template")));
    networks.add(persistenceService.loadMultiLayerNetwork(
        getResource("/digits-input-reconstruction-ae-layers-6-bottleneck-12.yaprnn-mln")));

    // Input reconstruction autoencoder (14 layers, bottleneck 6)
    templates.add(persistenceService.loadMultiLayerNetworkTemplate(
        getResource(
            "/digits-input-reconstruction-ae-layers-14-bottleneck-6.yaprnn-mln-template")));
    networks.add(persistenceService.loadMultiLayerNetwork(
        getResource("/digits-input-reconstruction-ae-layers-14-bottleneck-6.yaprnn-mln")));

    // Super-resolution
    trainingDataList.add(persistenceService.loadTrainingData(
        getResource("/digits-super-resolution.yaprnn-training-data")));
    templates.add(persistenceService.loadMultiLayerNetworkTemplate(
        getResource("/digits-super-resolution-layers-3.yaprnn-mln-template")));

    return new ScenarioData(samples, trainingDataList, templates, networks,
        inputReconstructionTrainingData);
  }

  private URL getResource(String path) {
    return Objects.requireNonNull(ScenarioService.class.getResource(path),
        () -> "Resource not found: %s".formatted(path));
  }

  public record ScenarioData(
      List<ImageSample> samples,
      List<TrainingData> trainingData,
      List<MultiLayerNetworkTemplate> templates,
      List<MultiLayerNetwork> networks,
      TrainingData defaultSelectedTrainingData) {
  }
}
