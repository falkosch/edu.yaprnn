package edu.yaprnn.gui.model.nodes;

import edu.yaprnn.gui.services.IconsService;
import edu.yaprnn.networks.loss.LossFunction;
import edu.yaprnn.networks.templates.MultiLayerNetworkTemplate;
import edu.yaprnn.support.Providers;
import java.util.Collections;
import java.util.function.Supplier;
import lombok.Getter;

/**
 * Represents the {@link LossFunction} of a {@link MultiLayerNetworkTemplate}.
 */
@Getter
public class LossFunctionNode extends DefaultNode {

  private final Supplier<MultiLayerNetworkTemplate> multiLayerNetworkTemplateSupplier;

  public LossFunctionNode(Supplier<MultiLayerNetworkTemplate> multiLayerNetworkTemplateSupplier) {
    super(Providers.constant(IconsService.ICON_LOSS_FUNCTION),
        Providers.mapped(multiLayerNetworkTemplateSupplier, LossFunctionNode::labelFrom),
        Collections::emptyList);
    this.multiLayerNetworkTemplateSupplier = multiLayerNetworkTemplateSupplier;
  }

  private static String labelFrom(MultiLayerNetworkTemplate multiLayerNetworkTemplate) {
    return "L(x) %s".formatted(multiLayerNetworkTemplate.getLossFunction());
  }
}
