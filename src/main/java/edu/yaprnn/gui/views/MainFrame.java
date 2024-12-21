package edu.yaprnn.gui.views;

import edu.yaprnn.events.OnModelNodeSelected;
import edu.yaprnn.events.OnModelNodeSelectedRouter;
import edu.yaprnn.events.OnMultiLayerNetworkSelected;
import edu.yaprnn.events.OnMultiLayerNetworkSelectedRouter;
import edu.yaprnn.events.OnMultiLayerNetworkTemplateSelectedRouter;
import edu.yaprnn.events.OnMultiLayerNetworkWeightsPreviewModified;
import edu.yaprnn.events.OnRepositoryElementsChanged;
import edu.yaprnn.events.OnRepositoryElementsRemoved;
import edu.yaprnn.events.OnSamplePreviewModified;
import edu.yaprnn.events.OnSamplePreviewModifiedRouter;
import edu.yaprnn.events.OnSampleSelected;
import edu.yaprnn.events.OnTrainingDataSelected;
import edu.yaprnn.events.OnTrainingDataSelectedRouter;
import edu.yaprnn.gui.model.AllSamplesListNode;
import edu.yaprnn.gui.model.MultiLayerNetworkListNode;
import edu.yaprnn.gui.model.MultiLayerNetworkTemplateListNode;
import edu.yaprnn.gui.model.NetworksTreeCellRenderer;
import edu.yaprnn.gui.model.NetworksTreeModel;
import edu.yaprnn.gui.model.TrainingDataListNode;
import edu.yaprnn.gui.model.editors.NetworksTreeCellEditor;
import edu.yaprnn.gui.model.nodes.ActivationFunctionNode;
import edu.yaprnn.gui.model.nodes.LayerSizeNode;
import edu.yaprnn.gui.model.nodes.LayerTemplateNode;
import edu.yaprnn.gui.model.nodes.ModelNode;
import edu.yaprnn.gui.model.nodes.MultiLayerNetworkNode;
import edu.yaprnn.gui.model.nodes.MultiLayerNetworkTemplateNode;
import edu.yaprnn.gui.model.nodes.MultiLayerNetworkWeightsNode;
import edu.yaprnn.gui.model.nodes.SampleNameNode;
import edu.yaprnn.gui.model.nodes.SampleNode;
import edu.yaprnn.gui.model.nodes.TrainingDataNode;
import edu.yaprnn.gui.services.ControlsService;
import edu.yaprnn.gui.services.FilesService;
import edu.yaprnn.gui.services.IconsService;
import edu.yaprnn.gui.services.NetworksControlsService;
import edu.yaprnn.gui.services.PersistenceService;
import edu.yaprnn.gui.services.SamplesService;
import edu.yaprnn.gui.views.di.NetworksTree;
import edu.yaprnn.gui.views.mappings.MultiLayerNetworkMapper;
import edu.yaprnn.gui.views.mappings.MultiLayerNetworkTemplateMapper;
import edu.yaprnn.gui.views.mappings.TrainingDataMapper;
import edu.yaprnn.model.Repository;
import edu.yaprnn.networks.GradientMatrixService;
import edu.yaprnn.networks.MultiLayerNetwork;
import edu.yaprnn.samples.model.Sample;
import edu.yaprnn.support.swing.DialogsService;
import edu.yaprnn.training.TrainingData;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.EventQueue;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Objects;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.tree.TreePath;
import lombok.SneakyThrows;
import org.jboss.weld.environment.se.events.ContainerInitialized;

@Singleton
public class MainFrame extends JFrame {

  @Inject
  ControlsService controlsService;
  @Inject
  DialogsService dialogsService;
  @Inject
  FilesService filesService;
  @Inject
  GradientMatrixService gradientMatrixService;
  @Inject
  IconsService iconsService;
  @Inject
  MultiLayerNetworkMapper multiLayerNetworkMapper;
  @Inject
  MultiLayerNetworkTemplateMapper multiLayerNetworkTemplateMapper;
  @Inject
  NetworksControlsService networksControlsService;
  @Inject
  NetworksTreeCellEditor networksTreeCellEditor;
  @Inject
  NetworksTreeCellRenderer networksTreeCellRenderer;
  @Inject
  NetworksTreeModel networksTreeModel;
  @Inject
  OnModelNodeSelectedRouter onModelNodeSelectedRouter;
  @Inject
  OnMultiLayerNetworkTemplateSelectedRouter onMultiLayerNetworkTemplateSelectedRouter;
  @Inject
  OnMultiLayerNetworkSelectedRouter onMultiLayerNetworkSelectedRouter;
  @Inject
  OnSamplePreviewModifiedRouter onSamplePreviewModifiedRouter;
  @Inject
  OnTrainingDataSelectedRouter onTrainingDataSelectedRouter;
  @Inject
  PersistenceService persistenceService;
  @Inject
  Repository repository;
  @Inject
  SamplesService samplesService;
  @Inject
  TrainingDataMapper trainingDataMapper;

