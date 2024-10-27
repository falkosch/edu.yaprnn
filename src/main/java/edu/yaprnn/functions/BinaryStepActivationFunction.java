package edu.yaprnn.functions;

public class BinaryStepActivationFunction implements ActivationFunction {

  @Override
  public float[] apply(float[] v) {
    var h = new float[v.length];
    for (var i = 0; i < v.length; i++) {
      h[i] = v[i] < 0 ? 0 : 1;
    }
    return h;
  }

  @Override
  public float[] derivative(float[] h, float[] v) {
    return derivative(v);
  }

  @Override
  public float[] derivative(float[] v) {
    // all 0
    return new float[v.length];
  }

  @Override
  public String toString() {
    return "BinaryStep: v < 0 ? 0 : 1";
  }
}
