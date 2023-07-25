package edu.yaprnn.gui.model.editors;

import edu.yaprnn.gui.model.editors.di.DefaultSelectableTreeCellEditor;
import jakarta.inject.Singleton;
import java.awt.Component;
import java.util.EventObject;
import java.util.Objects;
import javax.swing.JTree;
import javax.swing.event.CellEditorListener;
import javax.swing.tree.TreeCellEditor;

@DefaultSelectableTreeCellEditor
@Singleton
public class SelectableTreeCellEditor implements TreeCellEditor {

  public boolean isEditorOf(Object value) {
    return false;
  }

  @Override
  public Component getTreeCellEditorComponent(JTree tree, Object value, boolean isSelected,
      boolean expanded, boolean leaf, int row) {
    return null;
  }

  @Override
  public Object getCellEditorValue() {
    return null;
  }

  @Override
  public boolean isCellEditable(EventObject event) {
    return Objects.isNull(event);
  }

  @Override
  public boolean shouldSelectCell(EventObject event) {
    return Objects.isNull(event);
  }

  @Override
  public boolean stopCellEditing() {
    return true;
  }

  @Override
  public void cancelCellEditing() {
  }

  @Override
  public void addCellEditorListener(CellEditorListener listener) {
  }

  @Override
  public void removeCellEditorListener(CellEditorListener listener) {
  }
}
