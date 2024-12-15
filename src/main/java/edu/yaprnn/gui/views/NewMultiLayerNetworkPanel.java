package edu.yaprnn.gui.views;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;

import edu.yaprnn.gui.services.ControlsService;
import edu.yaprnn.model.Repository;
import edu.yaprnn.networks.templates.MultiLayerNetworkTemplate;
import edu.yaprnn.support.swing.DialogsService;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import java.awt.Component;
import java.util.function.Consumer;
import javax.swing.GroupLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import lombok.Builder;

public class NewMultiLayerNetworkPanel extends JPanel {

  private static final String TITLE = "New Multilayer Network";

  private static int counter = 0;

  @Inject
  ControlsService controlsService;
  @Inject
  DialogsService dialogsService;
  @Inject
  Repository repository;

  private JTextField nameTextField;
  private JComboBox<MultiLayerNetworkTemplate> multiLayerNetworkTemplatesComboBox;

  @PostConstruct
  void initialize() {
    var nameLabel = new JLabel("Name");
    var templateLabel = new JLabel("Multilayer Network Template");

    nameTextField = new JTextField("%s %d".formatted(TITLE, ++counter));

    multiLayerNetworkTemplatesComboBox = new JComboBox<>(
        repository.getMultiLayerNetworkTemplates().toArray(MultiLayerNetworkTemplate[]::new));

    var groupLayout = new GroupLayout(this);
    groupLayout.setHorizontalGroup(groupLayout.createParallelGroup()
        .addComponent(nameLabel)
        .addComponent(nameTextField, PREFERRED_SIZE, DEFAULT_SIZE, 200)
        .addComponent(templateLabel)
        .addComponent(multiLayerNetworkTemplatesComboBox, PREFERRED_SIZE, DEFAULT_SIZE, 200));
    groupLayout.setVerticalGroup(groupLayout.createSequentialGroup()
        .addComponent(nameLabel)
        .addComponent(nameTextField, PREFERRED_SIZE, DEFAULT_SIZE, 28)
        .addComponent(templateLabel)
        .addComponent(multiLayerNetworkTemplatesComboBox, PREFERRED_SIZE, DEFAULT_SIZE, 28));
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
        var multiLayerNetworkTemplate = (MultiLayerNetworkTemplate) multiLayerNetworkTemplatesComboBox.getSelectedItem();
        var parameters = Parameters.builder()
            .name(nameTextField.getText())
            .multiLayerNetworkTemplate(multiLayerNetworkTemplate)
            .build();

        nameTextField.setBackground(controlsService.validationColor(parameters.isNameValid()));

        if (parameters.isValid()) {
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
  public record Parameters(String name, MultiLayerNetworkTemplate multiLayerNetworkTemplate) {

    public boolean isValid() {
      return isNameValid();
    }

    public boolean isNameValid() {
      return !name.isBlank();
    }
  }
}
