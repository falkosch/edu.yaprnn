package edu.yaprnn.events;

import edu.yaprnn.networks.MultiLayerNetwork;
import jakarta.enterprise.event.Event;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.Objects;
import lombok.Getter;

@Singleton
public final class OnMultiLayerNetworkSelectedRouter {

  @Inject
  EventsMapper eventsMapper;
  @Inject
  Event<OnMultiLayerNetworkSelected> onMultiLayerNetworkSelectedEvent;

  @Getter
  private MultiLayerNetwork selected;

  void unselectIfRemoved(@Observes OnRepositoryElementsRemoved event) {
    if (Objects.isNull(selected)) {
      return;
    }
    if (event.elementTypeClass() != MultiLayerNetwork.class) {
      return;
    }
    if (event.removed().contains(selected)) {
      setSelected((MultiLayerNetwork) null);
    }
  }

  public void setSelected(MultiLayerNetwork value) {
    selected = value;
    fireEvent();
  }

  private void fireEvent() {
    var event = new OnMultiLayerNetworkSelected(selected);
    onMultiLayerNetworkSelectedEvent.fire(event);
  }

  void setSelected(@Observes OnModelNodeSelected event) {
    setSelected(eventsMapper.toMultiLayerNetwork(event.value()));
  }
}
