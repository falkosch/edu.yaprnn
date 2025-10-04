package edu.yaprnn.gui.views.mappings;

import edu.yaprnn.gui.views.NewMultiLayerNetworkPanel.Parameters;
import edu.yaprnn.networks.MultiLayerNetwork;
import edu.yaprnn.networks.templates.LayerTemplate;
import edu.yaprnn.networks.templates.MultiLayerNetworkTemplate;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants.ComponentModel;

@Mapper(componentModel = ComponentModel.CDI)
public interface MultiLayerNetworkMapper {

  @Mapping(target = "activationFunctions", expression = "java(parameters.multiLayerNetworkTemplate().collectActivationFunctions())")
  @Mapping(target = "layerSizes", expression = "java(parameters.multiLayerNetworkTemplate().collectLayerSizes())")
  @Mapping(target = "layerWeights", ignore = true)
  @Mapping(target = "previousLayerGradients", ignore = true)
  MultiLayerNetwork toMultiLayerNetwork(Parameters parameters);

  MultiLayerNetworkTemplate clone(MultiLayerNetworkTemplate source);

  List<LayerTemplate> clone(List<LayerTemplate> source);

  LayerTemplate clone(LayerTemplate source);
}
