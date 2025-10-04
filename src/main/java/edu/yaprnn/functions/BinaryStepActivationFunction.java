package edu.yaprnn.functions;

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
  public String toString() {
    return "BinaryStep: v < 0 ? 0 : 1";
  }
}
