package edu.yaprnn.gui.model;

import edu.yaprnn.gui.model.nodes.ActivationFunctionNode;
import edu.yaprnn.gui.model.nodes.BiasNode;
import edu.yaprnn.gui.model.nodes.LayerSizeNode;
import edu.yaprnn.gui.model.nodes.LayerTemplateNode;
import edu.yaprnn.gui.model.nodes.LossFunctionNode;
import edu.yaprnn.gui.model.nodes.ModelNode;
import edu.yaprnn.gui.model.nodes.MultiLayerNetworkNode;
import edu.yaprnn.gui.model.nodes.MultiLayerNetworkTemplateNode;
import edu.yaprnn.gui.model.nodes.SampleNode;
import edu.yaprnn.gui.model.nodes.TrainingDataNode;
import edu.yaprnn.model.Repository;
import edu.yaprnn.networks.MultiLayerNetwork;
import edu.yaprnn.networks.activation.ActivationFunction;
import edu.yaprnn.networks.loss.LossFunction;
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
 *       LossFunctionNode
 *       BiasNode
 *       LayerNode...
 *         LayerSizeNode
 *         ActivationFunctionNode
 *   TrainingDataListNode
 *     TrainingDataNode...
 *       SampleNameListNode "Training"
 *         SampleNameNode...
 *       SampleNameListNode "Dev Test"
 *         SampleNameNode...
 *       DataSelectorNode
 *   AllSamplesListNode (extends SampleListNode)
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
    switch (modelNode) {
      case SampleNode node -> remove(node.getSampleSupplier().get());
      case TrainingDataNode node -> remove(node.getTrainingDataSupplier().get());
      case MultiLayerNetworkTemplateNode node -> remove(node.getTemplateSupplier().get());
      case LayerTemplateNode node -> remove(node.getMultiLayerNetworkTemplateSupplier().get(),
          node.getLayerTemplateSupplier().get());
      case MultiLayerNetworkNode node -> remove(node.getMultiLayerNetworkSupplier().get());
      case null, default -> {
        if (modelNode == allSamplesListNode) {
          removeSamples(List.copyOf(repository.getSamples()));
        } else if (modelNode == trainingDataListNode) {
          removeTrainingData(List.copyOf(repository.getTrainingDataList()));
        } else if (modelNode == multiLayerNetworkTemplateListNode) {
          removeMultiLayerNetworkTemplates(List.copyOf(repository.getMultiLayerNetworkTemplates()));
        } else if (modelNode == multiLayerNetworkListNode) {
          removeMultiLayerNetworks(List.copyOf(repository.getMultiLayerNetworks()));
        }
      }
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
  public void valueForPathChanged(TreePath path, Object newValue) {
    var component = path.getLastPathComponent();

    switch (component) {
      case TrainingDataNode node -> changeNameOfTrainingData(node, String.valueOf(newValue));
      case MultiLayerNetworkTemplateNode node ->
          changeNameOfMultiLayerNetworkTemplate(node, String.valueOf(newValue));
      case BiasNode node -> changeBiasOfMultiLayerNetworkTemplate(node, (Float) newValue);
      case LossFunctionNode node ->
          changeLossFunctionOfMultiLayerNetworkTemplate(node, (LossFunction) newValue);
      case LayerSizeNode node -> changeLayerSizeOfLayerTemplate(node, (Integer) newValue);
      case ActivationFunctionNode node ->
          changeActivationFunctionOfLayerTemplate(node, (ActivationFunction) newValue);
      case MultiLayerNetworkNode node ->
          changeNameOfMultiLayerNetwork(node, String.valueOf(newValue));
      case null, default -> throw new UnsupportedOperationException(
          "Update of unknown component: %s".formatted(component));
    }

    refreshNodes(path);
  }

  private void changeNameOfTrainingData(TrainingDataNode node, String newValue) {
    node.getTrainingDataSupplier().get().setName(newValue);
  }

  private void changeNameOfMultiLayerNetworkTemplate(MultiLayerNetworkTemplateNode node,
      String newValue) {
    node.getTemplateSupplier().get().setName(newValue);
  }

  private void changeBiasOfMultiLayerNetworkTemplate(BiasNode node, float newValue) {
    node.getMultiLayerNetworkTemplateSupplier().get().setBias(newValue);
  }

  private void changeLossFunctionOfMultiLayerNetworkTemplate(LossFunctionNode node,
      LossFunction newValue) {
    node.getMultiLayerNetworkTemplateSupplier().get().setLossFunction(newValue);
  }

  private void changeLayerSizeOfLayerTemplate(LayerSizeNode node, int newValue) {
    var multiLayerNetworkTemplate = node.getMultiLayerNetworkTemplateSupplier().get();
    var layerTemplate = node.getLayerTemplateSupplier().get();
    var layerIndex = multiLayerNetworkTemplate.getLayers().indexOf(layerTemplate);
    multiLayerNetworkTemplate.getLayers().get(layerIndex).setSize(newValue);
  }

  private void changeActivationFunctionOfLayerTemplate(ActivationFunctionNode node,
      ActivationFunction newValue) {
    var networkTemplate = node.getMultiLayerNetworkTemplateSupplier().get();
    var layerTemplate = node.getLayerTemplateSupplier().get();
    var layerIndex = networkTemplate.getLayers().indexOf(layerTemplate);
    networkTemplate.getLayers().get(layerIndex).setActivationFunction(newValue);
  }

  private void changeNameOfMultiLayerNetwork(MultiLayerNetworkNode node, String newValue) {
    node.getMultiLayerNetworkSupplier().get().setName(newValue);
  }

  public void refreshNodes(TreePath path) {
    Arrays.stream(path.getPath()).map(ModelNode.class::cast).forEach(ModelNode::refresh);
    fireStructureChanged(path.getPath());
  }

  @Override
  public int getIndexOfChild(Object parent, Object child) {
    if (parent instanceof ModelNode parentModelNode && child instanceof ModelNode childModelNode) {
      return parentModelNode.getIndexOf(childModelNode);
    }
    return -1;
  }

  @Override
  public void addTreeModelListener(TreeModelListener listener) {
    treeModelListeners.add(listener);
  }

  @Override
  public void removeTreeModelListener(TreeModelListener listener) {
    treeModelListeners.remove(listener);
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

  public void add(MultiLayerNetworkTemplate template) {
    addMultiLayerNetworkTemplates(List.of(template));
  }

  public void addMultiLayerNetworkTemplates(Collection<? extends MultiLayerNetworkTemplate> items) {
    repository.addMultiLayerNetworkTemplates(items);
    refreshMultiLayerNetworkTemplateList();
  }

  public void refreshMultiLayerNetworkTemplateList() {
    refreshNodes(multiLayerNetworkTemplateListNode);
  }

  public void addLayerTemplateTo(MultiLayerNetworkTemplateNode node) {
    var multiLayerNetworkTemplate = node.getTemplateSupplier().get();
    var lastLayerTemplate = multiLayerNetworkTemplate.getLayers().getLast();
    multiLayerNetworkTemplate.getLayers().add(lastLayerTemplate.toBuilder().build());
    refreshNodes(multiLayerNetworkTemplateListNode, node);
  }

  public void remove(MultiLayerNetworkTemplate template) {
    removeMultiLayerNetworkTemplates(List.of(template));
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
