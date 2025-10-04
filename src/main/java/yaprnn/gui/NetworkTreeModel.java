package yaprnn.gui;

import java.util.Collection;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.Objects;
import java.util.Vector;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import yaprnn.dvv.Data;
import yaprnn.mlp.ActivationFunction;
import yaprnn.mlp.NeuralNetwork;

// The structure is organized according to the following:
// root
// |-Networks
// | |-Network 1
// | |...
// | |-Network n
// |-Datasets
// _ |-Loaded
// _ | |-Data 1
// _ | |...
// _ | |-Data m
// _ |-for Network' 1
// _ | |-Training set
// _ | | |-Data' 1
// _ | | |...
// _ | | |-Data' x_1
// _ | |-Test set
// _ | _ |-Data' 1
// _ | _ |...
// _ | _ |-Data' y_1
// _ |...
// _ |-for Network' n
// _ _ |-Training set
// _ _ | |-Data' 1
// _ _ | |...
// _ _ | |-Data' x_n
// _ _ |-Test set
// _ _ _ |-Data' 1
// _ _ _ |...
// _ _ _ |-Data' y_n

/**
 * NetworkTreeModel is used to store and reflect NeuralNetworks and datasets in a JTree.
 */
class NetworkTreeModel implements TreeModel {

  final static int DEFAULT_ICONSIZE = 22;

  final static ImageIcon ICON_MLP = ImagesMacros.loadIcon(DEFAULT_ICONSIZE, DEFAULT_ICONSIZE,
      "/yaprnn/gui/view/iconMLP.png");
  final static ImageIcon ICON_NEURON = ImagesMacros.loadIcon(DEFAULT_ICONSIZE, DEFAULT_ICONSIZE,
      "/yaprnn/gui/view/iconNeuron.png");
  final static ImageIcon ICON_LAYER = ImagesMacros.loadIcon(DEFAULT_ICONSIZE, DEFAULT_ICONSIZE,
      "/yaprnn/gui/view/iconLayer.png");
  final static ImageIcon ICON_AVF = ImagesMacros.loadIcon(DEFAULT_ICONSIZE, DEFAULT_ICONSIZE,
      "/yaprnn/gui/view/iconAVF.png");
  final static ImageIcon ICON_TRAININGSET = ImagesMacros.loadIcon(DEFAULT_ICONSIZE,
      DEFAULT_ICONSIZE, "/yaprnn/gui/view/iconFolderTraining.png");
  final static ImageIcon ICON_TESTSET = ImagesMacros.loadIcon(DEFAULT_ICONSIZE, DEFAULT_ICONSIZE,
      "/yaprnn/gui/view/iconFolderTest.png");
  final static ImageIcon ICON_DATASETS = ImagesMacros.loadIcon(DEFAULT_ICONSIZE, DEFAULT_ICONSIZE,
      "/yaprnn/gui/view/iconFolderDataSet.png");
  final static ImageIcon ICON_AUDIO = ImagesMacros.loadIcon(DEFAULT_ICONSIZE, DEFAULT_ICONSIZE,
      "/yaprnn/gui/view/iconLoadAudio.png");
  final static ImageIcon ICON_IMAGE = ImagesMacros.loadIcon(DEFAULT_ICONSIZE, DEFAULT_ICONSIZE,
      "/yaprnn/gui/view/iconLoadImage.png");
  final static ImageIcon ICON_OPENED = ImagesMacros.loadIcon(DEFAULT_ICONSIZE, DEFAULT_ICONSIZE,
      "/yaprnn/gui/view/iconFolderWhite.png");
  final static ImageIcon ICON_CLOSED = ImagesMacros.loadIcon(DEFAULT_ICONSIZE, DEFAULT_ICONSIZE,
      "/yaprnn/gui/view/iconFolderGrey.png");
  final static ImageIcon ICON_EDITABLE = ImagesMacros.loadIcon(DEFAULT_ICONSIZE, DEFAULT_ICONSIZE,
      "/yaprnn/gui/view/iconEditable.png");
  final static ImageIcon ICON_NOTEDITABLE = ImagesMacros.loadIcon(DEFAULT_ICONSIZE,
      DEFAULT_ICONSIZE, "/yaprnn/gui/view/iconNotEditable.png");
  // Listeners
  private final Collection<TreeModelListener> listeners = new Vector<>();
  // Speicher fr die eigentlichen Netzwerke und Daten
  private final List<NeuralNetwork> nets = new Vector<>();
  private final List<Data> loadedData = new Vector<>();
  private final Dictionary<NeuralNetwork, List<Data>> trainingSets = new Hashtable<>();
  private final Dictionary<NeuralNetwork, List<Data>> testSets = new Hashtable<>();
  // Dynamische Knoten
  private final Dictionary<NeuralNetwork, NetworkNode> netsNodes = new Hashtable<>();
  private final Dictionary<NeuralNetwork, NetworkSetsNode> setsNodes = new Hashtable<>();
  // Statische Knoten
  private final DataSetNode loadedNode = new DataSetNode("Loaded", loadedData);
  private final DatasetsNode datasetsNode = new DatasetsNode(loadedNode, nets, setsNodes);
  private final NetworksNode netsNode = new NetworksNode(nets, netsNodes);
  private final RootNode rootNode = new RootNode(netsNode, datasetsNode);

