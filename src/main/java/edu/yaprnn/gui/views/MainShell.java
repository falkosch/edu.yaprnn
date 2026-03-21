package edu.yaprnn.gui.views;

import edu.yaprnn.events.OnModelNodeSelectedRouter;
import edu.yaprnn.events.OnMultiLayerNetworkSelected;
import edu.yaprnn.events.OnMultiLayerNetworkSelectedRouter;
import edu.yaprnn.events.OnMultiLayerNetworkTemplateSelectedRouter;
import edu.yaprnn.events.OnRepositoryElementsChanged;
import edu.yaprnn.events.OnRepositoryElementsRemoved;
import edu.yaprnn.events.OnSampleSelected;
import edu.yaprnn.events.OnTrainingDataSelected;
import edu.yaprnn.events.OnTrainingDataSelectedRouter;
import edu.yaprnn.gui.model.AllSamplesListNode;
import edu.yaprnn.gui.model.MultiLayerNetworkListNode;
import edu.yaprnn.gui.model.MultiLayerNetworkTemplateListNode;
import edu.yaprnn.gui.model.NetworksTreeModel;
import edu.yaprnn.gui.model.NetworksTreeRefreshService;
import edu.yaprnn.gui.model.NetworksTreeViewerFactory;
import edu.yaprnn.gui.model.TrainingDataListNode;
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
import edu.yaprnn.gui.services.ActivationFunctionControlsService;
import edu.yaprnn.gui.services.DataSelectorControlsService;
import edu.yaprnn.gui.services.LossFunctionControlsService;
import edu.yaprnn.gui.services.PersistenceService;
import edu.yaprnn.gui.services.SamplesService;
import edu.yaprnn.gui.services.SwtIconsService;
import edu.yaprnn.gui.services.VisualizationService;
import edu.yaprnn.gui.views.mappings.MultiLayerNetworkMapper;
import edu.yaprnn.gui.views.mappings.MultiLayerNetworkTemplateMapper;
import edu.yaprnn.gui.views.mappings.NewMultiLayerNetworkParameters;
import edu.yaprnn.gui.views.mappings.NewMultiLayerNetworkTemplateParameters;
import edu.yaprnn.gui.views.mappings.RandomizeTrainingDataParameters;
import edu.yaprnn.gui.views.mappings.TrainingDataMapper;
import edu.yaprnn.model.Repository;
import edu.yaprnn.model.ScenarioService;
import edu.yaprnn.networks.GradientMatrixService;
import edu.yaprnn.networks.activation.ActivationFunction;
import edu.yaprnn.networks.loss.LossFunction;
import edu.yaprnn.networks.templates.MultiLayerNetworkTemplate;
import edu.yaprnn.samples.AudiosImportService;
import edu.yaprnn.samples.ImagesImportService;
import edu.yaprnn.training.selectors.DataSelector;
import edu.yaprnn.networks.MultiLayerNetwork;
import edu.yaprnn.samples.model.Sample;
import edu.yaprnn.training.TrainingData;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.List;
import java.awt.Desktop;
import java.net.URI;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import lombok.extern.java.Log;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

/**
 * Main application shell (SWT replacement for MainFrame).
 */
@Log
@Singleton
public class MainShell {

  // Action icon resource paths
  private static final String ICON_ADD = "/edu/yaprnn/gui/views/action/srip-add.png";
  private static final String ICON_EDIT = "/edu/yaprnn/gui/views/action/srip-edit.png";
  private static final String ICON_REMOVE = "/edu/yaprnn/gui/views/action/srip-remove.png";
  private static final String ICON_SUBSAMPLE = "/edu/yaprnn/gui/views/action/srip-sub-sample.png";
  private static final String ICON_RANDOMIZE = "/edu/yaprnn/gui/views/action/buandesign-randomize.png";
  private static final String ICON_TRAIN = "/edu/yaprnn/gui/views/action/srip-train.png";
  private static final String ICON_CLASSIFY = "/edu/yaprnn/gui/views/action/srip-classify.png";
  private static final String ICON_RESET = "/edu/yaprnn/gui/views/action/srip-reset.png";
  private static final String ICON_OPEN = "/edu/yaprnn/gui/views/action/srip-open.png";
  private static final String ICON_SAVE = "/edu/yaprnn/gui/views/action/srip-save.png";
  private static final String ICON_IMPORT_IMAGES = "/edu/yaprnn/gui/views/action/srip-import-images.png";
  private static final String ICON_IMPORT_AUDIO = "/edu/yaprnn/gui/views/action/srip-import-audio.png";
  private static final String ICON_VISIT = "/edu/yaprnn/gui/views/action/srip-visit.png";

