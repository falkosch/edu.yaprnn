package edu.yaprnn.functions;

public final class SignumActivationFunction implements ActivationFunction {

  @Override
  public float[] apply(float[] v) {
    var h = new float[v.length];
    for (var i = 0; i < v.length; i++) {
      h[i] = Math.signum(v[i]);
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
      // good enough as a surrogate derivative
      d[i] = 1f;
    }
    return d;
  }

  @Override
  public String toString() {
    return "Signum: sign(v)";
  }
}
