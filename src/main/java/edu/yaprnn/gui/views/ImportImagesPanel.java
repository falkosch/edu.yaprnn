package edu.yaprnn.gui.views;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;

import edu.yaprnn.gui.services.ControlsService;
import edu.yaprnn.gui.services.FilesService;
import edu.yaprnn.support.swing.DialogsService;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import java.awt.Component;
import java.io.File;
import java.util.function.Consumer;
import javax.swing.GroupLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class ImportImagesPanel extends JPanel {

  public static final String TITLE = "Import Images";

  @Inject
  ControlsService controlsService;
  @Inject
  DialogsService dialogsService;
  @Inject
  FilesService filesService;

  private JTextField labelsPackageTextField;
  private JTextField imagesPackageTextField;

  @PostConstruct
  void initialize() {
    var labelsPackageLabel = new JLabel("Path to the labels package");
    var imagesPackageLabel = new JLabel("Path to the images package");

    labelsPackageTextField = new JTextField();
    imagesPackageTextField = new JTextField();

    var labelsPackageChooseFileButton = filesService.chooseFileButton(this, labelsPackageTextField,
        filesService.labelsIdx1UByteFileExtension());
    var imagesPackageChooseFileButton = filesService.chooseFileButton(this, imagesPackageTextField,
        filesService.imagesIdx3UByteFileExtension());

    var groupLayout = new GroupLayout(this);
    groupLayout.setHorizontalGroup(groupLayout.createParallelGroup()
        .addComponent(labelsPackageLabel)
        .addGroup(groupLayout.createSequentialGroup()
            .addComponent(labelsPackageTextField, PREFERRED_SIZE, DEFAULT_SIZE, 200)
            .addComponent(labelsPackageChooseFileButton, PREFERRED_SIZE, PREFERRED_SIZE,
                PREFERRED_SIZE))
        .addComponent(imagesPackageLabel)
        .addGroup(groupLayout.createSequentialGroup()
            .addComponent(imagesPackageTextField, PREFERRED_SIZE, DEFAULT_SIZE, 200)
            .addComponent(imagesPackageChooseFileButton, PREFERRED_SIZE, PREFERRED_SIZE,
                PREFERRED_SIZE)));
    groupLayout.setVerticalGroup(groupLayout.createSequentialGroup()
        .addComponent(labelsPackageLabel)
        .addGroup(groupLayout.createParallelGroup()
            .addComponent(labelsPackageTextField, PREFERRED_SIZE, DEFAULT_SIZE, 28)
            .addComponent(labelsPackageChooseFileButton, PREFERRED_SIZE, DEFAULT_SIZE, 28))
        .addComponent(imagesPackageLabel)
        .addGroup(groupLayout.createParallelGroup()
            .addComponent(imagesPackageTextField, PREFERRED_SIZE, DEFAULT_SIZE, 28)
            .addComponent(imagesPackageChooseFileButton, PREFERRED_SIZE, DEFAULT_SIZE, 28)));
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
        var labelsPackage = labelsPackageTextField.getText();
        var labelsExists = new File(labelsPackage).exists();
        labelsPackageTextField.setBackground(controlsService.validationColor(labelsExists));

        var imagesPackage = imagesPackageTextField.getText();
        var imagesExists = new File(imagesPackage).exists();
        imagesPackageTextField.setBackground(controlsService.validationColor(imagesExists));

        if (labelsExists && imagesExists) {
          parametersConsumer.accept(new Parameters(labelsPackage, imagesPackage));
          return;
        }
      } catch (Throwable throwable) {
        dialogsService.showError(parent, TITLE, throwable);
        return;
      }
    }
  }

  public record Parameters(String labelsPackage, String imagesPackage) {

  }
}
