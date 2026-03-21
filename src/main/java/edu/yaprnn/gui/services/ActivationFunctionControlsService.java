package edu.yaprnn.gui.services;

import edu.yaprnn.networks.activation.ActivationFunction;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class ActivationFunctionControlsService {

  @Any
  @Inject
  Instance<ActivationFunction> activationFunctionInstance;

  public ActivationFunction[] activationFunctions() {
    return activationFunctionInstance.stream()
        .sorted(ActivationFunction.COMPARATOR)
        .toArray(ActivationFunction[]::new);
  }
}
