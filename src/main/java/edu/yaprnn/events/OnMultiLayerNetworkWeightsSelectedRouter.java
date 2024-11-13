package edu.yaprnn.events;

import edu.yaprnn.networks.MultiLayerNetwork;
import jakarta.enterprise.event.Event;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.Getter;

@Singleton
public final class OnMultiLayerNetworkWeightsSelectedRouter {

  @Inject
  EventsMapper eventsMapper;
  @Inject
  Event<OnMultiLayerNetworkWeightsSelected> onMultiLayerNetworkWeightsSelectedEvent;

  @Getter
  private MultiLayerNetwork multiLayerNetwork;
  @Getter
  private int weightsIndex;

  void setWeights(@Observes OnModelNodeSelected event) {
    var modelNode = event.value();
    setWeights(eventsMapper.toMultiLayerNetwork(modelNode), eventsMapper.toWeightsIndex(modelNode));
  }

  public void setWeights(MultiLayerNetwork multiLayerNetwork, int weightsIndex) {
    this.multiLayerNetwork = multiLayerNetwork;
    this.weightsIndex = weightsIndex;
    fireEvent();
  }

  private void fireEvent() {
    var event = new OnMultiLayerNetworkWeightsSelected(multiLayerNetwork, weightsIndex);
    onMultiLayerNetworkWeightsSelectedEvent.fire(event);
  }
}
