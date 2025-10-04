package edu.yaprnn.events;

import edu.yaprnn.gui.model.nodes.ActivationFunctionNode;
import edu.yaprnn.gui.model.nodes.LayerSizeNode;
import edu.yaprnn.gui.model.nodes.LayerTemplateNode;
import edu.yaprnn.gui.model.nodes.ModelNode;
import edu.yaprnn.gui.model.nodes.MultiLayerNetworkNode;
import edu.yaprnn.gui.model.nodes.MultiLayerNetworkTemplateNode;
import edu.yaprnn.gui.model.nodes.MultiLayerNetworkWeightsNode;
import edu.yaprnn.gui.model.nodes.SampleNameNode;
import edu.yaprnn.gui.model.nodes.SampleNode;
import edu.yaprnn.gui.model.nodes.TrainingDataNode;
import edu.yaprnn.model.Repository;
import edu.yaprnn.networks.MultiLayerNetwork;
import edu.yaprnn.networks.templates.MultiLayerNetworkTemplate;
import edu.yaprnn.samples.model.Sample;
import edu.yaprnn.training.TrainingData;
import jakarta.inject.Inject;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants.ComponentModel;

@Mapper(componentModel = ComponentModel.CDI)
public abstract class EventsMapper {

  @Inject
  Repository repository;

  public MultiLayerNetwork toMultiLayerNetwork(ModelNode modelNode) {
    return switch (modelNode) {
      case MultiLayerNetworkNode node -> node.getMultiLayerNetworkSupplier().get();
      case MultiLayerNetworkWeightsNode node -> node.getMultiLayerNetworkSupplier().get();
      case null, default -> null;
    };
  }

  public int toWeightsIndex(ModelNode modelNode) {
    return switch (modelNode) {
      case MultiLayerNetworkWeightsNode node -> node.getWeightsIndexSupplier().getAsInt();
      case null, default -> -1;
    };
  }

  public MultiLayerNetworkTemplate toMultiLayerNetworkTemplate(ModelNode modelNode) {
    return switch (modelNode) {
      case MultiLayerNetworkTemplateNode node -> node.getTemplateSupplier().get();
      case LayerTemplateNode node -> node.getMultiLayerNetworkTemplateSupplier().get();
      case LayerSizeNode node -> node.getMultiLayerNetworkTemplateSupplier().get();
      case ActivationFunctionNode node -> node.getMultiLayerNetworkTemplateSupplier().get();
      case null, default -> null;
    };
  }

  public TrainingData toTrainingData(ModelNode modelNode) {
    return switch (modelNode) {
      case TrainingDataNode node -> node.getTrainingDataSupplier().get();
      case null, default -> null;
    };
  }

  public Sample toSample(ModelNode modelNode) {
    return switch (modelNode) {
      case SampleNode node -> node.getSampleSupplier().get();
      case SampleNameNode node ->
          repository.getSamplesGroupedByName().get(node.getSampleNameSupplier().get());
      case null, default -> null;
    };
  }
}
