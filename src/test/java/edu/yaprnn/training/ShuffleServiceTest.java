package edu.yaprnn.training;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

import edu.yaprnn.samples.model.Sample;
import edu.yaprnn.samples.model.SimpleSample;
import edu.yaprnn.training.mappings.TrainingDataMapper;
import java.lang.reflect.Field;
import java.security.SecureRandom;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

class ShuffleServiceTest {

  final ShuffleService shuffleService = new ShuffleService();

  @Test
  void shouldReturnPermutationOfInput() {
    var items = List.of("A", "B", "C", "D", "E");
    var random = new SecureRandom(new byte[42]);

    var result = shuffleService.shuffleList(items, random);

    assertThat(result).containsExactlyInAnyOrderElementsOf(items);
  }

  @Test
  void shouldProduceDeterministicResultWithSeededRandom() {
    var items = List.of("A", "B", "C", "D", "E");
    var seed = System.nanoTime();

    var result1 = shuffleService.shuffleList(items, new Random(seed));
    var result2 = shuffleService.shuffleList(items, new Random(seed));

    assertThat(result1).isEqualTo(result2);
  }

  @Test
  void shouldReturnEmptyListForEmptyInput() {
    var result = shuffleService.shuffleList(Collections.emptyList(), new Random());

    assertThat(result).isEmpty();
  }

  @Test
  void shouldNotModifyOriginalList() {
    var items = List.of("A", "B", "C");
    var original = List.copyOf(items);

    shuffleService.shuffleList(items, new Random());

    assertThat(items).isEqualTo(original);
  }

  @Nested
  @ExtendWith(MockitoExtension.class)
  class Partition {

    @Mock
    TrainingDataMapper trainingDataMapper;

    static SimpleSample sample(String name) {
      return SimpleSample.builder().name(name).labels(new String[]{"A"}).target(new float[]{1f})
          .input(new float[]{1f}).build();
    }

    private ShuffleService serviceWithMocks(Random random) throws Exception {
      var service = new ShuffleService();
      setField(service, "random", random);
      setField(service, "trainingDataMapper", trainingDataMapper);
      return service;
    }

    private void setField(Object target, String name, Object value) throws Exception {
      Field field = ShuffleService.class.getDeclaredField(name);
      field.setAccessible(true);
      field.set(target, value);
    }

    @Test
    void shouldPartitionAndSortResultsForDeterministicOrder() throws Exception {
      var samples = List.<Sample>of(sample("C"), sample("A"), sample("D"), sample("B"));
      when(trainingDataMapper.toNames(anyList()))
          .thenAnswer(inv -> inv.<List<Sample>>getArgument(0).stream()
              .map(Sample::getName).toList());
      var service = serviceWithMocks(new Random(42L));

      var result = service.partition(samples, 0.5f, 0.5f,
          (training, devTest) -> List.of(training, devTest));

      // Both partitions should be sorted regardless of shuffle order
      assertThat(result.get(0)).isSorted();
      assertThat(result.get(1)).isSorted();
      // All names should be accounted for across both partitions
      var allNames = new java.util.ArrayList<>(result.get(0));
      allNames.addAll(result.get(1));
      assertThat(allNames).containsExactlyInAnyOrder("A", "B", "C", "D");
    }

    @Test
    void shouldHandleNormalizationWhenPercentagesExceedOne() throws Exception {
      var samples = List.<Sample>of(sample("A"), sample("B"), sample("C"), sample("D"));
      when(trainingDataMapper.toNames(anyList()))
          .thenAnswer(inv -> inv.<List<Sample>>getArgument(0).stream()
              .map(Sample::getName).toList());
      var service = serviceWithMocks(new Random(42L));

      var result = service.partition(samples, 0.8f, 0.8f,
          (training, devTest) -> List.of(training, devTest));

      // With normalization, total should not exceed sample count
      var totalSize = result.get(0).size() + result.get(1).size();
      assertThat(totalSize).isLessThanOrEqualTo(samples.size());
    }
  }
}
