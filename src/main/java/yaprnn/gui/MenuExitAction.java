package yaprnn.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JOptionPane;

class MenuExitAction implements ActionListener {

  private final GUI gui;

  MenuExitAction(GUI gui) {
    this.gui = gui;
    gui.getView().getMenuExit().addActionListener(this);
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    if (JOptionPane.showConfirmDialog(gui.getView(), "Do you really want to exit?", "Exit",
        JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
      System.exit(0);
    }
  }

}
