package yaprnn.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import yaprnn.dvv.Data;
import yaprnn.mlp.NeuralNetwork;

class MenuSetAsNotUsedAction implements ActionListener {

  private final GUI gui;

  MenuSetAsNotUsedAction(GUI gui) {
    this.gui = gui;
    setEnabled(false);
    gui.getView().getMenuSetAsNotUsed().addActionListener(this);
  }

  void setEnabled(boolean enabled) {
    gui.getView().getMenuSetAsNotUsed().setEnabled(enabled);
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    // TODO : Wir sind zurzeit begrenzt auf eine MLP!
    NeuralNetwork n = gui.getTreeModel().getNetworks().get(0);
    Data d = gui.getSelectedData();
    gui.getTreeModel().remove(d, n);
  }

}
