package edu.yaprnn.gui.services;

import edu.yaprnn.training.selectors.DataSelector;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class DataSelectorControlsService {

  @Any
  @Inject
  Instance<DataSelector> dataSelectorInstance;

  public DataSelector[] dataSelectors() {
    return dataSelectorInstance.stream().toArray(DataSelector[]::new);
  }
}
