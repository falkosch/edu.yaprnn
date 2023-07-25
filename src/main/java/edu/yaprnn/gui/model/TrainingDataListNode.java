package edu.yaprnn.gui.model;

import edu.yaprnn.functions.Providers;
import edu.yaprnn.gui.model.nodes.DefaultNode;
import edu.yaprnn.gui.model.nodes.ModelNode;
import edu.yaprnn.gui.model.nodes.TrainingDataNode;
import edu.yaprnn.gui.services.IconsService;
import edu.yaprnn.model.Repository;
import edu.yaprnn.training.TrainingData;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.List;

/**
 * Displays a {@link TrainingDataNode} for each {@link TrainingData}.
 */
@Singleton
public class TrainingDataListNode extends DefaultNode {

  @Inject
  public TrainingDataListNode(Repository repository) {
    super(Providers.constant(IconsService.ICON_NODE),
        Providers.mapped(repository::getTrainingDataList, TrainingDataListNode::labelFrom),
        Providers.mapped(repository::getTrainingDataList, TrainingDataListNode::childrenFrom));
  }

  private static String labelFrom(List<TrainingData> trainingDataList) {
    return "Training Data (%d)".formatted(trainingDataList.size());
  }

  private static List<? extends ModelNode> childrenFrom(List<TrainingData> trainingDataList) {
    return trainingDataList.stream().map(Providers::constant).map(TrainingDataNode::new).toList();
  }
}
