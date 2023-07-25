package edu.yaprnn.events;

import java.util.List;

public record OnRepositoryElementsChanged(Class<?> elementTypeClass, List<?> changedItems) {

  public <T> List<T> castList(Class<T> targetElementTypeClass) {
    return changedItems.stream().map(targetElementTypeClass::cast).toList();
  }
}
