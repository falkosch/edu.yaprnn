package yaprnn.gui;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.Serial;
import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import yaprnn.mlp.NeuralNetwork;

class NewMLPAction implements ActionListener {

  private final static int DEFAULT_NUMLAYERS = 3;
  private final static int DEFAULT_NUMNEURONS = 20;
  private final static double DEFAULT_BIAS = 0;
  private final static int DEFAULT_ACTIVATIONFUNCTION = 0;
  private final static boolean DEFAULT_AUTOENCODING = false;
  private final static int DEFAULT_MAXITERATIONS = 1000;
  private final static double DEFAULT_UPPERBOUND = 0.01;
  private final static double DEFAULT_ETA = 0.01;

  private static int counter = 1;

  private final GUI gui;

  NewMLPAction(GUI gui) {
    this.gui = gui;
    gui.getView().getMenuNewMLP().addActionListener(this);
    gui.getView().getToolNewMLP().addActionListener(this);
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    // Input Dialog vorbereiten.
    NewMLPForm form = new NewMLPForm();
    form.optionName.setText("Network " + counter++);

    // Parameter anfragen
    int ret = JOptionPane.showConfirmDialog(gui.getView(), form, "New MLP",
        JOptionPane.OK_CANCEL_OPTION);
    if (ret != JOptionPane.OK_OPTION) {
      return;
    }

    // Parameter lesen
    String name = form.optionName.getText();
    int numLayers = (Integer) form.optionNumLayers.getValue();
    int numNeurons = (Integer) form.optionNumNeurons.getValue();
    double bias = (Double) form.optionBias.getValue();
    boolean autoEncoding = form.optionAutoEncoding.isSelected();
    int maxIterations = (Integer) form.optionMaxIterations.getValue();
    double upperBound = (Double) form.optionUpperBound.getValue();
    double eta = (Double) form.optionETA.getValue();

    NeuralNetwork mlp;
    if (autoEncoding) {
      mlp = gui.getCore()
          .newMLP(name, numLayers + 2, numNeurons, DEFAULT_ACTIVATIONFUNCTION, bias, maxIterations,
              upperBound, eta);
    } else {
      mlp = gui.getCore().newMLP(name, numLayers + 2, numNeurons, DEFAULT_ACTIVATIONFUNCTION, bias);
    }

    // TODO : Da wir noch nicht mehrere MLPs untersttzen, lschen wir das
    // gerade stehende MLP aus dem Baum!
    if (gui.getTreeModel().getNetworks().size() > 0) {
      gui.getTreeModel().remove(gui.getTreeModel().getNetworks().get(0));
    }

    gui.getTreeModel().add(mlp);
  }

  /**
   * Input form for "New MLP"
   */
  private static class NewMLPForm extends JPanel {

    @Serial
    private static final long serialVersionUID = -8649479219918738235L;
    private final JTextField optionName = new JTextField();
    private final JSpinner optionNumLayers = new JSpinner(
        new SpinnerNumberModel(DEFAULT_NUMLAYERS, 1, Integer.MAX_VALUE, 1));
    private final JSpinner optionNumNeurons = new JSpinner(
        new SpinnerNumberModel(DEFAULT_NUMNEURONS, 1, Integer.MAX_VALUE, 1));
    private final JSpinner optionBias = new JSpinner(
        new SpinnerNumberModel(DEFAULT_BIAS, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY,
            0.1));
    private final JCheckBox optionAutoEncoding = new JCheckBox("Use auto encoding initialization",
        DEFAULT_AUTOENCODING);
    private final JSpinner optionMaxIterations = new JSpinner(
        new SpinnerNumberModel(DEFAULT_MAXITERATIONS, 1, Integer.MAX_VALUE, 1));
    private final JSpinner optionUpperBound = new JSpinner(
        new SpinnerNumberModel(DEFAULT_UPPERBOUND, 0, Double.POSITIVE_INFINITY, 0.01));
    private final JSpinner optionETA = new JSpinner(
        new SpinnerNumberModel(DEFAULT_ETA, 0, Double.POSITIVE_INFINITY, 0.01));

    NewMLPForm() {
      super();
      setBorder(BorderFactory.createTitledBorder("Preferences"));

      // Allgemeine Optionen einfuegen
      JPanel inner = new JPanel(new GridLayout(9, 1));
      inner.add(new JLabel("Name:"));
      inner.add(optionName);
      inner.add(new JLabel("Number of layers:"));
      inner.add(optionNumLayers);
      inner.add(new JLabel("Number of neurons per layer:"));
      inner.add(optionNumNeurons);
      inner.add(new JLabel("Bias:"));
      inner.add(optionBias);
      inner.add(optionAutoEncoding);
      inner.validate();
      add(inner);

      // Auto-Encoding Optionen einfuegen
      inner = new JPanel(new GridLayout(6, 1));
      inner.setBorder(BorderFactory.createTitledBorder("Auto encoding options"));
      inner.add(new JLabel("Max iterations:"));
      inner.add(optionMaxIterations);
      inner.add(new JLabel("Upper bound:"));
      inner.add(optionUpperBound);
      inner.add(new JLabel("ETA learning rate:"));
      inner.add(optionETA);
      inner.validate();
      add(inner);

      validate();

      // Listener anfuegen
      new AutoEncodingChange(this);

      // Initial Enabled-Status setzen, wegen optionAutoEncoding
      optionMaxIterations.setEnabled(DEFAULT_AUTOENCODING);
      optionUpperBound.setEnabled(DEFAULT_AUTOENCODING);
      optionETA.setEnabled(DEFAULT_AUTOENCODING);

    }

    private record AutoEncodingChange(NewMLPForm form) implements ItemListener {

      private AutoEncodingChange(NewMLPForm form) {
        this.form = form;
        form.optionAutoEncoding.addItemListener(this);
      }

      @Override
      public void itemStateChanged(ItemEvent e) {
        boolean enable = e.getStateChange() == ItemEvent.SELECTED;
        form.optionMaxIterations.setEnabled(enable);
        form.optionUpperBound.setEnabled(enable);
        form.optionETA.setEnabled(enable);
      }

    }

  }

}
