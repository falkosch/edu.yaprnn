package edu.yaprnn.gui.views;

import edu.yaprnn.gui.model.NetworksTreeModel;
import edu.yaprnn.gui.services.SamplesService;
import edu.yaprnn.gui.services.SwtIconsService;
import edu.yaprnn.model.Repository;
import edu.yaprnn.samples.model.Sample;
import jakarta.inject.Inject;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import lombok.extern.java.Log;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

/**
 * SWT shell for sample preprocessing. Replaces the Swing SamplePreprocessingFrame.
 */
@Log
public class SamplePreprocessingShell {

  static final String TITLE = "Sample Preprocessing";

  @Inject
  Display display;
  @Inject
  NetworksTreeModel networksTreeModel;
  @Inject
  Repository repository;
  @Inject
  SamplesService samplesService;
  @Inject
  SwtIconsService swtIconsService;

  private Shell shell;
  private ComboViewer samplesViewer;
  private SwtSampleDetailsComposite sampleDetailsComposite;

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
    shell.setSize(700, 500);

    createToolBar();
    createContent();
  }

  private void createToolBar() {
    var toolBar = new ToolBar(shell, SWT.HORIZONTAL | SWT.FLAT);
    toolBar.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

    var processButton = new ToolItem(toolBar, SWT.PUSH);
    processButton.setText("Process");
    processButton.setImage(icon("/edu/yaprnn/gui/views/action/srip-sub-sample.png"));
    processButton.addListener(SWT.Selection, e -> process());

    new ToolItem(toolBar, SWT.SEPARATOR);

    var samplesItem = new ToolItem(toolBar, SWT.SEPARATOR);
    samplesViewer = new ComboViewer(toolBar, SWT.READ_ONLY);
    samplesViewer.setContentProvider(ArrayContentProvider.getInstance());
    samplesViewer.setLabelProvider(new LabelProvider() {
      @Override
      public String getText(Object element) {
        return element instanceof Sample s ? s.getName() : element.toString();
      }
    });
    samplesViewer.setInput(repository.getSamples().toArray(Sample[]::new));
    samplesViewer.addSelectionChangedListener(e -> onSampleSelectionChanged());
    samplesItem.setControl(samplesViewer.getCombo());
    samplesItem.setWidth(200);
  }

  private void createContent() {
    sampleDetailsComposite = new SwtSampleDetailsComposite(shell, display, samplesService);
    sampleDetailsComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
  }

  private void onSampleSelectionChanged() {
    var sel = samplesViewer.getStructuredSelection();
    var sample = sel.isEmpty() ? null : (Sample) sel.getFirstElement();
    if (sample != null) {
      sampleDetailsComposite.setSample(sample);
    } else {
      sampleDetailsComposite.clear();
    }
  }

  private void process() {
    var resolution = sampleDetailsComposite.getResolution();
    var overlap = sampleDetailsComposite.getOverlap();

    var originalSamples = List.copyOf(repository.getSamples());
    var processedSamples = originalSamples.stream()
        .map(sample -> samplesService.subSample(sample, resolution, overlap))
        .toList();

    networksTreeModel.removeSamples(originalSamples);
    networksTreeModel.addSamples(processedSamples);
  }

  public void setSelectedSample(Sample sample) {
    if (Objects.nonNull(sample) && shell != null && !shell.isDisposed()) {
      samplesViewer.setSelection(new StructuredSelection(sample));
    }
  }

  public void prepareSampleSelectionControls(List<Sample> samples) {
    if (shell != null && !shell.isDisposed()) {
      samplesViewer.setInput(samples.toArray(Sample[]::new));
    }
  }

  public void removeSamplesFromSelectionControls(List<Sample> removed) {
    if (shell == null || shell.isDisposed()) {
      return;
    }
    var input = (Sample[]) samplesViewer.getInput();
    if (input != null) {
      var filtered = java.util.Arrays.stream(input)
          .filter(s -> !removed.contains(s))
          .toArray(Sample[]::new);
      samplesViewer.setInput(filtered);
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
