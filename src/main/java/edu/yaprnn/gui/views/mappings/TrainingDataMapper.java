package edu.yaprnn.gui.views.mappings;

import edu.yaprnn.gui.views.mappings.RandomizeTrainingDataParameters;
import edu.yaprnn.samples.model.Sample;
import edu.yaprnn.training.ShuffleService;
import edu.yaprnn.training.TrainingData;
import jakarta.inject.Inject;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants.ComponentModel;

@Mapper(componentModel = ComponentModel.CDI)
public abstract class TrainingDataMapper {

  @Inject
  ShuffleService shuffleService;

  public TrainingData from(RandomizeTrainingDataParameters source, List<Sample> allSamples) {
    return shuffleService.partition(allSamples, source.trainingPercentage(),
        source.devTestPercentage(),
        (trainingSamples, devTestSamples) -> from(source, trainingSamples, devTestSamples));
  }

  public abstract TrainingData from(RandomizeTrainingDataParameters parameters, List<String> trainingSampleNames,
      List<String> devTestSampleNames);
}
