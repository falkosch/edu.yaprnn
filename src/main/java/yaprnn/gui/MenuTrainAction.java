package yaprnn.gui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import yaprnn.dvv.DataTypeMismatchException;
import yaprnn.gui.view.TrainingView;
import yaprnn.mlp.DynamicEtaAdjustment;
import yaprnn.mlp.Eta;
import yaprnn.mlp.NeuralNetwork;
import yaprnn.mlp.NoEtaAdjustment;
import yaprnn.mlp.StaticEtaAdjustment;

class MenuTrainAction implements ActionListener {

  private final static ImageIcon ICON_TRAIN = ImagesMacros.loadIcon(22, 22,
      "/yaprnn/gui/view/iconTraining.png");
  private final static ImageIcon ICON_STOP = ImagesMacros.loadIcon(22, 22,
      "/yaprnn/gui/view/iconStop.png");

  private static final Object trainingsRunningLockObj = new Object();
  private static int trainingsRunning = 0;
  // TODO : Zur zeit kann nur ein Netzwerk trainiert werden.
  private static TrainingInfo ti = null;
  private final GUI gui;

  MenuTrainAction(GUI gui) {
    this.gui = gui;
    setEnabled(false);
    gui.getView().getMenuTrain().addActionListener(this);
  }

  void setEnabled(boolean enabled) {
    gui.getView().getMenuTrain().setEnabled(enabled);
  }

  static void setTestError(List<Double> errorData) {
    ti.testError.add(ti.testError.getItemCount(), errorData.get(errorData.size() - 1));
  }

  static void setTrainingError(List<Double> errorData) {
    ti.trainingError.add(ti.trainingError.getItemCount(), errorData.get(errorData.size() - 1));
  }

  static boolean areTrainingsInProgress() {
    int running;
    synchronized (trainingsRunningLockObj) {
      running = trainingsRunning;
    }
    return running <= 0;
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    if (gui.getSelectedNetwork() == null)
    // Kein Netzwerk ausgewaehlt
    {
      return;
    }

    if (ti != null) {
      // Wir koennen zurzeit nur ein MLP trainieren
      JOptionPane.showMessageDialog(gui.getView(),
          "A training is already in progress. This version doesn't support more than one training window up at a time.",
          "Training", JOptionPane.ERROR_MESSAGE);
      return;
    }

    ti = new TrainingInfo(gui, new TrainingView(), gui.getSelectedNetwork());

    // Das JFreeChart zur Visualisierung erstellen
    XYSeriesCollection xyDataset = new XYSeriesCollection();
    xyDataset.addSeries(ti.trainingError);
    xyDataset.addSeries(ti.testError);
    JFreeChart chart = ChartFactory.createXYLineChart("Training statistics", "Index", "Error value",
        xyDataset, PlotOrientation.VERTICAL, true, false, false);
    ChartPanel cp = new ChartPanel(chart);
    cp.setMouseZoomable(true, true);
    ti.tv.getGraphPanel().add(cp, BorderLayout.CENTER);
    ti.tv.getGraphPanel().validate();

    // Listener hinzufuegen
    new TrainAction(ti);
    new ClearGraphAction(ti);
    new TrainingWindowListener(ti);
    new OptionItemChange(ti);
    new OptionTrainingMethodAction(ti);

    // Einstellungen initialisieren
    ti.tv.getToolTrain().setIcon(ICON_TRAIN);
    ti.tv.getToolTrain().setText("Train");
    ti.tv.getOptionTrainingMethod().setModel(
        new DefaultComboBoxModel<>(new Object[]{new OnlineTraining(), new BatchTraining()}));
    ti.tv.getOptionTrainingMethod().setEditable(false);
    ti.tv.setTitle("Training: " + ti.network.getName());

    ti.tv.setVisible(true);
  }

  private abstract static class TrainingMethod {

  }

  private static class OnlineTraining extends TrainingMethod {

    @Override
    public String toString() {
      return "Online";
    }
  }

  private static class BatchTraining extends TrainingMethod {

    @Override
    public String toString() {
      return "Batch";
    }
  }

  /**
   * Used to hold required parameters and view objects.
   */
  private class TrainingInfo {

    final GUI gui;
    final TrainingView tv;
    final NeuralNetwork network;
    // JFreeChart Einbindung, Messpunkte
    final XYSeries trainingError = new XYSeries("Training error");
    final XYSeries testError = new XYSeries("Test error");
    TrainingWorker tw = null;

    TrainingInfo(GUI gui, TrainingView tv, NeuralNetwork network) {
      this.gui = gui;
      this.tv = tv;
      this.network = network;
    }

  }

  private class TrainAction implements ActionListener {

