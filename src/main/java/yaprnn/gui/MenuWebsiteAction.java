package yaprnn.gui;

import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;
import javax.swing.JOptionPane;

class MenuWebsiteAction implements ActionListener {

  private final GUI gui;

  MenuWebsiteAction(GUI gui) {
    this.gui = gui;
    gui.getView().getMenuWebsite().addActionListener(this);
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    try {
      Desktop.getDesktop().browse(new URI("https://code.google.com/p/yaprnn/"));
    } catch (Exception ex) {
      JOptionPane.showMessageDialog(gui.getView(), "Could not open a browser for the website.",
          "Website", JOptionPane.ERROR_MESSAGE);
    }
  }

}
