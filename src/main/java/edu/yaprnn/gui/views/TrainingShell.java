package edu.yaprnn.gui.views;

import edu.yaprnn.events.OnMultiLayerNetworkWeightsPreviewModifiedRouter;
import edu.yaprnn.gui.services.SwtIconsService;
import edu.yaprnn.model.Repository;
import edu.yaprnn.networks.MultiLayerNetwork;
import edu.yaprnn.training.LearningRateModifier;
import edu.yaprnn.training.TrainingData;
import edu.yaprnn.training.TrainingParameters;
import edu.yaprnn.training.TrainingService;
import jakarta.inject.Inject;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ForkJoinPool;
import java.util.logging.Level;
import lombok.extern.java.Log;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

/**
 * SWT shell for training a neural network. Replaces the Swing TrainingFrame.
 */
@Log
public class TrainingShell {

  static final String TITLE = "Training";

  @Inject
  Display display;
  @Inject
  Repository repository;
  @Inject
  TrainingService trainingService;
  @Inject
  OnMultiLayerNetworkWeightsPreviewModifiedRouter onMultiLayerNetworkWeightsPreviewModifiedRouter;
  @Inject
  SwtIconsService swtIconsService;

  private Shell shell;
  private ToolItem startButton;
  private ToolItem stopButton;
  private ComboViewer trainingDataViewer;
  private ComboViewer networkViewer;

  // Hyperparameter controls
  private Spinner maxIterationsSpinner;
  private Spinner maxTrainingErrorSpinner;
  private Spinner batchSizeSpinner;
  private Spinner maxParallelismSpinner;
  private Spinner learningRateSpinner;
  private ComboViewer learningRateModifierViewer;
  private Spinner learningRateChangeIntervalSpinner;
  private Spinner learningRateAscendSpinner;
  private Spinner learningRateDescendSpinner;
  private Spinner momentumSpinner;
  private Spinner decayL1Spinner;
  private Spinner decayL2Spinner;

  // Chart
  private XYSeries trainingError;
  private XYSeries trainingHitRate;
  private XYSeries devTestError;
  private XYSeries devTestHitRate;
  private org.jfree.chart.swt.ChartComposite chartComposite;

  private Thread trainingThread;

  public void open() {
    if (shell != null && !shell.isDisposed()) {
      shell.setActive();
      return;
    }
    createShell();
    shell.open();
  }

  private void createShell() {
    shell = new Shell(display, SWT.SHELL_TRIM);
    shell.setText(TITLE);
    shell.setLayout(new GridLayout(1, false));
    shell.setSize(900, 600);

    createToolBar();
    createContent();

    shell.addListener(SWT.Close, e -> stopTraining());

    syncView();
  }

  private void createToolBar() {
    var toolBar = new ToolBar(shell, SWT.HORIZONTAL | SWT.FLAT);
    toolBar.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

    startButton = new ToolItem(toolBar, SWT.PUSH);
    startButton.setText("Start");
    startButton.setImage(icon("/edu/yaprnn/gui/views/action/srip-train.png"));
    startButton.addListener(SWT.Selection, e -> startTraining());

    stopButton = new ToolItem(toolBar, SWT.PUSH);
    stopButton.setText("Stop");
    stopButton.setImage(icon("/edu/yaprnn/gui/views/action/srip-train.png"));
    stopButton.addListener(SWT.Selection, e -> stopTraining());

    var clearButton = new ToolItem(toolBar, SWT.PUSH);
    clearButton.setText("Clear Graph");
    clearButton.setImage(icon("/edu/yaprnn/gui/views/action/srip-remove.png"));
    clearButton.addListener(SWT.Selection, e -> clearGraph());

    new ToolItem(toolBar, SWT.SEPARATOR);

    var trainingDataItem = new ToolItem(toolBar, SWT.SEPARATOR);
    trainingDataViewer = createComboViewer(toolBar, TrainingData.class);
    trainingDataViewer.setInput(repository.getTrainingDataList().toArray(TrainingData[]::new));
    trainingDataViewer.addSelectionChangedListener(e -> syncView());
    trainingDataItem.setControl(trainingDataViewer.getCombo());
    trainingDataItem.setWidth(200);

    var networkItem = new ToolItem(toolBar, SWT.SEPARATOR);
    networkViewer = createComboViewer(toolBar, MultiLayerNetwork.class);
    networkViewer.setInput(repository.getMultiLayerNetworks().toArray(MultiLayerNetwork[]::new));
    networkViewer.addSelectionChangedListener(e -> syncView());
    networkItem.setControl(networkViewer.getCombo());
    networkItem.setWidth(200);
  }

