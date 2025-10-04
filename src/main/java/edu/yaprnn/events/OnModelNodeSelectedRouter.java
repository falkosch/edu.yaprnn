package edu.yaprnn.events;

import edu.yaprnn.gui.model.nodes.ModelNode;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.Getter;

@Singleton
public final class OnModelNodeSelectedRouter {

  @Inject
  Event<OnModelNodeSelected> onModelNodeSelectedEvent;

  @Getter
  private ModelNode selected;

  public void setSelected(ModelNode value) {
    selected = value;
    fireEvent();
  }

  private void fireEvent() {
    var event = new OnModelNodeSelected(selected);
    onModelNodeSelectedEvent.fire(event);
  }
}
