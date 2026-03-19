package edu.yaprnn.samples;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

class ImportServiceTest {

  final ImportService importService = new ImportService();
  final List<String> labels = List.of("A", "B", "C", "D");

  @Test
  void shouldCreateOneHotForFirstLabel() {
    var target = importService.toTarget("A", labels);

    assertThat(target).containsExactly(1f, 0f, 0f, 0f);
  }

  @Test
  void shouldCreateOneHotForLastLabel() {
    var target = importService.toTarget("D", labels);

    assertThat(target).containsExactly(0f, 0f, 0f, 1f);
  }

  @Test
  void shouldCreateOneHotForMiddleLabel() {
    var target = importService.toTarget("C", labels);

    assertThat(target).containsExactly(0f, 0f, 1f, 0f);
  }

  @Test
  void shouldHaveLengthMatchingLabelsCount() {
    var target = importService.toTarget("B", labels);

    assertThat(target).hasSize(labels.size());
  }
}
