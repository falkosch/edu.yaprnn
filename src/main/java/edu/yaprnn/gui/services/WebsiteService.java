package edu.yaprnn.gui.services;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.awt.Component;
import java.awt.Desktop;
import java.net.URI;

@Singleton
public class WebsiteService {

  @Inject
  DialogsService dialogsService;

  public void visit(Component parent) {
    try {
      Desktop.getDesktop().browse(new URI("https://github.com/falkosch/edu.yaprnn"));
    } catch (Throwable throwable) {
      dialogsService.showError(parent, "Visit code repository", throwable);
    }
  }
}
