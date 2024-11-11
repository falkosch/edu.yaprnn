package edu.yaprnn.gui.views;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;

import edu.yaprnn.events.OnMultiLayerNetworkWeightsPreviewModifiedRouter;
import edu.yaprnn.gui.services.NetworksControlsService;
import edu.yaprnn.gui.services.VisualizationService;
import edu.yaprnn.gui.services.ZoomControlsService;
import edu.yaprnn.networks.MultiLayerNetwork;
import edu.yaprnn.support.swing.ImagePanel;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import java.util.Objects;
import java.util.stream.IntStream;
import javax.swing.GroupLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

public class WeightsDetailsTabbedPane extends JTabbedPane {

  public static final String TITLE = "Weights Details";

  @Inject
  NetworksControlsService networksControlsService;
  @Inject
  OnMultiLayerNetworkWeightsPreviewModifiedRouter onMultiLayerNetworkWeightsPreviewModifiedRouter;
  @Inject
  VisualizationService visualizationService;
  @Inject
  ZoomControlsService zoomControlsService;

  private JTable weightsTable;
  private ImagePanel weightsImagePanel;

  public void setWeightsPreview(MultiLayerNetwork multiLayerNetwork, int weightsIndex, float zoom,
      float gamma) {
    if (!Objects.nonNull(multiLayerNetwork) || weightsIndex < 0) {
      weightsTable.setModel(networksControlsService.emptyTableModel());
      weightsImagePanel.setImage(null);
      return;
    }

    var weights = multiLayerNetwork.getLayerWeights()[weightsIndex];
    var outputSize = multiLayerNetwork.getLayerSizes()[weightsIndex + 1];

    weightsImagePanel.setImage(visualizationService.fromWeights(weights, outputSize, zoom, gamma));

    var inputSizeWithBias = visualizationService.inputSizeWithBias(weights, outputSize);
    var columnNames = IntStream.range(0, 1 + outputSize)
        .mapToObj(i -> i > 0 ? "out[%d]".formatted(i - 1) : "")
        .toArray();
    var tableData = new Object[inputSizeWithBias][columnNames.length];
    for (int row = 0, w = 0; row < inputSizeWithBias; row++) {
      tableData[row][0] = "in[%d]".formatted(row);
      for (var col = 1; col <= outputSize; col++, w++) {
        tableData[row][col] = visualizationService.formatTableValue(weights[w]);
      }
    }

    weightsTable.setModel(new DefaultTableModel(tableData, columnNames));
  }

  @PostConstruct
  void initialize() {
    var zoomWeightsLabel = new JLabel("Zoom");
    var gammaLabel = new JLabel("Gamma");

    var weightsPanel = new JPanel();
    weightsTable = networksControlsService.valuesTable();
    var weightsTableScrollPane = new JScrollPane(weightsTable);
    addTab("Weights Image", weightsPanel);
    addTab("Weights Table", weightsTableScrollPane);

    var gammaSlider = networksControlsService.gammaSlider(
        onMultiLayerNetworkWeightsPreviewModifiedRouter::setGamma);
    var zoomWeightsComboBox = zoomControlsService.zoomComboBox(
        onMultiLayerNetworkWeightsPreviewModifiedRouter::setZoom);

    weightsImagePanel = new ImagePanel();
    var weightsImageScrollPane = new JScrollPane(weightsImagePanel);

    var weightsGroupLayout = new GroupLayout(weightsPanel);
    weightsGroupLayout.setHorizontalGroup(weightsGroupLayout.createParallelGroup()
        .addGroup(weightsGroupLayout.createSequentialGroup()
            .addGroup(weightsGroupLayout.createParallelGroup()
                .addComponent(zoomWeightsLabel)
                .addComponent(zoomWeightsComboBox, PREFERRED_SIZE, DEFAULT_SIZE, 100))
            .addGroup(weightsGroupLayout.createParallelGroup()
                .addComponent(gammaLabel)
                .addComponent(gammaSlider, PREFERRED_SIZE, DEFAULT_SIZE, 300)))
        .addComponent(weightsImageScrollPane));
    weightsGroupLayout.setVerticalGroup(weightsGroupLayout.createSequentialGroup()
        .addGroup(weightsGroupLayout.createParallelGroup()
            .addGroup(weightsGroupLayout.createSequentialGroup()
                .addComponent(zoomWeightsLabel)
                .addComponent(zoomWeightsComboBox, PREFERRED_SIZE, DEFAULT_SIZE, 28))
            .addGroup(weightsGroupLayout.createSequentialGroup()
                .addComponent(gammaLabel)
                .addComponent(gammaSlider, PREFERRED_SIZE, DEFAULT_SIZE, 28)))
        .addComponent(weightsImageScrollPane));
    weightsGroupLayout.setAutoCreateContainerGaps(true);
    weightsGroupLayout.setAutoCreateGaps(true);
    weightsPanel.setLayout(weightsGroupLayout);
    weightsPanel.setOpaque(false);
  }
}
