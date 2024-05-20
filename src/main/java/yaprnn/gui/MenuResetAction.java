package yaprnn.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import yaprnn.mlp.NeuralNetwork;

class MenuResetAction implements ActionListener {

  private final GUI gui;

  MenuResetAction(GUI gui) {
    this.gui = gui;
    setEnabled(false);
    gui.getView().getMenuReset().addActionListener(this);
  }

  void setEnabled(boolean enabled) {
    gui.getView().getMenuReset().setEnabled(enabled);
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    NeuralNetwork n = gui.getSelectedNetwork();
    if (n == null) {
      return;
    }
    n.reset();
  }

}
