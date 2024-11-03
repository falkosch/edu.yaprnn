package edu.yaprnn.gui.model;

import edu.yaprnn.gui.model.nodes.DefaultNode;
import edu.yaprnn.gui.model.nodes.ModelNode;
import edu.yaprnn.gui.model.nodes.MultiLayerNetworkTemplateNode;
import edu.yaprnn.gui.services.IconsService;
import edu.yaprnn.model.Repository;
import edu.yaprnn.networks.templates.MultiLayerNetworkTemplate;
import edu.yaprnn.support.Providers;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.List;

/**
 * Displays the list of {@link MultiLayerNetworkTemplate} in the {@link NetworksTreeModel}.
 */
@Singleton
public class MultiLayerNetworkTemplateListNode extends DefaultNode {

  @Inject
  public MultiLayerNetworkTemplateListNode(Repository repository) {
    super(Providers.constant(IconsService.ICON_NODE),
        Providers.mapped(repository::getMultiLayerNetworkTemplates,
            MultiLayerNetworkTemplateListNode::labelFrom),
        Providers.mapped(repository::getMultiLayerNetworkTemplates,
            MultiLayerNetworkTemplateListNode::childrenFrom));
  }

  private static String labelFrom(List<MultiLayerNetworkTemplate> multiLayerNetworkTemplates) {
    return "Multilayer Network Templates (%d)".formatted(multiLayerNetworkTemplates.size());
  }

  private static List<? extends ModelNode> childrenFrom(
      List<MultiLayerNetworkTemplate> multiLayerNetworkTemplates) {
    return multiLayerNetworkTemplates.stream()
        .map(Providers::constant)
        .map(MultiLayerNetworkTemplateNode::new)
        .toList();
  }
}
