package yaprnn.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

class MenuRefresh implements ActionListener {

  private final GUI gui;

  MenuRefresh(GUI gui) {
    this.gui = gui;
    gui.getView().getMenuRefresh().addActionListener(this);
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    gui.getTreeModel().refresh();
  }

}
