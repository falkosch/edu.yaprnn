package edu.yaprnn.training;

import edu.yaprnn.model.Repository;
import edu.yaprnn.samples.model.Sample;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RequiredArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class TrainingData {

  private String name;
  private List<String> trainingSampleNames;
  private List<String> devTestSampleNames;
  private DataSelector dataSelector;

  public List<Sample> getTrainingSamples(Repository repository) {
    return repository.querySamplesByName(trainingSampleNames);
  }

  public List<Sample> getDevTestSamples(Repository repository) {
    return repository.querySamplesByName(devTestSampleNames);
  }

  public List<Sample> shuffleTrainingSamples(ShuffleService shuffleService) {
    return shuffleService.shuffleSamples(trainingSampleNames);
  }

  @Override
  public String toString() {
    return "%s (%d|%d)".formatted(name, trainingSampleNames.size(), devTestSampleNames.size());
  }
}
