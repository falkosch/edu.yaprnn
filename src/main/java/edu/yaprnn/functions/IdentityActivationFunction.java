package edu.yaprnn.functions;

import java.util.Arrays;

public class IdentityActivationFunction implements ActivationFunction {

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
    Arrays.fill(d, 1f);
    return d;
  }

  @Override
  public String toString() {
    return "Identity: v";
  }
}
