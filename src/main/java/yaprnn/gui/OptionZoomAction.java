package yaprnn.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JOptionPane;

class OptionZoomAction implements ActionListener {

  private final GUI gui;

  OptionZoomAction(GUI gui) {
    this.gui = gui;
    gui.getView().getOptionZoom1().addActionListener(this);
    gui.getView().getOptionZoom1().addKeyListener(new OnlyNumbersKeyAdapter(true, false));
    gui.getView().getOptionZoom2().addActionListener(this);
    gui.getView().getOptionZoom2().addKeyListener(new OnlyNumbersKeyAdapter(true, false));
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    var zoom1 = gui.getView().getOptionZoom1();
    var zoom2 = gui.getView().getOptionZoom2();
    try {
      Object item = null;
      if (e.getSource() == zoom1) {
        item = zoom1.getSelectedItem();
        zoom2.setSelectedItem(item);
      } else if (e.getSource() == zoom2) {
        item = zoom2.getSelectedItem();
        zoom1.setSelectedItem(item);
      }
      if (item instanceof String) {
        gui.setZoom(Double.parseDouble((String) item));
      }
    } catch (Exception ex) {
      JOptionPane.showMessageDialog(gui.getView(),
          "The value you have entered cannot be parsed to floating point value. Please enter a correct zoom value.",
          "Parsing error", JOptionPane.ERROR_MESSAGE);
    }
  }

}
