package edu.yaprnn.networks.activation;

import edu.yaprnn.networks.weights.Initialization;
import edu.yaprnn.networks.weights.UniformInitializer;
import java.util.Random;

public final class SignumActivationFunction implements ActivationFunction {

  @Override
  public float[] apply(float[] v) {
    var h = new float[v.length];
    for (var i = 0; i < v.length; i++) {
      h[i] = Math.signum(v[i]);
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
      // good enough as a surrogate derivative
      d[i] = 1f;
    }
    return d;
  }

  @Override
  public float[] initialize(Random random, int count, int outputSize) {
    return Initialization.shell(random, count, outputSize, UniformInitializer::normalized);
  }

  @Override
  public String toString() {
    return "Signum: sign(v)";
  }
}