  void refresh() {
    rootNode.update();
    fireStructureChanged(new Object[]{rootNode});
  }

  /**
   * Fires event to all listeners that the tree structure has changed.
   */
  private void fireStructureChanged(Object[] path) {
    TreeModelEvent e = new TreeModelEvent(this, path);
    for (TreeModelListener tml : listeners) {
      tml.treeStructureChanged(e);
    }
  }

  void refreshNetwork(NeuralNetwork n) {
    NetworkNode nn = netsNodes.get(n);
    nn.update();
    fireStructureChanged(new Object[]{rootNode, netsNode, nn});
  }

  /**
   * Adds a NeuralNetwork to the tree model.
   *
   * @param n the network to add
   */
  void add(NeuralNetwork n) {
    if (nets.contains(n)) {
      return;
    }
    nets.add(n);

    // Training- und Testset anlegen
    List<Data> trainingSet = new Vector<>();
    List<Data> testSet = new Vector<>();
    trainingSets.put(n, trainingSet);
    testSets.put(n, testSet);

    // Dynamische Knoten erstellen
    netsNodes.put(n, new NetworkNode(n));
    setsNodes.put(n, new NetworkSetsNode(n, trainingSet, testSet));

    fireStructureChanged(new Object[]{rootNode, netsNode});
    fireStructureChanged(new Object[]{rootNode, datasetsNode});
  }

  /**
   * Removes a NeuralNetwork from the tree model.
   *
   * @param n the network to remove
   */
  void remove(NeuralNetwork n) {
    if (!nets.contains(n)) {
      return;
    }
    nets.remove(n);

    // Training- und Testset entferne
    trainingSets.remove(n);
    testSets.remove(n);

    // Dynamische Knoten entfernen
    netsNodes.remove(n);
    setsNodes.remove(n);

    fireStructureChanged(new Object[]{rootNode, netsNode});
    fireStructureChanged(new Object[]{rootNode, datasetsNode});
  }

  /**
   * Adds a Data-Object to the tree model.
   *
   * @param d the data to add
   */
  void add(Data d) {
    if (loadedData.contains(d)) {
      return;
    }
    loadedData.add(d);

    // Dynamische Knoten erstellen
    loadedNode.add(d);

    fireStructureChanged(new Object[]{rootNode, datasetsNode, loadedNode});
  }

  /**
   * Adds a Data-Object to the training or test set of a network.
   *
   * @param d             the data to add
   * @param n             the network which set gets it
   * @param asTrainingSet if true the data will be added in the training set, else it will be added
   *                      in the test set
   */
  void add(Data d, NeuralNetwork n, boolean asTrainingSet) {
    if (d == null || n == null) {
      return;
    }
    NetworkSetsNode nsn = setsNodes.get(n);
    if (asTrainingSet) {
      trainingSets.get(n).add(d);
      // Dynamische Knoten erstellen
      nsn.getTrainingSetNode().add(d);
      fireStructureChanged(new Object[]{rootNode, datasetsNode, nsn, nsn.getTrainingSetNode()});
    } else {
      testSets.get(n).add(d);
      // Dynamische Knoten erstellen
      setsNodes.get(n).getTestSetNode().add(d);
      fireStructureChanged(new Object[]{rootNode, datasetsNode, nsn, nsn.getTestSetNode()});
    }
  }

