package edu.yaprnn.gui.model.nodes;

import edu.yaprnn.gui.model.NetworksTreeModel;
import edu.yaprnn.gui.services.IconsService;
import edu.yaprnn.networks.MultiLayerNetwork;
import edu.yaprnn.networks.templates.MultiLayerNetworkTemplate;
import edu.yaprnn.support.Providers;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import lombok.Getter;

/**
 * Represents the structure of a {@link MultiLayerNetwork} in the {@link NetworksTreeModel}.
 */
@Getter
public class MultiLayerNetworkTemplateNode extends DefaultNode {

  private final Supplier<MultiLayerNetworkTemplate> multiLayerNetworkTemplateSupplier;

  public MultiLayerNetworkTemplateNode(
      Supplier<MultiLayerNetworkTemplate> multiLayerNetworkTemplateSupplier) {
    super(Providers.constant(IconsService.ICON_MULTI_LAYER_NETWORK_TEMPLATE),
        Providers.mapped(multiLayerNetworkTemplateSupplier,
            MultiLayerNetworkTemplateNode::labelFrom),
        Providers.mapped(multiLayerNetworkTemplateSupplier,
            MultiLayerNetworkTemplateNode::childrenFrom));
    this.multiLayerNetworkTemplateSupplier = multiLayerNetworkTemplateSupplier;
  }

  private static String labelFrom(MultiLayerNetworkTemplate multiLayerNetworkTemplate) {
    return multiLayerNetworkTemplate.getName();
  }

  private static List<? extends ModelNode> childrenFrom(
      MultiLayerNetworkTemplate multiLayerNetworkTemplate) {
    var multiLayerNetworkTemplateSupplier = Providers.constant(multiLayerNetworkTemplate);
    return IntStream.range(0, multiLayerNetworkTemplate.getLayers().size())
        .mapToObj(Providers::constant)
        .map(layerIndexSupplier -> new LayerTemplateNode(multiLayerNetworkTemplateSupplier,
            layerIndexSupplier))
        .toList();
  }
}
