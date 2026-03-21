package edu.yaprnn.gui.model;

import edu.yaprnn.gui.model.nodes.ActivationFunctionNode;
import edu.yaprnn.gui.model.nodes.BiasNode;
import edu.yaprnn.gui.model.nodes.DataSelectorNode;
import edu.yaprnn.gui.model.nodes.DefaultNode;
import edu.yaprnn.gui.model.nodes.LayerSizeNode;
import edu.yaprnn.gui.model.nodes.LossFunctionNode;
import edu.yaprnn.gui.model.nodes.ModelNode;
import edu.yaprnn.gui.model.nodes.MultiLayerNetworkNode;
import edu.yaprnn.gui.model.nodes.MultiLayerNetworkTemplateNode;
import edu.yaprnn.gui.model.nodes.TrainingDataNode;
import edu.yaprnn.gui.services.ActivationFunctionControlsService;
import edu.yaprnn.gui.services.DataSelectorControlsService;
import edu.yaprnn.gui.services.LossFunctionControlsService;
import edu.yaprnn.networks.activation.ActivationFunction;
import edu.yaprnn.networks.loss.LossFunction;
import edu.yaprnn.training.selectors.DataSelector;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.function.BiConsumer;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationEvent;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationStrategy;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.TreeViewerEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

/**
 * Factory for creating the main JFace TreeViewer with content/label providers and editing support.
 */
@Singleton
public class NetworksTreeViewerFactory {

  @Inject
  NetworksTreeContentProvider contentProvider;
  @Inject
  NetworksTreeLabelProvider labelProvider;
  @Inject
  ActivationFunctionControlsService activationFunctionControlsService;
  @Inject
  LossFunctionControlsService lossFunctionControlsService;
  @Inject
  DataSelectorControlsService dataSelectorControlsService;

  public TreeViewer createTreeViewer(Composite parent,
      BiConsumer<ModelNode, Object> selectionAction) {
    var viewer = new TreeViewer(parent,
        SWT.BORDER | SWT.SINGLE | SWT.V_SCROLL | SWT.H_SCROLL | SWT.FULL_SELECTION);
    viewer.setContentProvider(contentProvider);

    var activationStrategy = new ColumnViewerEditorActivationStrategy(viewer) {
      @Override
      protected boolean isEditorActivationEvent(ColumnViewerEditorActivationEvent event) {
        return event.eventType == ColumnViewerEditorActivationEvent.TRAVERSAL
            || event.eventType == ColumnViewerEditorActivationEvent.MOUSE_DOUBLE_CLICK_SELECTION
            || event.eventType == ColumnViewerEditorActivationEvent.PROGRAMMATIC
            || (event.eventType == ColumnViewerEditorActivationEvent.KEY_PRESSED
            && event.keyCode == SWT.F2);
      }
    };
    TreeViewerEditor.create(viewer, activationStrategy, TreeViewerEditor.DEFAULT);

    var column = new TreeViewerColumn(viewer, SWT.NONE);
    column.getColumn().setWidth(400);
    column.setLabelProvider(labelProvider);
    column.setEditingSupport(new TreeEditingSupport(viewer));

    viewer.addSelectionChangedListener(event -> {
      var selection = event.getStructuredSelection();
      var selected = selection.isEmpty() ? null : selection.getFirstElement();
      selectionAction.accept(selected instanceof ModelNode node ? node : null, selected);
    });

    viewer.setInput("root");
    viewer.expandToLevel(2);

    return viewer;
  }

  /**
   * Unified EditingSupport that dispatches to the right cell editor based on node type.
   */
  private class TreeEditingSupport extends EditingSupport {

    private final TreeViewer viewer;
    private final ActivationFunction[] activationFunctions;
    private final String[] activationFunctionLabels;
    private final LossFunction[] lossFunctions;
    private final String[] lossFunctionLabels;
    private final DataSelector[] dataSelectors;
    private final String[] dataSelectorLabels;

