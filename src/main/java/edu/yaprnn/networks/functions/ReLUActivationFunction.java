package edu.yaprnn.networks.functions;

import edu.yaprnn.networks.weights.Initialization;
import edu.yaprnn.networks.weights.UniformInitializer;
import java.util.Random;

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
  public float[] initialize(Random random, int count, int outputSize) {
    return Initialization.shell(random, count, outputSize, UniformInitializer::he);
  }

  @Override
  public String toString() {
    return "ReLU: max(v, 0)";
  }
}
