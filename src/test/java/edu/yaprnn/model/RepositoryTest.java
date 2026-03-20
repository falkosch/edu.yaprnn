package edu.yaprnn.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import edu.yaprnn.events.OnRepositoryElementsChangedRouter;
import edu.yaprnn.events.OnRepositoryElementsRemovedRouter;
import edu.yaprnn.networks.MultiLayerNetwork;
import edu.yaprnn.networks.templates.MultiLayerNetworkTemplate;
import edu.yaprnn.samples.model.Sample;
import edu.yaprnn.samples.model.SimpleSample;
import edu.yaprnn.training.TrainingData;
import java.lang.reflect.Field;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RepositoryTest {

  @Mock
  OnRepositoryElementsChangedRouter onRepositoryElementsChangedRouter;
  @Mock
  OnRepositoryElementsRemovedRouter onRepositoryElementsRemovedRouter;

  Repository repository;

  static SimpleSample sample(String name) {
    return SimpleSample.builder().name(name).labels(new String[]{"A"}).target(new float[]{1f})
        .input(new float[]{1f}).build();
  }

  @BeforeEach
  void setUp() throws Exception {
    repository = new Repository();
    setField("onRepositoryElementsChangedRouter", onRepositoryElementsChangedRouter);
    setField("onRepositoryElementsRemovedRouter", onRepositoryElementsRemovedRouter);
  }

  private void setField(String name, Object value) throws Exception {
    Field field = Repository.class.getDeclaredField(name);
    field.setAccessible(true);
    field.set(repository, value);
  }

  @Nested
  class GettersReturnUnmodifiableViews {

    @Test
    void samplesShouldBeUnmodifiable() {
      assertThatThrownBy(() -> repository.getSamples().add(sample("x")))
          .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void trainingDataListShouldBeUnmodifiable() {
      assertThatThrownBy(() -> repository.getTrainingDataList().add(null))
          .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void multiLayerNetworkTemplatesShouldBeUnmodifiable() {
      assertThatThrownBy(() -> repository.getMultiLayerNetworkTemplates().add(null))
          .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void multiLayerNetworksShouldBeUnmodifiable() {
      assertThatThrownBy(() -> repository.getMultiLayerNetworks().add(null))
          .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void samplesGroupedByNameShouldBeUnmodifiable() {
      assertThatThrownBy(() -> repository.getSamplesGroupedByName().put("x", sample("x")))
          .isInstanceOf(UnsupportedOperationException.class);
    }
  }

  @Nested
  class AddAndRemoveSamples {

    @Test
    void shouldAddSamplesAndIndexByName() {
      var s1 = sample("s1");
      var s2 = sample("s2");

      repository.addSamples(List.of(s1, s2));

      assertThat(repository.getSamples()).containsExactly(s1, s2);
      assertThat(repository.getSamplesGroupedByName()).containsEntry("s1", s1);
      assertThat(repository.getSamplesGroupedByName()).containsEntry("s2", s2);
      verify(onRepositoryElementsChangedRouter).fireEvent(eq(Sample.class), any());
    }

    @Test
    void shouldRemoveSamplesAndUpdateIndex() {
      var s1 = sample("s1");
      repository.addSamples(List.of(s1));

      repository.removeSamples(List.of(s1));

      assertThat(repository.getSamples()).isEmpty();
      assertThat(repository.getSamplesGroupedByName()).doesNotContainKey("s1");
      verify(onRepositoryElementsRemovedRouter).fireEvent(eq(Sample.class), any());
    }
  }

  @Nested
  class QuerySamplesByName {

    @Test
    void shouldReturnMatchingSamples() {
      var s1 = sample("s1");
      var s2 = sample("s2");
      repository.addSamples(List.of(s1, s2));

      var result = repository.querySamplesByName(List.of("s1", "s2"));

      assertThat(result).containsExactly(s1, s2);
    }

    @Test
    void shouldFilterOutMissingNames() {
      var s1 = sample("s1");
      repository.addSamples(List.of(s1));

      var result = repository.querySamplesByName(List.of("s1", "nonexistent", "alsoMissing"));

      assertThat(result).containsExactly(s1);
    }

    @Test
    void shouldReturnEmptyListForAllMissingNames() {
      var result = repository.querySamplesByName(List.of("missing1", "missing2"));

      assertThat(result).isEmpty();
    }
  }

  @Nested
  class AddAndRemoveTrainingData {

    @Mock
    TrainingData trainingData;

    @Test
    void shouldAddTrainingData() {
      repository.addTrainingData(List.of(trainingData));

      assertThat(repository.getTrainingDataList()).containsExactly(trainingData);
      verify(onRepositoryElementsChangedRouter).fireEvent(eq(TrainingData.class), any());
    }

    @Test
    void shouldRemoveTrainingData() {
      repository.addTrainingData(List.of(trainingData));

      repository.removeTrainingData(List.of(trainingData));

      assertThat(repository.getTrainingDataList()).isEmpty();
      verify(onRepositoryElementsRemovedRouter).fireEvent(eq(TrainingData.class), any());
    }
  }

  @Nested
  class AddAndRemoveMultiLayerNetworkTemplates {

    @Mock
    MultiLayerNetworkTemplate template;

    @Test
    void shouldAddTemplates() {
      repository.addMultiLayerNetworkTemplates(List.of(template));

      assertThat(repository.getMultiLayerNetworkTemplates()).containsExactly(template);
      verify(onRepositoryElementsChangedRouter).fireEvent(eq(MultiLayerNetworkTemplate.class),
          any());
    }

    @Test
    void shouldRemoveTemplates() {
      repository.addMultiLayerNetworkTemplates(List.of(template));

      repository.removeMultiLayerNetworkTemplates(List.of(template));

      assertThat(repository.getMultiLayerNetworkTemplates()).isEmpty();
      verify(onRepositoryElementsRemovedRouter).fireEvent(eq(MultiLayerNetworkTemplate.class),
          any());
    }
  }

  @Nested
  class AddAndRemoveMultiLayerNetworks {

    @Mock
    MultiLayerNetwork network;

    @Test
    void shouldAddNetworks() {
      repository.addMultiLayerNetworks(List.of(network));

      assertThat(repository.getMultiLayerNetworks()).containsExactly(network);
      verify(onRepositoryElementsChangedRouter).fireEvent(eq(MultiLayerNetwork.class), any());
    }

    @Test
    void shouldRemoveNetworks() {
      repository.addMultiLayerNetworks(List.of(network));

      repository.removeMultiLayerNetworks(List.of(network));

      assertThat(repository.getMultiLayerNetworks()).isEmpty();
      verify(onRepositoryElementsRemovedRouter).fireEvent(eq(MultiLayerNetwork.class), any());
    }
  }

  @Nested
  class GettersReflectMutations {

    @Test
    void shouldReflectAddedSamplesInUnmodifiableView() {
      var s1 = sample("s1");
      assertThat(repository.getSamples()).isEmpty();

      repository.addSamples(List.of(s1));

      assertThat(repository.getSamples()).containsExactly(s1);
    }
  }
}