  private static final AtomicInteger trainingDataCounter = new AtomicInteger();
  private static final AtomicInteger templateCounter = new AtomicInteger();
  private static final AtomicInteger networkCounter = new AtomicInteger();

  @Inject
  Display display;
  @Inject
  Shell shell;
  @Inject
  ActivationFunctionControlsService activationFunctionControlsService;
  @Inject
  AudiosImportService audiosImportService;
  @Inject
  DataSelectorControlsService dataSelectorControlsService;
  @Inject
  GradientMatrixService gradientMatrixService;
  @Inject
  ImagesImportService imagesImportService;
  @Inject
  LossFunctionControlsService lossFunctionControlsService;
  @Inject
  MultiLayerNetworkMapper multiLayerNetworkMapper;
  @Inject
  MultiLayerNetworkTemplateMapper multiLayerNetworkTemplateMapper;
  @Inject
  NetworksTreeModel networksTreeModel;
  @Inject
  NetworksTreeViewerFactory treeViewerFactory;
  @Inject
  NetworksTreeRefreshService treeRefreshService;
  @Inject
  OnModelNodeSelectedRouter onModelNodeSelectedRouter;
  @Inject
  OnMultiLayerNetworkSelectedRouter onMultiLayerNetworkSelectedRouter;
  @Inject
  OnMultiLayerNetworkTemplateSelectedRouter onMultiLayerNetworkTemplateSelectedRouter;
  @Inject
  OnTrainingDataSelectedRouter onTrainingDataSelectedRouter;
  @Inject
  PersistenceService persistenceService;
  @Inject
  Repository repository;
  @Inject
  ScenarioService scenarioService;
  @Inject
  SamplesService samplesService;
  @Inject
  SwtIconsService swtIconsService;
  @Inject
  TrainingDataMapper trainingDataMapper;
  @Inject
  VisualizationService visualizationService;
  @Inject
  Instance<TrainingShell> trainingShellInstance;
  @Inject
  Instance<ClassifyShell> classifyShellInstance;
  @Inject
  Instance<SamplePreprocessingShell> samplePreprocessingShellInstance;

  private TrainingShell trainingShell;
  private ClassifyShell classifyShell;
  private SamplePreprocessingShell samplePreprocessingShell;

  private SashForm sashForm;
  private Composite treeComposite;
  private TreeViewer treeViewer;
  private CTabFolder detailsTabFolder;
  private SwtSampleDetailsComposite sampleDetailsComposite;
  private SwtWeightsDetailsComposite weightsDetailsComposite;
  private ModelNode selectedNode;

  // Toolbar items that need enable/disable management
  private ToolItem addItem;
  private ToolItem editItem;
  private ToolItem removeItem;
  private ToolItem preprocessItem;
  private ToolItem randomizeItem;
  private ToolItem resetItem;

  // Menu items that need enable/disable management
  private MenuItem saveTrainingDataMenuItem;
  private MenuItem saveTemplateMenuItem;
  private MenuItem saveNetworkMenuItem;

  public void initialize() {
    shell.setText("YAPRNN");
    shell.setLayout(new GridLayout(1, false));

    createMenuBar();
    createToolBar();
    createContent();

    setEnabledOfCommands(null);
    shell.setSize(1024, 768);
  }

  public void openAndRunEventLoop() {
    shell.open();
    while (!shell.isDisposed()) {
      if (!display.readAndDispatch()) {
        display.sleep();
      }
    }
    display.dispose();
  }

  public Shell getShell() {
    return shell;
  }

  public TreeViewer getTreeViewer() {
    return treeViewer;
  }

  public Composite getTreeComposite() {
    return treeComposite;
  }

  public CTabFolder getDetailsTabFolder() {
    return detailsTabFolder;
  }

  private void onTreeSelectionChanged(ModelNode node, Object element) {
    this.selectedNode = node;
    onModelNodeSelectedRouter.setSelected(node);
    setEnabledOfCommands(node);
    updateDetailPanels(node);
  }

  // --- Menu bar ---

  private void createMenuBar() {
    var menuBar = new Menu(shell, SWT.BAR);

    createFileMenu(menuBar);
    createHelpMenu(menuBar);

    shell.setMenuBar(menuBar);
  }

