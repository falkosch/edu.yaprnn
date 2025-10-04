package edu.yaprnn.functions;

import java.util.Arrays;

public final class LinearActivationFunction implements ActivationFunction {

  @Override
  public float[] apply(float[] v) {
    return Arrays.copyOf(v, v.length);
  }

  @Override
  public String toString() {
    return "Linear: v";
  }
}
