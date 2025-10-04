package edu.yaprnn.functions;

public class SoftMaxActivationFunction implements ActivationFunction {

  @Override
  public String toString() {
    return "SoftMax: exp(v[i])/sum(exp(v))";
  }

  @Override
  public float[] apply(float[] v) {
    var s = 0f;
    var h = new float[v.length];
    for (var i = 0; i < v.length; i++) {
      s += (h[i] = (float) Math.exp(v[i]));
    }
    for (var i = 0; i < v.length; i++) {
      h[i] /= s;
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
    var d = apply(v);
    for (var i = 0; i < v.length; i++) {
      d[i] *= (1f - d[i]);
    }
    return d;
  }
}
