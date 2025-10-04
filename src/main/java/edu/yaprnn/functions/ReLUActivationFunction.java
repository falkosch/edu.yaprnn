package edu.yaprnn.functions;

import edu.yaprnn.networks.WeightsDimension;
import java.util.Random;
import org.apache.commons.math3.random.GaussianRandomGenerator;
import org.apache.commons.math3.random.RandomGeneratorFactory;

public final class ReLUActivationFunction implements ActivationFunction {

  @Override
  public float[] apply(float[] v) {
    var h = new float[v.length];
    for (var i = 0; i < v.length; i++) {
      h[i] = Math.max(v[i], 0f);
    }
    return h;
  }

  @Override
  public float[] derivative(float[] h, float[] v) {
    return derivative(v);
  }

  @Override
  public float[] derivative(float[] v) {
    var d = new float[v.length];
    for (var i = 0; i < v.length; i++) {
      d[i] = v[i] >= 0f ? 1f : 0f;
    }
    return d;
  }

  @Override
  public float[] randomWeights(Random random, int count, int outputSize) {
    var generator = new GaussianRandomGenerator(
        RandomGeneratorFactory.createRandomGenerator(random));
    var weights = new float[count];
    var inputSize = WeightsDimension.from(weights, outputSize).inputSize();
    var deviation = Math.sqrt(2d / (inputSize + outputSize));

    // from-bias weights are zeroed/excluded
    for (int row = 0, w = 0; row < inputSize; row++) {
      for (var col = 0; col < outputSize; col++, w++) {
        weights[w] = (float) (deviation * generator.nextNormalizedDouble());
      }
    }
    return weights;
  }

  @Override
  public String toString() {
    return "ReLU: max(v, 0)";
  }
}
