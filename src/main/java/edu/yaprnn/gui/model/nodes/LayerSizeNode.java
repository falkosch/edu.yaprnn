package edu.yaprnn.gui.model.nodes;

import edu.yaprnn.functions.Providers;
import edu.yaprnn.gui.services.IconsService;
import edu.yaprnn.networks.templates.LayerTemplate;
import edu.yaprnn.networks.templates.MultiLayerNetworkTemplate;
import java.util.Collections;
import java.util.function.Supplier;
import lombok.Getter;

/**
 * Represents the layer size of a {@link LayerTemplate} in a {@link MultiLayerNetworkTemplate}.
 */
@Getter
public class LayerSizeNode extends DefaultNode {

  private final Supplier<MultiLayerNetworkTemplate> multiLayerNetworkTemplateSupplier;
  private final Supplier<LayerTemplate> layerTemplateSupplier;

  public LayerSizeNode(Supplier<MultiLayerNetworkTemplate> multiLayerNetworkTemplateSupplier,
      Supplier<LayerTemplate> layerTemplateSupplier) {
    super(Providers.constant(IconsService.ICON_LAYER_SIZE),
        Providers.mapped(layerTemplateSupplier, LayerSizeNode::labelFrom), Collections::emptyList);
    this.multiLayerNetworkTemplateSupplier = multiLayerNetworkTemplateSupplier;
    this.layerTemplateSupplier = layerTemplateSupplier;
  }

  private static String labelFrom(LayerTemplate layerTemplate) {
    return "Size %d".formatted(layerTemplate.getSize());
  }
}
