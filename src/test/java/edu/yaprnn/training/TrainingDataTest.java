package edu.yaprnn.training;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

class TrainingDataTest {

  @Test
  void shouldFormatToString() {
    var data = new TrainingData();
    data.setName("MNIST");
    data.setTrainingSampleNames(List.of("a", "b", "c"));
    data.setDevTestSampleNames(List.of("d"));

    assertThat(data.toString()).isEqualTo("MNIST (3|1)");
  }

  @Test
  void shouldReturnName() {
    var data = new TrainingData();
    data.setName("Test");
    assertThat(data.getName()).isEqualTo("Test");
  }

  @Test
  void shouldReturnSampleNames() {
    var training = List.of("s1", "s2");
    var devTest = List.of("s3");
    var data = new TrainingData();
    data.setTrainingSampleNames(training);
    data.setDevTestSampleNames(devTest);

    assertThat(data.getTrainingSampleNames()).isEqualTo(training);
    assertThat(data.getDevTestSampleNames()).isEqualTo(devTest);
  }
}
