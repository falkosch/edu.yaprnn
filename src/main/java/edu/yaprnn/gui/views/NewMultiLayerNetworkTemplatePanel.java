package edu.yaprnn.gui.views;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;

import edu.yaprnn.functions.ActivationFunction;
import edu.yaprnn.gui.services.ControlsService;
import edu.yaprnn.gui.services.DialogsService;
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

public class NewMultiLayerNetworkTemplatePanel extends JPanel {

  private static final String TITLE = "New Multilayer Network Template";

  private static int counter = 0;

  @Inject
  ControlsService controlsService;
  @Inject
  DialogsService dialogsService;

  private JTextField nameTextField;
  private SpinnerNumberModel layersCountSpinnerNumberModel;
  private SpinnerNumberModel layersSizeSpinnerNumberModel;
  private ComboBoxModel<ActivationFunction> activationFunctionsComboBoxModel;

  @PostConstruct
  void initialize() {
    var nameLabel = new JLabel("Name");
    var layersCountLabel = new JLabel("Layers Count");
    var layerSizeLabel = new JLabel("Layer size");
    var activationFunctionLabel = new JLabel("Activation function");

    nameTextField = new JTextField("%s %d".formatted(TITLE, ++counter));
    layersCountSpinnerNumberModel = controlsService.layersCountSpinnerNumberModel();
    layersSizeSpinnerNumberModel = controlsService.layerSizeSpinnerNumberModel();
    activationFunctionsComboBoxModel = controlsService.activationFunctionsComboBoxModel();

    var layersCountSpinner = new JSpinner(layersCountSpinnerNumberModel);
    layersCountSpinner.addKeyListener(controlsService.onlyNumbersKeyListener(true, true));
    var layersSizeSpinner = new JSpinner(layersSizeSpinnerNumberModel);
    layersSizeSpinner.addKeyListener(controlsService.onlyNumbersKeyListener(true, true));
    var activationFunctionComboBox = new JComboBox<>(activationFunctionsComboBoxModel);

    var groupLayout = new GroupLayout(this);
    groupLayout.setHorizontalGroup(groupLayout.createParallelGroup()
        .addComponent(nameLabel)
        .addComponent(nameTextField, PREFERRED_SIZE, DEFAULT_SIZE, 200)
        .addComponent(layersCountLabel)
        .addComponent(layersCountSpinner, PREFERRED_SIZE, DEFAULT_SIZE, 100)
        .addComponent(layerSizeLabel)
        .addComponent(layersSizeSpinner, PREFERRED_SIZE, DEFAULT_SIZE, 100)
        .addComponent(activationFunctionLabel)
        .addComponent(activationFunctionComboBox, PREFERRED_SIZE, DEFAULT_SIZE, 200));
    groupLayout.setVerticalGroup(groupLayout.createSequentialGroup()
        .addComponent(nameLabel)
        .addComponent(nameTextField, PREFERRED_SIZE, DEFAULT_SIZE, 28)
        .addComponent(layersCountLabel)
        .addComponent(layersCountSpinner, PREFERRED_SIZE, DEFAULT_SIZE, 28)
        .addComponent(layerSizeLabel)
        .addComponent(layersSizeSpinner, PREFERRED_SIZE, DEFAULT_SIZE, 28)
        .addComponent(activationFunctionLabel)
        .addComponent(activationFunctionComboBox, PREFERRED_SIZE, DEFAULT_SIZE, 28));
    groupLayout.setAutoCreateContainerGaps(true);
    groupLayout.setAutoCreateGaps(true);
    setLayout(groupLayout);
  }

  public void show(Component parent, Consumer<Parameters> consumer) {
    setVisible(true);

    while (true) {
      var response = dialogsService.confirm(parent, this, TITLE);
      if (response != JOptionPane.OK_OPTION) {
        return;
      }

      try {
        var name = nameTextField.getText();
        var layersCount = layersCountSpinnerNumberModel.getNumber().intValue();
        var layersSize = layersSizeSpinnerNumberModel.getNumber().intValue();
        var activationFunction = (ActivationFunction) activationFunctionsComboBoxModel.getSelectedItem();

        var isNameValid = !name.isBlank();
        nameTextField.setBackground(controlsService.validationColor(isNameValid));

        if (isNameValid) {
          consumer.accept(new Parameters(name, layersCount, layersSize, activationFunction));
          return;
        }
      } catch (Throwable throwable) {
        dialogsService.showError(parent, TITLE, throwable);
        return;
      }
    }
  }

  public record Parameters(String name, int layersCount, int layersSize,
                           ActivationFunction activationFunction) {

  }
}
