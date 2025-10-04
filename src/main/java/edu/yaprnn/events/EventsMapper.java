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
    if (modelNode instanceof MultiLayerNetworkNode multiLayerNetworkNode) {
      return multiLayerNetworkNode.getMultiLayerNetworkSupplier().get();
    }
    if (modelNode instanceof MultiLayerNetworkWeightsNode multiLayerNetworkWeightsNode) {
      return multiLayerNetworkWeightsNode.getMultiLayerNetworkSupplier().get();
    }
    return null;
  }

  public int toWeightsIndex(ModelNode modelNode) {
    if (modelNode instanceof MultiLayerNetworkWeightsNode multiLayerNetworkWeightsNode) {
      return multiLayerNetworkWeightsNode.getWeightsIndexSupplier().getAsInt();
    }
    return -1;
  }

  public MultiLayerNetworkTemplate toMultiLayerNetworkTemplate(ModelNode modelNode) {
    if (modelNode instanceof MultiLayerNetworkTemplateNode multiLayerNetworkTemplateNode) {
      return multiLayerNetworkTemplateNode.getMultiLayerNetworkTemplateSupplier().get();
    }
    if (modelNode instanceof LayerTemplateNode layerTemplateNode) {
      return layerTemplateNode.getMultiLayerNetworkTemplateSupplier().get();
    }
    if (modelNode instanceof LayerSizeNode layerSizeNode) {
      return layerSizeNode.getMultiLayerNetworkTemplateSupplier().get();
    }
    if (modelNode instanceof ActivationFunctionNode activationFunctionNode) {
      return activationFunctionNode.getMultiLayerNetworkTemplateSupplier().get();
    }
    return null;
  }

  public TrainingData toTrainingData(ModelNode modelNode) {
    if (modelNode instanceof TrainingDataNode trainingDataNode) {
      return trainingDataNode.getTrainingDataSupplier().get();
    }
    return null;
  }

  public Sample toSample(ModelNode modelNode) {
    if (modelNode instanceof SampleNode sampleNode) {
      return sampleNode.getSampleSupplier().get();
    }
    if (modelNode instanceof SampleNameNode sampleNameNode) {
      return repository.getSamplesGroupedByName().get(sampleNameNode.getSampleNameSupplier().get());
    }
    return null;
  }
}
