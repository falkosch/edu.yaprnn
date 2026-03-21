package edu.yaprnn.gui.views;

import edu.yaprnn.gui.services.VisualizationService;
import edu.yaprnn.networks.MultiLayerNetwork;
import edu.yaprnn.support.swt.ImageCanvas;
import edu.yaprnn.support.swt.SwtImages;
import java.awt.image.BufferedImage;
import java.util.logging.Level;
import java.util.stream.IntStream;
import lombok.extern.java.Log;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Scale;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

/**
 * SWT composite for displaying weights details: heatmap image and weights table. Replaces the
 * Swing WeightsDetailsTabbedPane.
 */
@Log
public class SwtWeightsDetailsComposite extends Composite {

  private final Display display;
  private final VisualizationService visualizationService;

  private ImageCanvas weightsCanvas;
  private Table weightsTable;
  private Spinner zoomSpinner;
  private Scale gammaScale;

  private MultiLayerNetwork network;
  private int weightsIndex = -1;
  private Image weightsImage;

  public SwtWeightsDetailsComposite(Composite parent, Display display,
      VisualizationService visualizationService) {
    super(parent, SWT.NONE);
    this.display = display;
    this.visualizationService = visualizationService;
    setLayout(new GridLayout(1, false));
    createContent();
  }

  private void createContent() {
    // Controls bar
    var controlsComposite = new Composite(this, SWT.NONE);
    controlsComposite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
    controlsComposite.setLayout(new GridLayout(4, false));

    new Label(controlsComposite, SWT.NONE).setText("Zoom:");
    zoomSpinner = new Spinner(controlsComposite, SWT.BORDER);
    zoomSpinner.setValues(4, 1, 20, 0, 1, 5);
    zoomSpinner.addListener(SWT.Selection, e -> updatePreview());

    new Label(controlsComposite, SWT.NONE).setText("Gamma:");
    gammaScale = new Scale(controlsComposite, SWT.HORIZONTAL);
    gammaScale.setMinimum(0);
    gammaScale.setMaximum(1000);
    gammaScale.setSelection(500);
    gammaScale.setIncrement(1);
    gammaScale.setPageIncrement(100);
    gammaScale.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
    gammaScale.addListener(SWT.Selection, e -> updatePreview());

    // Main content split: image on top, table on bottom
    var sash = new SashForm(this, SWT.VERTICAL);
    sash.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

    var imageScroll = new ScrolledComposite(sash,
        SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
    imageScroll.setExpandHorizontal(true);
    imageScroll.setExpandVertical(true);
    weightsCanvas = new ImageCanvas(imageScroll, SWT.NONE);
    imageScroll.setContent(weightsCanvas);

    weightsTable = new Table(sash, SWT.BORDER | SWT.FULL_SELECTION | SWT.V_SCROLL | SWT.H_SCROLL);
    weightsTable.setHeaderVisible(true);
    weightsTable.setLinesVisible(true);

    sash.setWeights(50, 50);
  }

  public void setWeightsPreview(MultiLayerNetwork network, int weightsIndex) {
    this.network = network;
    this.weightsIndex = weightsIndex;
    updatePreview();
  }

  public void clear() {
    this.network = null;
    this.weightsIndex = -1;
    disposeImage();
    weightsCanvas.setImage(null);
    clearTable();
  }

  private void updatePreview() {
    if (network == null || weightsIndex < 0) {
      clear();
      return;
    }

    var currentNetwork = network;
    var currentIndex = weightsIndex;
    var zoom = zoomSpinner.getSelection();
    // Scale 0..1000 maps to gamma -1.0..0.0
    var gamma = (gammaScale.getSelection() - 1000) / 1000f;

    Thread.ofVirtual().name("weights-preview").start(() -> {
      try {
        var weights = currentNetwork.getLayerWeights()[currentIndex];
        var outputSize = currentNetwork.getLayerSizes()[currentIndex + 1];

        var awtImage = visualizationService.fromWeights(weights, outputSize, zoom, gamma);
        var swtData = toSwtImageData(awtImage);

        var inputSizeWithBias = visualizationService.inputSizeWithBias(weights, outputSize);
        var columnNames = IntStream.range(0, 1 + outputSize)
            .mapToObj(i -> i > 0 ? "out[%d]".formatted(i - 1) : "")
            .toArray(String[]::new);
        var tableData = new String[inputSizeWithBias][columnNames.length];
        for (int row = 0, w = 0; row < inputSizeWithBias; row++) {
          tableData[row][0] = "in[%d]".formatted(row);
          for (var col = 1; col <= outputSize; col++, w++) {
            tableData[row][col] = visualizationService.formatTableValue(weights[w]);
          }
        }

        display.asyncExec(() -> {
          if (isDisposed()) {
            return;
          }
          disposeImage();

          if (swtData != null) {
            weightsImage = new Image(display, swtData);
            weightsCanvas.setImage(weightsImage);
          }

          populateTable(columnNames, tableData);
        });
      } catch (Exception e) {
        log.log(Level.WARNING, "Failed to update weights preview", e);
      }
    });
  }

  private org.eclipse.swt.graphics.ImageData toSwtImageData(java.awt.Image awtImage) {
    if (awtImage == null) {
      return null;
    }
    var buffered = toBufferedImage(awtImage);
    var data = SwtImages.toSwtImageData(buffered);
    buffered.flush();
    awtImage.flush();
    return data;
  }

  private BufferedImage toBufferedImage(java.awt.Image awtImage) {
    if (awtImage instanceof BufferedImage bi) {
      return bi;
    }
    var w = awtImage.getWidth(null);
    var h = awtImage.getHeight(null);
    var bi = new BufferedImage(Math.max(w, 1), Math.max(h, 1), BufferedImage.TYPE_INT_RGB);
    var g = bi.createGraphics();
    g.drawImage(awtImage, 0, 0, null);
    g.dispose();
    return bi;
  }

  private void clearTable() {
    weightsTable.removeAll();
    for (var col : weightsTable.getColumns()) {
      col.dispose();
    }
  }

  private void populateTable(String[] columnNames, String[][] tableData) {
    clearTable();

    for (var name : columnNames) {
      var col = new TableColumn(weightsTable, SWT.NONE);
      col.setText(name);
      col.setWidth(80);
    }

    for (var row : tableData) {
      var item = new TableItem(weightsTable, SWT.NONE);
      item.setText(row);
    }
  }

  private void disposeImage() {
    if (weightsImage != null && !weightsImage.isDisposed()) {
      weightsImage.dispose();
      weightsImage = null;
    }
  }

  @Override
  public void dispose() {
    disposeImage();
    super.dispose();
  }
}