  private void createFileMenu(Menu menuBar) {
    var fileMenuItem = new MenuItem(menuBar, SWT.CASCADE);
    fileMenuItem.setText("&File");
    var fileMenu = new Menu(shell, SWT.DROP_DOWN);
    fileMenuItem.setMenu(fileMenu);

    addMenuItem(fileMenu, "Setup digits scenario", icon(ICON_OPEN), this::setupDigitsScenario);
    new MenuItem(fileMenu, SWT.SEPARATOR);

    addMenuItem(fileMenu, "Import images...", icon(ICON_IMPORT_IMAGES), this::importImages);
    addMenuItem(fileMenu, "Import audio...", icon(ICON_IMPORT_AUDIO), this::importAudio);
    new MenuItem(fileMenu, SWT.SEPARATOR);

    addMenuItem(fileMenu, "Load Training Data...", icon(ICON_OPEN),
        () -> loadFile("Training Data", "*.yaprnn-training-data", this::loadTrainingData));
    saveTrainingDataMenuItem = addMenuItem(fileMenu, "Save Training Data...", icon(ICON_SAVE),
        () -> saveFile("Training Data", "*.yaprnn-training-data", this::saveTrainingData));
    new MenuItem(fileMenu, SWT.SEPARATOR);

    addMenuItem(fileMenu, "Load Network Template...", icon(ICON_OPEN),
        () -> loadFile("Network Template", "*.yaprnn-mln-template",
            this::loadMultiLayerNetworkTemplate));
    saveTemplateMenuItem = addMenuItem(fileMenu, "Save Network Template...", icon(ICON_SAVE),
        () -> saveFile("Network Template", "*.yaprnn-mln-template",
            this::saveMultiLayerNetworkTemplate));
    new MenuItem(fileMenu, SWT.SEPARATOR);

    addMenuItem(fileMenu, "Load Network...", icon(ICON_OPEN),
        () -> loadFile("Network", "*.yaprnn-mln", this::loadMultiLayerNetwork));
    saveNetworkMenuItem = addMenuItem(fileMenu, "Save Network...", icon(ICON_SAVE),
        () -> saveFile("Network", "*.yaprnn-mln", this::saveMultiLayerNetwork));
  }

  private void createHelpMenu(Menu menuBar) {
    var helpMenuItem = new MenuItem(menuBar, SWT.CASCADE);
    helpMenuItem.setText("&Help");
    var helpMenu = new Menu(shell, SWT.DROP_DOWN);
    helpMenuItem.setMenu(helpMenu);

    addMenuItem(helpMenu, "Visit YAPRNN GitHub", icon(ICON_VISIT), this::visitWebsite);
  }

  private MenuItem addMenuItem(Menu menu, String text, Image image, Runnable action) {
    var item = new MenuItem(menu, SWT.PUSH);
    item.setText(text);
    if (image != null) {
      item.setImage(image);
    }
    item.addListener(SWT.Selection, e -> action.run());
    return item;
  }

  // --- Toolbar ---

  private void createToolBar() {
    var toolBar = new ToolBar(shell, SWT.HORIZONTAL | SWT.FLAT);
    toolBar.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

    addItem = addToolItem(toolBar, "Add New", icon(ICON_ADD), this::add);
    editItem = addToolItem(toolBar, "Edit", icon(ICON_EDIT), this::startEditing);
    removeItem = addToolItem(toolBar, "Remove", icon(ICON_REMOVE), this::removeFromTree);
    new ToolItem(toolBar, SWT.SEPARATOR);

    preprocessItem = addToolItem(toolBar, "Preprocess", icon(ICON_SUBSAMPLE),
        this::openSamplePreprocessingShell);
    randomizeItem = addToolItem(toolBar, "Randomize Training Data", icon(ICON_RANDOMIZE),
        this::addRandomizedTrainingData);
    new ToolItem(toolBar, SWT.SEPARATOR);

    addToolItem(toolBar, "Train", icon(ICON_TRAIN), this::openTrainingShell);
    addToolItem(toolBar, "Classify", icon(ICON_CLASSIFY), this::openClassifyShell);
    resetItem = addToolItem(toolBar, "Reset", icon(ICON_RESET), this::resetMultiLayerNetwork);
  }

  private ToolItem addToolItem(ToolBar toolBar, String text, Image image, Runnable action) {
    var item = new ToolItem(toolBar, SWT.PUSH);
    item.setText(text);
    if (image != null) {
      item.setImage(image);
    }
    item.addListener(SWT.Selection, e -> action.run());
    return item;
  }

  private Image icon(String resourcePath) {
    try {
      return swtIconsService.getImage(resourcePath);
    } catch (Exception e) {
      log.log(Level.WARNING, "Failed to load icon: " + resourcePath, e);
      return null;
    }
  }

