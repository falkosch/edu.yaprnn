package edu.yaprnn.gui.model.nodes;

import edu.yaprnn.gui.services.IconsService;
import edu.yaprnn.networks.templates.MultiLayerNetworkTemplate;
import edu.yaprnn.support.Providers;
import java.util.Collections;
import java.util.function.Supplier;
import lombok.Getter;

/**
 * Represents the bias input value of a {@link MultiLayerNetworkTemplate}.
 */
@Getter
public class BiasNode extends DefaultNode {

  private final Supplier<MultiLayerNetworkTemplate> multiLayerNetworkTemplateSupplier;

  public BiasNode(Supplier<MultiLayerNetworkTemplate> multiLayerNetworkTemplateSupplier) {
    super(Providers.constant(IconsService.ICON_BIAS),
        Providers.mapped(multiLayerNetworkTemplateSupplier, BiasNode::labelFrom),
        Collections::emptyList);
    this.multiLayerNetworkTemplateSupplier = multiLayerNetworkTemplateSupplier;
  }

  private static String labelFrom(MultiLayerNetworkTemplate multiLayerNetworkTemplate) {
    return "Bias %s".formatted(multiLayerNetworkTemplate.getBias());
  }
}
