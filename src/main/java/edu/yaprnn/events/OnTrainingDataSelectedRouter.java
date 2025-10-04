package edu.yaprnn.events;

import edu.yaprnn.training.TrainingData;
import jakarta.enterprise.event.Event;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.Objects;
import lombok.Getter;

@Singleton
public final class OnTrainingDataSelectedRouter {

  @Inject
  EventsMapper eventsMapper;
  @Inject
  Event<OnTrainingDataSelected> onTrainingDataSelectedEvent;

  @Getter
  private TrainingData selected;

  void unselectIfRemoved(@Observes OnRepositoryElementsRemoved event) {
    if (Objects.isNull(selected)) {
      return;
    }
    if (event.elementTypeClass() != TrainingData.class) {
      return;
    }
    if (event.removed().contains(selected)) {
      setSelected((TrainingData) null);
    }
  }

  public void setSelected(TrainingData value) {
    selected = value;
    fireEvent();
  }

  private void fireEvent() {
    var event = new OnTrainingDataSelected(selected);
    onTrainingDataSelectedEvent.fire(event);
  }

  void setSelected(@Observes OnModelNodeSelected event) {
    setSelected(eventsMapper.toTrainingData(event.value()));
  }
}
