package edu.yaprnn.gui.model.editors;

import edu.yaprnn.gui.model.NetworksTreeCellRenderer;
import edu.yaprnn.gui.model.nodes.LossFunctionNode;
import edu.yaprnn.gui.services.LossFunctionControlsService;
import edu.yaprnn.gui.views.di.NetworksTree;
import edu.yaprnn.networks.loss.LossFunction;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JTree;

@ApplicationScoped
public class LossFunctionTreeCellEditor extends
    SelectableTreeCellEditorWithComponent<JComboBox<LossFunction>> {

  @Inject
  public LossFunctionTreeCellEditor(@NetworksTree JTree networksTree,
      NetworksTreeCellRenderer networksTreeCellRenderer,
      LossFunctionControlsService controlsService) {
    this(networksTree, networksTreeCellRenderer,
        controlsService.lossFunctionsComboBox(_ -> {/*NOOP*/}));
  }

  private LossFunctionTreeCellEditor(JTree networksTree,
      NetworksTreeCellRenderer networksTreeCellRenderer, JComboBox<LossFunction> component) {
    super(networksTree, networksTreeCellRenderer, new DefaultCellEditor(component), component);
  }

  @Override
  public boolean isEditorOf(Object value) {
    return value instanceof LossFunctionNode;
  }

  @Override
  protected void setEditorValueOnStartEdit(Object value) {
    if (value instanceof LossFunctionNode node) {
      getComponent().setSelectedItem(
          node.getMultiLayerNetworkTemplateSupplier().get().getLossFunction());
    }
  }

  @Override
  protected Object mapCellEditorValue(Object value) {
    return value;
  }
}
