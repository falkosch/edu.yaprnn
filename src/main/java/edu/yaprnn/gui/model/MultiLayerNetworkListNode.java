package edu.yaprnn.gui.model;

import edu.yaprnn.functions.Providers;
import edu.yaprnn.gui.model.nodes.DefaultNode;
import edu.yaprnn.gui.model.nodes.ModelNode;
import edu.yaprnn.gui.model.nodes.MultiLayerNetworkNode;
import edu.yaprnn.gui.services.IconsService;
import edu.yaprnn.model.Repository;
import edu.yaprnn.networks.MultiLayerNetwork;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.List;

@Singleton
public class MultiLayerNetworkListNode extends DefaultNode {

  @Inject
  public MultiLayerNetworkListNode(Repository repository) {
    super(Providers.constant(IconsService.ICON_NODE),
        Providers.mapped(repository::getMultiLayerNetworks, MultiLayerNetworkListNode::labelFrom),
        Providers.mapped(repository::getMultiLayerNetworks,
            MultiLayerNetworkListNode::childrenFrom));
  }

  private static String labelFrom(List<MultiLayerNetwork> multiLayerNetworks) {
    return "Multilayer Networks (%d)".formatted(multiLayerNetworks.size());
  }

  private static List<? extends ModelNode> childrenFrom(
      List<MultiLayerNetwork> multiLayerNetworks) {
    return multiLayerNetworks.stream()
        .map(Providers::constant)
        .map(MultiLayerNetworkNode::new)
        .toList();
  }
}