  private ComboViewer createComboViewer(Composite parent, Class<?> type) {
    var viewer = new ComboViewer(parent, SWT.READ_ONLY);
    viewer.setContentProvider(ArrayContentProvider.getInstance());
    viewer.setLabelProvider(new LabelProvider() {
      @Override
      public String getText(Object element) {
        return switch (element) {
          case TrainingData td -> td.getName();
          case MultiLayerNetwork n -> n.getName();
          default -> element.toString();
        };
      }
    });
    return viewer;
  }

  private void createContent() {
    var sash = new SashForm(shell, SWT.HORIZONTAL);
    sash.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

    createPreferencesPanel(sash);
    createChartPanel(sash);

    sash.setWeights(30, 70);
  }

  private void createPreferencesPanel(Composite parent) {
    var scroll = new ScrolledComposite(parent, SWT.V_SCROLL);
    var panel = new Composite(scroll, SWT.NONE);
    panel.setLayout(new GridLayout(2, false));

    maxIterationsSpinner = createIntSpinner(panel, "Max iterations", 50, 1, 100000, 1000);
    maxTrainingErrorSpinner = createDecimalSpinner(panel, "Max error", 1, 1, 1000, 1, 3);
    batchSizeSpinner = createIntSpinner(panel, "Batch size", 100, 1, 10000, 1);
    maxParallelismSpinner = createIntSpinner(panel, "Max parallelism",
        ForkJoinPool.getCommonPoolParallelism(), 1, ForkJoinPool.getCommonPoolParallelism(), 1);
    learningRateSpinner = createDecimalSpinner(panel, "Learning rate", 20, 0, 1000, 1, 3);

    new Label(panel, SWT.NONE).setText("Learning rate modifier");
    learningRateModifierViewer = new ComboViewer(panel, SWT.READ_ONLY);
    learningRateModifierViewer.getCombo()
        .setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
    learningRateModifierViewer.setContentProvider(ArrayContentProvider.getInstance());
    learningRateModifierViewer.setLabelProvider(new LabelProvider());
    learningRateModifierViewer.setInput(LearningRateModifier.values());
    learningRateModifierViewer.setSelection(
        new StructuredSelection(LearningRateModifier.ADAPTIVE));

    learningRateChangeIntervalSpinner = createIntSpinner(panel, "LR change interval", 5, 1, 1000,
        1);
    learningRateAscendSpinner = createDecimalSpinner(panel, "LR ascend", 1010, 0, 2000, 1, 3);
    learningRateDescendSpinner = createDecimalSpinner(panel, "LR descend", 980, 0, 1000, 1, 3);
    momentumSpinner = createDecimalSpinner(panel, "Momentum", 200, -1000, 1000, 1, 3);
    decayL1Spinner = createDecimalSpinner(panel, "Decay L1", 1, -1000, 1000, 1, 3);
    decayL2Spinner = createDecimalSpinner(panel, "Decay L2", 1, -1000, 1000, 1, 3);

    scroll.setContent(panel);
    scroll.setExpandHorizontal(true);
    panel.setSize(panel.computeSize(SWT.DEFAULT, SWT.DEFAULT));
  }

