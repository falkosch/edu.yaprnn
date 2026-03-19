package edu.yaprnn.training;

import static org.assertj.core.api.Assertions.assertThat;

import java.security.SecureRandom;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.Test;

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
}
