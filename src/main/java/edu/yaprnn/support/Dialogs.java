package edu.yaprnn.support;

/**
 * Toolkit-agnostic interface for user dialogs (error, confirmation, information).
 */
public interface Dialogs {

  void showFinished(Object parent, String title);

  void showError(Object parent, String title, Throwable throwable);

  int confirm(Object parent, Object content, String title);
}
