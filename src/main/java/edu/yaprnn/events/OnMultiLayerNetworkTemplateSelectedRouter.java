package edu.yaprnn.events;

import edu.yaprnn.gui.model.nodes.ModelNode;
import edu.yaprnn.networks.templates.MultiLayerNetworkTemplate;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.function.Function;

@Singleton
public class OnMultiLayerNetworkTemplateSelectedRouter
    extends AbstractSelectionRouter<MultiLayerNetworkTemplate, OnMultiLayerNetworkTemplateSelected> {

  @Inject
  EventsMapper eventsMapper;
  @Inject
  Event<OnMultiLayerNetworkTemplateSelected> onMultiLayerNetworkTemplateSelectedEvent;

  @Override
  protected Class<MultiLayerNetworkTemplate> elementType() {
    return MultiLayerNetworkTemplate.class;
  }

  @Override
  protected OnMultiLayerNetworkTemplateSelected createEvent(
      MultiLayerNetworkTemplate selected) {
    return new OnMultiLayerNetworkTemplateSelected(selected);
  }

  @Override
  protected Event<OnMultiLayerNetworkTemplateSelected> event() {
    return onMultiLayerNetworkTemplateSelectedEvent;
  }

  @Override
  protected Function<ModelNode, MultiLayerNetworkTemplate> modelNodeMapper() {
    return eventsMapper::toMultiLayerNetworkTemplate;
  }
}
