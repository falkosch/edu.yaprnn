package edu.yaprnn.gui.views;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;

import edu.yaprnn.events.OnMultiLayerNetworkWeightsPreviewModifiedRouter;
import edu.yaprnn.gui.services.ControlsService;
import edu.yaprnn.gui.services.IconsService;
import edu.yaprnn.model.Repository;
import edu.yaprnn.networks.GradientMatrixService;
import edu.yaprnn.networks.MultiLayerNetwork;
import edu.yaprnn.networks.learningrate.ConstantLearningRateState;
import edu.yaprnn.networks.learningrate.DynamicLearningRateState;
import edu.yaprnn.networks.learningrate.EpochLearningRateState;
import edu.yaprnn.networks.learningrate.LearningRateState;
import edu.yaprnn.samples.model.Sample;
import edu.yaprnn.support.swing.DialogsService;
import edu.yaprnn.training.DataSelector;
import edu.yaprnn.training.ShuffleService;
import edu.yaprnn.training.TrainingData;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import java.awt.BorderLayout;
import java.awt.event.ItemEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ForkJoinPool;
import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingWorker;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

@Log
public class TrainingFrame extends JFrame {

  public static final String TITLE = "Training";

  private static TrainingWorker trainingWorker;
  @Inject
  ControlsService controlsService;
  @Inject
  DialogsService dialogsService;
  @Inject
  GradientMatrixService gradientMatrixService;
  @Inject
  IconsService iconsService;
  @Inject
  Repository repository;
  @Inject
  ShuffleService shuffleService;
  @Inject
  OnMultiLayerNetworkWeightsPreviewModifiedRouter onMultiLayerNetworkWeightsPreviewModifiedRouter;
  private JButton startTrainingButton;
  private JButton stopTrainingButton;
  private DefaultComboBoxModel<TrainingData> trainingDataComboBoxModel;
  private DefaultComboBoxModel<MultiLayerNetwork> multiLayerNetworksComboBoxModel;
  private SpinnerNumberModel maxIterationsSpinnerNumberModel;
  private SpinnerNumberModel maxTrainingErrorSpinnerNumberModel;
  private JComboBox<TrainingMethod> trainingMethodComboBox;
  private SpinnerNumberModel batchSizeSpinnerNumberModel;
  private SpinnerNumberModel learningRateSpinnerNumberModel;
  private SpinnerNumberModel momentumSpinnerNumberModel;
  private SpinnerNumberModel decayL1SpinnerNumberModel;
  private SpinnerNumberModel decayL2SpinnerNumberModel;
  private JComboBox<LearningRateModifier> learningRateModifierComboBox;
  private SpinnerNumberModel learningRateChangeIntervalSpinnerNumberModel;
  private SpinnerNumberModel learningRateAscendSpinnerNumberModel;
  private SpinnerNumberModel learningRateDescendSpinnerNumberModel;
  private XYSeries devTestError;
  private XYSeries devTestHitRate;
  private XYSeries trainingError;
  private XYSeries trainingHitRate;

  private MultiLayerNetwork getSelectedMultiLayerNetwork() {
    return (MultiLayerNetwork) multiLayerNetworksComboBoxModel.getSelectedItem();
  }

  public void setSelectedMultiLayerNetwork(MultiLayerNetwork multiLayerNetwork) {
    if (Objects.nonNull(multiLayerNetwork)) {
      multiLayerNetworksComboBoxModel.setSelectedItem(multiLayerNetwork);
    }
  }

  private void syncViewOnSelectionChanged(TrainingData selectedTrainingData,
      MultiLayerNetwork selectedMultiLayerNetwork) {
    setTitle("%s: %s, %s".formatted(TITLE,
        controlsService.toTitlePart(selectedTrainingData, TrainingData::getName),
        controlsService.toTitlePart(selectedMultiLayerNetwork, MultiLayerNetwork::getName)));

    var trainingInProgress = isTrainingInProgress();
    var hasSelectedTrainingData = Objects.nonNull(selectedTrainingData);
    var hasSelectedMultiLayerNetwork = Objects.nonNull(selectedMultiLayerNetwork);

    startTrainingButton.setEnabled(
        !trainingInProgress && hasSelectedTrainingData && hasSelectedMultiLayerNetwork);
    stopTrainingButton.setEnabled(trainingInProgress);
  }

