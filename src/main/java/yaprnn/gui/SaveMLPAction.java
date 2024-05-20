package yaprnn.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

class SaveMLPAction implements ActionListener {

  private final GUI gui;

  SaveMLPAction(GUI gui) {
    this.gui = gui;
    setEnabled(false);
    gui.getView().getMenuSaveMLP().addActionListener(this);
    gui.getView().getToolSaveMLP().addActionListener(this);
  }

  void setEnabled(boolean enabled) {
    gui.getView().getMenuSaveMLP().setEnabled(enabled);
    gui.getView().getToolSaveMLP().setEnabled(enabled);
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    JFileChooser chooser = new JFileChooser();
    chooser.setMultiSelectionEnabled(false);
    chooser.setFileFilter(GUI.FILEFILTER_MLP);
    if (chooser.showSaveDialog(gui.getView()) == JFileChooser.APPROVE_OPTION) {
      try {
        String path = chooser.getSelectedFile().getPath();

        if (path.toLowerCase().lastIndexOf(".mlp") != (path.length() - 4)) {
          path += ".mlp";
        }

        gui.getCore().saveMLP(path);
      } catch (Exception ex) {
        JOptionPane.showMessageDialog(gui.getView(), "An error occured",
            "An error occured while saving the mlp.\n" + ex.getMessage(),
            JOptionPane.ERROR_MESSAGE);
      }
    }
  }

}
