package edu.yaprnn.support.swt;

import java.awt.image.BufferedImage;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;

/**
 * Utility for SWT image operations. Provides conversion from AWT BufferedImage to SWT ImageData.
 */
public final class SwtImages {

  static final float RESIZE_MAXSIZE = 4095f;

  private SwtImages() {
  }

  /**
   * Converts an AWT BufferedImage to SWT ImageData via direct pixel transfer.
   */
  public static ImageData toSwtImageData(BufferedImage bufferedImage) {
    var width = bufferedImage.getWidth();
    var height = bufferedImage.getHeight();
    var palette = new PaletteData(0xFF0000, 0x00FF00, 0x0000FF);
    var imageData = new ImageData(width, height, 24, palette);

    for (var y = 0; y < height; y++) {
      for (var x = 0; x < width; x++) {
        var rgb = bufferedImage.getRGB(x, y);
        imageData.setPixel(x, y, rgb & 0x00FFFFFF);
      }
    }
    return imageData;
  }

  /**
   * Scales ImageData by the given zoom factor, clamped to RESIZE_MAXSIZE.
   */
  public static ImageData resize(ImageData source, float zoom) {
    var newWidth = Math.clamp((int) (source.width * zoom), 1, (int) RESIZE_MAXSIZE);
    var newHeight = Math.clamp((int) (source.height * zoom), 1, (int) RESIZE_MAXSIZE);
    return source.scaledTo(newWidth, newHeight);
  }
}
