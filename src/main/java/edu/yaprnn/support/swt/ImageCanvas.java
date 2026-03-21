package edu.yaprnn.support.swt;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;

/**
 * SWT Canvas that displays an image centered on a white background. Replacement for Swing's
 * ImagePanel.
 */
public class ImageCanvas extends Canvas {

  private Image image;

  public ImageCanvas(Composite parent, int style) {
    super(parent, style | SWT.DOUBLE_BUFFERED);
    setBackground(getDisplay().getSystemColor(SWT.COLOR_WHITE));

    addPaintListener(event -> {
      if (image == null || image.isDisposed()) {
        return;
      }
      var bounds = image.getBounds();
      var clientArea = getClientArea();
      var x = Math.max(0, (clientArea.width - bounds.width) / 2);
      var y = Math.max(0, (clientArea.height - bounds.height) / 2);
      event.gc.drawImage(image, x, y);
    });
  }

  /**
   * Sets the image to display. The image is NOT owned by this canvas — caller manages lifecycle.
   */
  public void setImage(Image image) {
    this.image = image;
    updateSize();
    redraw();
  }

  @Override
  public Point computeSize(int wHint, int hHint, boolean changed) {
    if (image != null && !image.isDisposed()) {
      var bounds = image.getBounds();
      return new Point(bounds.width, bounds.height);
    }
    return new Point(wHint == SWT.DEFAULT ? 100 : wHint, hHint == SWT.DEFAULT ? 100 : hHint);
  }

  private void updateSize() {
    if (image != null && !image.isDisposed()) {
      var bounds = image.getBounds();
      setSize(bounds.width, bounds.height);
    }
    // If parent is a ScrolledComposite, update min size
    var parent = getParent();
    if (parent instanceof ScrolledComposite scrolled) {
      scrolled.setMinSize(computeSize(SWT.DEFAULT, SWT.DEFAULT, true));
    }
  }
}
