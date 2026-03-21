package edu.yaprnn.support.swt;

import jakarta.enterprise.inject.Produces;
import jakarta.inject.Singleton;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * CDI producer for the SWT {@link Display} and root {@link Shell}. The Display is created eagerly
 * and must be on the main thread.
 */
@Singleton
public class SwtDisplayProducer {

  private final Display display;
  private final Shell rootShell;

  public SwtDisplayProducer() {
    this.display = new Display();
    this.rootShell = new Shell(display, SWT.SHELL_TRIM);
  }

  @Produces
  @Singleton
  public Display getDisplay() {
    return display;
  }

  @Produces
  @Singleton
  public Shell getRootShell() {
    return rootShell;
  }
}
