package edu.yaprnn.support.swt;

import edu.yaprnn.gui.views.MainShell;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.jboss.weld.environment.se.events.ContainerInitialized;

/**
 * SWT application bootstrap. Observes CDI container initialization to build and run the SWT UI.
 * <p>
 * This replaces the Swing MainFrame.start() observer. The SWT event loop blocks on the main thread
 * until the shell is closed, then the Weld container shuts down normally.
 */
@Singleton
public class SwtApplication {

  @Inject
  MainShell mainShell;

  void start(@Observes ContainerInitialized event) {
    mainShell.initialize();
    mainShell.openAndRunEventLoop();
  }
}
