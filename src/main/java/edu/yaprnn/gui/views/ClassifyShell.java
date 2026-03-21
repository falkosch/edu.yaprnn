package edu.yaprnn.gui.views;

import edu.yaprnn.gui.services.DataSelectorControlsService;
import edu.yaprnn.gui.services.SamplesService;
import edu.yaprnn.gui.services.SwtIconsService;
import edu.yaprnn.gui.services.VisualizationService;
import edu.yaprnn.model.Repository;
import edu.yaprnn.networks.ClassificationService;
import edu.yaprnn.networks.ClassificationService.ClassificationResult;
import edu.yaprnn.networks.Layer;
import edu.yaprnn.networks.MultiLayerNetwork;
import edu.yaprnn.samples.model.ImageSample;
import edu.yaprnn.samples.model.Sample;
import edu.yaprnn.support.swt.ImageCanvas;
import edu.yaprnn.support.swt.SwtImages;
import edu.yaprnn.training.selectors.DataSelector;
import jakarta.inject.Inject;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.stream.IntStream;
import lombok.extern.java.Log;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Scale;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

/**
 * SWT shell for classification. Replaces the Swing ClassifyFrame.
 */
@Log
public class ClassifyShell {

  static final String TITLE = "Classify";

  @Inject
  Display display;
  @Inject
  ClassificationService classificationService;
  @Inject
  DataSelectorControlsService dataSelectorControlsService;
  @Inject
  Repository repository;
  @Inject
  SamplesService samplesService;
  @Inject
  SwtIconsService swtIconsService;
  @Inject
  VisualizationService visualizationService;

  private Shell shell;
  private ToolItem classifyButton;
  private ComboViewer samplesViewer;
  private ComboViewer networksViewer;
  private ComboViewer dataSelectorViewer;
  private Table layersTable;
  private SwtSampleDetailsComposite sampleDetailsComposite;
  private ImageCanvas outputReconstructionCanvas;
  private Spinner zoomSpinner;
  private Scale gammaScale;
  private Image outputReconstructionImage;

  // Cached result for output reconstruction re-render on zoom/gamma change
  private float[] lastOutput;
  private int lastOutputWidth;

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
    shell.setSize(900, 700);

