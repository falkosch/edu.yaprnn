package edu.yaprnn.functions;

import java.util.Random;

public final class TangentHyperbolicHardActivationFunction implements ActivationFunction {

  @Override
  public float[] apply(float[] v) {
    var h = new float[v.length];
    for (var i = 0; i < v.length; i++) {
      h[i] = Math.clamp(v[i], -1f, 1f);
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
      d[i] = -1f < v[i] && v[i] < 1f ? 1f : 0f;
    }
    return d;
  }

  @Override
  public float[] initialize(Random random, int count, int outputSize) {
    return Initialization.shell(random, count, outputSize, Initialization::xavierUniform);
  }

  @Override
  public String toString() {
    return "TanH Hard: max(min[v, 1], -1)";
  }
}
