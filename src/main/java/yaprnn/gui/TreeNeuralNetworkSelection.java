package yaprnn.gui;

import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreeSelectionModel;

/**
 * This listener reacts to selection events on the tree.
 */
class TreeNeuralNetworkSelection implements TreeSelectionListener {

  private final GUI gui;

  TreeNeuralNetworkSelection(GUI gui) {
    this.gui = gui;
    JTree tree = gui.getView().getTreeNeuralNetwork();
    tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
    tree.addTreeSelectionListener(this);
  }

  @Override
  public void valueChanged(TreeSelectionEvent e) {
    gui.setSelectedPath((e.isAddedPath()) ? e.getPath() : null);
  }

}
