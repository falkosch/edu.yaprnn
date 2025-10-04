package edu.yaprnn.networks.functions;

import edu.yaprnn.networks.weights.GaussianInitializer;
import edu.yaprnn.networks.weights.Initialization;
import java.util.Arrays;
import java.util.Random;

public final class LinearActivationFunction implements ActivationFunction {

  @Override
  public float[] apply(float[] v) {
    return Arrays.copyOf(v, v.length);
  }

  @Override
  public float[] derivative(float[] h, float[] v) {
    return derivative(v);
  }

  @Override
  public float[] derivative(float[] v) {
    var d = new float[v.length];
    for (var i = 0; i < v.length; i++) {
      d[i] = 1f;
    }
    return d;
  }

  @Override
  public float[] initialize(Random random, int count, int outputSize) {
    return Initialization.shell(random, count, outputSize, GaussianInitializer::he);
  }

  @Override
  public String toString() {
    return "Linear: v";
  }
}
