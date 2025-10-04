package yaprnn.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import yaprnn.dvv.Data;
import yaprnn.mlp.NeuralNetwork;

class LoadDataSetAction implements ActionListener {

  private final GUI gui;

  LoadDataSetAction(GUI gui) {
    this.gui = gui;
    setEnabled(false);
    gui.getView().getMenuLoadDataSet().addActionListener(this);
    gui.getView().getToolLoadDataSet().addActionListener(this);
  }

  void setEnabled(boolean enabled) {
    gui.getView().getMenuLoadDataSet().setEnabled(enabled);
    gui.getView().getToolLoadDataSet().setEnabled(enabled);
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    NeuralNetwork n = gui.getSelectedNetwork();
    if (n == null) {
      return;
    }
    JFileChooser chooser = new JFileChooser();
    chooser.setMultiSelectionEnabled(false);
    chooser.setFileFilter(GUI.FILEFILTER_SETLIST);
    if (chooser.showOpenDialog(gui.getView()) == JFileChooser.APPROVE_OPTION) {
      try {
        gui.getCore().loadDataSet(chooser.getSelectedFile().getPath());

        // Wir schauen nur noch nach welche wo gelandet sind.
        for (Data d : gui.getTreeModel().getDatasets()) {
          gui.getTreeModel().remove(d, n);
          if (d.isTraining()) {
            gui.getTreeModel().add(d, n, true);
          } else if (d.isTest()) {
            gui.getTreeModel().add(d, n, false);
          }
        }

        JOptionPane.showMessageDialog(gui.getView(), "Finished.", "Load dataset",
            JOptionPane.INFORMATION_MESSAGE);
      } catch (Exception ex) {
        JOptionPane.showMessageDialog(gui.getView(),
            "An error occured while loading the dataset.\nDetails:\n" + ex.getMessage(),
            "Load dataset", JOptionPane.ERROR_MESSAGE);
      }
    }
  }

}
