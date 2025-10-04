package edu.yaprnn.gui.views;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;

import edu.yaprnn.events.OnMultiLayerNetworkWeightsPreviewModifiedRouter;
import edu.yaprnn.gui.images.ImagePanel;
import edu.yaprnn.gui.services.ControlsService;
import edu.yaprnn.gui.services.IconsService;
import edu.yaprnn.gui.services.WeightsService;
import edu.yaprnn.model.Repository;
import edu.yaprnn.networks.Layer;
import edu.yaprnn.networks.MultiLayerNetwork;
import edu.yaprnn.samples.model.ImageSample;
import edu.yaprnn.samples.model.Sample;
import edu.yaprnn.training.DataSelector;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.awt.BorderLayout;
import java.awt.event.ItemEvent;
import java.util.List;
import java.util.Objects;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JToolBar;

@Singleton
public class ClassifyFrame extends JFrame {

  public static final String TITLE = "Classify";

  @Inject
  ControlsService controlsService;
  @Inject
  IconsService iconsService;
  @Inject
  OnMultiLayerNetworkWeightsPreviewModifiedRouter onMultiLayerNetworkWeightsPreviewModifiedRouter;
  @Inject
  Repository repository;
  @Inject
  WeightsService weightsService;

  @Inject
  Instance<SampleDetailsView> sampleDetailsViewInstance;

  private JButton classifyButton;
  private DefaultComboBoxModel<MultiLayerNetwork> multiLayerNetworksComboBoxModel;
  private DefaultComboBoxModel<Sample> samplesComboBoxModel;
  private ComboBoxModel<DataSelector> dataSelectorComboBoxModel;
  private JTable layersTable;
  private SampleDetailsView sampleDetailsView;
  private ImagePanel outputReconstructionImagePanel;

  public void removeSamplesFromSelectionControls(List<Sample> removed) {
    controlsService.silenceListModelListenersDuringRunnable(samplesComboBoxModel,
        () -> removed.forEach(samplesComboBoxModel::removeElement));
  }

  public void removeMultiLayerNetworksFromSelectionControls(List<MultiLayerNetwork> removed) {
    controlsService.silenceListModelListenersDuringRunnable(multiLayerNetworksComboBoxModel,
        () -> removed.forEach(multiLayerNetworksComboBoxModel::removeElement));
  }

  @PostConstruct
  void initializeComponents() {
    var zoomWeightsLabel = new JLabel("Zoom");
    var gammaLabel = new JLabel("Gamma");

    classifyButton = controlsService.actionButton(TITLE, iconsService.classifyIcon(),
        this::classify);

    samplesComboBoxModel = new DefaultComboBoxModel<>(
        repository.getSamples().toArray(Sample[]::new));
    var samplesComboBox = new JComboBox<>(samplesComboBoxModel);
    samplesComboBox.addItemListener(this::syncViewOnSampleSelectionChanged);

    multiLayerNetworksComboBoxModel = new DefaultComboBoxModel<>(
        repository.getMultiLayerNetworks().toArray(MultiLayerNetwork[]::new));
    var networksComboBox = new JComboBox<>(multiLayerNetworksComboBoxModel);
    networksComboBox.addItemListener(this::syncViewOnMultiLayerNetworkSelectionChanged);

    dataSelectorComboBoxModel = controlsService.dataSelectorsComboBoxModel();
    var dataSelectorComboBox = new JComboBox<>(dataSelectorComboBoxModel);
    var gammaSlider = controlsService.gammaSlider(
        onMultiLayerNetworkWeightsPreviewModifiedRouter::setGamma);
    var zoomWeightsComboBox = controlsService.zoomComboBox(
        onMultiLayerNetworkWeightsPreviewModifiedRouter::setZoom);

    var toolBar = new JToolBar();
    toolBar.add(classifyButton);
    toolBar.add(samplesComboBox);
    toolBar.add(networksComboBox);
    toolBar.add(dataSelectorComboBox);

    layersTable = controlsService.valuesTable();
    sampleDetailsView = sampleDetailsViewInstance.get();
    outputReconstructionImagePanel = new ImagePanel();

    var layersScrollPane = new JScrollPane(layersTable);
    var layersPanel = new JPanel();
    var layersLayout = new GroupLayout(layersPanel);
    layersLayout.setHorizontalGroup(
        layersLayout.createParallelGroup().addComponent(layersScrollPane));
    layersLayout.setVerticalGroup(
        layersLayout.createParallelGroup().addComponent(layersScrollPane));
    layersLayout.setAutoCreateContainerGaps(true);
    layersLayout.setAutoCreateGaps(true);
    layersPanel.setLayout(layersLayout);
    layersPanel.setOpaque(false);

    var outputReconstructionScrollPane = new JScrollPane(outputReconstructionImagePanel);
    var outputReconstructionPanel = new JPanel();
    var outputReconstructionLayout = new GroupLayout(outputReconstructionPanel);
    outputReconstructionLayout.setHorizontalGroup(outputReconstructionLayout.createParallelGroup()
        .addGroup(outputReconstructionLayout.createSequentialGroup()
            .addGroup(outputReconstructionLayout.createParallelGroup()
                .addComponent(zoomWeightsLabel)
                .addComponent(zoomWeightsComboBox, PREFERRED_SIZE, DEFAULT_SIZE, 100))
            .addGroup(outputReconstructionLayout.createParallelGroup()
                .addComponent(gammaLabel)
                .addComponent(gammaSlider, PREFERRED_SIZE, DEFAULT_SIZE, 300)))
        .addComponent(outputReconstructionScrollPane));
    outputReconstructionLayout.setVerticalGroup(outputReconstructionLayout.createParallelGroup()
        .addGroup(outputReconstructionLayout.createParallelGroup()
            .addGroup(outputReconstructionLayout.createSequentialGroup()
                .addComponent(zoomWeightsLabel)
                .addComponent(zoomWeightsComboBox, PREFERRED_SIZE, DEFAULT_SIZE, 28))
            .addGroup(outputReconstructionLayout.createSequentialGroup()
                .addComponent(gammaLabel)
                .addComponent(gammaSlider, PREFERRED_SIZE, DEFAULT_SIZE, 28)))
        .addComponent(outputReconstructionScrollPane));
    outputReconstructionLayout.setAutoCreateContainerGaps(true);
    outputReconstructionLayout.setAutoCreateGaps(true);
    outputReconstructionPanel.setLayout(outputReconstructionLayout);
    outputReconstructionPanel.setOpaque(false);

    var tabbedPane = new JTabbedPane();
    tabbedPane.add(SampleDetailsView.TITLE, sampleDetailsView.getContent());
    tabbedPane.add("Layers", layersPanel);
    tabbedPane.add("Output reconstruction", outputReconstructionPanel);

    getContentPane().add(toolBar, BorderLayout.NORTH);
    getContentPane().add(tabbedPane);
    setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    syncViewOnSelectionChanged(null, null);
    pack();
    sampleDetailsView.doLayout();
  }

