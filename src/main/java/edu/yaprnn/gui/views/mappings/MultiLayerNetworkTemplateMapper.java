package edu.yaprnn.gui.views.mappings;

import edu.yaprnn.gui.views.mappings.NewMultiLayerNetworkTemplateParameters;
import edu.yaprnn.networks.templates.LayerTemplate;
import edu.yaprnn.networks.templates.MultiLayerNetworkTemplate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants.ComponentModel;
import org.mapstruct.Named;

@Mapper(componentModel = ComponentModel.CDI)
public interface MultiLayerNetworkTemplateMapper {

  @Mapping(target = "layers", source = "parameters", qualifiedByName = "toLayerTemplates")
  MultiLayerNetworkTemplate from(NewMultiLayerNetworkTemplateParameters parameters);

  @Named("toLayerTemplates")
  default List<LayerTemplate> toLayerTemplates(NewMultiLayerNetworkTemplateParameters parameters) {
    var layerTemplates = IntStream.range(0, parameters.layersCount())
        .mapToObj(_ -> toLayerTemplate(parameters))
        .toList();
    return new ArrayList<>(layerTemplates);
  }

  @Mapping(target = "size", source = "layersSize")
  LayerTemplate toLayerTemplate(NewMultiLayerNetworkTemplateParameters parameters);
}