  private boolean isTrainingInProgress() {
    return Objects.nonNull(trainingWorker);
  }

  public void removeTrainingDataFromSelectionControls(List<TrainingData> removed) {
    removed.forEach(trainingDataComboBoxModel::removeElement);
  }

  public void removeMultiLayerNetworksFromSelectionControls(List<MultiLayerNetwork> removed) {
    removed.forEach(multiLayerNetworksComboBoxModel::removeElement);
  }

  @PostConstruct
  void initialize() {
    var maxIterationsLabel = new JLabel("Max iterations");
    var maxTrainingErrorLabel = new JLabel("Max error");
    var trainingMethodLabel = new JLabel("Training method");
    var batchSizeLabel = new JLabel("Batch size");
    var learningRateLabel = new JLabel("Learning rate");
    var learningRateModifierLabel = new JLabel("Learning rate modifier");
    var learningRateChangeIntervalLabel = new JLabel("Learning rate change interval");
    var learningRateAscendLabel = new JLabel("Learning rate ascend");
    var learningRateDescendLabel = new JLabel("Learning rate descend");
    var momentumLabel = new JLabel("Momentum");
    var decayL1Label = new JLabel("Decay L1");
    var decayL2Label = new JLabel("Decay L2");

    startTrainingButton = controlsService.actionButton("Start", iconsService.trainIcon(),
        this::startTraining);
    stopTrainingButton = controlsService.actionButton("Stop", iconsService.trainIcon(),
        this::stopTraining);
    var clearGraphButton = controlsService.actionButton("Clear graph", iconsService.removeIcon(),
        this::clearGraph);

    trainingDataComboBoxModel = new DefaultComboBoxModel<>(
        repository.getTrainingDataList().toArray(TrainingData[]::new));
    var trainingDataComboBox = new JComboBox<>(trainingDataComboBoxModel);
    trainingDataComboBox.addItemListener(this::syncViewOnTrainingDataSelectionChanged);

    multiLayerNetworksComboBoxModel = new DefaultComboBoxModel<>(
        repository.getMultiLayerNetworks().toArray(MultiLayerNetwork[]::new));
    var multiLayerNetworksComboBox = new JComboBox<>(multiLayerNetworksComboBoxModel);
    multiLayerNetworksComboBox.addItemListener(this::syncViewOnMultiLayerNetworkSelectionChanged);

    var toolBar = new JToolBar();
    toolBar.add(startTrainingButton);
    toolBar.add(stopTrainingButton);
    toolBar.add(clearGraphButton);
    toolBar.add(trainingDataComboBox);
    toolBar.add(multiLayerNetworksComboBox);

    maxTrainingErrorSpinnerNumberModel = new SpinnerNumberModel(0.001, 0.001, null, 0.001);
    var maxTrainingErrorSpinner = new JSpinner(maxTrainingErrorSpinnerNumberModel);
    maxIterationsSpinnerNumberModel = new SpinnerNumberModel(100, 1, null, 1000);
    var maxIterationsSpinner = new JSpinner(maxIterationsSpinnerNumberModel);

    trainingMethodComboBox = new JComboBox<>(TrainingMethod.values());
    trainingMethodComboBox.getModel().setSelectedItem(TrainingMethod.BATCH);
    batchSizeSpinnerNumberModel = new SpinnerNumberModel(ForkJoinPool.getCommonPoolParallelism(), 1,
        null, 1);
    var batchSizeSpinner = new JSpinner(batchSizeSpinnerNumberModel);

    learningRateSpinnerNumberModel = new SpinnerNumberModel(0.02, 0.0, 1.0, 0.001);
    var learningRateSpinner = new JSpinner(learningRateSpinnerNumberModel);
    learningRateModifierComboBox = new JComboBox<>(LearningRateModifier.values());
    learningRateModifierComboBox.setSelectedItem(LearningRateModifier.ADAPTIVE);
    learningRateChangeIntervalSpinnerNumberModel = new SpinnerNumberModel(5, 1, null, 1);
    var learningRateChangeIntervalSpinner = new JSpinner(
        learningRateChangeIntervalSpinnerNumberModel);
    learningRateAscendSpinnerNumberModel = new SpinnerNumberModel(1.01, 0.0, 2.0, 0.001);
    var learningRateAscendSpinner = new JSpinner(learningRateAscendSpinnerNumberModel);
    learningRateDescendSpinnerNumberModel = new SpinnerNumberModel(0.98, 0.0, 1.0, 0.001);
    var learningRateDescendSpinner = new JSpinner(learningRateDescendSpinnerNumberModel);
    momentumSpinnerNumberModel = new SpinnerNumberModel(0.5, -1.0, 1.0, 0.001);
    var momentumSpinner = new JSpinner(momentumSpinnerNumberModel);
    decayL1SpinnerNumberModel = new SpinnerNumberModel(0.001, -1.0, 1.0, 0.001);
    var decayL1Spinner = new JSpinner(decayL1SpinnerNumberModel);
    decayL2SpinnerNumberModel = new SpinnerNumberModel(0.001, -1.0, 1.0, 0.001);
    var decayL2Spinner = new JSpinner(decayL2SpinnerNumberModel);

    devTestError = new XYSeries("Dev Test error");
    devTestHitRate = new XYSeries("Dev Test accuracy");
    trainingError = new XYSeries("Training error");
    trainingHitRate = new XYSeries("Training accuracy");
    var errorXYSeriesCollection = new XYSeriesCollection();
    errorXYSeriesCollection.addSeries(devTestError);
    errorXYSeriesCollection.addSeries(devTestHitRate);
    errorXYSeriesCollection.addSeries(trainingError);
    errorXYSeriesCollection.addSeries(trainingHitRate);
    var xyLineChart = ChartFactory.createXYLineChart("Training progress", "Iteration", "Value",
        errorXYSeriesCollection, PlotOrientation.VERTICAL, true, false, false);
    xyLineChart.getXYPlot().getRangeAxis().setRange(0d, 1d);

    var preferencesPanel = new JPanel();
    var preferencesGroupLayout = new GroupLayout(preferencesPanel);
    preferencesGroupLayout.setHorizontalGroup(preferencesGroupLayout.createParallelGroup()
        .addGroup(preferencesGroupLayout.createParallelGroup()
            .addComponent(maxIterationsLabel)
            .addComponent(maxIterationsSpinner, PREFERRED_SIZE, DEFAULT_SIZE, 100)
            .addComponent(maxTrainingErrorLabel)
            .addComponent(maxTrainingErrorSpinner, PREFERRED_SIZE, DEFAULT_SIZE, 100))
        .addGroup(preferencesGroupLayout.createParallelGroup()
            .addComponent(trainingMethodLabel)
            .addComponent(trainingMethodComboBox, PREFERRED_SIZE, DEFAULT_SIZE, 200)
            .addComponent(batchSizeLabel)
            .addComponent(batchSizeSpinner, PREFERRED_SIZE, DEFAULT_SIZE, 100))
        .addGroup(preferencesGroupLayout.createParallelGroup()
            .addComponent(learningRateLabel)
            .addComponent(learningRateSpinner, PREFERRED_SIZE, DEFAULT_SIZE, 100)
            .addComponent(learningRateModifierLabel)
            .addComponent(learningRateModifierComboBox, PREFERRED_SIZE, DEFAULT_SIZE, 200)
            .addComponent(learningRateChangeIntervalLabel)
            .addComponent(learningRateChangeIntervalSpinner, PREFERRED_SIZE, DEFAULT_SIZE, 100)
            .addComponent(learningRateAscendLabel)
            .addComponent(learningRateAscendSpinner, PREFERRED_SIZE, DEFAULT_SIZE, 100)
            .addComponent(learningRateDescendLabel)
            .addComponent(learningRateDescendSpinner, PREFERRED_SIZE, DEFAULT_SIZE, 100))
        .addGroup(preferencesGroupLayout.createParallelGroup()
            .addComponent(momentumLabel)
            .addComponent(momentumSpinner, PREFERRED_SIZE, DEFAULT_SIZE, 100)
            .addComponent(decayL1Label)
            .addComponent(decayL1Spinner, PREFERRED_SIZE, DEFAULT_SIZE, 100)
            .addComponent(decayL2Label)
            .addComponent(decayL2Spinner, PREFERRED_SIZE, DEFAULT_SIZE, 100)));
    preferencesGroupLayout.setVerticalGroup(preferencesGroupLayout.createSequentialGroup()
        .addGroup(preferencesGroupLayout.createSequentialGroup()
            .addComponent(maxIterationsLabel)
            .addComponent(maxIterationsSpinner, PREFERRED_SIZE, DEFAULT_SIZE, 28)
            .addComponent(maxTrainingErrorLabel)
            .addComponent(maxTrainingErrorSpinner, PREFERRED_SIZE, DEFAULT_SIZE, 28))
        .addGroup(preferencesGroupLayout.createSequentialGroup()
            .addComponent(trainingMethodLabel)
            .addComponent(trainingMethodComboBox, PREFERRED_SIZE, DEFAULT_SIZE, 28)
            .addComponent(batchSizeLabel)
            .addComponent(batchSizeSpinner, PREFERRED_SIZE, DEFAULT_SIZE, 28))
        .addGroup(preferencesGroupLayout.createSequentialGroup()
            .addComponent(learningRateLabel)
            .addComponent(learningRateSpinner, PREFERRED_SIZE, DEFAULT_SIZE, 28)
            .addComponent(learningRateModifierLabel)
            .addComponent(learningRateModifierComboBox, PREFERRED_SIZE, DEFAULT_SIZE, 28)
            .addComponent(learningRateChangeIntervalLabel)
            .addComponent(learningRateChangeIntervalSpinner, PREFERRED_SIZE, DEFAULT_SIZE, 28)
            .addComponent(learningRateAscendLabel)
            .addComponent(learningRateAscendSpinner, PREFERRED_SIZE, DEFAULT_SIZE, 28)
            .addComponent(learningRateDescendLabel)
            .addComponent(learningRateDescendSpinner, PREFERRED_SIZE, DEFAULT_SIZE, 28))
        .addGroup(preferencesGroupLayout.createSequentialGroup()
            .addComponent(momentumLabel)
            .addComponent(momentumSpinner, PREFERRED_SIZE, DEFAULT_SIZE, 28)
            .addComponent(decayL1Label)
            .addComponent(decayL1Spinner, PREFERRED_SIZE, DEFAULT_SIZE, 28)
            .addComponent(decayL2Label)
            .addComponent(decayL2Spinner, PREFERRED_SIZE, DEFAULT_SIZE, 28)));
    preferencesGroupLayout.setAutoCreateContainerGaps(true);
    preferencesGroupLayout.setAutoCreateGaps(true);
    preferencesPanel.setLayout(preferencesGroupLayout);

    var chartPanel = new ChartPanel(xyLineChart);
    chartPanel.setMouseZoomable(true, true);
    var graphPanel = new JPanel(new BorderLayout());
    graphPanel.add(chartPanel, BorderLayout.CENTER);

    var contentSplitPane = new JSplitPane();
    contentSplitPane.setLeftComponent(new JScrollPane(preferencesPanel));
    contentSplitPane.setRightComponent(graphPanel);

    getContentPane().add(toolBar, BorderLayout.NORTH);
    getContentPane().add(contentSplitPane);
    addWindowListener(new TrainingFrameWindowAdapter());
    setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    syncViewOnSelectionChanged(null, null);
    pack();
  }