  /**
   * Removes a Data-Object from a training or test set of a network.
   *
   * @param d the data to remove
   * @param n the network which set will lose it
   */
  void remove(Data d, NeuralNetwork n) {
    if (d == null || n == null) {
      return;
    }
    trainingSets.get(n).remove(d);
    testSets.get(n).remove(d);
    // Dynamische Knoten entfernen (falls vorhanden)
    NetworkSetsNode nsn = setsNodes.get(n);
    nsn.getTrainingSetNode().remove(d);
    nsn.getTestSetNode().remove(d);
    fireStructureChanged(new Object[]{rootNode, datasetsNode, nsn});
  }

  /**
   * Returns a copy of the list of networks loaded
   *
   * @return copy of the list nets
   */
  List<NeuralNetwork> getNetworks() {
    return nets;
  }

  /**
   * Returns a copy of the list of loaded data objects
   *
   * @return copy of the list
   */
  List<Data> getDatasets() {
    return loadedData;
  }

  @Override
  public Object getRoot() {
    return rootNode;
  }

  @Override
  public Object getChild(Object parent, int index) {
    if (parent instanceof ModelNode) {
      return ((ModelNode) parent).getChild(index);
    }
    return null;
  }

  @Override
  public int getChildCount(Object parent) {
    if (parent instanceof ModelNode) {
      return ((ModelNode) parent).getChildsCount();
    }
    return 0;
  }

  @Override
  public boolean isLeaf(Object node) {
    if (node instanceof ModelNode) {
      return ((ModelNode) node).isLeaf();
    }
    return true;
  }

  @Override
  public void valueForPathChanged(TreePath path, Object newValue) {
    if (newValue == null) {
      return;
    }

    // Knoten aus dem Path ziehen
    Object last = path.getLastPathComponent();
    if (!(last instanceof ModelNode mn)) {
      return;
    }

    // Knoten updaten
    if (isNetworkNode(mn)) {
      NetworkNode n = (NetworkNode) mn;
      n.getNetwork().setName((String) newValue);
      n.update();
      fireStructureChanged(path.getPath());

      // Wir mssen noch die NetworkSetsNode updaten
      NetworkSetsNode n2 = setsNodes.get(n.getNetwork());
      n2.update();
      fireStructureChanged(new Object[]{rootNode, datasetsNode, n2});

    }
    if (isNeuronsNode(mn)) {
      NeuronsNode n = null;
      if (mn instanceof NeuronsNode) {
        n = (NeuronsNode) mn;
      }
      if (newValue instanceof Integer) {
        Objects.requireNonNull(n).getNetwork().setLayerSize(n.getLayerIndex(), (Integer) newValue);
      }
      Objects.requireNonNull(n).update();
      fireStructureChanged(path.getPath());
    }
    if (isAVFNode(mn)) {
      AVFNode n = null;
      if (mn instanceof AVFNode) {
        n = (AVFNode) mn;
      }
      if (newValue instanceof ActivationFunction) {
        Objects.requireNonNull(n).getNetwork()
            .setActivationFunction(n.getLayerIndex(), (ActivationFunction) newValue);
      }
      Objects.requireNonNull(n).update();
      fireStructureChanged(path.getPath());
    }
    if (isBiasNode(mn)) {
      BiasNode n = null;
      if (mn instanceof BiasNode) {
        n = (BiasNode) mn;
      }
      if (newValue instanceof Double) {
        Objects.requireNonNull(n).getNetwork().setBias(n.getLayerIndex(), (Double) newValue);
      }
      Objects.requireNonNull(n).update();
      fireStructureChanged(path.getPath());
    }

  }

  @Override
  public int getIndexOfChild(Object parent, Object child) {
    if (parent instanceof ModelNode && child instanceof ModelNode) {
      return ((ModelNode) parent).getIndexOf((ModelNode) child);
    }
    return -1;
  }

  @Override
  public void addTreeModelListener(TreeModelListener l) {
    listeners.add(l);
  }

  @Override
  public void removeTreeModelListener(TreeModelListener l) {
    listeners.remove(l);
  }

  private static boolean isNetworkNode(ModelNode n) {
    return n instanceof NetworkNode;
  }

  private static boolean isNeuronsNode(ModelNode n) {
    return n instanceof NeuronsNode;
  }

