package edu.yaprnn.gui.views;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;

import edu.yaprnn.gui.services.ActivationFunctionControlsService;
import edu.yaprnn.gui.services.ControlsService;
import edu.yaprnn.gui.services.LossFunctionControlsService;
import edu.yaprnn.gui.services.NetworksControlsService;
import edu.yaprnn.networks.activation.ActivationFunction;
import edu.yaprnn.networks.loss.LossFunction;
import edu.yaprnn.support.swing.DialogsService;
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
import lombok.Builder;

public class NewMultiLayerNetworkTemplatePanel extends JPanel {

  private static final String TITLE = "New Multilayer Network Template";

  private static int counter = 0;

  @Inject
  ActivationFunctionControlsService activationFunctionControlsService;
  @Inject
  ControlsService controlsService;
  @Inject
  DialogsService dialogsService;
  @Inject
  LossFunctionControlsService lossFunctionControlsService;
  @Inject
  NetworksControlsService networksControlsService;

  private JTextField nameTextField;
  private SpinnerNumberModel layersCountSpinnerNumberModel;
  private SpinnerNumberModel layersSizeSpinnerNumberModel;
  private ComboBoxModel<ActivationFunction> activationFunctionsComboBoxModel;
  private ComboBoxModel<LossFunction> lossFunctionsComboBoxModel;

  @PostConstruct
  void initialize() {
    var nameLabel = new JLabel("Name");
    var layersCountLabel = new JLabel("Layers Count");
    var layerSizeLabel = new JLabel("Layer size");
    var activationFunctionLabel = new JLabel("Activation function");
    var lossFunctionLabel = new JLabel("Loss function");

    nameTextField = new JTextField("%s %d".formatted(TITLE, ++counter));
    layersCountSpinnerNumberModel = networksControlsService.layersCountSpinnerNumberModel();
    layersSizeSpinnerNumberModel = networksControlsService.layerSizeSpinnerNumberModel();
    activationFunctionsComboBoxModel = activationFunctionControlsService.activationFunctionsComboBoxModel();
    lossFunctionsComboBoxModel = lossFunctionControlsService.lossFunctionsComboBoxModel();

    var layersCountSpinner = new JSpinner(layersCountSpinnerNumberModel);
    layersCountSpinner.addKeyListener(controlsService.onlyNumbersKeyListener(true, true));
    var layersSizeSpinner = new JSpinner(layersSizeSpinnerNumberModel);
    layersSizeSpinner.addKeyListener(controlsService.onlyNumbersKeyListener(true, true));
    var activationFunctionComboBox = new JComboBox<>(activationFunctionsComboBoxModel);
    var lossFunctionComboBox = new JComboBox<>(lossFunctionsComboBoxModel);

    var groupLayout = new GroupLayout(this);
    groupLayout.setHorizontalGroup(groupLayout.createParallelGroup()
        .addComponent(nameLabel)
        .addComponent(nameTextField, PREFERRED_SIZE, DEFAULT_SIZE, 200)
        .addComponent(layersCountLabel)
        .addComponent(layersCountSpinner, PREFERRED_SIZE, DEFAULT_SIZE, 100)
        .addComponent(layerSizeLabel)
        .addComponent(layersSizeSpinner, PREFERRED_SIZE, DEFAULT_SIZE, 100)
        .addComponent(activationFunctionLabel)
        .addComponent(activationFunctionComboBox, PREFERRED_SIZE, DEFAULT_SIZE, 200)
        .addComponent(lossFunctionLabel)
        .addComponent(lossFunctionComboBox, PREFERRED_SIZE, DEFAULT_SIZE, 200));
    groupLayout.setVerticalGroup(groupLayout.createSequentialGroup()
        .addComponent(nameLabel)
        .addComponent(nameTextField, PREFERRED_SIZE, DEFAULT_SIZE, 28)
        .addComponent(layersCountLabel)
        .addComponent(layersCountSpinner, PREFERRED_SIZE, DEFAULT_SIZE, 28)
        .addComponent(layerSizeLabel)
        .addComponent(layersSizeSpinner, PREFERRED_SIZE, DEFAULT_SIZE, 28)
        .addComponent(activationFunctionLabel)
        .addComponent(activationFunctionComboBox, PREFERRED_SIZE, DEFAULT_SIZE, 28)
        .addComponent(lossFunctionLabel)
        .addComponent(lossFunctionComboBox, PREFERRED_SIZE, DEFAULT_SIZE, 28));
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
        var activationFunction = (ActivationFunction) activationFunctionsComboBoxModel.getSelectedItem();
        var lossFunction = (LossFunction) lossFunctionsComboBoxModel.getSelectedItem();
        var parameters = Parameters.builder()
            .name(nameTextField.getText())
            .layersCount(layersCountSpinnerNumberModel.getNumber().intValue())
            .layersSize(layersSizeSpinnerNumberModel.getNumber().intValue())
            .activationFunction(activationFunction)
            .lossFunction(lossFunction)
            .build();

        var isValid = parameters.isValid();
        nameTextField.setBackground(controlsService.validationColor(isValid));

        if (isValid) {
          consumer.accept(parameters);
          return;
        }
      } catch (Throwable throwable) {
        dialogsService.showError(parent, TITLE, throwable);
        return;
      }
    }
  }

  @Builder
  public record Parameters(String name, int layersCount, int layersSize,
                           ActivationFunction activationFunction, LossFunction lossFunction) {

    boolean isValid() {
      return !name.isBlank();
    }
  }
}
