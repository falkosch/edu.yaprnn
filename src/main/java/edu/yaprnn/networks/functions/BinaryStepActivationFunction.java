package edu.yaprnn.networks.functions;

import java.util.Random;

public final class BinaryStepActivationFunction implements ActivationFunction {

  @Override
  public float[] apply(float[] v) {
    var h = new float[v.length];
    for (var i = 0; i < v.length; i++) {
      h[i] = v[i] < 0f ? 0f : 1f;
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
    return Initialization.shell(random, count, outputSize, Initialization::uniform);
  }

  @Override
  public String toString() {
    return "BinaryStep: v < 0 ? 0 : 1";
  }
}
