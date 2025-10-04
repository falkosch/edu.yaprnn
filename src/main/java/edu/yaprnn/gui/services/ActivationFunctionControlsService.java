package edu.yaprnn.gui.services;

import edu.yaprnn.networks.functions.ActivationFunction;
import edu.yaprnn.networks.functions.TangentHyperbolicActivationFunction;
import edu.yaprnn.support.swing.DialogsService;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.function.Consumer;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;

@Singleton
public class ActivationFunctionControlsService {

  @Inject
  DialogsService dialogsService;

  @Any
  @Inject
  Instance<ActivationFunction> activationFunctionInstance;

  public JComboBox<ActivationFunction> activationFunctionsComboBox(
      Consumer<ActivationFunction> consumer) {
    return activationFunctionsComboBox(activationFunctionsComboBoxModel(), consumer);
  }

  public JComboBox<ActivationFunction> activationFunctionsComboBox(
      ComboBoxModel<ActivationFunction> model, Consumer<ActivationFunction> consumer) {
    var comboBox = new JComboBox<>(model);
    comboBox.addActionListener(_ -> {
      try {
        if (comboBox.getSelectedItem() instanceof ActivationFunction value) {
          consumer.accept(value);
        }
      } catch (Throwable throwable) {
        dialogsService.showError(comboBox, "Activation Function", throwable);
      }
    });
    return comboBox;
  }

  public ComboBoxModel<ActivationFunction> activationFunctionsComboBoxModel() {
    var defaultActivationFunction = activationFunctionInstance.select(
        TangentHyperbolicActivationFunction.class).get();

    var model = new DefaultComboBoxModel<>(activationFunctions());
    model.setSelectedItem(defaultActivationFunction);
    return model;
  }

  public ActivationFunction[] activationFunctions() {
    return activationFunctionInstance.stream()
        .sorted(ActivationFunction.COMPARATOR)
        .toArray(ActivationFunction[]::new);
  }
}
