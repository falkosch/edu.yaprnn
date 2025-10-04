package edu.yaprnn.gui.model.editors;

import edu.yaprnn.gui.model.editors.di.DefaultSelectableTreeCellEditor;
import edu.yaprnn.gui.model.nodes.ModelNode;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.awt.Component;
import java.util.EventObject;
import java.util.Objects;
import javax.swing.JTree;
import javax.swing.event.CellEditorListener;
import javax.swing.tree.TreeCellEditor;

/**
 * Enables customizing the structure of a multiLayerNetwork.
 */
@Singleton
public class NetworksTreeCellEditor implements TreeCellEditor {

  @Inject
  @DefaultSelectableTreeCellEditor
  SelectableTreeCellEditor defaultSelectedTreeCellEditor;

  @Inject
  @Any
  Instance<SelectableTreeCellEditor> selectableTreeCellEditorInstance;

  private SelectableTreeCellEditor selectedTreeCellEditor;

  @PostConstruct
  void postConstruct() {
    selectedTreeCellEditor = defaultSelectedTreeCellEditor;
  }

  @Override
  public Component getTreeCellEditorComponent(JTree tree, Object value, boolean isSelected,
      boolean expanded, boolean leaf, int row) {
    if (value instanceof ModelNode modelNode) {
      selectTreeCellEditor(modelNode);
    }
    return selectedTreeCellEditor.getTreeCellEditorComponent(tree, value, isSelected, expanded,
        leaf, row);
  }

  private void selectTreeCellEditor(ModelNode modelNode) {
    selectedTreeCellEditor = selectableTreeCellEditorInstance.stream()
        .filter(x -> x.isEditorOf(modelNode))
        .findAny()
        .orElse(defaultSelectedTreeCellEditor);
  }

  @Override
  public Object getCellEditorValue() {
    return selectedTreeCellEditor.getCellEditorValue();
  }

  @Override
  public boolean isCellEditable(EventObject event) {
    if (event.getSource() instanceof JTree tree) {
      var treePath = tree.getSelectionPath();
      if (Objects.nonNull(treePath)
          && treePath.getLastPathComponent() instanceof ModelNode modelNode) {
        selectTreeCellEditor(modelNode);
      }
    }

    return selectedTreeCellEditor.isCellEditable(event);
  }

  @Override
  public boolean shouldSelectCell(EventObject event) {
    return selectedTreeCellEditor.shouldSelectCell(event);
  }

  @Override
  public boolean stopCellEditing() {
    return selectedTreeCellEditor.stopCellEditing();
  }

  @Override
  public void cancelCellEditing() {
    selectedTreeCellEditor.cancelCellEditing();
  }

  @Override
  public void addCellEditorListener(CellEditorListener listener) {
    selectableTreeCellEditorInstance.forEach(x -> x.addCellEditorListener(listener));
  }

  @Override
  public void removeCellEditorListener(CellEditorListener listener) {
    selectableTreeCellEditorInstance.forEach(x -> x.removeCellEditorListener(listener));
  }
}