  @Inject
  Instance<ClassifyFrame> classifyFrameInstance;
  @Inject
  Instance<WeightsDetailsTabbedPane> weightsDetailsTabbedPaneInstance;
  @Inject
  Instance<NewMultiLayerNetworkPanel> newMultiLayerNetworkPanelInstance;
  @Inject
  Instance<NewMultiLayerNetworkTemplatePanel> newMultiLayerNetworkTemplatePanelInstance;
  @Inject
  Instance<RandomizeTrainingDataPanel> randomizeTrainingDataPanelInstance;
  @Inject
  Instance<SampleDetailsView> sampleDetailsViewInstance;
  @Inject
  Instance<SamplePreprocessingFrame> samplePreprocessingFrameInstance;
  @Inject
  Instance<TrainingFrame> trainingFrameInstance;

  private ClassifyFrame classifyFrame;
  private JButton addButton;
  private JButton classifyButton;
  private JButton editButton;
  private JButton randomizedTrainingDataButton;
  private JButton removeButton;
  private JButton resetButton;
  private JButton samplePreprocessingButton;
  private JButton trainButton;
  private JMenuItem saveMultiLayerNetworkMenuItem;
  private JMenuItem saveMultiLayerNetworkTemplateMenuItem;
  private JMenuItem saveTrainingDataMenuItem;
  private JTabbedPane detailsTabbedPane;
  private JTree networksTree;
  private WeightsDetailsTabbedPane weightsDetailsTabbedPane;
  private SampleDetailsView sampleDetailsView;
  private SamplePreprocessingFrame samplePreprocessingFrame;
  private TrainingFrame trainingFrame;

  @Produces
  @NetworksTree
  public JTree getNetworksTree() {
    return networksTree;
  }

  @SneakyThrows
  void start(@Observes ContainerInitialized event) {
    initializeComponents();
    EventQueue.invokeAndWait(() -> setVisible(true));
  }