  private static boolean isAVFNode(ModelNode n) {
    return n instanceof AVFNode;
  }

  private static boolean isBiasNode(ModelNode n) {
    return n instanceof BiasNode;
  }

  /**
   * Base node class for all nodes in NetworkTreeModel
   */
  abstract static class ModelNode {

    private Icon icon;
    private String label;

    ModelNode() {
      this.icon = null;
      this.label = null;
    }

    ModelNode(Icon icon) {
      this.icon = icon;
      this.label = null;
    }

    ModelNode(Icon icon, String label) {
      this.icon = icon;
      this.label = label;
    }

    int getIndexOf(ModelNode child) {
      return -1;
    }

    ModelNode getChild(int index) {
      return null;
    }

    int getChildsCount() {
      return 0;
    }

    /**
     * Returns false when this node can have childs, else true.
     */
    boolean isLeaf() {
      return false;
    }

    final Icon getIcon() {
      return icon;
    }

    final void setIcon(Icon icon) {
      this.icon = icon;
    }

    final void setLabel(String label) {
      this.label = label;
    }

    @Override
    public String toString() {
      return label;
    }

  }

  /**
   * IsTrainedNode is a node that displays the trained status of a NeuralNetwork.
   */
  static class IsTrainedNode extends ModelNode {

    private final NeuralNetwork network;

    IsTrainedNode(NeuralNetwork network) {
      this.network = network;
      update();
    }

    private void update() {
      setIcon(network.isTrained() ? ICON_NOTEDITABLE : ICON_EDITABLE);
      setLabel("IsTrained: " + network.isTrained());
    }

    @Override
    boolean isLeaf() {
      return true;
    }

  }

  /**
   * LayerNode is a node that displays one layer of a NeuralNetwork.
   */
  static class LayerNode extends ModelNode {

    private final NeuralNetwork network;
    private final int layerIndex;
    private final NeuronsNode neuronsNode;
    private final AVFNode avfNode;
    private final BiasNode biasNode;

    LayerNode(NeuralNetwork network, int layerIndex) {
      super(ICON_LAYER);
      this.network = network;
      this.layerIndex = layerIndex;
      neuronsNode = new NeuronsNode(network, layerIndex);
      avfNode = new AVFNode(network, layerIndex);
      biasNode = new BiasNode(network, layerIndex);
      update();
    }

    private void update() {
      setLabel((layerIndex == 0 || layerIndex + 1 == network.getNumLayers()) ? (layerIndex == 0
          ? "Input layer" : "Output layer") : "Layer " + layerIndex);
      neuronsNode.update();
      avfNode.update();
      biasNode.update();
    }

    @Override
    int getIndexOf(ModelNode child) {
      if (child == neuronsNode) {
        return 0;
      }
      if (child == avfNode) {
        return 1;
      }
      return (child == biasNode) ? 2 : -1;
    }

    @Override
    ModelNode getChild(int index) {
      if (index == 0) {
        return neuronsNode;
      }
      if (index == 1) {
        return avfNode;
      }
      return (index == 2) ? biasNode : null;
    }

    @Override
    int getChildsCount() {
      return 3;
    }

    NeuralNetwork getNetwork() {
      return network;
    }

    int getLayerIndex() {
      return layerIndex;
    }

  }

  /**
   * NeuronsNode is a node that represents the count of neurons in one layer of a NeuralNetwork.
   */
  static class NeuronsNode extends ModelNode {

    private final NeuralNetwork network;
    private final int layerIndex;

    NeuronsNode(NeuralNetwork network, int layerIndex) {
      super(ICON_NEURON, "Neuron count: " + network.getLayerSize(layerIndex));
      this.network = network;
      this.layerIndex = layerIndex;
    }

    private void update() {
      setLabel("Neuron count: " + network.getLayerSize(layerIndex));
    }

    @Override
    boolean isLeaf() {
      return true;
    }

    NeuralNetwork getNetwork() {
      return network;
    }

    int getLayerIndex() {
      return layerIndex;
    }

  }

  /**
   * AVFNode is a node that represents the activation function of one layer of a NeuralNetwork.
   */
  static class AVFNode extends ModelNode {

    private final NeuralNetwork network;
    private final int layerIndex;

