package yaprnn.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import yaprnn.dvv.Data;
import yaprnn.mlp.NeuralNetwork;

class MenuSetAsTestDataAction implements ActionListener {

  private final GUI gui;

  MenuSetAsTestDataAction(GUI gui) {
    this.gui = gui;
    setEnabled(false);
    gui.getView().getMenuSetAsTestData().addActionListener(this);
  }

  void setEnabled(boolean enabled) {
    gui.getView().getMenuSetAsTestData().setEnabled(enabled);
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    // TODO : Wir sind zurzeit begrenzt auf eine MLP!
    NeuralNetwork n = gui.getTreeModel().getNetworks().get(0);
    Data d = gui.getSelectedData();
    if (!d.isTest()) {
      gui.getTreeModel().remove(d, n);
      gui.getTreeModel().add(d, n, false);
    }
  }

}
