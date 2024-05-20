package yaprnn.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import yaprnn.gui.NetworkTreeModel.ModelNode;

class MenuEditAction implements ActionListener {

  private final GUI gui;

  MenuEditAction(GUI gui) {
    this.gui = gui;
    setEnabled(false);
    gui.getView().getMenuEdit().addActionListener(this);
  }

  void setEnabled(boolean enabled) {
    gui.getView().getMenuEdit().setEnabled(enabled);
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    ModelNode n = gui.getSelected();
    if (n == null) {
      return;
    }
    gui.getView().getTreeNeuralNetwork().startEditingAtPath(gui.getSelectedPath());
  }

}
