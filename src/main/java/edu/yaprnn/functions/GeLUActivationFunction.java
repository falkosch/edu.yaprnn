package edu.yaprnn.functions;

import java.util.Random;

public final class GeLUActivationFunction implements ActivationFunction {

  @Override
  public float[] apply(float[] v) {
    var h = new float[v.length];
    for (var i = 0; i < v.length; i++) {
      var x = v[i];
      h[i] = 0.5f * x * (1f + ErfApproximation.of(x));
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
      var x = v[i];
      var erf = ErfApproximation.of(x);
      d[i] = 0.5f * (1f + erf + (x - x * erf * erf) * ErfApproximation.transformDerivative(x));
    }
    return d;
  }

  @Override
  public float[] initialize(Random random, int count, int outputSize) {
    return Initialization.shell(random, count, outputSize, Initialization::xavierUniform);
  }

  @Override
  public String toString() {
    return "GeLU: x/2 * (1 + erf[x / sqrt(2)])";
  }

  /**
   * Approximation of the error function as proposed in https://arxiv.org/pdf/1606.08415
   */
  private static final class ErfApproximation {

    private static final float SQRT_2_DIVIDED_BY_PI = 0.7978845608028654f;

    private static final float ALPHA = 0.044715f;
    private static final float DERIVATIVE_ALPHA = SQRT_2_DIVIDED_BY_PI * 3f * ALPHA;

    private static float of(float x) {
      return (float) Math.tanh(transform(x));
    }

    private static float transform(float x) {
      return SQRT_2_DIVIDED_BY_PI * (x + ALPHA * x * x * x);
    }

    private static float transformDerivative(float x) {
      return SQRT_2_DIVIDED_BY_PI + DERIVATIVE_ALPHA * x * x;
    }
  }
}
