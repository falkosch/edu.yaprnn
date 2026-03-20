package edu.yaprnn.events;

import edu.yaprnn.gui.model.nodes.ModelNode;
import edu.yaprnn.training.TrainingData;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.function.Function;

@Singleton
public class OnTrainingDataSelectedRouter
    extends AbstractSelectionRouter<TrainingData, OnTrainingDataSelected> {

  @Inject
  EventsMapper eventsMapper;
  @Inject
  Event<OnTrainingDataSelected> onTrainingDataSelectedEvent;

  @Override
  protected Class<TrainingData> elementType() {
    return TrainingData.class;
  }

  @Override
  protected OnTrainingDataSelected createEvent(TrainingData selected) {
    return new OnTrainingDataSelected(selected);
  }

  @Override
  protected Event<OnTrainingDataSelected> event() {
    return onTrainingDataSelectedEvent;
  }

  @Override
  protected Function<ModelNode, TrainingData> modelNodeMapper() {
    return eventsMapper::toTrainingData;
  }
}
