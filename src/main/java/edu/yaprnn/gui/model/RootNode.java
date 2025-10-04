package edu.yaprnn.gui.model;

import edu.yaprnn.gui.model.nodes.DefaultNode;
import edu.yaprnn.gui.services.IconsService;
import edu.yaprnn.support.Providers;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.List;

@Singleton
public class RootNode extends DefaultNode {

  @Inject
  public RootNode(MultiLayerNetworkListNode multiLayerNetworkListNode,
      MultiLayerNetworkTemplateListNode multiLayerNetworkTemplateListNode,
      TrainingDataListNode trainingDataListNode, AllSamplesListNode allSamplesListNode) {
    super(Providers.constant(IconsService.ICON_NODE), Providers.constant("root"),
        Providers.constant(List.of(multiLayerNetworkListNode, multiLayerNetworkTemplateListNode,
            trainingDataListNode, allSamplesListNode)));
  }
}
