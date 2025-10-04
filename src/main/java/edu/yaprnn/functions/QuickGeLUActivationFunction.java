package edu.yaprnn.functions;

public final class QuickGeLUActivationFunction implements ActivationFunction {

  @Override
  public float[] apply(float[] v) {
    var h = new float[v.length];
    for (var i = 0; i < v.length; i++) {
      var x = v[i];
      h[i] = x * Sigmoid.of(Sigmoid.ALPHA * x);
    }
    return h;
  }

  @Override
  public float[] derivative(float[] h, float[] v) {
    var d = new float[h.length];
    for (var i = 0; i < h.length; i++) {
      var s = Sigmoid.of(Sigmoid.ALPHA * v[i]);
      d[i] = s + Sigmoid.ALPHA * h[i] * (1f - s);
    }
    return d;
  }

  @Override
  public float[] derivative(float[] v) {
    var d = new float[v.length];
    for (var i = 0; i < v.length; i++) {
      var x = v[i];
      var s = Sigmoid.of(Sigmoid.ALPHA * x);
      d[i] = s + Sigmoid.ALPHA * x * s * (1f - s);
    }
    return d;
  }

  @Override
  public String toString() {
    return "QuickGeLU: x * Sigmoid(x)";
  }

  private static final class Sigmoid {

    private static final float ALPHA = 1.702f;

    private static float of(float x) {
      return 1f / (1f + (float) Math.exp(-x));
    }
  }
}
