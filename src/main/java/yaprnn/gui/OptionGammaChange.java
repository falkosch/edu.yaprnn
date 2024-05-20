package yaprnn.gui;

import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

class OptionGammaChange implements ChangeListener {

  private final GUI gui;

  public OptionGammaChange(GUI gui) {
    this.gui = gui;
    gui.getView().getOptionGamma().addChangeListener(this);
  }

  @Override
  public void stateChanged(ChangeEvent e) {
    JSlider source = gui.getView().getOptionGamma();
    double range = source.getMaximum() - source.getMinimum();
    gui.setGamma((source.getValue() - source.getMinimum() + 1) / (range + 2));
  }
}