  private void syncViewOnMultiLayerNetworkSelectionChanged(ItemEvent event) {
    var selectedNetwork = (MultiLayerNetwork) (event.getStateChange() == ItemEvent.SELECTED
        ? event.getItem() : null);
    syncViewOnSelectionChanged(getSelectedTrainingData(), selectedNetwork);
  }

  private void syncViewOnTrainingDataSelectionChanged(ItemEvent event) {
    var selectedTrainingData = (TrainingData) (event.getStateChange() == ItemEvent.SELECTED
        ? event.getItem() : null);
    syncViewOnSelectionChanged(selectedTrainingData, getSelectedMultiLayerNetwork());
  }

  private void startTraining() {
    trainingWorker = new TrainingWorker(maxIterationsSpinnerNumberModel.getNumber().intValue(),
        maxTrainingErrorSpinnerNumberModel.getNumber().floatValue(),
        (TrainingMethod) trainingMethodComboBox.getSelectedItem(),
        batchSizeSpinnerNumberModel.getNumber().intValue(),
        learningRateSpinnerNumberModel.getNumber().floatValue(),
        (LearningRateModifier) learningRateModifierComboBox.getSelectedItem(),
        learningRateChangeIntervalSpinnerNumberModel.getNumber().intValue(),
        learningRateAscendSpinnerNumberModel.getNumber().floatValue(),
        learningRateDescendSpinnerNumberModel.getNumber().floatValue(),
        momentumSpinnerNumberModel.getNumber().floatValue(),
        decayL1SpinnerNumberModel.getNumber().floatValue(),
        decayL2SpinnerNumberModel.getNumber().floatValue(), getSelectedTrainingData(),
        getSelectedMultiLayerNetwork());
    trainingWorker.execute();
  }

