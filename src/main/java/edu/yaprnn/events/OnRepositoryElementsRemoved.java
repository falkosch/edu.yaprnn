package edu.yaprnn.events;

import java.util.List;

public record OnRepositoryElementsRemoved(Class<?> elementTypeClass, List<?> removed) {

  public <T> List<T> castList(Class<T> targetElementTypeClass) {
    return removed.stream().map(targetElementTypeClass::cast).toList();
  }
}
