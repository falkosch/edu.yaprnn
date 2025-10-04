package edu.yaprnn.gui.model;

import edu.yaprnn.gui.model.nodes.ActivationFunctionNode;
import edu.yaprnn.gui.model.nodes.LayerSizeNode;
import edu.yaprnn.gui.model.nodes.LayerTemplateNode;
import edu.yaprnn.gui.model.nodes.ModelNode;
import edu.yaprnn.gui.model.nodes.MultiLayerNetworkNode;
import edu.yaprnn.gui.model.nodes.MultiLayerNetworkTemplateNode;
import edu.yaprnn.gui.model.nodes.SampleNode;
import edu.yaprnn.gui.model.nodes.TrainingDataNode;
import edu.yaprnn.model.Repository;
import edu.yaprnn.networks.MultiLayerNetwork;
import edu.yaprnn.networks.activation.ActivationFunction;
import edu.yaprnn.networks.templates.LayerTemplate;
import edu.yaprnn.networks.templates.MultiLayerNetworkTemplate;
import edu.yaprnn.samples.model.Sample;
import edu.yaprnn.training.TrainingData;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Stream;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

/**
 * Displays a list of networks and a list samples in a {@link javax.swing.JTree}.
 *
 * <p>The tree model of the UI is as follows:
 * <pre>
 * RootNode (hidden)
 *   MultiLayerNetworkListNode
 *     MultiLayerNetworkNode...
 *       WeightsNode...
 *   MultiLayerNetworkTemplateListNode
 *     MultiLayerNetworkTemplateNode...
 *       LayerNode...
 *         LayerSizeNode
 *         ActivationFunctionNode
 *   TrainingDataListNode
 *     TrainingDataNode...
 *   SampleSetNode
 *     Sample...
 * </pre>
 */
@Singleton
public class NetworksTreeModel implements TreeModel {

  private final Collection<TreeModelListener> treeModelListeners = new HashSet<>();

  @Inject
  AllSamplesListNode allSamplesListNode;
  @Inject
  MultiLayerNetworkListNode multiLayerNetworkListNode;
  @Inject
  MultiLayerNetworkTemplateListNode multiLayerNetworkTemplateListNode;
  @Inject
  Repository repository;
  @Inject
  RootNode rootNode;
  @Inject
  TrainingDataListNode trainingDataListNode;

  public void remove(ModelNode modelNode) {
    if (modelNode instanceof SampleNode sampleNode) {
      remove(sampleNode.getSampleSupplier().get());
    } else if (modelNode instanceof TrainingDataNode trainingDataNode) {
      remove(trainingDataNode.getTrainingDataSupplier().get());
    } else if (modelNode instanceof MultiLayerNetworkTemplateNode multiLayerNetworkTemplateNode) {
      remove(multiLayerNetworkTemplateNode.getMultiLayerNetworkTemplateSupplier().get());
    } else if (modelNode instanceof LayerTemplateNode layerTemplateNode) {
      remove(layerTemplateNode.getMultiLayerNetworkTemplateSupplier().get(),
          layerTemplateNode.getLayerTemplateSupplier().get());
    } else if (modelNode instanceof MultiLayerNetworkNode multiLayerNetworkNode) {
      remove(multiLayerNetworkNode.getMultiLayerNetworkSupplier().get());
    } else if (modelNode == allSamplesListNode) {
      removeSamples(List.copyOf(repository.getSamples()));
    } else if (modelNode == trainingDataListNode) {
      removeTrainingData(List.copyOf(repository.getTrainingDataList()));
    } else if (modelNode == multiLayerNetworkTemplateListNode) {
      removeMultiLayerNetworkTemplates(List.copyOf(repository.getMultiLayerNetworkTemplates()));
    } else if (modelNode == multiLayerNetworkListNode) {
      removeMultiLayerNetworks(List.copyOf(repository.getMultiLayerNetworks()));
    }
  }

  public void remove(Sample sample) {
    removeSamples(List.of(sample));
  }

  public void removeSamples(Collection<? extends Sample> items) {
    repository.removeSamples(items);
    refreshTrainingDataList();
    refreshAllSamplesList();
  }