  // --- Content ---

  private void createContent() {
    sashForm = new SashForm(shell, SWT.HORIZONTAL);
    sashForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

    treeComposite = new Composite(sashForm, SWT.BORDER);
    treeComposite.setLayout(new FillLayout());
    treeViewer = treeViewerFactory.createTreeViewer(treeComposite, this::onTreeSelectionChanged);
    treeRefreshService.setTreeViewer(treeViewer);

    detailsTabFolder = new CTabFolder(sashForm, SWT.BORDER | SWT.TOP);

    var sampleTab = new CTabItem(detailsTabFolder, SWT.NONE);
    sampleTab.setText("Sample Details");
    sampleDetailsComposite = new SwtSampleDetailsComposite(detailsTabFolder, display,
        samplesService);
    sampleTab.setControl(sampleDetailsComposite);

    var weightsTab = new CTabItem(detailsTabFolder, SWT.NONE);
    weightsTab.setText("Weights Details");
    weightsDetailsComposite = new SwtWeightsDetailsComposite(detailsTabFolder, display,
        visualizationService);
    weightsTab.setControl(weightsDetailsComposite);

    detailsTabFolder.setSelection(0);
    sashForm.setWeights(30, 70);
  }

  private void updateDetailPanels(ModelNode node) {
    if (node instanceof SampleNode sampleNode) {
      var sample = sampleNode.getSampleSupplier().get();
      if (sample != null) {
        sampleDetailsComposite.setSample(sample);
      } else {
        sampleDetailsComposite.clear();
      }
      detailsTabFolder.setSelection(0);
    } else if (node instanceof SampleNameNode) {
      sampleDetailsComposite.clear();
      detailsTabFolder.setSelection(0);
    } else if (node instanceof MultiLayerNetworkWeightsNode weightsNode) {
      var network = weightsNode.getMultiLayerNetworkSupplier().get();
      var index = weightsNode.getWeightsIndexSupplier().getAsInt();
      if (network != null) {
        weightsDetailsComposite.setWeightsPreview(network, index);
      } else {
        weightsDetailsComposite.clear();
      }
      detailsTabFolder.setSelection(1);
    }
  }

  // --- CDI event observers for preselection propagation ---

  private void onSampleSelected(@Observes OnSampleSelected event) {
    if (classifyShell != null) {
      classifyShell.setSelectedSample(event.value());
    }
    if (samplePreprocessingShell != null) {
      samplePreprocessingShell.setSelectedSample(event.value());
    }
  }

  private void onMultiLayerNetworkSelected(@Observes OnMultiLayerNetworkSelected event) {
    if (classifyShell != null) {
      classifyShell.setSelectedMultiLayerNetwork(event.value());
    }
    if (trainingShell != null) {
      trainingShell.setSelectedMultiLayerNetwork(event.value());
    }
  }

  private void onTrainingDataSelected(@Observes OnTrainingDataSelected event) {
    if (trainingShell != null) {
      trainingShell.setSelectedTrainingData(event.value());
    }
  }

  private void onRepositoryElementsChanged(@Observes OnRepositoryElementsChanged event) {
    var elementTypeClass = event.elementTypeClass();
    if (elementTypeClass == Sample.class) {
      var samples = event.castList(Sample.class);
      if (classifyShell != null) {
        classifyShell.prepareSampleSelectionControls(samples);
      }
      if (samplePreprocessingShell != null) {
        samplePreprocessingShell.prepareSampleSelectionControls(samples);
      }
    } else if (elementTypeClass == TrainingData.class) {
      if (trainingShell != null) {
        trainingShell.prepareTrainingDataSelectionControls(event.castList(TrainingData.class));
      }
    } else if (elementTypeClass == MultiLayerNetwork.class) {
      var networks = event.castList(MultiLayerNetwork.class);
      if (classifyShell != null) {
        classifyShell.prepareMultiLayerNetworkSelectionControls(networks);
      }
      if (trainingShell != null) {
        trainingShell.prepareMultiLayerNetworkSelectionControls(networks);
      }
    }
  }