  private void initializeComponents() {
    setTitle("YAPRNN");
    var fileMenu = new JMenu("File");
    var helpMenu = new JMenu("Help");

    var loadTrainingDataMenuItem = controlsService.loadMenuItem(this, "Load Training Data",
        filesService.trainingDataFileExtension(), this::loadTrainingData);
    saveTrainingDataMenuItem = controlsService.saveMenuItem(this, "Save Training Data",
        filesService.trainingDataFileExtension(), this::saveTrainingData);
    var loadMultiLayerNetworkTemplateMenuItem = controlsService.loadMenuItem(this,
        "Load Multilayer Network Template", filesService.multiLayerNetworkTemplateFileExtension(),
        this::loadMultiLayerNetworkTemplate);
    saveMultiLayerNetworkTemplateMenuItem = controlsService.saveMenuItem(this,
        "Save Multilayer Network Template", filesService.multiLayerNetworkTemplateFileExtension(),
        this::saveMultiLayerNetworkTemplate);
    var loadMultiLayerNetworkMenuItem = controlsService.loadMenuItem(this,
        "Load Multilayer Network", filesService.multiLayerNetworkFileExtension(),
        this::loadMultiLayerNetwork);
    saveMultiLayerNetworkMenuItem = controlsService.saveMenuItem(this, "Save Multilayer Network",
        filesService.multiLayerNetworkFileExtension(), this::saveMultiLayerNetwork);

    var visitWebsiteMenuItem = controlsService.actionMenuItem("Visit YAPRRN GitHub",
        iconsService.visitIcon(), this::visitWebsite);

    addButton = controlsService.actionButton("Add New", iconsService.addIcon(), this::add);
    editButton = controlsService.actionButton("Edit", iconsService.editIcon(), this::startEditing);
    removeButton = controlsService.actionButton("Remove", iconsService.removeIcon(),
        this::removeFromNetworksTree);
    samplePreprocessingButton = controlsService.actionButton(SamplePreprocessingFrame.TITLE,
        iconsService.subSampleIcon(), this::showSamplePreprocessingFrame);

    randomizedTrainingDataButton = controlsService.actionButton(RandomizeTrainingDataPanel.TITLE,
        iconsService.randomizeTrainingDataIcon(), this::addRandomizedTrainingData);
    classifyButton = controlsService.actionButton(ClassifyFrame.TITLE, iconsService.classifyIcon(),
        this::showClassifyFrame);
    trainButton = controlsService.actionButton("Train", iconsService.trainIcon(),
        this::showTrainingFrame);
    resetButton = controlsService.actionButton("Reset", iconsService.resetIcon(),
        this::resetMultiLayerNetwork);

    var importImagesMenuItem = samplesService.importImagesMenuItem(this,
        networksTreeModel::addSamples);
    var importAudioMenuItem = samplesService.importAudioMenuItem(this,
        networksTreeModel::addSamples);
    var setupDigitsScenario = controlsService.actionMenuItem("Setup digits scenario",
        iconsService.loadIcon(), this::setupDigitsScenario);

    fileMenu.add(setupDigitsScenario);
    fileMenu.add(new JSeparator());
    fileMenu.add(importImagesMenuItem);
    fileMenu.add(importAudioMenuItem);
    fileMenu.add(new JSeparator());
    fileMenu.add(loadTrainingDataMenuItem);
    fileMenu.add(saveTrainingDataMenuItem);
    fileMenu.add(new JSeparator());
    fileMenu.add(loadMultiLayerNetworkTemplateMenuItem);
    fileMenu.add(saveMultiLayerNetworkTemplateMenuItem);
    fileMenu.add(new JSeparator());
    fileMenu.add(loadMultiLayerNetworkMenuItem);
    fileMenu.add(saveMultiLayerNetworkMenuItem);

    helpMenu.add(visitWebsiteMenuItem);

    var menuBar = new JMenuBar();
    menuBar.add(fileMenu);
    menuBar.add(helpMenu);

    var toolBar = new JToolBar();
    toolBar.add(addButton);
    toolBar.add(editButton);
    toolBar.add(removeButton);
    toolBar.add(samplePreprocessingButton);
    toolBar.add(randomizedTrainingDataButton);
    toolBar.add(trainButton);
    toolBar.add(classifyButton);
    toolBar.add(resetButton);

    classifyFrame = classifyFrameInstance.get();
    networksTree = networksControlsService.networksTree(networksTreeCellEditor,
        networksTreeCellRenderer, this::setSelectedPath);
    networksTree.addKeyListener(new RemoveFromNetworksTreeKeyAdapter());
    sampleDetailsView = sampleDetailsViewInstance.get();
    samplePreprocessingFrame = samplePreprocessingFrameInstance.get();
    trainingFrame = trainingFrameInstance.get();
    weightsDetailsTabbedPane = weightsDetailsTabbedPaneInstance.get();

    detailsTabbedPane = new JTabbedPane();
    detailsTabbedPane.addTab(SampleDetailsView.TITLE, sampleDetailsView.getContent());
    detailsTabbedPane.addTab(WeightsDetailsTabbedPane.TITLE, weightsDetailsTabbedPane);

    var networksTreeScrollPane = new JScrollPane(networksTree);
    var networksTreePanel = new JPanel();
    var networksTreeGroupLayout = new GroupLayout(networksTreePanel);
    networksTreeGroupLayout.setHorizontalGroup(
        networksTreeGroupLayout.createParallelGroup().addComponent(networksTreeScrollPane));
    networksTreeGroupLayout.setVerticalGroup(
        networksTreeGroupLayout.createParallelGroup().addComponent(networksTreeScrollPane));
    networksTreePanel.setLayout(networksTreeGroupLayout);

    var contentSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, networksTreePanel,
        detailsTabbedPane);

