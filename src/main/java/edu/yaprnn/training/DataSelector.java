package edu.yaprnn.training;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import edu.yaprnn.samples.model.Sample;

@JsonTypeInfo(use = Id.CLASS)
public interface DataSelector {

  float[] input(Sample sample);

  float[] target(Sample sample);
}