  private void onRepositoryElementsRemoved(@Observes OnRepositoryElementsRemoved event) {
    var elementTypeClass = event.elementTypeClass();
    if (elementTypeClass == Sample.class) {
      var removed = event.castList(Sample.class);
      if (classifyShell != null) {
        classifyShell.removeSamplesFromSelectionControls(removed);
      }
      if (samplePreprocessingShell != null) {
        samplePreprocessingShell.removeSamplesFromSelectionControls(removed);
      }
    } else if (elementTypeClass == TrainingData.class) {
      if (trainingShell != null) {
        trainingShell.removeTrainingDataFromSelectionControls(event.castList(TrainingData.class));
      }
    } else if (elementTypeClass == MultiLayerNetwork.class) {
      var removed = event.castList(MultiLayerNetwork.class);
      if (classifyShell != null) {
        classifyShell.removeMultiLayerNetworksFromSelectionControls(removed);
      }
      if (trainingShell != null) {
        trainingShell.removeMultiLayerNetworksFromSelectionControls(removed);
      }
    }
  }

  // --- Enable/disable commands based on selection ---

  private void setEnabledOfCommands(ModelNode selected) {
    var isAllSamplesNode = selected instanceof AllSamplesListNode;
    var isSampleNode = selected instanceof SampleNode;
    var isTrainingDataListNode = selected instanceof TrainingDataListNode;
    var isTrainingDataNode = selected instanceof TrainingDataNode;
    var isTemplateListNode = selected instanceof MultiLayerNetworkTemplateListNode;
    var isTemplateNode = selected instanceof MultiLayerNetworkTemplateNode;
    var isLayerTemplateNode = selected instanceof LayerTemplateNode;
    var isLayerSizeNode = selected instanceof LayerSizeNode;
    var isActivationFunctionNode = selected instanceof ActivationFunctionNode;
    var isNetworkListNode = selected instanceof MultiLayerNetworkListNode;
    var isNetworkNode = selected instanceof MultiLayerNetworkNode;
    var isWeightsNode = selected instanceof MultiLayerNetworkWeightsNode;

    preprocessItem.setEnabled(isAllSamplesNode || isTrainingDataNode);
    randomizeItem.setEnabled(isAllSamplesNode || isTrainingDataListNode);
    resetItem.setEnabled(isNetworkNode);

    addItem.setEnabled(
        isTrainingDataListNode || isTemplateListNode || isTemplateNode || isNetworkListNode);
    editItem.setEnabled(isTemplateNode || isNetworkNode || isTrainingDataNode || isLayerSizeNode
        || isActivationFunctionNode);
    removeItem.setEnabled(
        isAllSamplesNode || isSampleNode || isTrainingDataListNode || isTrainingDataNode
            || isTemplateListNode || isTemplateNode || isLayerTemplateNode || isNetworkListNode
            || isNetworkNode);

    saveTrainingDataMenuItem.setEnabled(isTrainingDataNode);
    saveTemplateMenuItem.setEnabled(
        isTemplateNode || isLayerTemplateNode || isLayerSizeNode || isActivationFunctionNode);
    saveNetworkMenuItem.setEnabled(isNetworkNode || isWeightsNode);
  }

  // --- Actions ---

  private void add() {
    switch (selectedNode) {
      case TrainingDataListNode _ -> addRandomizedTrainingData();
      case MultiLayerNetworkTemplateListNode _ -> showNewTemplateDialog();
      case MultiLayerNetworkTemplateNode node -> networksTreeModel.addLayerTemplateTo(node);
      case MultiLayerNetworkListNode _ -> showNewNetworkDialog();
      case null, default -> {
      }
    }
  }

  private void startEditing() {
    if (selectedNode != null) {
      treeViewer.editElement(selectedNode, 0);
    }
  }

  private void removeFromTree() {
    networksTreeModel.remove(selectedNode);
  }