    TreeEditingSupport(TreeViewer viewer) {
      super(viewer);
      this.viewer = viewer;
      this.activationFunctions = activationFunctionControlsService.activationFunctions();
      this.activationFunctionLabels = toLabels(activationFunctions);
      this.lossFunctions = lossFunctionControlsService.lossFunctions();
      this.lossFunctionLabels = toLabels(lossFunctions);
      this.dataSelectors = dataSelectorControlsService.dataSelectors();
      this.dataSelectorLabels = toLabels(dataSelectors);
    }

    @Override
    protected CellEditor getCellEditor(Object element) {
      return switch (element) {
        case ActivationFunctionNode _ ->
            new ComboBoxCellEditor(viewer.getTree(), activationFunctionLabels, SWT.READ_ONLY);
        case LossFunctionNode _ ->
            new ComboBoxCellEditor(viewer.getTree(), lossFunctionLabels, SWT.READ_ONLY);
        case DataSelectorNode _ ->
            new ComboBoxCellEditor(viewer.getTree(), dataSelectorLabels, SWT.READ_ONLY);
        case BiasNode _, LayerSizeNode _, MultiLayerNetworkNode _,
             MultiLayerNetworkTemplateNode _, TrainingDataNode _ ->
            new TextCellEditor(viewer.getTree());
        default -> null;
      };
    }

    @Override
    protected boolean canEdit(Object element) {
      return element instanceof ActivationFunctionNode || element instanceof LossFunctionNode
          || element instanceof DataSelectorNode || element instanceof BiasNode
          || element instanceof LayerSizeNode || element instanceof MultiLayerNetworkNode
          || element instanceof MultiLayerNetworkTemplateNode
          || element instanceof TrainingDataNode;
    }

    @Override
    protected Object getValue(Object element) {
      return switch (element) {
        case ActivationFunctionNode node -> indexOf(activationFunctions,
            node.getLayerTemplateSupplier().get().getActivationFunction());
        case LossFunctionNode node -> indexOf(lossFunctions,
            node.getMultiLayerNetworkTemplateSupplier().get().getLossFunction());
        case DataSelectorNode node -> indexOf(dataSelectors,
            node.getTrainingDataSupplier().get().getDataSelector());
        case BiasNode node -> String.valueOf(
            node.getMultiLayerNetworkTemplateSupplier().get().getBias());
        case LayerSizeNode node -> String.valueOf(
            node.getLayerTemplateSupplier().get().getSize());
        case ModelNode node -> node.getLabel();
        default -> "";
      };
    }

    @Override
    protected void setValue(Object element, Object value) {
      if (!(element instanceof DefaultNode node)) {
        return;
      }
      var newValue = switch (element) {
        case ActivationFunctionNode _ -> value instanceof Integer i ? activationFunctions[i] : null;
        case LossFunctionNode _ -> value instanceof Integer i ? lossFunctions[i] : null;
        case DataSelectorNode _ -> value instanceof Integer i ? dataSelectors[i] : null;
        case BiasNode _ -> parseFloat(value);
        case LayerSizeNode _ -> parseInt(value);
        default -> value;
      };
      if (newValue != null) {
        node.applyValueChange(newValue);
        node.refresh();
        viewer.update(element, null);
      }
    }

    private static <T> int indexOf(T[] array, T value) {
      for (int i = 0; i < array.length; i++) {
        if (array[i].equals(value)) {
          return i;
        }
      }
      return 0;
    }

    private static String[] toLabels(Object[] values) {
      var labels = new String[values.length];
      for (int i = 0; i < values.length; i++) {
        labels[i] = values[i].toString();
      }
      return labels;
    }

    private static Float parseFloat(Object value) {
      try {
        return Float.parseFloat(String.valueOf(value));
      } catch (NumberFormatException e) {
        return null;
      }
    }

    private static Integer parseInt(Object value) {
      try {
        return Integer.parseInt(String.valueOf(value));
      } catch (NumberFormatException e) {
        return null;
      }
    }
  }
}
