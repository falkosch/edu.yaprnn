package edu.yaprnn.gui.model.editors;

import edu.yaprnn.gui.model.NetworksTreeCellRenderer;
import edu.yaprnn.gui.model.nodes.TrainingDataNode;
import edu.yaprnn.gui.views.di.NetworksTree;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import javax.swing.DefaultCellEditor;
import javax.swing.JTextField;
import javax.swing.JTree;

@ApplicationScoped
public class TrainingDataTreeCellEditor extends SelectableTreeCellEditorWithComponent<JTextField> {

  @Inject
  public TrainingDataTreeCellEditor(@NetworksTree JTree networksTree,
      NetworksTreeCellRenderer networksTreeCellRenderer) {
    this(networksTree, networksTreeCellRenderer, new JTextField());
  }

  private TrainingDataTreeCellEditor(JTree networksTree,
      NetworksTreeCellRenderer networksTreeCellRenderer, JTextField component) {
    super(networksTree, networksTreeCellRenderer, new DefaultCellEditor(component), component);
  }

  @Override
  public boolean isEditorOf(Object value) {
    return value instanceof TrainingDataNode;
  }

  @Override
  protected void setEditorValueOnStartEdit(Object value) {
    if (value instanceof TrainingDataNode node) {
      getComponent().setText(node.getTrainingDataSupplier().get().getName());
    }
  }

  @Override
  protected Object mapCellEditorValue(Object value) {
    return value;
  }
}