  private void addRandomizedTrainingData() {
    var dialogShell = new Shell(shell, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
    dialogShell.setText("Randomized Training Data");
    dialogShell.setLayout(new GridLayout(2, false));

    new Label(dialogShell, SWT.NONE).setText("Name:");
    var nameText = new Text(dialogShell, SWT.BORDER);
    nameText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
    nameText.setText("Randomized Training Data %d".formatted(
        trainingDataCounter.incrementAndGet()));

    new Label(dialogShell, SWT.NONE).setText("Training size (%):");
    var trainingSizeSpinner = new Spinner(dialogShell, SWT.BORDER);
    trainingSizeSpinner.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
    trainingSizeSpinner.setValues(80, 1, 100, 0, 1, 10);

    new Label(dialogShell, SWT.NONE).setText("Dev test size (%):");
    var devTestSizeSpinner = new Spinner(dialogShell, SWT.BORDER);
    devTestSizeSpinner.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
    devTestSizeSpinner.setValues(20, 1, 100, 0, 1, 10);

    new Label(dialogShell, SWT.NONE).setText("Data selector:");
    var dataSelectorViewer = new ComboViewer(dialogShell, SWT.READ_ONLY);
    dataSelectorViewer.getCombo().setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
    dataSelectorViewer.setContentProvider(ArrayContentProvider.getInstance());
    dataSelectorViewer.setLabelProvider(new LabelProvider() {
      @Override
      public String getText(Object element) {
        return element.getClass().getSimpleName();
      }
    });
    var selectors = dataSelectorControlsService.dataSelectors();
    dataSelectorViewer.setInput(selectors);
    if (selectors.length > 0) {
      dataSelectorViewer.setSelection(new StructuredSelection(selectors[0]));
    }

    var buttonComposite = new Composite(dialogShell, SWT.NONE);
    buttonComposite.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 2, 1));
    buttonComposite.setLayout(new GridLayout(2, true));
    var okButton = new org.eclipse.swt.widgets.Button(buttonComposite, SWT.PUSH);
    okButton.setText("OK");
    okButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
    var cancelButton = new org.eclipse.swt.widgets.Button(buttonComposite, SWT.PUSH);
    cancelButton.setText("Cancel");
    cancelButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

    var result = new boolean[]{false};
    okButton.addListener(SWT.Selection, e -> {
      result[0] = true;
      dialogShell.setVisible(false);
    });
    cancelButton.addListener(SWT.Selection, e -> dialogShell.setVisible(false));
    dialogShell.setDefaultButton(okButton);

    dialogShell.pack();
    dialogShell.open();

    while (dialogShell.isVisible()) {
      if (!display.readAndDispatch()) {
        display.sleep();
      }
    }