    createToolBar();
    createContent();
    syncView();
  }

  private void createToolBar() {
    var toolBar = new ToolBar(shell, SWT.HORIZONTAL | SWT.FLAT);
    toolBar.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

    classifyButton = new ToolItem(toolBar, SWT.PUSH);
    classifyButton.setText(TITLE);
    classifyButton.setImage(icon("/edu/yaprnn/gui/views/action/srip-classify.png"));
    classifyButton.addListener(SWT.Selection, e -> classify());

    new ToolItem(toolBar, SWT.SEPARATOR);

    var samplesItem = new ToolItem(toolBar, SWT.SEPARATOR);
    samplesViewer = createComboViewer(toolBar);
    samplesViewer.setInput(repository.getSamples().toArray(Sample[]::new));
    samplesViewer.addSelectionChangedListener(e -> onSampleSelectionChanged());
    samplesItem.setControl(samplesViewer.getCombo());
    samplesItem.setWidth(200);

    var networksItem = new ToolItem(toolBar, SWT.SEPARATOR);
    networksViewer = createComboViewer(toolBar);
    networksViewer.setInput(
        repository.getMultiLayerNetworks().toArray(MultiLayerNetwork[]::new));
    networksViewer.addSelectionChangedListener(e -> syncView());
    networksItem.setControl(networksViewer.getCombo());
    networksItem.setWidth(200);

    var dataSelectorItem = new ToolItem(toolBar, SWT.SEPARATOR);
    dataSelectorViewer = createComboViewer(toolBar);
    dataSelectorViewer.setInput(dataSelectorControlsService.dataSelectors());
    dataSelectorItem.setControl(dataSelectorViewer.getCombo());
    dataSelectorItem.setWidth(200);
    var selectors = dataSelectorControlsService.dataSelectors();
    if (selectors.length > 0) {
      dataSelectorViewer.setSelection(new StructuredSelection(selectors[0]));
    }
  }

  private ComboViewer createComboViewer(Composite parent) {
    var viewer = new ComboViewer(parent, SWT.READ_ONLY);
    viewer.setContentProvider(ArrayContentProvider.getInstance());
    viewer.setLabelProvider(new LabelProvider() {
      @Override
      public String getText(Object element) {
        return switch (element) {
          case Sample s -> s.getName();
          case MultiLayerNetwork n -> n.getName();
          case DataSelector ds -> ds.getClass().getSimpleName();
          default -> element.toString();
        };
      }
    });
    return viewer;
  }

  private void createContent() {
    var tabFolder = new CTabFolder(shell, SWT.BORDER | SWT.TOP);
    tabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

    // Sample Details tab
    var sampleTab = new CTabItem(tabFolder, SWT.NONE);
    sampleTab.setText("Sample Details");
    sampleDetailsComposite = new SwtSampleDetailsComposite(tabFolder, display, samplesService);
    sampleTab.setControl(sampleDetailsComposite);

    // Layers tab with classification results table
    var layersTab = new CTabItem(tabFolder, SWT.NONE);
    layersTab.setText("Layers");
    var layersComposite = new Composite(tabFolder, SWT.NONE);
    layersComposite.setLayout(new FillLayout());
    layersTable = new Table(layersComposite,
        SWT.BORDER | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL);
    layersTable.setHeaderVisible(true);
    layersTable.setLinesVisible(true);
    layersTab.setControl(layersComposite);

    // Output Reconstruction tab
    var outputTab = new CTabItem(tabFolder, SWT.NONE);
    outputTab.setText("Output Reconstruction");
    outputTab.setControl(createOutputReconstructionComposite(tabFolder));

    tabFolder.setSelection(0);
  }

  private Composite createOutputReconstructionComposite(Composite parent) {
    var composite = new Composite(parent, SWT.NONE);
    composite.setLayout(new GridLayout(1, false));

    // Controls bar
    var controlsComposite = new Composite(composite, SWT.NONE);
    controlsComposite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
    controlsComposite.setLayout(new GridLayout(4, false));

    new Label(controlsComposite, SWT.NONE).setText("Zoom:");
    zoomSpinner = new Spinner(controlsComposite, SWT.BORDER);
    zoomSpinner.setValues(4, 1, 20, 0, 1, 5);
    zoomSpinner.addListener(SWT.Selection, e -> updateOutputReconstruction());

    new Label(controlsComposite, SWT.NONE).setText("Gamma:");
    gammaScale = new Scale(controlsComposite, SWT.HORIZONTAL);
    gammaScale.setMinimum(0);
    gammaScale.setMaximum(1000);
    gammaScale.setSelection(500);
    gammaScale.setPageIncrement(100);
    gammaScale.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
    gammaScale.addListener(SWT.Selection, e -> updateOutputReconstruction());

    // Image area
    var scrolled = new ScrolledComposite(composite,
        SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
    scrolled.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
    scrolled.setExpandHorizontal(true);
    scrolled.setExpandVertical(true);
    outputReconstructionCanvas = new ImageCanvas(scrolled, SWT.NONE);
    scrolled.setContent(outputReconstructionCanvas);

    return composite;
  }

  private void classify() {
    var sample = getSelectedSample();
    var network = getSelectedNetwork();
    var selector = getSelectedDataSelector();
    if (sample == null || network == null || selector == null) {
      return;
    }

    classifyButton.setEnabled(false);
    Thread.ofVirtual().name("classify").start(() -> {
      try {
        var result = classificationService.classify(network, sample, selector);

        // Compute output reconstruction image if applicable
        int outputWidth = 0;
        if (sample instanceof ImageSample imageSample) {
          outputWidth = selector.getOutputWidth(imageSample);
        }
        var finalOutputWidth = outputWidth;

        display.asyncExec(() -> {
          if (shell.isDisposed()) {
            return;
          }
          updateLayersTable(result.layers(), result.labels(), result.output());

          // Cache for output reconstruction
          lastOutput = result.output();
          lastOutputWidth = finalOutputWidth;
          updateOutputReconstruction();

          classifyButton.setEnabled(true);
        });
      } catch (Exception e) {
        log.log(Level.WARNING, "Classification failed", e);
        display.asyncExec(() -> {
          if (!shell.isDisposed()) {
            classifyButton.setEnabled(true);
          }
        });
      }
    });
  }

  private void updateLayersTable(Layer[] layers, String[] labels, float[] output) {
    layersTable.setRedraw(false);
    try {
      layersTable.removeAll();
      while (layersTable.getColumnCount() > 0) {
        layersTable.getColumns()[0].dispose();
      }

      // Build columns: Index | v[0] | h[0] | v[1] | h[1] | ... | Output | Label
      var columnsCount = 3 + layers.length * 2;
      var columnNames = IntStream.range(0, columnsCount).mapToObj(c -> {
        if (c == 0) {
          return "Index";
        }
        if (c == columnsCount - 1) {
          return "Label";
        }
        if (c == columnsCount - 2) {
          return "Output";
        }
        return "%s[%d]".formatted((c - 1) % 2 == 0 ? "v" : "h", (c - 1) / 2);
      }).toArray(String[]::new);

      for (var name : columnNames) {
        var col = new TableColumn(layersTable, SWT.NONE);
        col.setText(name);
        col.setWidth(80);
      }

      // Determine row count (max of all arrays)
      var layersSizes = Arrays.stream(layers)
          .flatMapToInt(layer -> IntStream.of(layer.v().length, layer.h().length));
      var rowCount = IntStream.concat(IntStream.of(labels != null ? labels.length : 0),
          layersSizes).max().orElse(0);

      // Fill rows
      for (var row = 0; row < rowCount; row++) {
        var item = new TableItem(layersTable, SWT.NONE);
        item.setText(0, "[%d]".formatted(row));
        item.setText(columnsCount - 1,
            visualizationService.valueAtOrDefault(labels, row, ""));
        item.setText(columnsCount - 2,
            visualizationService.valueAtOrDefault(output, row));

        for (int i = 0, col = 1; i < layers.length; i++, col += 2) {
          var layer = layers[i];
          item.setText(col, visualizationService.valueAtOrDefault(layer.v(), row));
          item.setText(col + 1, visualizationService.valueAtOrDefault(layer.h(), row));
        }
      }
    } finally {
      layersTable.setRedraw(true);
    }
  }

  private void updateOutputReconstruction() {
    if (lastOutput == null || lastOutputWidth <= 0) {
      return;
    }

    var output = lastOutput;
    var outputWidth = lastOutputWidth;
    var zoom = (float) zoomSpinner.getSelection();
    var gamma = (gammaScale.getSelection() - 1000) / 1000f;

    Thread.ofVirtual().name("output-reconstruction").start(() -> {
      try {
        var awtImage = visualizationService.fromOutput(output, outputWidth, zoom, gamma);
        var swtData = toSwtImageData(awtImage);

        display.asyncExec(() -> {
          if (shell.isDisposed()) {
            return;
          }
          disposeOutputImage();
          if (swtData != null) {
            outputReconstructionImage = new Image(display, swtData);
            outputReconstructionCanvas.setImage(outputReconstructionImage);
          }
        });
      } catch (Exception e) {
        log.log(Level.WARNING, "Output reconstruction failed", e);
      }
    });
  }

  private org.eclipse.swt.graphics.ImageData toSwtImageData(java.awt.Image awtImage) {
    if (awtImage == null) {
      return null;
    }
    BufferedImage buffered;
    if (awtImage instanceof BufferedImage bi) {
      buffered = bi;
    } else {
      var w = awtImage.getWidth(null);
      var h = awtImage.getHeight(null);
      buffered = new BufferedImage(Math.max(w, 1), Math.max(h, 1),
          BufferedImage.TYPE_INT_RGB);
      var g = buffered.createGraphics();
      g.drawImage(awtImage, 0, 0, null);
      g.dispose();
    }
    var data = SwtImages.toSwtImageData(buffered);
    buffered.flush();
    awtImage.flush();
    return data;
  }

  private void disposeOutputImage() {
    if (outputReconstructionImage != null && !outputReconstructionImage.isDisposed()) {
      outputReconstructionImage.dispose();
      outputReconstructionImage = null;
    }
  }

  private void onSampleSelectionChanged() {
    var sample = getSelectedSample();
    sampleDetailsComposite.setSample(sample);
    syncView();
  }

  private void syncView() {
    var hasSample = getSelectedSample() != null;
    var hasNetwork = getSelectedNetwork() != null;
    classifyButton.setEnabled(hasSample && hasNetwork);

    shell.setText("%s: %s, %s".formatted(TITLE,
        hasSample ? getSelectedSample().getName() : "\u2014",
        hasNetwork ? getSelectedNetwork().getName() : "\u2014"));
  }

  private Sample getSelectedSample() {
    var sel = samplesViewer.getStructuredSelection();
    return sel.isEmpty() ? null : (Sample) sel.getFirstElement();
  }

  private MultiLayerNetwork getSelectedNetwork() {
    var sel = networksViewer.getStructuredSelection();
    return sel.isEmpty() ? null : (MultiLayerNetwork) sel.getFirstElement();
  }

  private DataSelector getSelectedDataSelector() {
    var sel = dataSelectorViewer.getStructuredSelection();
    return sel.isEmpty() ? null : (DataSelector) sel.getFirstElement();
  }

  public void setSelectedSample(Sample sample) {
    if (Objects.nonNull(sample) && shell != null && !shell.isDisposed()) {
      samplesViewer.setSelection(new StructuredSelection(sample));
    }
  }

  public void setSelectedMultiLayerNetwork(MultiLayerNetwork network) {
    if (Objects.nonNull(network) && shell != null && !shell.isDisposed()) {
      networksViewer.setSelection(new StructuredSelection(network));
    }
  }

  public void prepareSampleSelectionControls(List<Sample> samples) {
    if (shell != null && !shell.isDisposed()) {
      samplesViewer.setInput(samples.toArray(Sample[]::new));
    }
  }

  public void prepareMultiLayerNetworkSelectionControls(List<MultiLayerNetwork> networks) {
    if (shell != null && !shell.isDisposed()) {
      networksViewer.setInput(networks.toArray(MultiLayerNetwork[]::new));
    }
  }

  public void removeSamplesFromSelectionControls(List<Sample> removed) {
    if (shell == null || shell.isDisposed()) {
      return;
    }
    var input = (Sample[]) samplesViewer.getInput();
    if (input != null) {
      var filtered = Arrays.stream(input)
          .filter(s -> !removed.contains(s))
          .toArray(Sample[]::new);
      samplesViewer.setInput(filtered);
    }
  }

  public void removeMultiLayerNetworksFromSelectionControls(List<MultiLayerNetwork> removed) {
    if (shell == null || shell.isDisposed()) {
      return;
    }
    var input = (MultiLayerNetwork[]) networksViewer.getInput();
    if (input != null) {
      var filtered = Arrays.stream(input)
          .filter(n -> !removed.contains(n))
          .toArray(MultiLayerNetwork[]::new);
      networksViewer.setInput(filtered);
    }
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
