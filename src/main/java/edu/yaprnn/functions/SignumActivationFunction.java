package edu.yaprnn.functions;

public class SignumActivationFunction implements ActivationFunction {

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
    return new float[v.length]; // all 0
  }

  @Override
  public String toString() {
    return "Signum: sign(v)";
  }
}
