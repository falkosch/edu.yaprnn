package edu.yaprnn.gui.services;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;

/**
 * Loads and caches SWT images from classpath resource paths. Uses JFace
 * {@link LocalResourceManager} for automatic disposal.
 */
@Singleton
public class SwtIconsService {

  private static final int SMALL_ICON_SIZE = 28;

  private final Map<String, ImageDescriptor> descriptorCache = new ConcurrentHashMap<>();

  @Inject
  LocalResourceManager resourceManager;

  /**
   * Gets an SWT Image for the given classpath resource path. The image is cached and managed by the
   * resource manager (auto-disposed when the root shell closes).
   */
  public Image getImage(String resourcePath) {
    return resourceManager.create(getDescriptor(resourcePath));
  }

  /**
   * Gets an ImageDescriptor for the given classpath resource path. Descriptors are lightweight and
   * cached.
   */
  public ImageDescriptor getDescriptor(String resourcePath) {
    return descriptorCache.computeIfAbsent(resourcePath, SwtIconsService::loadAndResize);
  }

  private static ImageDescriptor loadAndResize(String resourcePath) {
    var url = SwtIconsService.class.getResource(resourcePath);
    Objects.requireNonNull(url, () -> "Resource not found: %s".formatted(resourcePath));
    var descriptor = ImageDescriptor.createFromURL(url);

    // Resize to standard icon size
    var imageData = descriptor.getImageData(100);
    if (imageData != null
        && (imageData.width != SMALL_ICON_SIZE || imageData.height != SMALL_ICON_SIZE)) {
      var scaled = imageData.scaledTo(SMALL_ICON_SIZE, SMALL_ICON_SIZE);
      return ImageDescriptor.createFromImageDataProvider(zoom -> scaled);
    }
    return descriptor;
  }
}
