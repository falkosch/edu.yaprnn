package edu.yaprnn.functions;

public class TangentHyperbolicActivationFunction implements ActivationFunction {

  @Override
  public float[] apply(float[] v) {
    var h = new float[v.length];
    for (var i = 0; i < v.length; i++) {
      h[i] = (float) Math.tanh(v[i]);
    }
    return h;
  }

  @Override
  public float[] derivative(float[] h, float[] v) {
    var d = new float[h.length];
    for (var i = 0; i < h.length; i++) {
      d[i] = 1f - h[i] * h[i];
    }
    return d;
  }

  @Override
  public float[] derivative(float[] v) {
    var d = new float[v.length];
    for (var i = 0; i < v.length; i++) {
      var h = (float) Math.tanh(v[i]);
      d[i] = 1f - h * h;
    }
    return d;
  }

  @Override
  public String toString() {
    return "tanh(v)";
  }
}
