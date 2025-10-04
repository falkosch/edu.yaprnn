package yaprnn.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import yaprnn.dvv.FileMismatchException;
import yaprnn.dvv.InvalidFileException;
import yaprnn.dvv.NoSuchFileException;

class ImportImagesAction implements ActionListener {

  private final GUI gui;

  ImportImagesAction(GUI gui) {
    this.gui = gui;
    gui.getView().getMenuImportImages().addActionListener(this);
    gui.getView().getToolImportImages().addActionListener(this);
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    String labelsPKG = null;
    String imagesPKG = null;

    // Fr die Eingabe-Schleife.
    boolean notSatisfied;

    // Input Dialog vorbereiten.
    JPanel panel = new JPanel(new GridLayout(4, 1));
    JPanel innerPanel1 = new JPanel(new BorderLayout());
    JPanel innerPanel2 = new JPanel(new BorderLayout());
    JTextField optionLabelsPKG = new JTextField();
    JButton toolSearchLabelsPKG = new JButton("...");
    JTextField optionImagesPKG = new JTextField();
    JButton toolSearchImagesPKG = new JButton("...");
    toolSearchLabelsPKG.addActionListener(
        new ToolSearchForFileAction(gui.getView(), optionLabelsPKG, GUI.FILEFILTER_LBLPKG));
    toolSearchImagesPKG.addActionListener(
        new ToolSearchForFileAction(gui.getView(), optionImagesPKG, GUI.FILEFILTER_IMGPKG));
    innerPanel1.add(toolSearchLabelsPKG, BorderLayout.EAST);
    innerPanel1.add(optionLabelsPKG);
    innerPanel2.add(toolSearchImagesPKG, BorderLayout.EAST);
    innerPanel2.add(optionImagesPKG);
    panel.add(new JLabel("Please select the labels package:"));
    panel.add(innerPanel1);
    panel.add(new JLabel("Please select the images package:"));
    panel.add(innerPanel2);

    // Parameter anfragen
    notSatisfied = true;
    while (notSatisfied) {
      int ret = JOptionPane.showConfirmDialog(gui.getView(), panel, "Import images",
          JOptionPane.OK_CANCEL_OPTION);
      if (ret == JOptionPane.CANCEL_OPTION) {
        return;
      }
      labelsPKG = optionLabelsPKG.getText();
      imagesPKG = optionImagesPKG.getText();
      boolean labelsExists = false, imagesExists = false;
      try {
        labelsExists = new File(labelsPKG).exists();
        imagesExists = new File(imagesPKG).exists();
      } catch (SecurityException ex) {
        JOptionPane.showMessageDialog(gui.getView(),
            "You don't have granted access to the parent directory.", "Access error",
            JOptionPane.ERROR_MESSAGE);
      }
      if (labelsExists && imagesExists) {
        notSatisfied = false;
      } else {
        if (!labelsExists) {
          optionLabelsPKG.setBackground(new Color(255, 128, 128));
        } else {
          optionLabelsPKG.setBackground(SystemColor.text);
        }
        if (!imagesExists) {
          optionImagesPKG.setBackground(new Color(255, 128, 128));
        } else {
          optionImagesPKG.setBackground(SystemColor.text);
        }
      }
    }

    // Vorher versuchen etwas Speicher frei zu machen
    GUI.tryFreeMemory();

    // Importieren
    try {
      gui.getCore().openIdxPicture(imagesPKG, labelsPKG);
      JOptionPane.showMessageDialog(gui.getView(), "Finished.", "Import images",
          JOptionPane.INFORMATION_MESSAGE);
    } catch (NoSuchFileException ex) {
      JOptionPane.showMessageDialog(gui.getView(),
          "Import failed!\n" + "This file has not been found" + "\n" + ex.getFilename(),
          "An error occured", JOptionPane.ERROR_MESSAGE);
    } catch (InvalidFileException ex) {
      JOptionPane.showMessageDialog(gui.getView(),
          "Import failed!\n" + "Unsupported image or label file format in" + "\n"
              + ex.getFilename(), "An error occured", JOptionPane.ERROR_MESSAGE);
    } catch (FileMismatchException ex) {
      JOptionPane.showMessageDialog(gui.getView(),
          "Import failed!\n" + "No matching files" + "\n" + ex.getDataFilename() + "\n"
              + ex.getLabelFilename(), "An error occured", JOptionPane.ERROR_MESSAGE);
    } catch (IOException ex) {
      JOptionPane.showMessageDialog(gui.getView(),
          "Import failed!\n" + ex + "\n" + Arrays.toString(ex.getStackTrace()), "An error occured",
          JOptionPane.ERROR_MESSAGE);
    }

  }
}