    getContentPane().add(toolBar, BorderLayout.NORTH);
    getContentPane().add(contentSplitPane, BorderLayout.CENTER);
    setJMenuBar(menuBar);
    setDefaultCloseOperation(EXIT_ON_CLOSE);
    setEnabledOfCommands((ModelNode) null);
    setSamplePreview(onSamplePreviewModifiedRouter.current());
    pack();
    sampleDetailsView.doLayout();
  }

  private void loadTrainingData(String path) {
    networksTreeModel.add(persistenceService.loadTrainingData(path));
  }

  private void saveTrainingData(String path) {
    persistenceService.saveTrainingData(onTrainingDataSelectedRouter.getSelected(), path);
  }

  private void loadMultiLayerNetworkTemplate(String path) {
    networksTreeModel.add(persistenceService.loadMultiLayerNetworkTemplate(path));
  }

  private void saveMultiLayerNetworkTemplate(String path) {
    persistenceService.saveMultiLayerNetworkTemplate(
        onMultiLayerNetworkTemplateSelectedRouter.getSelected(), path);
  }

  private void loadMultiLayerNetwork(String path) {
    networksTreeModel.add(persistenceService.loadMultiLayerNetwork(path));
  }

  private void saveMultiLayerNetwork(String path) {
    persistenceService.saveMultiLayerNetwork(onMultiLayerNetworkSelectedRouter.getSelected(), path);
  }

  private void visitWebsite() {
    try {
      Desktop.getDesktop().browse(new URI("https://github.com/falkosch/edu.yaprnn"));
    } catch (Throwable throwable) {
      dialogsService.showError(this, "Visit code repository", throwable);
    }
  }

  private void add() {
    var selectedNode = onModelNodeSelectedRouter.getSelected();
    switch (selectedNode) {
      case TrainingDataListNode _ -> addRandomizedTrainingData();
      case MultiLayerNetworkTemplateListNode _ -> addMultiLayerNetworkTemplate();
      case MultiLayerNetworkTemplateNode node -> addLayerTemplateTo(node);
      case MultiLayerNetworkListNode _ -> addMultiLayerNetwork();
      case null, default -> {
      }
    }
  }

  private void startEditing() {
    networksTree.startEditingAtPath(networksTree.getSelectionPath());
  }

  private void removeFromNetworksTree() {
    networksTreeModel.remove(onModelNodeSelectedRouter.getSelected());
  }

  private void showSamplePreprocessingFrame() {
    samplePreprocessingFrame.setVisible(true);
  }

  private void addRandomizedTrainingData() {
    randomizeTrainingDataPanelInstance.get()
        .show(this, (parameters) -> networksTreeModel.add(
            trainingDataMapper.from(parameters, repository.getSamples())));
  }

  private void showClassifyFrame() {
    classifyFrame.setVisible(true);
  }

  private void showTrainingFrame() {
    trainingFrame.setVisible(true);
  }

  private void resetMultiLayerNetwork() {
    onMultiLayerNetworkSelectedRouter.getSelected().resetLayerWeights(gradientMatrixService);
  }

  private void setupDigitsScenario() {
    samplesService.importImages(this, networksTreeModel::addSamples,
        getResource("/digits.idx3-ubyte"), getResource("/digits.idx1-ubyte"));

    networksTreeModel.add(
        persistenceService.loadTrainingData(getResource("/digits.yaprnn-training-data")));
    networksTreeModel.add(persistenceService.loadMultiLayerNetworkTemplate(
        getResource("/digits.yaprnn-mln-template")));
    networksTreeModel.add(
        persistenceService.loadMultiLayerNetwork(getResource("/digits.yaprnn-mln")));

    networksTreeModel.add(persistenceService.loadTrainingData(
        getResource("/digits-image-from-label.yaprnn-training-data")));
    networksTreeModel.add(persistenceService.loadMultiLayerNetworkTemplate(
        getResource("/digits-image-from-label.yaprnn-mln-template")));
    networksTreeModel.add(persistenceService.loadMultiLayerNetwork(
        getResource("/digits-image-from-label.yaprnn-mln")));

    var digitInputReconstructionTrainingData = persistenceService.loadTrainingData(
        getResource("/digits-input-reconstruction.yaprnn-training-data"));
    networksTreeModel.add(digitInputReconstructionTrainingData);
    networksTreeModel.add(persistenceService.loadMultiLayerNetworkTemplate(
        getResource("/digits-input-reconstruction-ae-layers-6-bottleneck-12.yaprnn-mln-template")));
    networksTreeModel.add(persistenceService.loadMultiLayerNetwork(
        getResource("/digits-input-reconstruction-ae-layers-6-bottleneck-12.yaprnn-mln")));
    onTrainingDataSelectedRouter.setSelected(digitInputReconstructionTrainingData);

    networksTreeModel.add(persistenceService.loadMultiLayerNetworkTemplate(
        getResource("/digits-input-reconstruction-ae-layers-14-bottleneck-6.yaprnn-mln-template")));
    networksTreeModel.add(persistenceService.loadMultiLayerNetwork(
        getResource("/digits-input-reconstruction-ae-layers-14-bottleneck-6.yaprnn-mln")));
  }

  private void setSelectedPath(ModelNode selectedNode, TreePath selectedPath) {
    onModelNodeSelectedRouter.setSelected(selectedNode);
  }

  private void setEnabledOfCommands(ModelNode selected) {
    var isAllSamplesNode = selected instanceof AllSamplesListNode;
    var isSampleNode = selected instanceof SampleNode;

    var isTrainingDataListNode = selected instanceof TrainingDataListNode;
    var isTrainingDataNode = selected instanceof TrainingDataNode;
    var isSampleNameNode = selected instanceof SampleNameNode;

    var isMultiLayerNetworkTemplateListNode = selected instanceof MultiLayerNetworkTemplateListNode;
    var isMultiLayerNetworkTemplateNode = selected instanceof MultiLayerNetworkTemplateNode;
    var isLayerTemplateNode = selected instanceof LayerTemplateNode;
    var isLayerSizeNode = selected instanceof LayerSizeNode;
    var isActivationFunctionNode = selected instanceof ActivationFunctionNode;

    var isMultiLayerNetworkListNode = selected instanceof MultiLayerNetworkListNode;
    var isMultiLayerNetworkNode = selected instanceof MultiLayerNetworkNode;
    var isWeightsNode = selected instanceof MultiLayerNetworkWeightsNode;

    if (isSampleNode || isSampleNameNode) {
      detailsTabbedPane.setSelectedComponent(sampleDetailsView.getContent());
    }
    if (isWeightsNode) {
      detailsTabbedPane.setSelectedComponent(weightsDetailsTabbedPane);
    }

    randomizedTrainingDataButton.setEnabled(isAllSamplesNode | isTrainingDataListNode);
    samplePreprocessingButton.setEnabled(isAllSamplesNode | isTrainingDataNode);

    saveTrainingDataMenuItem.setEnabled(isTrainingDataNode);
    saveMultiLayerNetworkTemplateMenuItem.setEnabled(
        isMultiLayerNetworkTemplateNode | isLayerTemplateNode | isLayerSizeNode
            | isActivationFunctionNode);
    saveMultiLayerNetworkMenuItem.setEnabled(isMultiLayerNetworkNode | isWeightsNode);

    classifyButton.setEnabled(true);
    trainButton.setEnabled(true);
    resetButton.setEnabled(isMultiLayerNetworkNode);

    addButton.setEnabled(isTrainingDataListNode | isMultiLayerNetworkTemplateListNode
        | isMultiLayerNetworkTemplateNode | isMultiLayerNetworkListNode);
    editButton.setEnabled(networksTreeCellEditor.isCellEditable(selected));

    removeButton.setEnabled(
        isAllSamplesNode | isSampleNode | isTrainingDataListNode | isTrainingDataNode
            | isMultiLayerNetworkTemplateListNode | isMultiLayerNetworkTemplateNode
            | isLayerTemplateNode | isMultiLayerNetworkListNode | isMultiLayerNetworkNode);
  }

  private void setSamplePreview(@Observes OnSamplePreviewModified event) {
    setSamplePreview(event.zoom(), event.resolution(), event.overlap());
  }

  private void addMultiLayerNetworkTemplate() {
    newMultiLayerNetworkTemplatePanelInstance.get()
        .show(this, (parameters) -> networksTreeModel.add(
            multiLayerNetworkTemplateMapper.from(parameters)));
  }

  private void addLayerTemplateTo(MultiLayerNetworkTemplateNode multiLayerNetworkTemplateNode) {
    networksTreeModel.addLayerTemplateTo(multiLayerNetworkTemplateNode);
  }

  private void addMultiLayerNetwork() {
    newMultiLayerNetworkPanelInstance.get().show(this, (parameters) -> {
      var multiLayerNetwork = multiLayerNetworkMapper.toMultiLayerNetwork(parameters);
      multiLayerNetwork.resetLayerWeights(gradientMatrixService);
      networksTreeModel.add(multiLayerNetwork);
    });
  }

  private URL getResource(String src) {
    return Objects.requireNonNull(MainFrame.class.getResource(src));
  }

  private void setSamplePreview(float zoom, int resolution, float overlap) {
    sampleDetailsView.setSamplePreview(zoom, resolution, overlap);
    classifyFrame.setSamplePreview(zoom, resolution, overlap);
    samplePreprocessingFrame.setSamplePreview(zoom, resolution, overlap);
  }

  private void setEnabledOfCommands(@Observes OnModelNodeSelected event) {
    setEnabledOfCommands(event.value());
  }

  private void setSelectedSample(@Observes OnSampleSelected event) {
    setSelectedSample(event.value());
  }

  private void setSelectedSample(Sample sample) {
    sampleDetailsView.setSamplePreview(sample);
    classifyFrame.setSelectedSample(sample);
    samplePreprocessingFrame.setSelectedSample(sample);
  }

  private void setWeightsPreview(@Observes OnMultiLayerNetworkWeightsPreviewModified event) {
    setWeightsPreview(event.multiLayerNetwork(), event.weightsIndex(), event.zoom(), event.gamma());
  }

  private void setWeightsPreview(MultiLayerNetwork multiLayerNetwork, int weightsIndex, float zoom,
      float gamma) {
    weightsDetailsTabbedPane.setWeightsPreview(multiLayerNetwork, weightsIndex, zoom, gamma);
  }

  private void setSelectedMultiLayerNetwork(@Observes OnMultiLayerNetworkSelected event) {
    setSelectedMultiLayerNetwork(event.value());
  }

  private void setSelectedMultiLayerNetwork(MultiLayerNetwork multiLayerNetwork) {
    classifyFrame.setSelectedMultiLayerNetwork(multiLayerNetwork);
    trainingFrame.setSelectedMultiLayerNetwork(multiLayerNetwork);
  }

  private void setSelectedTrainingData(@Observes OnTrainingDataSelected event) {
    trainingFrame.setSelectedTrainingData(event.value());
  }

  private void prepareSelectionControls(@Observes OnRepositoryElementsChanged event) {
    var elementTypeClass = event.elementTypeClass();

    if (elementTypeClass == Sample.class) {
      prepareSampleSelectionControls(event.castList(Sample.class));
    } else if (elementTypeClass == TrainingData.class) {
      prepareTrainingDataSelectionControls(event.castList(TrainingData.class));
    } else if (elementTypeClass == MultiLayerNetwork.class) {
      prepareMultiLayerNetworkSelectionControls(event.castList(MultiLayerNetwork.class));
    }
  }

  private void prepareSampleSelectionControls(List<Sample> samples) {
    classifyFrame.prepareSampleSelectionControls(samples);
    samplePreprocessingFrame.prepareSampleSelectionControls(samples);
  }

  private void prepareTrainingDataSelectionControls(List<TrainingData> trainingData) {
    trainingFrame.prepareTrainingDataSelectionControls(trainingData);
  }

  private void prepareMultiLayerNetworkSelectionControls(List<MultiLayerNetwork> networks) {
    classifyFrame.prepareMultiLayerNetworkSelectionControls(networks);
    trainingFrame.prepareMultiLayerNetworkSelectionControls(networks);
  }

  private void removeFromSelectionControls(@Observes OnRepositoryElementsRemoved event) {
    var elementTypeClass = event.elementTypeClass();

    if (elementTypeClass == Sample.class) {
      removeSamplesFromSelectionControls(event.castList(Sample.class));
    } else if (elementTypeClass == TrainingData.class) {
      removeTrainingDataFromSelectionControls(event.castList(TrainingData.class));
    } else if (elementTypeClass == MultiLayerNetwork.class) {
      removeMultiLayerNetworksFromSelectionControls(event.castList(MultiLayerNetwork.class));
    }
  }

  private void removeSamplesFromSelectionControls(List<Sample> removed) {
    classifyFrame.removeSamplesFromSelectionControls(removed);
    samplePreprocessingFrame.removeSamplesFromSelectionControls(removed);
  }

  private void removeTrainingDataFromSelectionControls(List<TrainingData> removed) {
    trainingFrame.removeTrainingDataFromSelectionControls(removed);
  }

  private void removeMultiLayerNetworksFromSelectionControls(List<MultiLayerNetwork> removed) {
    classifyFrame.removeMultiLayerNetworksFromSelectionControls(removed);
    trainingFrame.removeMultiLayerNetworksFromSelectionControls(removed);
  }

  private class RemoveFromNetworksTreeKeyAdapter extends KeyAdapter {

    @Override
    public void keyTyped(KeyEvent e) {
      if (e.getKeyChar() == KeyEvent.VK_DELETE) {
        removeFromNetworksTree();
        return;
      }
      super.keyTyped(e);
    }
  }
}