    AVFNode(NeuralNetwork network, int layerIndex) {
      super(ICON_AVF, "AVF: " + network.getActivationFunction(layerIndex));
      this.network = network;
      this.layerIndex = layerIndex;
    }

    private void update() {
      ActivationFunction avf = network.getActivationFunction(layerIndex);
      setLabel("AVF: " + avf);
    }

    @Override
    boolean isLeaf() {
      return true;
    }

    NeuralNetwork getNetwork() {
      return network;
    }

    int getLayerIndex() {
      return layerIndex;
    }

  }

  /**
   * BiasNode is a node that represents the bias of one layer of a NeuralNetwork.
   */
  static class BiasNode extends ModelNode {

    private final NeuralNetwork network;
    private final int layerIndex;

    BiasNode(NeuralNetwork network, int layerIndex) {
      super(ICON_AVF, "Bias: " + network.getBias(layerIndex));
      this.network = network;
      this.layerIndex = layerIndex;
    }

    private void update() {
      setLabel("Bias: " + network.getBias(layerIndex));
    }

    @Override
    boolean isLeaf() {
      return true;
    }

    NeuralNetwork getNetwork() {
      return network;
    }

    int getLayerIndex() {
      return layerIndex;
    }

  }

  /**
   * DatasetsNode is a node that displays a DatasetsLoadedNode and a list of
   * NetTrainingTestSetsNodes linked with all loaded networks in the tree model.
   */
  static class DatasetsNode extends ModelNode {

    private final DataSetNode ldn;

    private final List<NeuralNetwork> nets;
    private final Dictionary<NeuralNetwork, NetworkSetsNode> setsNodes;

    public DatasetsNode(DataSetNode ldn, List<NeuralNetwork> nets,
        Dictionary<NeuralNetwork, NetworkSetsNode> setsNodes) {
      super(null, "Datasets");
      this.ldn = ldn;
      this.nets = nets;
      this.setsNodes = setsNodes;
    }

    private void update() {
      ldn.update();
      for (NeuralNetwork n : nets) {
        setsNodes.get(n).update();
      }
    }

    @Override
    int getIndexOf(ModelNode child) {
      if (child == ldn) {
        return 0;
      }
      if (child instanceof NetworkSetsNode) {
        int i = nets.indexOf(((NetworkSetsNode) child).getNetwork());
        return (i == -1) ? -1 : i + 1;
      }
      return -1;
    }

    @Override
    ModelNode getChild(int index) {
      return (index == 0) ? ldn : setsNodes.get(nets.get(index - 1));
    }

    @Override
    int getChildsCount() {
      return 1 + nets.size();
    }

  }

  /**
   * DataSetNode is a node that displays a set og data objects in the tree model.
   */
  static class DataSetNode extends ModelNode {

    private final List<Data> dataset;
    private Dictionary<Data, DataNode> dataNodes;

    public DataSetNode(String label, List<Data> dataset) {
      super(ICON_DATASETS, label);
      this.dataset = dataset;
      update();
    }

    private void update() {
      dataNodes = new Hashtable<>();
      for (Data d : dataset) {
        add(d);
      }
    }

    void add(Data d) {
      dataNodes.put(d, new DataNode(d));
    }

    @Override
    int getIndexOf(ModelNode child) {
      if (child instanceof DataNode) {
        return dataset.indexOf(((DataNode) child).getData());
      }
      return -1;
    }

    @Override
    ModelNode getChild(int index) {
      return dataNodes.get(dataset.get(index));
    }

    @Override
    int getChildsCount() {
      return dataset.size();
    }

    void remove(Data d) {
      dataNodes.remove(d);
    }

  }

  /**
   * DataNode is a node that displays a data object in the tree model.
   */
  static class DataNode extends ModelNode {

    private final Data data;

    public DataNode(Data data) {
      this.data = data;
      update();
    }

    private void update() {
      setIcon(data.isAudio() ? ICON_AUDIO : ICON_IMAGE);
      setLabel(data.getName());
    }

    @Override
    boolean isLeaf() {
      return true;
    }

    Data getData() {
      return data;
    }

  }

  /**
   * NetworkSetsNode is a node that displays training and test sets of NeuralNetwork.
   */
  static class NetworkSetsNode extends ModelNode {

    private final NeuralNetwork network;
    private final DataSetNode trainingSetNode;
    private final DataSetNode testSetNode;

