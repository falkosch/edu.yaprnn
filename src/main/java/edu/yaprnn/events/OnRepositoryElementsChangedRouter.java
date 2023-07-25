package edu.yaprnn.events;

import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.List;

@Singleton
public class OnRepositoryElementsChangedRouter {

  @Inject
  Event<OnRepositoryElementsChanged> onRepositoryElementsChangedEvent;

  public void fireEvent(Class<?> elementTypeClass, List<?> changedItems) {
    var event = new OnRepositoryElementsChanged(elementTypeClass, changedItems);
    onRepositoryElementsChangedEvent.fire(event);
  }
}
