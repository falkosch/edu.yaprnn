package edu.yaprnn.training.selectors;

import edu.yaprnn.networks.activation.ActivationFunction;
import edu.yaprnn.samples.model.ImageSample;
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
  public int getOutputWidth(ImageSample sample) {
    return sample.getInputWidth();
  }

  @Override
  public String toString() {
    return OnlyInputDataSelector.class.getSimpleName();
  }
}