    NetworkSetsNode(NeuralNetwork network, List<Data> trainingSet, List<Data> testSet) {
      super(ICON_MLP);
      this.network = network;
      trainingSetNode = new DataSetNode("Training set", trainingSet);
      trainingSetNode.setIcon(ICON_TRAININGSET);
      testSetNode = new DataSetNode("Test set", testSet);
      testSetNode.setIcon(ICON_TESTSET);
      update();
    }

    private void update() {
      setLabel("for " + network.getName());
      trainingSetNode.update();
      testSetNode.update();
    }

    @Override
    int getIndexOf(ModelNode child) {
      if (child == trainingSetNode) {
        return 0;
      }
      return (child == testSetNode) ? 1 : -1;
    }

    @Override
    ModelNode getChild(int index) {
      if (index == 0) {
        return trainingSetNode;
      }
      return (index == 1) ? testSetNode : null;
    }

    @Override
    int getChildsCount() {
      return 2;
    }

    NeuralNetwork getNetwork() {
      return network;
    }

    DataSetNode getTrainingSetNode() {
      return trainingSetNode;
    }

    DataSetNode getTestSetNode() {
      return testSetNode;
    }

  }

  /**
   * RootNode is the root node containing a NetworkListNode and DatasetsNode.
   */
  class RootNode extends ModelNode {

    private final NetworksNode nln;
    private final DatasetsNode dn;

    RootNode(NetworksNode nln, DatasetsNode dn) {
      super(null, "ROOT");
      this.nln = nln;
      this.dn = dn;
    }

    private void update() {
      nln.update();
      dn.update();
    }

    @Override
    int getIndexOf(ModelNode child) {
      if (child == nln) {
        return 0;
      }
      return (child == dn) ? 1 : -1;
    }

    @Override
    ModelNode getChild(int index) {
      if (index == 0) {
        return nln;
      }
      return (index == 1) ? dn : null;
    }

    @Override
    int getChildsCount() {
      return 2;
    }

  }

  /**
   * NetworkListNode is a node that displays the list of all loaded networks in the tree model.
   */
  class NetworksNode extends ModelNode {

    private final List<NeuralNetwork> nets;
    private final Dictionary<NeuralNetwork, NetworkNode> netsNodes;

    NetworksNode(List<NeuralNetwork> nets, Dictionary<NeuralNetwork, NetworkNode> netsNodes) {
      super(null, "Networks");
      this.nets = nets;
      this.netsNodes = netsNodes;
    }

    private void update() {
      for (NeuralNetwork n : nets) {
        netsNodes.get(n).update();
      }
    }

    @Override
    int getIndexOf(ModelNode child) {
      if (child instanceof NetworkNode) {
        return nets.indexOf(((NetworkNode) child).getNetwork());
      }
      return -1;
    }

    @Override
    ModelNode getChild(int index) {
      return netsNodes.get(nets.get(index));
    }

    @Override
    int getChildsCount() {
      return nets.size();
    }

  }

  /**
   * NetworkNode is a node that displays the structure of NeuralNetwork in the tree model.
   */
  class NetworkNode extends ModelNode {

    private final NeuralNetwork network;
    private final IsTrainedNode isTrainedNode;
    private final List<LayerNode> layerNodes = new Vector<>();

    NetworkNode(NeuralNetwork network) {
      super(ICON_MLP);
      this.network = network;
      isTrainedNode = new IsTrainedNode(network);
      update();
    }

    private void update() {
      setLabel(network.getName());
      isTrainedNode.update();
      layerNodes.clear();
      for (int i = 0; i < network.getNumLayers(); i++) {
        layerNodes.add(new LayerNode(network, i));
      }
    }

    @Override
    int getIndexOf(ModelNode child) {
      if (child == isTrainedNode) {
        return 0;
      }
      if (child instanceof LayerNode) {
        @SuppressWarnings("SuspiciousMethodCalls") int i = nets.indexOf(child);
        return (i == -1) ? -1 : i + 1;
      }
      return -1;
    }

    @Override
    ModelNode getChild(int index) {
      if (index == 0) {
        return isTrainedNode;
      }
      return layerNodes.get(index - 1);
    }

    @Override
    int getChildsCount() {
      return layerNodes.size() + 1;
    }

    NeuralNetwork getNetwork() {
      return network;
    }

  }

}
