package edu.yaprnn.gui.services;

import edu.yaprnn.events.OnMultiLayerNetworkWeightsPreviewModifiedRouter;
import edu.yaprnn.gui.model.NetworksTreeModel;
import edu.yaprnn.gui.model.nodes.ModelNode;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.function.BiConsumer;
import java.util.function.DoubleConsumer;
import javax.swing.BoundedRangeModel;
import javax.swing.DefaultBoundedRangeModel;
import javax.swing.JSlider;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.SpinnerNumberModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.tree.TreeCellEditor;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

@Singleton
public class NetworksControlsService {

  @Inject
  NetworksTreeModel networksTreeModel;

  @Inject
  OnMultiLayerNetworkWeightsPreviewModifiedRouter onMultiLayerNetworkWeightsPreviewModifiedRouter;

  public JTree networksTree(TreeCellEditor treeCellEditor, TreeCellRenderer treeCellRenderer,
      BiConsumer<ModelNode, TreePath> consumer) {
    return networksTree(treeCellEditor, treeCellRenderer, consumer,
        () -> consumer.accept(null, null));
  }

  public JTree networksTree(TreeCellEditor treeCellEditor, TreeCellRenderer treeCellRenderer,
      BiConsumer<ModelNode, TreePath> onSelect, Runnable onUnselect) {
    var tree = new JTree(networksTreeModel);
    tree.addTreeSelectionListener(event -> {
      if (event.isAddedPath()) {
        var treePath = event.getPath();
        if (treePath.getLastPathComponent() instanceof ModelNode modelNode) {
          onSelect.accept(modelNode, treePath);
          return;
        }
      }
      onUnselect.run();
    });
    tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
    tree.setCellEditor(treeCellEditor);
    tree.setCellRenderer(treeCellRenderer);
    tree.setEditable(true);
    tree.setLargeModel(true);
    tree.setRootVisible(false);
    tree.setShowsRootHandles(true);
    return tree;
  }

  public JTable valuesTable() {
    var table = new JTable(emptyTableModel());
    table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    table.setRowSelectionAllowed(false);
    return table;
  }

  public TableModel emptyTableModel() {
    return new DefaultTableModel(new Object[][]{}, new Object[]{});
  }

  public JSlider gammaSlider(DoubleConsumer consumer) {
    return gammaSlider(gammaBoundedRangeModel(), consumer);
  }

  public JSlider gammaSlider(BoundedRangeModel model, DoubleConsumer consumer) {
    var slider = new JSlider(model);
    slider.addChangeListener(_ -> consumer.accept(toGammaValue(model, slider.getValue())));
    slider.setMajorTickSpacing(model.getExtent());
    slider.setOpaque(false);
    slider.setPaintTicks(true);
    return slider;
  }

  public BoundedRangeModel gammaBoundedRangeModel() {
    var model = new DefaultBoundedRangeModel(-1, 0, -1000, 0);
    model.setValue(
        fromGammaValue(model, onMultiLayerNetworkWeightsPreviewModifiedRouter.getGamma()));
    return model;
  }

  private float toGammaValue(BoundedRangeModel model, int sliderValue) {
    return sliderValue / gammaRange(model);
  }

  private int fromGammaValue(BoundedRangeModel model, float gammaValue) {
    return (int) (gammaValue * gammaRange(model));
  }

  private float gammaRange(BoundedRangeModel model) {
    return 0.5f * (model.getMaximum() - model.getMinimum());
  }

  public SpinnerNumberModel trainingSizeSpinnerNumberModel() {
    return new SpinnerNumberModel(60, 1, 100, 1);
  }

  public SpinnerNumberModel devTestSizeSpinnerNumberModel() {
    return new SpinnerNumberModel(20, 1, 100, 1);
  }

  public SpinnerNumberModel layersCountSpinnerNumberModel() {
    return new SpinnerNumberModel(10, 2, Integer.MAX_VALUE, 1);
  }

  public SpinnerNumberModel layerSizeSpinnerNumberModel() {
    return new SpinnerNumberModel(10, 2, Integer.MAX_VALUE, 1);
  }
}
