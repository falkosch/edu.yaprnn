package edu.yaprnn.training;

import edu.yaprnn.networks.activation.ActivationFunction;
import edu.yaprnn.samples.model.Sample;

public final class OnlyInputDataSelector implements DataSelector {

  @Override
  public float[] input(Sample sample) {
    return sample.getInput();
  }

  @Override
  public float[] target(Sample sample, ActivationFunction outputActivationFunction) {
    return outputActivationFunction.apply(sample.getInput());
  }

  @Override
  public float[] postprocessOutput(float[] v, float[] h,
      ActivationFunction outputActivationFunction) {
    return v;
  }

  @Override
  public String toString() {
    return OnlyInputDataSelector.class.getSimpleName();
  }
}
