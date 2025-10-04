package edu.yaprnn.training;

import edu.yaprnn.networks.functions.ActivationFunction;
import edu.yaprnn.samples.model.Sample;
import edu.yaprnn.support.Floats;

public final class ClassifierDataSelector implements DataSelector {

  @Override
  public float[] input(Sample sample) {
    return sample.getInput();
  }

  @Override
  public float[] target(Sample sample, ActivationFunction outputActivationFunction) {
    return outputActivationFunction.apply(sample.getTarget());
  }

  @Override
  public float[] postprocessOutput(float[] v, float[] h,
      ActivationFunction outputActivationFunction) {
    var maxValue = Floats.max(h);
    var maxTarget = new float[h.length];
    for (var i = 0; i < maxTarget.length; i++) {
      maxTarget[i] = h[i] == maxValue ? 1 : 0;
    }
    return maxTarget;
  }

  @Override
  public String toString() {
    return ClassifierDataSelector.class.getSimpleName();
  }
}
