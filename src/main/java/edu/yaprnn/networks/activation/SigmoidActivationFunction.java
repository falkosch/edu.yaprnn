package edu.yaprnn.networks.activation;

import edu.yaprnn.networks.weights.Initialization;
import edu.yaprnn.networks.weights.UniformInitializer;
import java.util.Random;

public final class SigmoidActivationFunction implements ActivationFunction {

  @Override
  public float[] apply(float[] v) {
    var h = new float[v.length];
    for (var i = 0; i < v.length; i++) {
      h[i] = Sigmoid.of(v[i]);
    }
    return h;
  }

  @Override
  public float[] derivative(float[] h, float[] v) {
    var d = new float[h.length];
    for (var i = 0; i < h.length; i++) {
      var y = h[i];
      d[i] = y * (1f - y);
    }
    return d;
  }

  @Override
  public float[] derivative(float[] v) {
    var d = new float[v.length];
    for (var i = 0; i < v.length; i++) {
      var y = Sigmoid.of(v[i]);
      d[i] = y * (1f - y);
    }
    return d;
  }

  @Override
  public float[] initialize(Random random, int count, int outputSize) {
    return Initialization.shell(random, count, outputSize, UniformInitializer::he);
  }

  @Override
  public String toString() {
    return "Sigmoid: 1 / (1 + exp[-v])";
  }

  private static final class Sigmoid {

    private static float of(float x) {
      return 1f / (1f + (float) Math.exp(-x));
    }
  }
}