  public void add(Sample sample) {
    addSamples(List.of(sample));
  }

  public void addSamples(Collection<? extends Sample> items) {
    repository.addSamples(items);
    refreshAllSamplesList();
  }

  public void refreshAllSamplesList() {
    refreshNodes(allSamplesListNode);
  }

  private void refreshNodes(ModelNode subRoot, ModelNode... nodeList) {
    subRoot.refresh();
    Arrays.stream(nodeList).forEach(ModelNode::refresh);
    fireStructureChanged(
        Stream.concat(Stream.of(rootNode, subRoot), Arrays.stream(nodeList)).toArray());
  }

  private void fireStructureChanged(Object[] path) {
    treeModelListeners.forEach(x -> x.treeStructureChanged(new TreeModelEvent(this, path)));
  }

  public void add(MultiLayerNetwork multiLayerNetwork) {
    addMultiLayerNetworks(List.of(multiLayerNetwork));
  }

  public void addMultiLayerNetworks(Collection<? extends MultiLayerNetwork> items) {
    repository.addMultiLayerNetworks(items);
    refreshMultiLayerNetworkList();
  }

  public void refreshMultiLayerNetworkList() {
    refreshNodes(multiLayerNetworkListNode);
  }

  public void remove(MultiLayerNetwork multiLayerNetwork) {
    removeMultiLayerNetworks(List.of(multiLayerNetwork));
  }

  public void removeMultiLayerNetworks(Collection<? extends MultiLayerNetwork> items) {
    repository.removeMultiLayerNetworks(items);
    refreshMultiLayerNetworkList();
  }

  @Override
  public Object getRoot() {
    return rootNode;
  }

  @Override
  public Object getChild(Object parent, int index) {
    return parent instanceof ModelNode parentModelNode ? parentModelNode.getChild(index) : null;
  }

  @Override
  public int getChildCount(Object parent) {
    return parent instanceof ModelNode parentModelNode ? parentModelNode.getChildCount() : 0;
  }

  @Override
  public boolean isLeaf(Object node) {
    return !(node instanceof ModelNode modelNode) || modelNode.isLeaf();
  }

  @Override
  public void valueForPathChanged(TreePath treePath, Object newValue) {
    var lastPathComponent = treePath.getLastPathComponent();

    if (lastPathComponent instanceof TrainingDataNode trainingDataNode) {
      changeNameOfTrainingData(trainingDataNode, String.valueOf(newValue));
    }
    if (lastPathComponent instanceof MultiLayerNetworkTemplateNode multiLayerNetworkTemplateNode) {
      changeNameOfMultiLayerNetworkTemplate(multiLayerNetworkTemplateNode,
          String.valueOf(newValue));
    }
    if (lastPathComponent instanceof LayerSizeNode layerSizeNode) {
      changeLayerSizeOfLayerTemplate(layerSizeNode, (Integer) newValue);
    }
    if (lastPathComponent instanceof ActivationFunctionNode activationFunctionNode) {
      changeActivationFunctionOfLayerTemplate(activationFunctionNode,
          (ActivationFunction) newValue);
    }
    if (lastPathComponent instanceof MultiLayerNetworkNode multiLayerNetworkNode) {
      changeNameOfMultiLayerNetwork(multiLayerNetworkNode, String.valueOf(newValue));
    }

    refreshNodes(treePath);
  }

  private void changeNameOfTrainingData(TrainingDataNode trainingDataNode, String newValue) {
    trainingDataNode.getTrainingDataSupplier().get().setName(newValue);
  }

  private void changeNameOfMultiLayerNetworkTemplate(
      MultiLayerNetworkTemplateNode multiLayerNetworkTemplateNode, String newValue) {
    multiLayerNetworkTemplateNode.getMultiLayerNetworkTemplateSupplier().get().setName(newValue);
  }

  private void changeLayerSizeOfLayerTemplate(LayerSizeNode layerSizeNode, int newValue) {
    var multiLayerNetworkTemplate = layerSizeNode.getMultiLayerNetworkTemplateSupplier().get();
    var layerTemplate = layerSizeNode.getLayerTemplateSupplier().get();
    var layerIndex = multiLayerNetworkTemplate.indexOfLayer(layerTemplate);
    multiLayerNetworkTemplate.setLayerSize(layerIndex, newValue);
  }

