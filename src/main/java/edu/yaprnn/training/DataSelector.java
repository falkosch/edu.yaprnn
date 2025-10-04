package edu.yaprnn.training;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import edu.yaprnn.networks.activation.ActivationFunction;
import edu.yaprnn.samples.model.Sample;

@JsonTypeInfo(use = Id.CLASS)
public sealed interface DataSelector permits ClassifierDataSelector, OnlyInputDataSelector,
    TargetAsInputDataSelector {

  float[] input(Sample sample);

  float[] target(Sample sample, ActivationFunction outputActivationFunction);

  float[] postprocessOutput(float[] v, float[] h, ActivationFunction outputActivationFunction);
}
