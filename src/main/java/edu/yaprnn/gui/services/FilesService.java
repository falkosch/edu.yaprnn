package edu.yaprnn.gui.services;

import jakarta.inject.Singleton;
import java.awt.Component;
import java.io.File;
import java.util.Arrays;
import java.util.function.Consumer;
import java.util.stream.Stream;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;

@Singleton
public class FilesService {

  public FileNameExtensionFilter trainingDataFileExtension() {
    return new FileNameExtensionFilter("Training Data (*.yaprnn-training-data)",
        "yaprnn-training-data");
  }

  public FileNameExtensionFilter multiLayerNetworkTemplateFileExtension() {
    return new FileNameExtensionFilter("Multilayer Network Template (*.yaprnn-mln-template)",
        "yaprnn-mln-template");
  }

  public FileNameExtensionFilter multiLayerNetworkFileExtension() {
    return new FileNameExtensionFilter("Multilayer Network (*.yaprnn-mln)", "yaprnn-mln");
  }

  public FileNameExtensionFilter audioAiffFileExtension() {
    return new FileNameExtensionFilter("Audio (*.aiff)", "aiff");
  }

  public FileNameExtensionFilter imagesIdx3UByteFileExtension() {
    return new FileNameExtensionFilter("Images package (*.idx3-ubyte)", "idx3-ubyte");
  }

  public FileNameExtensionFilter labelsIdx1UByteFileExtension() {
    return new FileNameExtensionFilter("Labels package (*.idx1-ubyte)", "idx1-ubyte");
  }

  public JButton chooseFileButton(Component parent, JTextField target,
      FileNameExtensionFilter filter) {
    var button = new JButton("...");
    button.addActionListener(event -> selectFile(parent, filter, false, target::setText));
    return button;
  }

  public void selectFile(Component parent, FileNameExtensionFilter filter, boolean isSave,
      Consumer<String> onApprove) {
    selectFiles(parent, filter, false, isSave, files -> onApprove.accept(files[0].getPath()));
  }

  public void selectFiles(Component parent, FileNameExtensionFilter filter, boolean multiSelect,
      boolean isSave, Consumer<File[]> onApprove) {
    var chooser = new JFileChooser();
    chooser.setFileFilter(filter);
    chooser.setMultiSelectionEnabled(multiSelect);

    var result = isSave ? chooser.showSaveDialog(parent) : chooser.showOpenDialog(parent);
    if (result == JFileChooser.APPROVE_OPTION) {
      if (isSave) {
        onApprove.accept((multiSelect ? Arrays.stream(chooser.getSelectedFiles())
            : Stream.of(chooser.getSelectedFile())).map(
            file -> ensureEndsWith(file.getPath(), filter)).map(File::new).toArray(File[]::new));
      } else {
        onApprove.accept(
            multiSelect ? chooser.getSelectedFiles() : new File[]{chooser.getSelectedFile()});
      }
    }
  }

  public String ensureEndsWith(String path, FileNameExtensionFilter fileNameExtensionFilter) {
    var extensions = fileNameExtensionFilter.getExtensions();
    return ensureEndsWith(path, extensions[0]);
  }

  public String ensureEndsWith(String path, String extension) {
    var withDot = ".%s".formatted(extension);
    var expectedIndex = path.length() - withDot.length();
    var indexOfExtension = path.toLowerCase().lastIndexOf(withDot.toLowerCase());
    return indexOfExtension != expectedIndex ? path + withDot : path;
  }
}
