package edu.yaprnn.gui.services;

import edu.yaprnn.support.swing.DialogsService;
import edu.yaprnn.training.ClassifierDataSelector;
import edu.yaprnn.training.DataSelector;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.function.Consumer;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;

@Singleton
public class DataSelectorControlsService {

  @Inject
  DialogsService dialogsService;

  @Any
  @Inject
  Instance<DataSelector> dataSelectorInstance;

  public JComboBox<DataSelector> dataSelectorsComboBox(Consumer<DataSelector> consumer) {
    return dataSelectorsComboBox(dataSelectorsComboBoxModel(), consumer);
  }

  public JComboBox<DataSelector> dataSelectorsComboBox(ComboBoxModel<DataSelector> model,
      Consumer<DataSelector> consumer) {
    var comboBox = new JComboBox<>(model);
    comboBox.addActionListener(_ -> {
      try {
        if (comboBox.getSelectedItem() instanceof DataSelector value) {
          consumer.accept(value);
        }
      } catch (Throwable throwable) {
        dialogsService.showError(comboBox, "Data Selector", throwable);
      }
    });
    return comboBox;
  }

  public ComboBoxModel<DataSelector> dataSelectorsComboBoxModel() {
    var model = new DefaultComboBoxModel<>(dataSelectors());
    model.setSelectedItem(dataSelectorInstance.select(ClassifierDataSelector.class).get());
    return model;
  }

  public DataSelector[] dataSelectors() {
    return dataSelectorInstance.stream().toArray(DataSelector[]::new);
  }
}
