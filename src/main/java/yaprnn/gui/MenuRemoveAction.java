package yaprnn.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

class MenuRemoveAction implements ActionListener {

  private final GUI gui;

  MenuRemoveAction(GUI gui) {
    this.gui = gui;
    setEnabled(false);
    gui.getView().getMenuRemove().addActionListener(this);

    // TODO: Die Funktionen zum Entfernen von Layern wird zurzeit nicht
    // untersttzt, aber auch nicht dringend gebraucht.
    gui.getView().getMenuRemove().setVisible(false);
  }

  void setEnabled(boolean enabled) {
    gui.getView().getMenuRemove().setEnabled(enabled);
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    // TODO Auto-generated method stub

  }

}