  private TrainingData getSelectedTrainingData() {
    return (TrainingData) trainingDataComboBoxModel.getSelectedItem();
  }

  public void setSelectedTrainingData(TrainingData trainingData) {
    if (Objects.nonNull(trainingData)) {
      trainingDataComboBoxModel.setSelectedItem(trainingData);
    }
  }

  private void stopTraining() {
    trainingWorker.stop();
  }

  private void clearGraph() {
    devTestError.clear();
    devTestHitRate.clear();
    trainingError.clear();
    trainingHitRate.clear();
  }

  public void prepareMultiLayerNetworkSelectionControls(List<MultiLayerNetwork> items) {
    multiLayerNetworksComboBoxModel.addAll(items);
  }

  public void prepareTrainingDataSelectionControls(List<TrainingData> items) {
    trainingDataComboBoxModel.addAll(items);
  }

  private enum TrainingMethod {ONLINE, BATCH}

  private enum LearningRateModifier {CONSTANT, PERIODIC, ADAPTIVE}

  private interface LearnSamplesConsumer {

    void accept(List<Sample> samples, DataSelector dataSelector, int iteration, float learningRate);
  }

  @RequiredArgsConstructor
  private class TrainingWorker extends SwingWorker<MultiLayerNetwork, Integer> {

    private final int maxIterations;
    private final float maxTrainingError;
    private final TrainingMethod trainingMethod;
    private final int batchSize;
    private final float learningRate;
    private final LearningRateModifier learningRateModifier;
    private final int learningRateChangeInterval;
    private final float learningRateAscend;
    private final float learningRateDescend;
    private final float momentum;
    private final float decayL1;
    private final float decayL2;
    private final TrainingData trainingData;
    private final MultiLayerNetwork multiLayerNetwork;

