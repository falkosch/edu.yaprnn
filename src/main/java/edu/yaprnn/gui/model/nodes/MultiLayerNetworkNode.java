package edu.yaprnn.gui.model.nodes;

import edu.yaprnn.functions.Providers;
import edu.yaprnn.gui.model.NetworksTreeModel;
import edu.yaprnn.gui.services.IconsService;
import edu.yaprnn.networks.MultiLayerNetwork;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import lombok.Getter;

/**
 * Represents the structure of a {@link MultiLayerNetwork} in the {@link NetworksTreeModel}.
 */
@Getter
public class MultiLayerNetworkNode extends DefaultNode {

  private final Supplier<MultiLayerNetwork> multiLayerNetworkSupplier;

  public MultiLayerNetworkNode(Supplier<MultiLayerNetwork> multiLayerNetworkSupplier) {
    super(Providers.constant(IconsService.ICON_MULTI_LAYER_NETWORK),
        Providers.mapped(multiLayerNetworkSupplier, MultiLayerNetworkNode::labelFrom),
        Providers.mapped(multiLayerNetworkSupplier, MultiLayerNetworkNode::childrenFrom));
    this.multiLayerNetworkSupplier = multiLayerNetworkSupplier;
  }

  private static String labelFrom(MultiLayerNetwork multiLayerNetwork) {
    return multiLayerNetwork.getName();
  }

  private static List<? extends ModelNode> childrenFrom(MultiLayerNetwork multiLayerNetwork) {
    var multiLayerNetworkSupplier = Providers.constant(multiLayerNetwork);
    return IntStream.range(0, multiLayerNetwork.getLayerWeights().length)
        .mapToObj(Providers::constant)
        .map(weightsIndexSupplier -> new MultiLayerNetworkWeightsNode(multiLayerNetworkSupplier,
            weightsIndexSupplier))
        .toList();
  }
}
