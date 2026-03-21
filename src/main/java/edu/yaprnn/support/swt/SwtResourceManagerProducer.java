package edu.yaprnn.support.swt;

import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.swt.widgets.Shell;

/**
 * CDI producer for JFace {@link LocalResourceManager}. Auto-disposes all created Image, Color, Font
 * when the root shell is destroyed.
 */
@Singleton
public class SwtResourceManagerProducer {

  @Inject
  Shell rootShell;

  @Produces
  @Singleton
  public LocalResourceManager getResourceManager() {
    return new LocalResourceManager(JFaceResources.getResources(), rootShell);
  }
}