    private boolean stopped = false;

    private void stop() {
      stopped = true;
      super.cancel(true);
    }

    @Override
    protected MultiLayerNetwork doInBackground() {
      startTrainingButton.setEnabled(false);
      stopTrainingButton.setEnabled(true);

      try {
        train((samples, dataSelector, iteration, learningRate) -> {
          if (Objects.requireNonNull(trainingMethod) == TrainingMethod.ONLINE) {
            multiLayerNetwork.learnOnlineParallelized(gradientMatrixService, samples, dataSelector,
                learningRate, momentum, decayL1, decayL2);
          } else {
            multiLayerNetwork.learnMiniBatch(gradientMatrixService, samples, dataSelector,
                batchSize, learningRate, momentum, decayL1, decayL2);
          }
        });
      } catch (Throwable throwable) {
        dialogsService.showError(TrainingFrame.this, TITLE, throwable);
      }

      trainingWorker = null;
      startTrainingButton.setEnabled(true);
      stopTrainingButton.setEnabled(false);

      return multiLayerNetwork;
    }

    private void train(LearnSamplesConsumer learnSamplesConsumer) {
      var learningRateState = getLearningRateState();
      var dataSelector = trainingData.getDataSelector();
      var devTestSamples = repository.querySamplesByName(trainingData.getDevTestSampleNames());
      var trainingSamples = repository.querySamplesByName(trainingData.getTrainingSampleNames());

      var trainingError = trackError(-1, learningRate, trainingSamples, devTestSamples,
          dataSelector);

      for (var i = 0; i < maxIterations && trainingError > maxTrainingError && !stopped; i++) {
        var samples = shuffleService.shuffleList(trainingSamples);
        var learningRate = learningRateState.current();

        var iteration = i;
        measureIterationTime(
            () -> learnSamplesConsumer.accept(samples, dataSelector, iteration, learningRate));

        learningRateState = learningRateState.updateRate(trainingError);
        trainingError = trackError(iteration, learningRate, samples, devTestSamples, dataSelector);

        onMultiLayerNetworkWeightsPreviewModifiedRouter.fireEvent();
      }
    }