  private Spinner createIntSpinner(Composite parent, String label, int value, int min, int max,
      int increment) {
    new Label(parent, SWT.NONE).setText(label);
    var spinner = new Spinner(parent, SWT.BORDER);
    spinner.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
    spinner.setValues(value, min, max, 0, increment, increment * 10);
    return spinner;
  }

  private Spinner createDecimalSpinner(Composite parent, String label, int value, int min, int max,
      int increment, int digits) {
    new Label(parent, SWT.NONE).setText(label);
    var spinner = new Spinner(parent, SWT.BORDER);
    spinner.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
    spinner.setValues(value, min, max, digits, increment, increment * 10);
    return spinner;
  }

  private void createChartPanel(Composite parent) {
    trainingError = new XYSeries("Training error");
    trainingHitRate = new XYSeries("Training accuracy");
    devTestError = new XYSeries("Dev Test error");
    devTestHitRate = new XYSeries("Dev Test accuracy");

    var collection = new XYSeriesCollection();
    collection.addSeries(trainingError);
    collection.addSeries(trainingHitRate);
    collection.addSeries(devTestError);
    collection.addSeries(devTestHitRate);

    var chart = ChartFactory.createXYLineChart("Training progress", "Epoch", "Value", collection,
        PlotOrientation.VERTICAL, true, false, false);
    chart.getXYPlot().getRangeAxis().setRange(0d, 1d);

    chartComposite = new org.jfree.chart.swt.ChartComposite(parent, SWT.NONE, chart, true);
  }

  private void startTraining() {
    var selectedTrainingData = getSelectedTrainingData();
    var selectedNetwork = getSelectedNetwork();
    if (selectedTrainingData == null || selectedNetwork == null) {
      return;
    }

    var params = TrainingParameters.builder()
        .maxIterations(maxIterationsSpinner.getSelection())
        .maxTrainingError(getDecimalValue(maxTrainingErrorSpinner))
        .batchSize(batchSizeSpinner.getSelection())
        .maxParallelism(maxParallelismSpinner.getSelection())
        .learningRate(getDecimalValue(learningRateSpinner))
        .learningRateModifier(getSelectedLearningRateModifier())
        .learningRateChangeInterval(learningRateChangeIntervalSpinner.getSelection())
        .learningRateAscend(getDecimalValue(learningRateAscendSpinner))
        .learningRateDescend(getDecimalValue(learningRateDescendSpinner))
        .momentum(getDecimalValue(momentumSpinner))
        .decayL1(getDecimalValue(decayL1Spinner))
        .decayL2(getDecimalValue(decayL2Spinner))
        .trainingData(selectedTrainingData)
        .multiLayerNetwork(selectedNetwork)
        .build();

    trainingThread = Thread.ofVirtual().name("training").start(() -> {
      try {
        trainingService.train(params,
            (iteration, learningRate, trainingAccuracy, devTestAccuracy) ->
                display.asyncExec(() -> {
                  if (shell.isDisposed()) {
                    return;
                  }
                  trainingError.add(trainingError.getItemCount(), trainingAccuracy.error());
                  trainingHitRate.add(trainingHitRate.getItemCount(), trainingAccuracy.hits());
                  devTestError.add(devTestError.getItemCount(), devTestAccuracy.error());
                  devTestHitRate.add(devTestHitRate.getItemCount(), devTestAccuracy.hits());
                  chartComposite.forceRedraw();
                  onMultiLayerNetworkWeightsPreviewModifiedRouter.fireEvent();
                }));
      } catch (Exception e) {
        log.log(Level.SEVERE, "Training failed", e);
        display.asyncExec(() -> {
          if (!shell.isDisposed()) {
            org.eclipse.jface.dialogs.MessageDialog.openError(shell, TITLE, e.getMessage());
          }
        });
      } finally {
        display.asyncExec(() -> {
          trainingThread = null;
          if (!shell.isDisposed()) {
            syncView();
          }
        });
      }
    });

    syncView();
  }

