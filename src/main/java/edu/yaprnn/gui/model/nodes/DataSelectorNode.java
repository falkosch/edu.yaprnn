package edu.yaprnn.gui.model.nodes;

import edu.yaprnn.functions.Providers;
import edu.yaprnn.gui.services.IconsService;
import edu.yaprnn.training.TrainingData;
import java.util.Collections;
import java.util.function.Supplier;
import lombok.Getter;

@Getter
public class DataSelectorNode extends DefaultNode {

  private final Supplier<TrainingData> trainingDataSupplier;

  public DataSelectorNode(Supplier<TrainingData> trainingDataSupplier) {
    super(Providers.constant(IconsService.ICON_ACTIVATION_FUNCTION),
        Providers.mapped(trainingDataSupplier, DataSelectorNode::labelFrom),
        Collections::emptyList);
    this.trainingDataSupplier = trainingDataSupplier;
  }

  private static String labelFrom(TrainingData trainingData) {
    return trainingData.getDataSelector().getClass().getSimpleName();
  }
}
