package edu.yaprnn.support.swing;

import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;

public final class Images {

  private static final float RESIZE_MAXSIZE = 4095f;

  public static Image resize(Image original, int newWidth, int newHeight, int filterOp) {
    var oldWidth = original.getWidth(null);
    var oldHeight = original.getHeight(null);

    var bufferedImage = new BufferedImage(oldWidth, oldHeight, BufferedImage.TYPE_INT_ARGB);
    try {
      var bufferedImageGraphics = bufferedImage.getGraphics();
      try {
        bufferedImageGraphics.drawImage(original, 0, 0, oldWidth, oldHeight, null);
      } finally {
        bufferedImageGraphics.dispose();
      }

      var zoomClip = Math.min(1f, Math.min(RESIZE_MAXSIZE / newWidth, RESIZE_MAXSIZE / newHeight));

      var sx = (newWidth * zoomClip) / oldWidth;
      var sy = (newHeight * zoomClip) / oldHeight;
      var affineTransform = new AffineTransform();
      affineTransform.scale(sx, sy);
      return new AffineTransformOp(affineTransform, filterOp).filter(bufferedImage, null);
    } finally {
      bufferedImage.flush();
    }
  }
}
