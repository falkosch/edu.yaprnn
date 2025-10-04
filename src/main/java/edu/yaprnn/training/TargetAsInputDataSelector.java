package edu.yaprnn.training;

import edu.yaprnn.samples.model.Sample;

public class TargetAsInputDataSelector implements DataSelector {

  @Override
  public float[] input(Sample sample) {
    return sample.getTarget();
  }

  @Override
  public float[] target(Sample sample) {
    return sample.getInput();
  }

  @Override
  public String toString() {
    return TargetAsInputDataSelector.class.getSimpleName();
  }
}
