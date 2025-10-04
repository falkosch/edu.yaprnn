package edu.yaprnn.gui.views;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;

import edu.yaprnn.gui.services.ControlsService;
import edu.yaprnn.support.swing.DialogsService;
import edu.yaprnn.training.DataSelector;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import java.awt.Component;
import java.util.function.Consumer;
import javax.swing.ComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

public class RandomizeTrainingDataPanel extends JPanel {

  public static final String TITLE = "Randomized Training Data";

  private static int counter = 0;

  @Inject
  ControlsService controlsService;
  @Inject
  DialogsService dialogsService;

  private SpinnerNumberModel trainingSizeSpinnerNumberModel;
  private SpinnerNumberModel devTestSizeSpinnerNumberModel;
  private JTextField nameTextField;
  private ComboBoxModel<DataSelector> dataSelectorsComboBoxModel;

  @PostConstruct
  void initialize() {
    var trainingSizeLabel = new JLabel("Training size");
    var devTestSizeLabel = new JLabel("Dev Test size");
    var nameLabel = new JLabel("Name");
    var dataSelectorLabel = new JLabel("Data selector");

    trainingSizeSpinnerNumberModel = controlsService.trainingSizeSpinnerNumberModel();
    devTestSizeSpinnerNumberModel = controlsService.devTestSizeSpinnerNumberModel();
    nameTextField = new JTextField("%s %d".formatted(TITLE, ++counter));
    dataSelectorsComboBoxModel = controlsService.dataSelectorsComboBoxModel();

    var trainingSizeSpinner = new JSpinner(trainingSizeSpinnerNumberModel);
    trainingSizeSpinner.addKeyListener(controlsService.onlyNumbersKeyListener(true, true));
    var devTestSizeSpinner = new JSpinner(devTestSizeSpinnerNumberModel);
    devTestSizeSpinner.addKeyListener(controlsService.onlyNumbersKeyListener(true, true));
    var dataSelectorComboBox = new JComboBox<>(dataSelectorsComboBoxModel);

    var groupLayout = new GroupLayout(this);
    groupLayout.setHorizontalGroup(groupLayout.createParallelGroup()
        .addComponent(nameLabel)
        .addComponent(nameTextField, PREFERRED_SIZE, DEFAULT_SIZE, 200)
        .addComponent(trainingSizeLabel)
        .addComponent(trainingSizeSpinner, PREFERRED_SIZE, DEFAULT_SIZE, 100)
        .addComponent(devTestSizeLabel)
        .addComponent(devTestSizeSpinner, PREFERRED_SIZE, DEFAULT_SIZE, 100)
        .addComponent(dataSelectorLabel)
        .addComponent(dataSelectorComboBox, PREFERRED_SIZE, DEFAULT_SIZE, 200));
    groupLayout.setVerticalGroup(groupLayout.createSequentialGroup()
        .addComponent(nameLabel)
        .addComponent(nameTextField, PREFERRED_SIZE, DEFAULT_SIZE, 28)
        .addComponent(trainingSizeLabel)
        .addComponent(trainingSizeSpinner, PREFERRED_SIZE, DEFAULT_SIZE, 28)
        .addComponent(devTestSizeLabel)
        .addComponent(devTestSizeSpinner, PREFERRED_SIZE, DEFAULT_SIZE, 28)
        .addComponent(dataSelectorLabel)
        .addComponent(dataSelectorComboBox, PREFERRED_SIZE, DEFAULT_SIZE, 28));
    groupLayout.setAutoCreateContainerGaps(true);
    groupLayout.setAutoCreateGaps(true);
    setLayout(groupLayout);
  }

  public void show(Component parent, Consumer<Parameters> parametersConsumer) {
    setVisible(true);

    while (true) {
      var response = dialogsService.confirm(parent, this, TITLE);
      if (response != JOptionPane.OK_OPTION) {
        return;
      }

      try {
        var name = nameTextField.getText();
        var dataSelector = (DataSelector) dataSelectorsComboBoxModel.getSelectedItem();

        var isNameValid = !name.isBlank();
        nameTextField.setBackground(controlsService.validationColor(isNameValid));

        if (isNameValid) {
          parametersConsumer.accept(
              new Parameters(name, getSizePercentage(trainingSizeSpinnerNumberModel),
                  getSizePercentage(devTestSizeSpinnerNumberModel), dataSelector));
          return;
        }
      } catch (Throwable throwable) {
        dialogsService.showError(parent, TITLE, throwable);
        return;
      }
    }
  }

  private float getSizePercentage(SpinnerNumberModel sizeSpinnerNumberModel) {
    return sizeSpinnerNumberModel.getNumber().floatValue() / 100f;
  }

  public record Parameters(String name, float trainingPercentage, float devTestPercentage,
                           DataSelector dataSelector) {

  }
}
