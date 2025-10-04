package edu.yaprnn.training;

import edu.yaprnn.samples.model.Sample;

public class ClassifierDataSelector implements DataSelector {

  @Override
  public float[] input(Sample sample) {
    return sample.getInput();
  }

  @Override
  public float[] target(Sample sample) {
    return sample.getTarget();
  }

  @Override
  public String toString() {
    return ClassifierDataSelector.class.getSimpleName();
  }
}
