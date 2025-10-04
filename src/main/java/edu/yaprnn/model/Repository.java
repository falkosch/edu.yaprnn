package edu.yaprnn.model;

import edu.yaprnn.events.OnRepositoryElementsChangedRouter;
import edu.yaprnn.events.OnRepositoryElementsRemovedRouter;
import edu.yaprnn.gui.model.NetworksTreeModel;
import edu.yaprnn.networks.MultiLayerNetwork;
import edu.yaprnn.networks.templates.MultiLayerNetworkTemplate;
import edu.yaprnn.samples.model.Sample;
import edu.yaprnn.training.TrainingData;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;

/**
 * Represents the data of the artificial neural networks.
 *
 * @see NetworksTreeModel is the UI representation of this data
 */
@Getter
@Singleton
public final class Repository {

  private final List<Sample> samples = new ArrayList<>();
  private final Map<String, Sample> samplesGroupedByName = new HashMap<>();
  private final List<TrainingData> trainingDataList = new ArrayList<>();
  private final List<MultiLayerNetworkTemplate> multiLayerNetworkTemplates = new ArrayList<>();
  private final List<MultiLayerNetwork> multiLayerNetworks = new ArrayList<>();

  @Inject
  OnRepositoryElementsChangedRouter onRepositoryElementsChangedRouter;
  @Inject
  OnRepositoryElementsRemovedRouter onRepositoryElementsRemovedRouter;

  public void addSamples(Collection<? extends Sample> items) {
    samples.addAll(items);
    items.forEach(sample -> samplesGroupedByName.put(sample.getName(), sample));
    onRepositoryElementsChangedRouter.fireEvent(Sample.class, List.copyOf(items));
  }

  public List<Sample> querySamplesByName(List<String> sampleNames) {
    return sampleNames.stream().map(samplesGroupedByName::get).toList();
  }

  public void removeSamples(Collection<? extends Sample> items) {
    samples.removeAll(items);
    items.forEach(sample -> samplesGroupedByName.remove(sample.getName(), sample));
    onRepositoryElementsRemovedRouter.fireEvent(Sample.class, List.copyOf(items));
  }

  public void addTrainingData(Collection<? extends TrainingData> items) {
    trainingDataList.addAll(items);
    onRepositoryElementsChangedRouter.fireEvent(TrainingData.class, List.copyOf(items));
  }

  public void removeTrainingData(Collection<? extends TrainingData> items) {
    trainingDataList.removeAll(items);
    onRepositoryElementsRemovedRouter.fireEvent(TrainingData.class, List.copyOf(items));
  }

  public void addMultiLayerNetworkTemplates(Collection<? extends MultiLayerNetworkTemplate> items) {
    multiLayerNetworkTemplates.addAll(items);
    onRepositoryElementsChangedRouter.fireEvent(MultiLayerNetworkTemplate.class,
        List.copyOf(items));
  }

  public void removeMultiLayerNetworkTemplates(
      Collection<? extends MultiLayerNetworkTemplate> items) {
    multiLayerNetworkTemplates.removeAll(items);
    onRepositoryElementsRemovedRouter.fireEvent(MultiLayerNetworkTemplate.class,
        List.copyOf(items));
  }

  public void addMultiLayerNetworks(Collection<? extends MultiLayerNetwork> items) {
    multiLayerNetworks.addAll(items);
    onRepositoryElementsChangedRouter.fireEvent(MultiLayerNetwork.class, List.copyOf(items));
  }

  public void removeMultiLayerNetworks(Collection<? extends MultiLayerNetwork> items) {
    multiLayerNetworks.removeAll(items);
    onRepositoryElementsRemovedRouter.fireEvent(MultiLayerNetwork.class, List.copyOf(items));
  }
}
