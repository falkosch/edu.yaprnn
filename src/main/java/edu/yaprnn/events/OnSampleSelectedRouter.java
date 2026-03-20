package edu.yaprnn.events;

import edu.yaprnn.gui.model.nodes.ModelNode;
import edu.yaprnn.samples.model.Sample;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.function.Function;

@Singleton
public class OnSampleSelectedRouter
    extends AbstractSelectionRouter<Sample, OnSampleSelected> {

  @Inject
  EventsMapper eventsMapper;
  @Inject
  Event<OnSampleSelected> onSampleSelectedEvent;

  @Override
  protected Class<Sample> elementType() {
    return Sample.class;
  }

  @Override
  protected OnSampleSelected createEvent(Sample selected) {
    return new OnSampleSelected(selected);
  }

  @Override
  protected Event<OnSampleSelected> event() {
    return onSampleSelectedEvent;
  }

  @Override
  protected Function<ModelNode, Sample> modelNodeMapper() {
    return eventsMapper::toSample;
  }
}
