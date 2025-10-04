package yaprnn.gui;

import javax.swing.JSpinner;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

class OptionOverlapChange implements ChangeListener {

  private final GUI gui;

  public OptionOverlapChange(GUI gui) {
    this.gui = gui;
    gui.getView().getOptionOverlap().addChangeListener(this);
  }

  @Override
  public void stateChanged(ChangeEvent e) {
    JSpinner spinner = gui.getView().getOptionOverlap();
    gui.setOverlap((Double) spinner.getValue());
  }

}
