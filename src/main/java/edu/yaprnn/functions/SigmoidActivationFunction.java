package edu.yaprnn.functions;

public class SigmoidActivationFunction implements ActivationFunction {

  @Override
  public float[] apply(float[] v) {
    var h = new float[v.length];
    for (var i = 0; i < v.length; i++) {
      h[i] = 1f / (1f + (float) Math.exp(-v[i]));
    }
    return h;
  }

  @Override
  public float[] derivative(float[] h, float[] v) {
    var d = new float[h.length];
    for (var i = 0; i < h.length; i++) {
      d[i] = h[i] * (1f - h[i]);
    }
    return d;
  }

  @Override
  public float[] derivative(float[] v) {
    var d = new float[v.length];
    for (var i = 0; i < v.length; i++) {
      var h = 1f / (1f + (float) Math.exp(-v[i]));
      d[i] = h * (1f - h);
    }
    return d;
  }

  @Override
  public String toString() {
    return "Sigmoid: 1/(1+exp(-v))";
  }
}
