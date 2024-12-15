package edu.yaprnn.gui.services;

import edu.yaprnn.support.swing.DialogsService;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.awt.Color;
import java.awt.Component;
import java.awt.SystemColor;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import javax.swing.AbstractButton;
import javax.swing.AbstractListModel;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.filechooser.FileNameExtensionFilter;

@Singleton
public class ControlsService {

  @Inject
  DialogsService dialogsService;
  @Inject
  FilesService filesService;
  @Inject
  IconsService iconsService;

  public <T> String toTitlePart(T part, Function<T, String> toText) {
    return Optional.ofNullable(part).map(toText).orElse("N/A");
  }

  public Color validationColor(boolean valid) {
    return valid ? SystemColor.text : new Color(255, 160, 160);
  }

  public JMenuItem loadMenuItem(Component parent, String title,
      FileNameExtensionFilter fileExtension, Consumer<String> pathConsumer) {
    return selectFileMenuItem(parent, title, iconsService.loadIcon(), fileExtension, false,
        pathConsumer);
  }

  public JMenuItem selectFileMenuItem(Component parent, String title, Icon icon,
      FileNameExtensionFilter fileExtension, boolean isSaveAction, Consumer<String> pathConsumer) {
    return actionMenuItem(title, icon,
        () -> filesService.selectFile(parent, fileExtension, isSaveAction, path -> {
          try {
            pathConsumer.accept(path);
            if (isSaveAction) {
              dialogsService.showFinished(parent, title);
            }
          } catch (Throwable throwable) {
            dialogsService.showError(parent, title, throwable);
          }
        }));
  }

  public JMenuItem actionMenuItem(String title, Icon icon, Runnable action) {
    return actionComponent(new JMenuItem(title, icon), action);
  }

  public <T extends AbstractButton> T actionComponent(T component, Runnable action) {
    component.addActionListener(_ -> action.run());
    component.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
    component.setOpaque(false);
    return component;
  }

  public JMenuItem saveMenuItem(Component parent, String title,
      FileNameExtensionFilter fileExtension, Consumer<String> pathConsumer) {
    return selectFileMenuItem(parent, title, iconsService.saveIcon(), fileExtension, true,
        pathConsumer);
  }

  public JButton actionButton(String title, Icon icon, Runnable action) {
    return actionComponent(new JButton(title, icon), action);
  }

  public KeyListener onlyNumbersKeyListener(boolean onlyPositive, boolean onlyIntegers) {
    return new KeyAdapter() {

      @Override
      public void keyTyped(KeyEvent event) {
        var key = event.getKeyChar();
        var floating = key == '.' || key == 'E' || key == 'e';
        var sign = key == '-';
        if ((floating && onlyIntegers) || (sign && onlyPositive) || !(Character.isDigit(key) || sign
            || floating)) {
          event.consume(); // swallow it
        }
      }
    };
  }

  public <T> void silenceListModelListenersDuringRunnable(AbstractListModel<T> model,
      Runnable runnable) {
    var dataListeners = List.of(model.getListDataListeners());
    dataListeners.forEach(model::removeListDataListener);
    runnable.run();
    dataListeners.forEach(model::addListDataListener);
  }
}
