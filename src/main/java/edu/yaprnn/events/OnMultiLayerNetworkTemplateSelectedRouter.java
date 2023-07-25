package edu.yaprnn.events;

import edu.yaprnn.networks.templates.MultiLayerNetworkTemplate;
import jakarta.enterprise.event.Event;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.Objects;
import lombok.Getter;

@Singleton
public class OnMultiLayerNetworkTemplateSelectedRouter {

  @Inject
  EventsMapper eventsMapper;
  @Inject
  Event<OnMultiLayerNetworkTemplateSelected> onMultiLayerNetworkTemplateSelectedEvent;

  @Getter
  private MultiLayerNetworkTemplate selected;

  void unselectIfRemoved(@Observes OnRepositoryElementsRemoved event) {
    if (Objects.isNull(selected)) {
      return;
    }
    if (event.elementTypeClass() != MultiLayerNetworkTemplate.class) {
      return;
    }
    if (event.removed().contains(selected)) {
      setSelected((MultiLayerNetworkTemplate) null);
    }
  }

  public void setSelected(MultiLayerNetworkTemplate value) {
    selected = value;
    fireEvent();
  }

  private void fireEvent() {
    var event = new OnMultiLayerNetworkTemplateSelected(selected);
    onMultiLayerNetworkTemplateSelectedEvent.fire(event);
  }

  void setSelected(@Observes OnModelNodeSelected event) {
    setSelected(eventsMapper.toMultiLayerNetworkTemplate(event.value()));
  }
}
