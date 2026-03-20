package edu.yaprnn.events;

import edu.yaprnn.gui.model.nodes.ModelNode;
import jakarta.enterprise.event.Event;
import jakarta.enterprise.event.Observes;
import java.util.Objects;
import java.util.function.Function;
import lombok.Getter;

public abstract class AbstractSelectionRouter<T, E> {

  @Getter
  private T selected;

  protected abstract Class<T> elementType();

  protected abstract E createEvent(T selected);

  protected abstract Event<E> event();

  protected abstract Function<ModelNode, T> modelNodeMapper();

  void unselectIfRemoved(@Observes OnRepositoryElementsRemoved event) {
    if (Objects.nonNull(selected) && event.elementTypeClass() == elementType()
        && event.removed().contains(selected)) {
      setSelected((T) null);
    }
  }

  public void setSelected(T value) {
    selected = value;
    event().fire(createEvent(selected));
  }

  void setSelected(@Observes OnModelNodeSelected event) {
    setSelected(modelNodeMapper().apply(event.value()));
  }
}
