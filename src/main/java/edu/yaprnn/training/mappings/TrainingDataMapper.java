package edu.yaprnn.training.mappings;

import edu.yaprnn.samples.model.Sample;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants.ComponentModel;

@Mapper(componentModel = ComponentModel.CDI)
public interface TrainingDataMapper {

  List<String> toNames(List<Sample> samples);

  default String toName(Sample sample) {
    return sample.getName();
  }
}
