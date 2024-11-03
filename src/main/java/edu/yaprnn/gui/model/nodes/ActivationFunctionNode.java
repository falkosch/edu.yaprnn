package edu.yaprnn.gui.model.nodes;

import edu.yaprnn.functions.ActivationFunction;
import edu.yaprnn.gui.services.IconsService;
import edu.yaprnn.networks.templates.LayerTemplate;
import edu.yaprnn.networks.templates.MultiLayerNetworkTemplate;
import edu.yaprnn.support.Providers;
import java.util.Collections;
import java.util.function.Supplier;
import lombok.Getter;

/**
 * Represents the {@link ActivationFunction} of a {@link LayerTemplate} in a
 * {@link MultiLayerNetworkTemplate}.
 */
@Getter
public class ActivationFunctionNode extends DefaultNode {

  private final Supplier<MultiLayerNetworkTemplate> multiLayerNetworkTemplateSupplier;
  private final Supplier<LayerTemplate> layerTemplateSupplier;

  public ActivationFunctionNode(
      Supplier<MultiLayerNetworkTemplate> multiLayerNetworkTemplateSupplier,
      Supplier<LayerTemplate> layerTemplateSupplier) {
    super(Providers.constant(IconsService.ICON_ACTIVATION_FUNCTION),
        Providers.mapped(layerTemplateSupplier, ActivationFunctionNode::labelFrom),
        Collections::emptyList);
    this.multiLayerNetworkTemplateSupplier = multiLayerNetworkTemplateSupplier;
    this.layerTemplateSupplier = layerTemplateSupplier;
  }

  private static String labelFrom(LayerTemplate layerTemplate) {
    return "f(x) %s".formatted(layerTemplate.getActivationFunction());
  }
}
