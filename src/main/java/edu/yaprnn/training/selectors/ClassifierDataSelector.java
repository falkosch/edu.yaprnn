package edu.yaprnn.training.selectors;

import edu.yaprnn.networks.activation.ActivationFunction;
import edu.yaprnn.samples.model.ImageSample;
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
    var maxTarget = new float[h.length];
    maxTarget[Floats.argMax(h)] = 1f;
    return maxTarget;
  }

  @Override
  public int getOutputWidth(ImageSample sample) {
    return 1;
  }

  @Override
  public String toString() {
    return ClassifierDataSelector.class.getSimpleName();
  }
}