  private void stopTraining() {
    if (trainingThread != null) {
      trainingThread.interrupt();
    }
  }

  private void clearGraph() {
    trainingError.clear();
    trainingHitRate.clear();
    devTestError.clear();
    devTestHitRate.clear();
    chartComposite.forceRedraw();
  }

  private void syncView() {
    var inProgress = trainingThread != null;
    var hasTrainingData = getSelectedTrainingData() != null;
    var hasNetwork = getSelectedNetwork() != null;

    startButton.setEnabled(!inProgress && hasTrainingData && hasNetwork);
    stopButton.setEnabled(inProgress);

    var td = getSelectedTrainingData();
    var net = getSelectedNetwork();
    shell.setText("%s: %s, %s".formatted(TITLE,
        td != null ? td.getName() : "—",
        net != null ? net.getName() : "—"));
  }

  private TrainingData getSelectedTrainingData() {
    var sel = trainingDataViewer.getStructuredSelection();
    return sel.isEmpty() ? null : (TrainingData) sel.getFirstElement();
  }

  private MultiLayerNetwork getSelectedNetwork() {
    var sel = networkViewer.getStructuredSelection();
    return sel.isEmpty() ? null : (MultiLayerNetwork) sel.getFirstElement();
  }

  private LearningRateModifier getSelectedLearningRateModifier() {
    var sel = learningRateModifierViewer.getStructuredSelection();
    return sel.isEmpty() ? LearningRateModifier.ADAPTIVE
        : (LearningRateModifier) sel.getFirstElement();
  }

  public void setSelectedTrainingData(TrainingData trainingData) {
    if (Objects.nonNull(trainingData) && shell != null && !shell.isDisposed()) {
      trainingDataViewer.setSelection(new StructuredSelection(trainingData));
    }
  }

  public void setSelectedMultiLayerNetwork(MultiLayerNetwork network) {
    if (Objects.nonNull(network) && shell != null && !shell.isDisposed()) {
      networkViewer.setSelection(new StructuredSelection(network));
    }
  }

  public void prepareTrainingDataSelectionControls(List<TrainingData> items) {
    if (shell != null && !shell.isDisposed()) {
      trainingDataViewer.setInput(items.toArray(TrainingData[]::new));
    }
  }

  public void prepareMultiLayerNetworkSelectionControls(List<MultiLayerNetwork> items) {
    if (shell != null && !shell.isDisposed()) {
      networkViewer.setInput(items.toArray(MultiLayerNetwork[]::new));
    }
  }

  public void removeTrainingDataFromSelectionControls(List<TrainingData> removed) {
    if (shell == null || shell.isDisposed()) {
      return;
    }
    var input = (TrainingData[]) trainingDataViewer.getInput();
    if (input != null) {
      var filtered = java.util.Arrays.stream(input)
          .filter(td -> !removed.contains(td))
          .toArray(TrainingData[]::new);
      trainingDataViewer.setInput(filtered);
    }
  }

  public void removeMultiLayerNetworksFromSelectionControls(List<MultiLayerNetwork> removed) {
    if (shell == null || shell.isDisposed()) {
      return;
    }
    var input = (MultiLayerNetwork[]) networkViewer.getInput();
    if (input != null) {
      var filtered = java.util.Arrays.stream(input)
          .filter(n -> !removed.contains(n))
          .toArray(MultiLayerNetwork[]::new);
      networkViewer.setInput(filtered);
    }
  }

  private static float getDecimalValue(Spinner spinner) {
    return spinner.getSelection() / (float) Math.pow(10, spinner.getDigits());
  }

  private Image icon(String resourcePath) {
    try {
      return swtIconsService.getImage(resourcePath);
    } catch (Exception e) {
      log.log(Level.WARNING, "Failed to load icon: " + resourcePath, e);
      return null;
    }
  }
}
