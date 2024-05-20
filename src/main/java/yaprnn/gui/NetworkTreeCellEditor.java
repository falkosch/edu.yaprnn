package yaprnn.gui;

import java.awt.Component;
import java.util.EventObject;
import java.util.Vector;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.event.CellEditorListener;
import javax.swing.tree.DefaultTreeCellEditor;
import javax.swing.tree.TreeCellEditor;
import yaprnn.gui.NetworkTreeModel.AVFNode;
import yaprnn.gui.NetworkTreeModel.BiasNode;
import yaprnn.gui.NetworkTreeModel.ModelNode;
import yaprnn.gui.NetworkTreeModel.NetworkNode;
import yaprnn.gui.NetworkTreeModel.NeuronsNode;
import yaprnn.mlp.ActivationFunction;

/**
 * TreeCell editor to customize the structure of a neural network.
 */
class NetworkTreeCellEditor implements TreeCellEditor {

  // Die Editoren der jeweiligen Node-Typen
  private final TreeCellEditor networkEditor;
  private final TreeCellEditor biasEditor;
  private final TreeCellEditor neuronsEditor;
  private final TreeCellEditor avfEditor;
  // Wird benutzt fr avfEditor
  private final JTextField optionNetwork;
  private final JTextField optionNeurons;
  private final JComboBox<ActivationFunction> optionAVF;
  private final JTextField optionBias;
  /* Die durch getTreeCellEditorComponent selektierte Node */
  private ModelNode selected = null;

  NetworkTreeCellEditor(GUI gui) {
    JTree tree = gui.getView().getTreeNeuralNetwork();
    NetworkTreeRenderer ntr = gui.getTreeRenderer();

    // Editor-Components
    optionNetwork = new JTextField();
    optionNeurons = new JTextField();
    optionNeurons.addKeyListener(new OnlyNumbersKeyAdapter(true, true));
    optionAVF = new JComboBox<>(new Vector<>(gui.getCore().getAllActivationFunctions()));
    optionAVF.setEditable(false);
    optionBias = new JTextField();
    optionBias.addKeyListener(new OnlyNumbersKeyAdapter(false, false));

    // Editoren
    networkEditor = new DefaultTreeCellEditor(tree, ntr, new DefaultCellEditor(optionNetwork));
    neuronsEditor = new DefaultTreeCellEditor(tree, ntr, new DefaultCellEditor(optionNeurons));
    avfEditor = new DefaultTreeCellEditor(tree, ntr, new DefaultCellEditor(optionAVF));
    biasEditor = new DefaultTreeCellEditor(tree, ntr, new DefaultCellEditor(optionBias));
  }

  @Override
  public Component getTreeCellEditorComponent(JTree tree, Object value, boolean isSelected,
      boolean expanded, boolean leaf, int row) {
    if (!(value instanceof ModelNode)) {
      selected = null;
      return null;
    }

    Component comp = null;

    // Selektierte Node merken
    selected = (ModelNode) value;

    // Editor-Component auswhlen
    if (selected instanceof NetworkNode) {
      comp = networkEditor.getTreeCellEditorComponent(tree, value, isSelected, expanded, leaf, row);
      optionNetwork.setText(((NetworkNode) selected).getNetwork().getName());
    }

    if (selected instanceof NeuronsNode) {
      comp = neuronsEditor.getTreeCellEditorComponent(tree, value, isSelected, expanded, leaf, row);
      NeuronsNode nn = (NeuronsNode) selected;
      optionNeurons.setText(Integer.toString(nn.getNetwork().getLayerSize(nn.getLayerIndex())));
    }

    if (selected instanceof AVFNode) {
      comp = avfEditor.getTreeCellEditorComponent(tree, value, isSelected, expanded, leaf, row);
      AVFNode an = (AVFNode) selected;
      optionAVF.setSelectedItem(an.getNetwork().getActivationFunction(an.getLayerIndex()));
    }

    if (selected instanceof BiasNode) {
      comp = biasEditor.getTreeCellEditorComponent(tree, value, isSelected, expanded, leaf, row);
      BiasNode bn = (BiasNode) selected;
      optionBias.setText(Double.toString(bn.getNetwork().getBias(bn.getLayerIndex())));
    }

    return comp;
  }

  @Override
  public Object getCellEditorValue() {
    if (selected instanceof NetworkNode) {
      return networkEditor.getCellEditorValue();
    }
    if (selected instanceof NeuronsNode) {
      try {
        return Integer.valueOf((String) neuronsEditor.getCellEditorValue());
      } catch (Exception e) {
        System.out.println("NeuronsNode-Edit: Failed to parse to int");
      }
    }
    if (selected instanceof AVFNode) {
      return avfEditor.getCellEditorValue();
    }
    if (selected instanceof BiasNode) {
      try {
        return Double.valueOf((String) biasEditor.getCellEditorValue());
      } catch (Exception e) {
        System.out.println("BiasNode-Edit: Failed to parse to double");
      }
    }
    return null;
  }

  @Override
  public boolean isCellEditable(EventObject evt) {
    // Wenn evt == null, dann wurde das Editieren durch das PopupMenu Edit
    // aufgerufen! Nur dann erlauben wir das Editieren.
    return evt == null;
  }

  @Override
  public boolean shouldSelectCell(EventObject evt) {
    return true;
  }

  @Override
  public boolean stopCellEditing() {
    return networkEditor.stopCellEditing() || neuronsEditor.stopCellEditing()
        || avfEditor.stopCellEditing() || biasEditor.stopCellEditing();
  }

  @Override
  public void cancelCellEditing() {
    networkEditor.cancelCellEditing();
    neuronsEditor.cancelCellEditing();
    avfEditor.cancelCellEditing();
    biasEditor.cancelCellEditing();
  }

  @Override
  public void addCellEditorListener(CellEditorListener l) {
    networkEditor.addCellEditorListener(l);
    neuronsEditor.addCellEditorListener(l);
    avfEditor.addCellEditorListener(l);
    biasEditor.addCellEditorListener(l);
  }

  @Override
  public void removeCellEditorListener(CellEditorListener l) {
    networkEditor.removeCellEditorListener(l);
    neuronsEditor.removeCellEditorListener(l);
    avfEditor.removeCellEditorListener(l);
    biasEditor.removeCellEditorListener(l);
  }

}