  private void changeActivationFunctionOfLayerTemplate(
      ActivationFunctionNode activationFunctionNode, ActivationFunction newValue) {
    var networkTemplate = activationFunctionNode.getMultiLayerNetworkTemplateSupplier().get();
    var layerTemplate = activationFunctionNode.getLayerTemplateSupplier().get();
    var layerIndex = networkTemplate.indexOfLayer(layerTemplate);
    networkTemplate.setActivationFunction(layerIndex, newValue);
  }

  private void changeNameOfMultiLayerNetwork(MultiLayerNetworkNode multiLayerNetworkNode,
      String newValue) {
    multiLayerNetworkNode.getMultiLayerNetworkSupplier().get().setName(newValue);
  }

  public void refreshNodes(TreePath treePath) {
    Arrays.stream(treePath.getPath()).map(ModelNode.class::cast).forEach(ModelNode::refresh);
    fireStructureChanged(treePath.getPath());
  }

  @Override
  public int getIndexOfChild(Object parent, Object child) {
    if (parent instanceof ModelNode parentModelNode && child instanceof ModelNode childModelNode) {
      return parentModelNode.getIndexOf(childModelNode);
    }
    return -1;
  }

  @Override
  public void addTreeModelListener(TreeModelListener treeModelListener) {
    treeModelListeners.add(treeModelListener);
  }

  @Override
  public void removeTreeModelListener(TreeModelListener treeModelListener) {
    treeModelListeners.remove(treeModelListener);
  }

  public void add(TrainingData trainingData) {
    addTrainingData(List.of(trainingData));
  }

  public void addTrainingData(Collection<? extends TrainingData> items) {
    repository.addTrainingData(items);
    refreshTrainingDataList();
  }

  public void refreshTrainingDataList() {
    refreshNodes(trainingDataListNode);
  }

  public void remove(TrainingData trainingData) {
    removeTrainingData(List.of(trainingData));
  }

  public void add(MultiLayerNetworkTemplate multiLayerNetworkTemplate) {
    addMultiLayerNetworkTemplates(List.of(multiLayerNetworkTemplate));
  }

  public void addMultiLayerNetworkTemplates(Collection<? extends MultiLayerNetworkTemplate> items) {
    repository.addMultiLayerNetworkTemplates(items);
    refreshMultiLayerNetworkTemplateList();
  }

  public void refreshMultiLayerNetworkTemplateList() {
    refreshNodes(multiLayerNetworkTemplateListNode);
  }

  public void addLayerTemplateTo(MultiLayerNetworkTemplateNode multiLayerNetworkTemplateNode) {
    var multiLayerNetworkTemplate = multiLayerNetworkTemplateNode.getMultiLayerNetworkTemplateSupplier()
        .get();
    var lastLayerTemplate = multiLayerNetworkTemplate.getLayer(
        multiLayerNetworkTemplate.getLayers().size() - 1);
    multiLayerNetworkTemplate.addLayer(lastLayerTemplate.toBuilder().build());
    refreshNodes(multiLayerNetworkTemplateListNode, multiLayerNetworkTemplateNode);
  }

  public void remove(MultiLayerNetworkTemplate multiLayerNetworkTemplate) {
    removeMultiLayerNetworkTemplates(List.of(multiLayerNetworkTemplate));
  }

  public void removeTrainingData(Collection<? extends TrainingData> items) {
    repository.removeTrainingData(items);
    refreshTrainingDataList();
  }

  public void removeMultiLayerNetworkTemplates(
      Collection<? extends MultiLayerNetworkTemplate> items) {
    repository.removeMultiLayerNetworkTemplates(items);
    refreshMultiLayerNetworkTemplateList();
  }

  private void remove(MultiLayerNetworkTemplate multiLayerNetworkTemplate,
      LayerTemplate layerTemplate) {
    var layerTemplates = multiLayerNetworkTemplate.getLayers();

    if (layerTemplates.size() > 2) {
      layerTemplates.remove(layerTemplate);
      refreshMultiLayerNetworkTemplateList();
    }
  }
}
