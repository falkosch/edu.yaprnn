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
import java.util.Optional;
import javax.swing.JTree;
import javax.swing.event.CellEditorListener;
import javax.swing.tree.TreeCellEditor;
import javax.swing.tree.TreePath;

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
      selectedTreeCellEditor = findTreeCellEditor(modelNode).orElse(defaultSelectedTreeCellEditor);
    }
    return selectedTreeCellEditor.getTreeCellEditorComponent(tree, value, isSelected, expanded,
        leaf, row);
  }

  private Optional<SelectableTreeCellEditor> findTreeCellEditor(ModelNode node) {
    return selectableTreeCellEditorInstance.stream().filter(x -> x.isEditorOf(node)).findAny();
  }

  @Override
  public Object getCellEditorValue() {
    return selectedTreeCellEditor.getCellEditorValue();
  }

  @Override
  public boolean isCellEditable(EventObject event) {
    return findTreeCellEditor(event).map(editor -> editor.isCellEditable(event))
        .orElseGet(() -> Objects.isNull(event));
  }

  private Optional<SelectableTreeCellEditor> findTreeCellEditor(EventObject event) {
    return Optional.ofNullable(event)
        .map(EventObject::getSource)
        .filter(JTree.class::isInstance)
        .map(JTree.class::cast)
        .map(JTree::getSelectionPath)
        .map(TreePath::getLastPathComponent)
        .filter(ModelNode.class::isInstance)
        .map(ModelNode.class::cast)
        .flatMap(this::findTreeCellEditor);
  }

  @Override
  public boolean shouldSelectCell(EventObject event) {
    return findTreeCellEditor(event).map(editor -> editor.shouldSelectCell(event)).orElse(false);
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

  public boolean isCellEditable(ModelNode selected) {
    return findTreeCellEditor(selected).map(editor -> editor.isCellEditable(null)).orElse(false);
  }
}