  private void classify() {
    var selectedSample = getSelectedSample();
    var selectedMultiLayerNetwork = getSelectedMultiLayerNetwork();
    var selectedDataSelector = getSelectedDataSelector();
    var labels = selectedSample.getLabels();
    var layers = selectedMultiLayerNetwork.feedForward(selectedSample, selectedDataSelector);
    var tableModel = weightsService.classificationTableModel(labels, layers);
    layersTable.setModel(tableModel);

    if (selectedSample instanceof ImageSample imageSample) {
      var reconstruction = weightsService.from(Layer.output(layers).h(),
          imageSample.getInputWidth(), onMultiLayerNetworkWeightsPreviewModifiedRouter.getZoom(),
          onMultiLayerNetworkWeightsPreviewModifiedRouter.getGamma());
      outputReconstructionImagePanel.setImage(reconstruction);
    } else {
      outputReconstructionImagePanel.setImage(null);
    }
  }

  private void syncViewOnSampleSelectionChanged(ItemEvent event) {
    var selectedSample = (Sample) (event.getStateChange() == ItemEvent.SELECTED ? event.getItem()
        : null);
    sampleDetailsView.setSamplePreview(selectedSample);
    syncViewOnSelectionChanged(selectedSample, getSelectedMultiLayerNetwork());
  }

  private void syncViewOnMultiLayerNetworkSelectionChanged(ItemEvent event) {
    var selectedMultiLayerNetwork = (MultiLayerNetwork) (
        event.getStateChange() == ItemEvent.SELECTED ? event.getItem() : null);
    syncViewOnSelectionChanged(getSelectedSample(), selectedMultiLayerNetwork);
  }

  private void syncViewOnSelectionChanged(Sample selectedSample,
      MultiLayerNetwork selectedMultiLayerNetwork) {
    setTitle(
        "%s: %s, %s".formatted(TITLE, controlsService.toTitlePart(selectedSample, Sample::getName),
            controlsService.toTitlePart(selectedMultiLayerNetwork, MultiLayerNetwork::getName)));

    classifyButton.setEnabled(
        Objects.nonNull(selectedSample) && Objects.nonNull(selectedMultiLayerNetwork));
  }

  private Sample getSelectedSample() {
    return (Sample) samplesComboBoxModel.getSelectedItem();
  }

  public void setSelectedSample(Sample sample) {
    if (Objects.nonNull(sample)) {
      samplesComboBoxModel.setSelectedItem(sample);
    }
  }

  private MultiLayerNetwork getSelectedMultiLayerNetwork() {
    return (MultiLayerNetwork) multiLayerNetworksComboBoxModel.getSelectedItem();
  }

  public void setSelectedMultiLayerNetwork(MultiLayerNetwork multiLayerNetwork) {
    if (Objects.nonNull(multiLayerNetwork)) {
      multiLayerNetworksComboBoxModel.setSelectedItem(multiLayerNetwork);
    }
  }

  private DataSelector getSelectedDataSelector() {
    return (DataSelector) dataSelectorComboBoxModel.getSelectedItem();
  }

  public void setSamplePreview(float zoom, int resolution, float overlap) {
    sampleDetailsView.setSamplePreview(zoom, resolution, overlap);
  }

  public void prepareMultiLayerNetworkSelectionControls(List<MultiLayerNetwork> networks) {
    multiLayerNetworksComboBoxModel.addAll(networks);
  }

  public void prepareSampleSelectionControls(List<Sample> samples) {
    samplesComboBoxModel.addAll(samples);
  }
}