    private final TrainingInfo ti;

    TrainAction(TrainingInfo ti) {
      this.ti = ti;
      ti.tv.getToolTrain().addActionListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      ti.tv.getToolTrain().setEnabled(false);
      if (ti.tw != null) {
        ti.gui.getCore().stopLearning();
      } else {
        // Trainieren in einem Background-Worker arbeiten lassen
        ti.tw = new TrainingWorker(ti, (Integer) ti.tv.getOptionBatchsize().getValue(),
            (Double) ti.tv.getOptionLearningRate().getValue(),
            (Integer) ti.tv.getOptionMaxIterations().getValue(),
            (Double) ti.tv.getOptionMaxError().getValue(),
            ti.tv.getOptionTrainingMethod().getSelectedItem() instanceof OnlineTraining,
            ti.tv.getOptionUseMomentum().isSelected(),
            (Double) ti.tv.getOptionMomentum().getValue(),
            ti.tv.getOptionModifyLearningrate().isSelected(),
            ti.tv.getOptionDynamicAdjustment().isSelected(),
            (Double) ti.tv.getOptionDynamicReductionfactor().getValue(),
            (Double) ti.tv.getOptionDynamicMultiplier().getValue(),
            (Double) ti.tv.getOptionStaticReductionfactor().getValue(),
            (Integer) ti.tv.getOptionStaticIterations().getValue());
        ti.tw.execute();
      }
    }
  }

  private class ClearGraphAction implements ActionListener {

    private final TrainingInfo ti;

    ClearGraphAction(TrainingInfo ti) {
      this.ti = ti;
      ti.tv.getToolClearGraph().addActionListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      ti.testError.clear();
      ti.trainingError.clear();
    }
  }

  /**
   * This worker invokes the training method to not block the awt dispatcher thread.
   */
  private class TrainingWorker extends SwingWorker<Object, Object> {

    final int batchSize;
    final double maxError;
    final int maxIterations;
    final boolean onlineLearning;
    final double learningRate;
    final boolean useMomentum;
    final double momentum;
    final boolean modifyLearningrate;
    final boolean dynamicAdjustment;
    final double dynamicReductionFactor;
    final double dynamicMultiplier;
    final double staticReductionFactor;
    final int staticIterations;
    private final TrainingInfo ti;

    TrainingWorker(TrainingInfo ti, int batchSize, double learningRate, int maxIterations,
        double maxError, boolean onlineLearning, boolean useMomentum, double momentum,
        boolean modifyLearningrate, boolean dynamicAdjustment, double dynamicReductionFactor,
        double dynamicMultiplier, double staticReductionFactor, int staticIterations) {
      this.ti = ti;
      this.batchSize = batchSize;
      this.learningRate = learningRate;
      this.maxError = maxError;
      this.maxIterations = maxIterations;
      this.onlineLearning = onlineLearning;
      this.useMomentum = useMomentum;
      this.momentum = momentum;
      this.modifyLearningrate = modifyLearningrate;
      this.dynamicAdjustment = dynamicAdjustment;
      this.dynamicReductionFactor = dynamicReductionFactor;
      this.dynamicMultiplier = dynamicMultiplier;
      this.staticReductionFactor = staticReductionFactor;
      this.staticIterations = staticIterations;
    }

    @Override
    protected Object doInBackground() {
      synchronized (trainingsRunningLockObj) {
        trainingsRunning++;
      }

      ti.tv.getToolTrain().setIcon(ICON_STOP);
      ti.tv.getToolTrain().setText("Stop");
      ti.tv.getToolTrain().setEnabled(true);

      // Momentum auswaehlen
      double momentum = 0.0;
      if (useMomentum) {
        momentum = this.momentum;
      }

      Eta eta;

      if (modifyLearningrate) {
        if (dynamicAdjustment) {
          eta = new DynamicEtaAdjustment(learningRate, dynamicReductionFactor, dynamicMultiplier);
        } else {
          eta = new StaticEtaAdjustment(learningRate, staticReductionFactor, staticIterations);
        }
      } else {
        eta = new NoEtaAdjustment(learningRate);
      }

      try {
        if (onlineLearning) {
          ti.gui.getCore().trainOnline(eta, maxIterations, maxError, momentum);
        } else {
          ti.gui.getCore().trainBatch(eta, maxIterations, maxError, batchSize, momentum);
        }
      } catch (DataTypeMismatchException e) {
        JOptionPane.showMessageDialog(ti.tv,
            "The data you selected does not have the same type as data the neural network has previously been trained with.",
            "Training", JOptionPane.ERROR_MESSAGE);
      }
      // muss aus der GUI kommen!
      return null;
    }

