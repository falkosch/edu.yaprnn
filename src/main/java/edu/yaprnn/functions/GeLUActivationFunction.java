package edu.yaprnn.functions;

public final class GeLUActivationFunction implements ActivationFunction {

  @Override
  public float[] apply(float[] v) {
    var h = new float[v.length];
    for (var i = 0; i < v.length; i++) {
      var x = v[i];
      h[i] = 0.5f * x * (1f + Erf.ofXDividedBySqrt2(x));
    }
    return h;
  }

  @Override
  public float[] derivative(float[] v) {
    var d = new float[v.length];
    for (var i = 0; i < v.length; i++) {
      var x = v[i];
      var expTerm = 0.398942f * x * (float) Math.exp(-0.5f * x * x);
      d[i] = 0.5f + 0.5f * x * Erf.ofXDividedBySqrt2(x) + expTerm;
    }
    return d;
  }

  @Override
  public String toString() {
    return "GeLU: x/2 * (1 + erf[x / sqrt(2)])";
  }

  /**
   * Approximation of the error function as proposed in https://arxiv.org/pdf/1606.08415
   */
  private static class Erf {

    private static final float SQRT_PI_HALF = 0.7978845608028654f;
    private static final float SQRT_PI_HALF2 = 0.044715f * SQRT_PI_HALF;

    private static float ofXDividedBySqrt2(float x) {
      return (float) Math.tanh(x * (SQRT_PI_HALF + SQRT_PI_HALF2 * x * x));
    }
  }
}
