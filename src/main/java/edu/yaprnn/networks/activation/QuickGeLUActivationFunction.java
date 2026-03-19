package edu.yaprnn.networks.activation;

import edu.yaprnn.networks.weights.Initialization;
import edu.yaprnn.networks.weights.UniformInitializer;
import java.util.Random;

public final class QuickGeLUActivationFunction implements ActivationFunction {

  /** Scaling constant from the QuickGeLU formulation. */
  private static final float ALPHA = 1.702f;

  @Override
  public float[] apply(float[] v) {
    var h = new float[v.length];
    for (var i = 0; i < v.length; i++) {
      var x = v[i];
      h[i] = x * Sigmoid.of(ALPHA * x);
    }
    return h;
  }

  @Override
  public float[] derivative(float[] h, float[] v) {
    var d = new float[h.length];
    for (var i = 0; i < h.length; i++) {
      var s = Sigmoid.of(ALPHA * v[i]);
      d[i] = s + ALPHA * h[i] * (1f - s);
    }
    return d;
  }

  @Override
  public float[] derivative(float[] v) {
    var d = new float[v.length];
    for (var i = 0; i < v.length; i++) {
      var x = v[i];
      var s = Sigmoid.of(ALPHA * x);
      d[i] = s + ALPHA * x * s * (1f - s);
    }
    return d;
  }

  @Override
  public float[] initialize(Random random, int count, int outputSize) {
    return Initialization.shell(random, count, outputSize, UniformInitializer::he);
  }

  @Override
  public String toString() {
    return "QuickGeLU: x * Sigmoid(x)";
  }
}
