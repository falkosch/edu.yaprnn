package edu.yaprnn.gui.model.nodes;

import edu.yaprnn.gui.model.NetworksTreeModel;
import edu.yaprnn.gui.services.IconsService;
import edu.yaprnn.networks.MultiLayerNetwork;
import edu.yaprnn.networks.templates.MultiLayerNetworkTemplate;
import edu.yaprnn.support.Providers;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import lombok.Getter;

/**
 * Represents the structure of a {@link MultiLayerNetwork} in the {@link NetworksTreeModel}.
 */
@Getter
public class MultiLayerNetworkTemplateNode extends DefaultNode {

  private final Supplier<MultiLayerNetworkTemplate> templateSupplier;

  public MultiLayerNetworkTemplateNode(Supplier<MultiLayerNetworkTemplate> templateSupplier) {
    super(Providers.constant(IconsService.ICON_MULTI_LAYER_NETWORK_TEMPLATE),
        Providers.mapped(templateSupplier, MultiLayerNetworkTemplateNode::labelFrom),
        Providers.mapped(templateSupplier, MultiLayerNetworkTemplateNode::childrenFrom));
    this.templateSupplier = templateSupplier;
  }

  private static String labelFrom(MultiLayerNetworkTemplate template) {
    return template.getName();
  }

  private static List<? extends ModelNode> childrenFrom(MultiLayerNetworkTemplate template) {
    var templateSupplier = Providers.constant(template);
    var layers = template.getLayers();

    var valueNodes = Stream.of(new BiasNode(templateSupplier),
        new LossFunctionNode(templateSupplier));

    var layerNodes = IntStream.range(0, layers.size())
        .mapToObj(i -> new LayerTemplateNode(templateSupplier, Providers.constant(i)));

    return Stream.concat(valueNodes, layerNodes).toList();
  }
}
