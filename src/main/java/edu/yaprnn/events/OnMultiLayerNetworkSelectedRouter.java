package edu.yaprnn.events;

import edu.yaprnn.gui.model.nodes.ModelNode;
import edu.yaprnn.networks.MultiLayerNetwork;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.function.Function;

@Singleton
public class OnMultiLayerNetworkSelectedRouter
    extends AbstractSelectionRouter<MultiLayerNetwork, OnMultiLayerNetworkSelected> {

  @Inject
  EventsMapper eventsMapper;
  @Inject
  Event<OnMultiLayerNetworkSelected> onMultiLayerNetworkSelectedEvent;

  @Override
  protected Class<MultiLayerNetwork> elementType() {
    return MultiLayerNetwork.class;
  }

  @Override
  protected OnMultiLayerNetworkSelected createEvent(MultiLayerNetwork selected) {
    return new OnMultiLayerNetworkSelected(selected);
  }

  @Override
  protected Event<OnMultiLayerNetworkSelected> event() {
    return onMultiLayerNetworkSelectedEvent;
  }

  @Override
  protected Function<ModelNode, MultiLayerNetwork> modelNodeMapper() {
    return eventsMapper::toMultiLayerNetwork;
  }
}
