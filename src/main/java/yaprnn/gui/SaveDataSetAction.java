package yaprnn.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

class SaveDataSetAction implements ActionListener {

  private final GUI gui;

  SaveDataSetAction(GUI gui) {
    this.gui = gui;
    setEnabled(false);
    gui.getView().getMenuSaveDataSet().addActionListener(this);
    gui.getView().getToolSaveDataSet().addActionListener(this);
  }

  void setEnabled(boolean enabled) {
    gui.getView().getMenuSaveDataSet().setEnabled(enabled);
    gui.getView().getToolSaveDataSet().setEnabled(enabled);
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    JFileChooser chooser = new JFileChooser();
    chooser.setMultiSelectionEnabled(false);
    chooser.setFileFilter(GUI.FILEFILTER_SETLIST);
    if (chooser.showSaveDialog(gui.getView()) == JFileChooser.APPROVE_OPTION) {
      try {
        String path = chooser.getSelectedFile().getPath();
        if (path.toLowerCase().lastIndexOf(".setlist") != (path.length() - 8)) {
          path += ".setlist";
        }
        gui.getCore().saveDataSet(path);
        JOptionPane.showMessageDialog(gui.getView(), "Finished.", "Save dataset",
            JOptionPane.INFORMATION_MESSAGE);
      } catch (Exception ex) {
        JOptionPane.showMessageDialog(gui.getView(),
            "An error occured while saving the dataset.\nDetails:\n" + ex.getMessage(),
            "Save dataset", JOptionPane.ERROR_MESSAGE);
      }
    }
  }

}
