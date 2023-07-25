package edu.yaprnn.gui.model.editors;

import edu.yaprnn.gui.model.NetworksTreeCellRenderer;
import java.awt.Component;
import java.util.EventObject;
import javax.swing.JComponent;
import javax.swing.JTree;
import javax.swing.event.CellEditorListener;
import javax.swing.tree.DefaultTreeCellEditor;
import javax.swing.tree.TreeCellEditor;
import lombok.Getter;

@Getter
public abstract class SelectableTreeCellEditorWithComponent<T extends JComponent> extends
    SelectableTreeCellEditor {

  private final TreeCellEditor editor;
  private final T component;

  protected SelectableTreeCellEditorWithComponent(JTree networksTree,
      NetworksTreeCellRenderer networksTreeCellRenderer, TreeCellEditor editor, T component) {
    this.editor = new DefaultTreeCellEditor(networksTree, networksTreeCellRenderer, editor);
    this.component = component;
  }

  @Override
  public Component getTreeCellEditorComponent(JTree tree, Object value, boolean isSelected,
      boolean expanded, boolean leaf, int row) {
    var editorComponent = editor.getTreeCellEditorComponent(tree, value, isSelected, expanded, leaf,
        row);
    setEditorValueOnStartEdit(value);
    return editorComponent;
  }

  protected abstract void setEditorValueOnStartEdit(Object value);

  @Override
  public Object getCellEditorValue() {
    return mapCellEditorValue(editor.getCellEditorValue());
  }

  protected abstract Object mapCellEditorValue(Object value);

  @Override
  public boolean isCellEditable(EventObject event) {
    return editor.isCellEditable(event) || event == null;
  }

  @Override
  public boolean shouldSelectCell(EventObject event) {
    return editor.shouldSelectCell(event) || event == null;
  }

  @Override
  public boolean stopCellEditing() {
    return editor.stopCellEditing();
  }

  @Override
  public void cancelCellEditing() {
    editor.cancelCellEditing();
  }

  @Override
  public void addCellEditorListener(CellEditorListener listener) {
    editor.addCellEditorListener(listener);
  }

  @Override
  public void removeCellEditorListener(CellEditorListener listener) {
    editor.removeCellEditorListener(listener);
  }
}
