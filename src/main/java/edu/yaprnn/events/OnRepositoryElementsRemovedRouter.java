package edu.yaprnn.events;

import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.List;

@Singleton
public final class OnRepositoryElementsRemovedRouter {

  @Inject
  Event<OnRepositoryElementsRemoved> onRepositoryElementRemovedEvent;

  public void fireEvent(Class<?> elementTypeClass, List<?> removed) {
    var event = new OnRepositoryElementsRemoved(elementTypeClass, removed);
    onRepositoryElementRemovedEvent.fire(event);
  }
}