    private LearningRateState getLearningRateState() {
      return switch (learningRateModifier) {
        case PERIODIC ->
            EpochLearningRateState.from(learningRateChangeInterval, learningRateDescend,
                learningRate);
        case ADAPTIVE ->
            DynamicLearningRateState.from(learningRateAscend, learningRateDescend, learningRate);
        default -> ConstantLearningRateState.from(learningRate);
      };
    }

    private float trackError(int iteration, float learningRate, List<Sample> samples,
        List<Sample> devTestSamples, DataSelector dataSelector) {
      var trainingAccuracy = multiLayerNetwork.computeAccuracy(samples, dataSelector);
      var devTestAccuracy = multiLayerNetwork.computeAccuracy(devTestSamples, dataSelector);
      trainingError.add(trainingError.getItemCount(), trainingAccuracy.error());
      trainingHitRate.add(trainingHitRate.getItemCount(), trainingAccuracy.hits());
      devTestError.add(devTestError.getItemCount(), devTestAccuracy.error());
      devTestHitRate.add(devTestHitRate.getItemCount(), devTestAccuracy.hits());

      log.info(
          "[%s] lr=%s | training: accuracy=%s, error=%s | test: accuracy=%s, error=%s".formatted(
              iteration, learningRate, trainingAccuracy.hits(), trainingAccuracy.error(),
              devTestAccuracy.hits(), devTestAccuracy.error()));

      return trainingAccuracy.error();
    }

    private void measureIterationTime(Runnable runnable) {
      var t = System.nanoTime();
      runnable.run();
      var delta = System.nanoTime() - t;
      var iterationTime = delta / (float) Duration.ofSeconds(1).toNanos();
      log.info(() -> "Iteration time: %s".formatted(iterationTime));
    }
  }

  private class TrainingFrameWindowAdapter extends WindowAdapter {

    @Override
    public void windowClosing(WindowEvent event) {
      if (Objects.nonNull(trainingWorker)) {
        trainingWorker.stop();
      }
      dispose();
    }

    @Override
    public void windowClosed(WindowEvent event) {
      trainingWorker = null;
    }
  }
}