    @Override
    protected void done() {
      synchronized (trainingsRunningLockObj) {
        trainingsRunning--;
      }

      ti.tw = null;
      ti.tv.getToolTrain().setIcon(ICON_TRAIN);
      ti.tv.getToolTrain().setText("Train");
      ti.tv.getToolTrain().setEnabled(true);

      ti.gui.getTreeModel().refreshNetwork(ti.network);
    }

  }

  private class TrainingWindowListener implements WindowListener {

    private TrainingInfo ti;

    TrainingWindowListener(TrainingInfo ti) {
      this.ti = ti;
      ti.tv.addWindowListener(this);
    }

    @Override
    public void windowOpened(WindowEvent e) {
    }

    @Override
    public void windowClosing(WindowEvent e) {
      if (ti.tw == null) {
        ((JFrame) e.getSource()).dispose();
        // Siehe Kommentar bei Definition MenuTrainAction.ti.
        MenuTrainAction.ti = null;
      }
    }

    @Override
    public void windowClosed(WindowEvent e) {
      ti = null;
    }

    @Override
    public void windowIconified(WindowEvent e) {
    }

    @Override
    public void windowDeiconified(WindowEvent e) {
    }

    @Override
    public void windowActivated(WindowEvent e) {
    }

    @Override
    public void windowDeactivated(WindowEvent e) {
    }

  }

  /**
   * Handles the enabling states of certain sub options, which are only usefull when the
   * corresponding enabling option is selected.
   */
  private class OptionItemChange implements ItemListener {

    private final TrainingInfo ti;

    OptionItemChange(TrainingInfo ti) {
      this.ti = ti;
      ti.tv.getOptionUseMomentum().addItemListener(this);
      ti.tv.getOptionModifyLearningrate().addItemListener(this);
      ti.tv.getOptionDynamicAdjustment().addItemListener(this);
      ti.tv.getOptionStaticAdjustment().addItemListener(this);

      // Optionen init-aktivieren/deaktivieren
      ti.tv.getPreferencesTabs().setEnabledAt(2, ti.tv.getOptionUseMomentum().isSelected());
      ti.tv.getPreferencesTabs().setEnabledAt(1, ti.tv.getOptionModifyLearningrate().isSelected());
      ti.tv.getOptionDynamicMultiplier()
          .setEnabled(ti.tv.getOptionDynamicAdjustment().isSelected());
      ti.tv.getOptionDynamicReductionfactor()
          .setEnabled(ti.tv.getOptionDynamicAdjustment().isSelected());
      ti.tv.getOptionStaticIterations().setEnabled(ti.tv.getOptionStaticAdjustment().isSelected());
      ti.tv.getOptionStaticReductionfactor()
          .setEnabled(ti.tv.getOptionStaticAdjustment().isSelected());
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
      Object src = e.getSource();
      boolean enable = e.getStateChange() == ItemEvent.SELECTED;

      // Aktiviere Momentum-Optionen, wenn optionUseMomentum selektiert
      // ist
      if (src == ti.tv.getOptionUseMomentum()) {
        ti.tv.getPreferencesTabs().setEnabledAt(2, enable);
      }

      // Aktiviere Learningrate-Optionen, wenn optionModifyLearningrate
      // selektiert ist
      if (src == ti.tv.getOptionModifyLearningrate()) {
        ti.tv.getPreferencesTabs().setEnabledAt(1, enable);
      }

      // Aktiviere ...Adjustment-Optionen, wenn
      // option...Adjustment selektiert ist
      if (src == ti.tv.getOptionDynamicAdjustment()) {
        ti.tv.getOptionDynamicMultiplier().setEnabled(enable);
        ti.tv.getOptionDynamicReductionfactor().setEnabled(enable);
      }
      if (src == ti.tv.getOptionStaticAdjustment()) {
        ti.tv.getOptionStaticIterations().setEnabled(enable);
        ti.tv.getOptionStaticReductionfactor().setEnabled(enable);
      }

    }

  }

  private class OptionTrainingMethodAction implements ActionListener {

    private final TrainingInfo ti;

    public OptionTrainingMethodAction(TrainingInfo ti) {
      this.ti = ti;
      ti.tv.getOptionTrainingMethod().addActionListener(this);

      // Optionen init-aktivieren/deaktivieren
      ti.tv.getOptionBatchsize()
          .setEnabled(ti.tv.getOptionTrainingMethod().getSelectedItem() instanceof BatchTraining);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      ti.tv.getOptionBatchsize()
          .setEnabled(ti.tv.getOptionTrainingMethod().getSelectedItem() instanceof BatchTraining);
    }
  }

}
