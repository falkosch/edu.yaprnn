package edu.yaprnn.functions;

public final class SigmoidActivationFunction implements ActivationFunction {

  @Override
  public float[] apply(float[] v) {
    var h = new float[v.length];
    for (var i = 0; i < v.length; i++) {
      h[i] = Sigmoid.of(v[i]);
    }
    return h;
  }

  @Override
  public float[] derivative(float[] h, float[] v) {
    var d = new float[h.length];
    for (var i = 0; i < h.length; i++) {
      var y = h[i];
      d[i] = y * (1f - y);
    }
    return d;
  }

  @Override
  public float[] derivative(float[] v) {
    var d = new float[v.length];
    for (var i = 0; i < v.length; i++) {
      var y = Sigmoid.of(v[i]);
      d[i] = y * (1f - y);
    }
    return d;
  }

  @Override
  public String toString() {
    return "Sigmoid: 1 / (1 + exp[-v])";
  }

  private static final class Sigmoid {

    private static float of(float x) {
      return 1f / (1f + (float) Math.exp(-x));
    }
  }
}
