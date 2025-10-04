package edu.yaprnn.support.swing;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.util.Objects;
import javax.swing.JPanel;

/**
 * Displays an image in a {@link JPanel}.
 *
 * <p>Scrolling is supported by adding this component into a {@link javax.swing.JScrollPane}.
 */
public final class ImagePanel extends JPanel {

  private Dimension imageSize = new Dimension();

  private Image image;

  public void setImage(Image newImage) {
    if (this.image != null) {
      this.image.flush();
    }

    this.image = newImage;

    var oldImageSize = imageSize;
    imageSize = Objects.isNull(newImage) ? new Dimension(0, 0) : sizeFrom(newImage);

    firePropertyChange("preferredSize", oldImageSize, imageSize);
    revalidate();
    repaint();
  }

  private Dimension sizeFrom(Image image) {
    return new Dimension(image.getWidth(this), image.getHeight(this));
  }

  @Override
  protected void paintComponent(Graphics graphics) {
    var width = getWidth();
    var height = getHeight();
    graphics.clearRect(0, 0, width, height);

    if (image != null) {
      var x = (width - imageSize.width) / 2;
      var y = (height - imageSize.height) / 2;
      graphics.drawImage(image, x, y, this);
    }
  }

  @Override
  public Dimension getPreferredSize() {
    return imageSize;
  }
}
