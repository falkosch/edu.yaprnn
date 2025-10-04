package edu.yaprnn.training;

import edu.yaprnn.samples.model.Sample;

public class OnlyInputDataSelector implements DataSelector {

  @Override
  public float[] input(Sample sample) {
    return sample.getInput();
  }

  @Override
  public float[] target(Sample sample) {
    return sample.getInput();
  }

  @Override
  public String toString() {
    return OnlyInputDataSelector.class.getSimpleName();
  }
}
