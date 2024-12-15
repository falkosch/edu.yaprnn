package edu.yaprnn.gui.services;

import edu.yaprnn.networks.loss.HalfSquaredErrorLossFunction;
import edu.yaprnn.networks.loss.LossFunction;
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
public class LossFunctionControlsService {

  @Inject
  DialogsService dialogsService;

  @Any
  @Inject
  Instance<LossFunction> lossFunctionInstance;

  public JComboBox<LossFunction> lossFunctionsComboBox(Consumer<LossFunction> consumer) {
    return lossFunctionsComboBox(lossFunctionsComboBoxModel(), consumer);
  }

  public JComboBox<LossFunction> lossFunctionsComboBox(ComboBoxModel<LossFunction> model,
      Consumer<LossFunction> consumer) {
    var comboBox = new JComboBox<>(model);
    comboBox.addActionListener(_ -> {
      try {
        if (comboBox.getSelectedItem() instanceof LossFunction value) {
          consumer.accept(value);
        }
      } catch (Throwable throwable) {
        dialogsService.showError(comboBox, "Loss Function", throwable);
      }
    });
    return comboBox;
  }

  public ComboBoxModel<LossFunction> lossFunctionsComboBoxModel() {
    var defaultLossFunction = lossFunctionInstance.select(HalfSquaredErrorLossFunction.class).get();

    var model = new DefaultComboBoxModel<>(lossFunctions());
    model.setSelectedItem(defaultLossFunction);
    return model;
  }

  public LossFunction[] lossFunctions() {
    return lossFunctionInstance.stream()
        .sorted(LossFunction.COMPARATOR)
        .toArray(LossFunction[]::new);
  }
}
