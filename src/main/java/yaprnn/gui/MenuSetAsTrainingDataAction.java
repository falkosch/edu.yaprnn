package yaprnn.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import yaprnn.dvv.Data;
import yaprnn.mlp.NeuralNetwork;

class MenuSetAsTrainingDataAction implements ActionListener {

  private final GUI gui;

  MenuSetAsTrainingDataAction(GUI gui) {
    this.gui = gui;
    setEnabled(false);
    gui.getView().getMenuSetAsTrainingData().addActionListener(this);
  }

  void setEnabled(boolean enabled) {
    gui.getView().getMenuSetAsTrainingData().setEnabled(enabled);
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    // TODO : Wir sind zurzeit begrenzt auf eine MLP!
    NeuralNetwork n = gui.getTreeModel().getNetworks().get(0);
    Data d = gui.getSelectedData();
    if (!d.isTraining()) {
      gui.getTreeModel().remove(d, n);
      gui.getTreeModel().add(d, n, true);
    }
  }

}
