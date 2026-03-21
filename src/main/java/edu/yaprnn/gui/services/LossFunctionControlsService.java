package edu.yaprnn.gui.services;

import edu.yaprnn.networks.loss.LossFunction;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class LossFunctionControlsService {

  @Any
  @Inject
  Instance<LossFunction> lossFunctionInstance;

  public LossFunction[] lossFunctions() {
    return lossFunctionInstance.stream()
        .sorted(LossFunction.COMPARATOR)
        .toArray(LossFunction[]::new);
  }
}
