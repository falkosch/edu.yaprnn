package yaprnn.gui;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.io.Serial;
import javax.swing.JPanel;

/**
 * ImagePanel displays an image on a panel. It supports scrolling by being added into a scrollpane.
 */
public class ImagePanel extends JPanel {

  @Serial
  private static final long serialVersionUID = 6823314791815950876L;
  private final Dimension size = new Dimension();
  private Image image;

  public ImagePanel() {
    super();
  }

  public void setImage(Image image) {
    if (this.image != null) {
      this.image.flush();
    }
    this.image = image;

    Dimension old = new Dimension(size);

    if (image != null) {
      size.setSize(image.getWidth(this), image.getHeight(this));
    } else {
      size.setSize(0, 0);
    }

    firePropertyChange("preferredSize", old, size);

    revalidate();
    repaint();
  }

  @Override
  protected void paintComponent(Graphics g) {
    g.clearRect(0, 0, getWidth(), getHeight());
    if (image != null) {
      int x = (getWidth() - size.width) / 2;
      int y = (getHeight() - size.height) / 2;
      g.drawImage(image, x, y, this);
    }
  }

  @Override
  public Dimension getPreferredSize() {
    return size;
  }

}
