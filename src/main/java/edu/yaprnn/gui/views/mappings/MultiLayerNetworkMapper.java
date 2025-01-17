package edu.yaprnn.gui.views.mappings;

import edu.yaprnn.gui.views.NewMultiLayerNetworkPanel.Parameters;
import edu.yaprnn.networks.MultiLayerNetwork;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants.ComponentModel;

@Mapper(componentModel = ComponentModel.CDI)
public interface MultiLayerNetworkMapper {

  @Mapping(target = "activationFunctions", expression = "java(parameters.multiLayerNetworkTemplate().collectActivationFunctions())")
  @Mapping(target = "layerSizes", expression = "java(parameters.multiLayerNetworkTemplate().collectLayerSizes())")
  @Mapping(target = "bias", expression = "java(parameters.multiLayerNetworkTemplate().getBias())")
  @Mapping(target = "lossFunction", expression = "java(parameters.multiLayerNetworkTemplate().getLossFunction())")
  @Mapping(target = "layerWeights", ignore = true)
  @Mapping(target = "previousLayerGradients", ignore = true)
  MultiLayerNetwork toMultiLayerNetwork(Parameters parameters);
}
