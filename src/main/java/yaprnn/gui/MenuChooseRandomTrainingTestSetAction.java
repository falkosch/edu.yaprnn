package yaprnn.gui;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import yaprnn.dvv.Data;
import yaprnn.mlp.NeuralNetwork;

class MenuChooseRandomTrainingTestSetAction implements ActionListener {

  private final GUI gui;

  MenuChooseRandomTrainingTestSetAction(GUI gui) {
    this.gui = gui;
    setEnabled(false);
    gui.getView().getMenuChooseRandomTrainingTestData().addActionListener(this);
  }

  void setEnabled(boolean enabled) {
    gui.getView().getMenuChooseRandomTrainingTestData().setEnabled(enabled);
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    NeuralNetwork n = gui.getSelectedNetwork();
    if (n == null) {
      return;
    }
    int trainPerc = 75, testPerc = 20;

    boolean notSatisfied;

    // Input Dialog vorbereiten.
    JPanel panel = new JPanel(new GridLayout(4, 1));
    JSpinner optionTrainPerc = new JSpinner(new SpinnerNumberModel(70, 1, 99, 1));
    JSpinner optionTestPerc = new JSpinner(new SpinnerNumberModel(20, 1, 99, 1));
    panel.add(new JLabel("Trainingset size in percent"));
    panel.add(optionTrainPerc);
    panel.add(new JLabel("Testset size in percent"));
    panel.add(optionTestPerc);

    // Parameter anfragen
    notSatisfied = true;
    while (notSatisfied) {
      int ret = JOptionPane.showConfirmDialog(gui.getView(), panel, "Choosing random sets",
          JOptionPane.OK_CANCEL_OPTION);
      if (ret == JOptionPane.CANCEL_OPTION) {
        return;
      }
      trainPerc = (Integer) optionTrainPerc.getValue();
      testPerc = (Integer) optionTestPerc.getValue();
      if (trainPerc + testPerc <= 100) {
        notSatisfied = false;
      } else {
        JOptionPane.showMessageDialog(gui.getView(),
            "The sum of both values cannot exceed 100 percent.", "Error",
            JOptionPane.ERROR_MESSAGE);
      }
    }

    gui.getCore().chooseRandomTrainingData(trainPerc / 100.0d, testPerc / 100.0d);

    // Wir schauen nur noch nach welche wo gelandet sind.
    for (Data d : gui.getTreeModel().getDatasets()) {
      gui.getTreeModel().remove(d, n);
      if (d.isTraining()) {
        gui.getTreeModel().add(d, n, true);
      } else if (d.isTest()) {
        gui.getTreeModel().add(d, n, false);
      }
    }

    JOptionPane.showMessageDialog(gui.getView(), "Finished.", "Choosing random sets",
        JOptionPane.INFORMATION_MESSAGE);

  }

}
