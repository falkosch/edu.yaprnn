package edu.yaprnn.gui.model.editors;

import edu.yaprnn.functions.ActivationFunction;
import edu.yaprnn.functions.Functions;
import edu.yaprnn.gui.model.NetworksTreeCellRenderer;
import edu.yaprnn.gui.model.nodes.ActivationFunctionNode;
import edu.yaprnn.gui.services.ControlsService;
import edu.yaprnn.gui.views.di.NetworksTree;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JTree;

@ApplicationScoped
public class ActivationFunctionTreeCellEditor extends
    SelectableTreeCellEditorWithComponent<JComboBox<ActivationFunction>> {

  @Inject
  public ActivationFunctionTreeCellEditor(@NetworksTree JTree networksTree,
      NetworksTreeCellRenderer networksTreeCellRenderer, ControlsService controlsService) {
    this(networksTree, networksTreeCellRenderer,
        controlsService.activationFunctionsComboBox(Functions::noop));
  }

  private ActivationFunctionTreeCellEditor(JTree networksTree,
      NetworksTreeCellRenderer networksTreeCellRenderer, JComboBox<ActivationFunction> component) {
    super(networksTree, networksTreeCellRenderer, new DefaultCellEditor(component), component);
  }

  @Override
  public boolean isEditorOf(Object value) {
    return value instanceof ActivationFunctionNode;
  }

  @Override
  protected void setEditorValueOnStartEdit(Object value) {
    if (value instanceof ActivationFunctionNode node) {
      getComponent().setSelectedItem(node.getLayerTemplateSupplier().get().getActivationFunction());
    }
  }

  @Override
  protected Object mapCellEditorValue(Object value) {
    return value;
  }
}
