package yaprnn.gui;

import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;
import javax.swing.JOptionPane;

class MenuManualAction implements ActionListener {

  private final GUI gui;

  MenuManualAction(GUI gui) {
    this.gui = gui;
    gui.getView().getMenuManual().addActionListener(this);
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    try {
      // TODO : Manual URL anpassen
      Desktop.getDesktop().browse(new URI("https://code.google.com/p/yaprnn/wiki/Main"));
    } catch (Exception ex) {
      JOptionPane.showMessageDialog(gui.getView(), "Could not open a browser for the manual.",
          "Manual", JOptionPane.ERROR_MESSAGE);
    }
  }

}
