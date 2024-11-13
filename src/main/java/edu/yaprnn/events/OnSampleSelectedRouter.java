package edu.yaprnn.events;

import edu.yaprnn.samples.model.Sample;
import jakarta.enterprise.event.Event;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.Objects;
import lombok.Getter;

@Singleton
public final class OnSampleSelectedRouter {

  @Inject
  EventsMapper eventsMapper;
  @Inject
  Event<OnSampleSelected> onSampleSelectedEvent;

  @Getter
  private Sample selected;

  void unselectIfRemoved(@Observes OnRepositoryElementsRemoved event) {
    if (Objects.isNull(selected)) {
      return;
    }
    if (event.elementTypeClass() != Sample.class) {
      return;
    }
    if (event.removed().contains(selected)) {
      setSelected((Sample) null);
    }
  }

  public void setSelected(Sample value) {
    selected = value;
    fireEvent();
  }

  private void fireEvent() {
    var event = new OnSampleSelected(selected);
    onSampleSelectedEvent.fire(event);
  }

  void setSelected(@Observes OnModelNodeSelected event) {
    setSelected(eventsMapper.toSample(event.value()));
  }
}
