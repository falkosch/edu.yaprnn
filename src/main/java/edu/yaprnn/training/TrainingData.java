package edu.yaprnn.training;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RequiredArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public final class TrainingData {

  private String name;
  private List<String> trainingSampleNames;
  private List<String> devTestSampleNames;
  private DataSelector dataSelector;

  @Override
  public String toString() {
    return "%s (%d|%d)".formatted(name, trainingSampleNames.size(), devTestSampleNames.size());
  }
}
