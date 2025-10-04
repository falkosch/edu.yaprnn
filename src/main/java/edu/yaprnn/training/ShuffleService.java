package edu.yaprnn.training;

import edu.yaprnn.samples.model.Sample;
import edu.yaprnn.support.RandomConfigurer;
import edu.yaprnn.training.mappings.TrainingDataMapper;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.function.BiFunction;

@Singleton
public final class ShuffleService {

  @Named(RandomConfigurer.YAPRNN_RANDOM_BEAN)
  @Inject
  Random random;
  @Inject
  TrainingDataMapper trainingDataMapper;

  public <R> R partition(List<Sample> samples, float trainingPercentage, float devTestPercentage,
      BiFunction<List<String>, List<String>, R> trainingAndDevTestSetMapper) {
    var shuffledNames = trainingDataMapper.toNames(shuffleList(samples));

    var normalization = Math.max(1.0, trainingPercentage + devTestPercentage);
    var size = shuffledNames.size();
    var trainingSize = (int) (size * (trainingPercentage / normalization));
    var devTestSize = (int) (size * (devTestPercentage / normalization));

    var trainingList = new ArrayList<>(shuffledNames.subList(0, trainingSize));
    trainingList.sort(String::compareTo);
    var devTestList = new ArrayList<>(
        shuffledNames.subList(trainingSize, trainingSize + devTestSize));
    devTestList.sort(String::compareTo);

    return trainingAndDevTestSetMapper.apply(trainingList, devTestList);
  }

  public <T> List<T> shuffleList(List<T> items) {
    return shuffleList(items, random);
  }

  public <T> List<T> shuffleList(List<T> items, Random random) {
    var result = new ArrayList<>(items);
    Collections.shuffle(result, random);
    return result;
  }
}
