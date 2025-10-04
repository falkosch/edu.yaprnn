package edu.yaprnn.gui.services;

import jakarta.inject.Singleton;
import java.awt.Component;
import java.util.logging.Level;
import javax.swing.JOptionPane;
import lombok.extern.java.Log;

@Log
@Singleton
public class DialogsService {

  public void showFinished(Component parent, String title) {
    JOptionPane.showMessageDialog(parent, "Finished.", title, JOptionPane.INFORMATION_MESSAGE);
  }

  public void showError(Component parent, String title, Throwable throwable) {
    var message = "An error occurred:\n%s".formatted(throwable.getMessage());
    log.log(Level.SEVERE, message, throwable);
    JOptionPane.showMessageDialog(parent, message, title, JOptionPane.ERROR_MESSAGE);
  }

  public int confirm(Component parent, Object content, String title) {
    return JOptionPane.showConfirmDialog(parent, content, title, JOptionPane.OK_CANCEL_OPTION);
  }
}
