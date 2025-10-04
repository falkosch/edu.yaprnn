package edu.yaprnn.gui.model.nodes;

import edu.yaprnn.functions.Providers;
import edu.yaprnn.gui.services.IconsService;
import edu.yaprnn.training.TrainingData;
import java.util.List;
import java.util.function.Supplier;
import lombok.Getter;

/**
 * Represents the sample sets in a {@link TrainingData}.
 */
@Getter
public class TrainingDataNode extends DefaultNode {

  private final Supplier<TrainingData> trainingDataSupplier;

  public TrainingDataNode(Supplier<TrainingData> trainingDataSupplier) {
    super(Providers.constant(IconsService.ICON_TRAINING_DATA_NODE),
        Providers.mapped(trainingDataSupplier, TrainingDataNode::labelFrom),
        Providers.mapped(trainingDataSupplier, TrainingDataNode::childrenFrom));
    this.trainingDataSupplier = trainingDataSupplier;
  }

  private static String labelFrom(TrainingData trainingData) {
    var trainingSize = trainingData.getTrainingSampleNames().size();
    var devTestSize = trainingData.getDevTestSampleNames().size();
    var sum = Math.max(1, trainingSize + devTestSize);
    var trainingWeight = 100 * trainingSize / sum;
    var devTestWeight = 100 * devTestSize / sum;
    return "%s (%d: %d%% | %d%%)".formatted(trainingData.getName(), sum, trainingWeight,
        devTestWeight);
  }

  private static List<? extends ModelNode> childrenFrom(TrainingData trainingData) {
    var trainingSampleNames = trainingData.getTrainingSampleNames();
    var devTestSampleNames = trainingData.getDevTestSampleNames();
    return List.of(
        new SampleNameListNode(Providers.constant(IconsService.ICON_TRAINING_SAMPLES_NODE),
            Providers.constant("Training (%d)".formatted(trainingSampleNames.size())),
            Providers.constant(trainingSampleNames)),
        new SampleNameListNode(Providers.constant(IconsService.ICON_DEV_TEST_SAMPLES_NODE),
            Providers.constant("Dev Test (%d)".formatted(devTestSampleNames.size())),
            Providers.constant(devTestSampleNames)),
        new DataSelectorNode(Providers.constant(trainingData)));
  }
}