    try {
      if (result[0]) {
        var name = nameText.getText();
        var trainingPct = trainingSizeSpinner.getSelection() / 100f;
        var devTestPct = devTestSizeSpinner.getSelection() / 100f;
        var sel = dataSelectorViewer.getStructuredSelection();
        var selector = sel.isEmpty() ? selectors[0] : (DataSelector) sel.getFirstElement();

        var params = new RandomizeTrainingDataParameters(name, trainingPct, devTestPct,
            selector);
        networksTreeModel.add(trainingDataMapper.from(params, repository.getSamples()));
      }
    } finally {
      dialogShell.dispose();
    }
  }

  private void showNewTemplateDialog() {
    var dialogShell = new Shell(shell, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
    dialogShell.setText("New Multilayer Network Template");
    dialogShell.setLayout(new GridLayout(2, false));

    new Label(dialogShell, SWT.NONE).setText("Name:");
    var nameText = new Text(dialogShell, SWT.BORDER);
    nameText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
    nameText.setText("Template %d".formatted(templateCounter.incrementAndGet()));

    new Label(dialogShell, SWT.NONE).setText("Layers count:");
    var layersCountSpinner = new Spinner(dialogShell, SWT.BORDER);
    layersCountSpinner.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
    layersCountSpinner.setValues(3, 2, 100, 0, 1, 5);

    new Label(dialogShell, SWT.NONE).setText("Layer size:");
    var layerSizeSpinner = new Spinner(dialogShell, SWT.BORDER);
    layerSizeSpinner.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
    layerSizeSpinner.setValues(100, 1, 10000, 0, 10, 100);

    new Label(dialogShell, SWT.NONE).setText("Bias:");
    var biasSpinner = new Spinner(dialogShell, SWT.BORDER);
    biasSpinner.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
    biasSpinner.setValues(1000, -10000, 10000, 3, 1, 10);

    new Label(dialogShell, SWT.NONE).setText("Activation function:");
    var afViewer = new ComboViewer(dialogShell, SWT.READ_ONLY);
    afViewer.getCombo().setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
    afViewer.setContentProvider(ArrayContentProvider.getInstance());
    afViewer.setLabelProvider(new LabelProvider());
    var activationFunctions = activationFunctionControlsService.activationFunctions();
    afViewer.setInput(activationFunctions);
    if (activationFunctions.length > 0) {
      afViewer.setSelection(new StructuredSelection(activationFunctions[0]));
    }

    new Label(dialogShell, SWT.NONE).setText("Loss function:");
    var lfViewer = new ComboViewer(dialogShell, SWT.READ_ONLY);
    lfViewer.getCombo().setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
    lfViewer.setContentProvider(ArrayContentProvider.getInstance());
    lfViewer.setLabelProvider(new LabelProvider());
    var lossFunctions = lossFunctionControlsService.lossFunctions();
    lfViewer.setInput(lossFunctions);
    if (lossFunctions.length > 0) {
      lfViewer.setSelection(new StructuredSelection(lossFunctions[0]));
    }

    var buttonComposite = new Composite(dialogShell, SWT.NONE);
    buttonComposite.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 2, 1));
    buttonComposite.setLayout(new GridLayout(2, true));
    var okButton = new org.eclipse.swt.widgets.Button(buttonComposite, SWT.PUSH);
    okButton.setText("OK");
    okButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
    var cancelButton = new org.eclipse.swt.widgets.Button(buttonComposite, SWT.PUSH);
    cancelButton.setText("Cancel");
    cancelButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

    var result = new boolean[]{false};
    okButton.addListener(SWT.Selection, e -> {
      result[0] = true;
      dialogShell.setVisible(false);
    });
    cancelButton.addListener(SWT.Selection, e -> dialogShell.setVisible(false));
    dialogShell.setDefaultButton(okButton);

    dialogShell.pack();
    dialogShell.open();

    while (dialogShell.isVisible()) {
      if (!display.readAndDispatch()) {
        display.sleep();
      }
    }

    try {
      if (result[0]) {
        var afSel = afViewer.getStructuredSelection();
        var lfSel = lfViewer.getStructuredSelection();
        var params = new NewMultiLayerNetworkTemplateParameters(nameText.getText(),
            layersCountSpinner.getSelection(), layerSizeSpinner.getSelection(),
            biasSpinner.getSelection() / 1000f,
            afSel.isEmpty() ? activationFunctions[0]
                : (ActivationFunction) afSel.getFirstElement(),
            lfSel.isEmpty() ? lossFunctions[0] : (LossFunction) lfSel.getFirstElement());
        networksTreeModel.add(multiLayerNetworkTemplateMapper.from(params));
      }
    } finally {
      dialogShell.dispose();
    }
  }

  private void showNewNetworkDialog() {
    var templates = repository.getMultiLayerNetworkTemplates();
    if (templates.isEmpty()) {
      MessageDialog.openWarning(shell, "New Network",
          "Create a network template first.");
      return;
    }

    var dialogShell = new Shell(shell, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
    dialogShell.setText("New Multilayer Network");
    dialogShell.setLayout(new GridLayout(2, false));

    new Label(dialogShell, SWT.NONE).setText("Name:");
    var nameText = new Text(dialogShell, SWT.BORDER);
    nameText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
    nameText.setText("Network %d".formatted(networkCounter.incrementAndGet()));

    new Label(dialogShell, SWT.NONE).setText("Template:");
    var templateViewer = new ComboViewer(dialogShell, SWT.READ_ONLY);
    templateViewer.getCombo().setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
    templateViewer.setContentProvider(ArrayContentProvider.getInstance());
    templateViewer.setLabelProvider(new LabelProvider() {
      @Override
      public String getText(Object element) {
        return element instanceof MultiLayerNetworkTemplate t ? t.getName() : element.toString();
      }
    });
    templateViewer.setInput(templates.toArray(MultiLayerNetworkTemplate[]::new));
    templateViewer.setSelection(new StructuredSelection(templates.getFirst()));

    var buttonComposite = new Composite(dialogShell, SWT.NONE);
    buttonComposite.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 2, 1));
    buttonComposite.setLayout(new GridLayout(2, true));
    var okButton = new org.eclipse.swt.widgets.Button(buttonComposite, SWT.PUSH);
    okButton.setText("OK");
    okButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
    var cancelButton = new org.eclipse.swt.widgets.Button(buttonComposite, SWT.PUSH);
    cancelButton.setText("Cancel");
    cancelButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

    var result = new boolean[]{false};
    okButton.addListener(SWT.Selection, e -> {
      result[0] = true;
      dialogShell.setVisible(false);
    });
    cancelButton.addListener(SWT.Selection, e -> dialogShell.setVisible(false));
    dialogShell.setDefaultButton(okButton);

    dialogShell.pack();
    dialogShell.open();

    while (dialogShell.isVisible()) {
      if (!display.readAndDispatch()) {
        display.sleep();
      }
    }

    try {
      if (result[0]) {
        var templateSel = templateViewer.getStructuredSelection();
        var template = (MultiLayerNetworkTemplate) templateSel.getFirstElement();
        var params = new NewMultiLayerNetworkParameters(nameText.getText(), template);
        var network = multiLayerNetworkMapper.toMultiLayerNetwork(params);
        network.resetLayerWeights(gradientMatrixService);
        networksTreeModel.add(network);
      }
    } finally {
      dialogShell.dispose();
    }
  }

  private void resetMultiLayerNetwork() {
    var selected = onMultiLayerNetworkSelectedRouter.getSelected();
    if (selected != null) {
      selected.resetLayerWeights(gradientMatrixService);
    }
  }

  private void setupDigitsScenario() {
    var scenario = scenarioService.loadDigitsScenario();
    networksTreeModel.addSamples(scenario.samples());
    scenario.trainingData().forEach(networksTreeModel::add);
    scenario.templates().forEach(networksTreeModel::add);
    scenario.networks().forEach(networksTreeModel::add);
    onTrainingDataSelectedRouter.setSelected(scenario.defaultSelectedTrainingData());
  }

  private void importImages() {
    var imagesDialog = new FileDialog(shell, SWT.OPEN);
    imagesDialog.setText("Select Images Package (idx3-ubyte)");
    imagesDialog.setFilterExtensions(new String[]{"*.idx3-ubyte", "*.*"});
    imagesDialog.setFilterNames(
        new String[]{"Images package (*.idx3-ubyte)", "All files (*.*)"});
    var imagesPath = imagesDialog.open();
    if (imagesPath == null) {
      return;
    }

    var labelsDialog = new FileDialog(shell, SWT.OPEN);
    labelsDialog.setText("Select Labels Package (idx1-ubyte)");
    labelsDialog.setFilterExtensions(new String[]{"*.idx1-ubyte", "*.*"});
    labelsDialog.setFilterNames(
        new String[]{"Labels package (*.idx1-ubyte)", "All files (*.*)"});
    labelsDialog.setFilterPath(imagesDialog.getFilterPath());
    var labelsPath = labelsDialog.open();
    if (labelsPath == null) {
      return;
    }

    try {
      var samples = imagesImportService.fromImagesLabelsPackage(imagesPath, labelsPath);
      networksTreeModel.addSamples(samples);
    } catch (Exception e) {
      log.log(Level.WARNING, "Failed to import images", e);
      MessageDialog.openError(shell, "Import Images", e.getMessage());
    }
  }

  private void importAudio() {
    var dialog = new FileDialog(shell, SWT.OPEN | SWT.MULTI);
    dialog.setText("Import Audio");
    dialog.setFilterExtensions(new String[]{"*.aiff", "*.*"});
    dialog.setFilterNames(new String[]{"Audio (*.aiff)", "All files (*.*)"});
    if (dialog.open() != null) {
      var filterPath = dialog.getFilterPath();
      var files = java.util.Arrays.stream(dialog.getFileNames())
          .map(name -> new java.io.File(filterPath, name))
          .toArray(java.io.File[]::new);
      try {
        networksTreeModel.addSamples(audiosImportService.fromAiff(files));
      } catch (Exception e) {
        log.log(Level.WARNING, "Failed to import audio", e);
        MessageDialog.openError(shell, "Import Audio", e.getMessage());
      }
    }
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

  private void loadFile(String title, String extension,
      java.util.function.Consumer<String> loader) {
    var dialog = new FileDialog(shell, SWT.OPEN);
    dialog.setText("Load " + title);
    dialog.setFilterExtensions(new String[]{extension});
    var path = dialog.open();
    if (path != null) {
      loader.accept(path);
    }
  }

  private void saveFile(String title, String extension,
      java.util.function.Consumer<String> saver) {
    var dialog = new FileDialog(shell, SWT.SAVE);
    dialog.setText("Save " + title);
    dialog.setFilterExtensions(new String[]{extension});
    var path = dialog.open();
    if (path != null) {
      saver.accept(path);
    }
  }

  private void visitWebsite() {
    try {
      Desktop.getDesktop().browse(new URI("https://github.com/falkosch/edu.yaprnn"));
    } catch (Exception e) {
      log.log(Level.WARNING, "Failed to open browser", e);
    }
  }

  // --- Secondary shells ---

  private void openTrainingShell() {
    if (trainingShell == null) {
      trainingShell = trainingShellInstance.get();
    }
    trainingShell.open();
  }

  private void openClassifyShell() {
    if (classifyShell == null) {
      classifyShell = classifyShellInstance.get();
    }
    classifyShell.open();
  }

  private void openSamplePreprocessingShell() {
    if (samplePreprocessingShell == null) {
      samplePreprocessingShell = samplePreprocessingShellInstance.get();
    }
    samplePreprocessingShell.open();
  }
}
