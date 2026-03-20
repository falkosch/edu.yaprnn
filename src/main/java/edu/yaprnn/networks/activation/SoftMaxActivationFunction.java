package edu.yaprnn.networks.activation;

import edu.yaprnn.networks.weights.Initialization;
import edu.yaprnn.networks.weights.UniformInitializer;
import java.util.Random;

/**
 * SoftMax activation function: {@code h[i] = exp(v[i]) / sum(exp(v))}.
 *
 * <p>The derivative uses the diagonal Jacobian approximation {@code h[i] * (1 - h[i])}, which is
 * only correct when combined with a cross-entropy loss function. The combined SoftMax +
 * cross-entropy gradient simplifies to {@code h - target} regardless of the off-diagonal Jacobian
 * terms. Do not pair this activation with squared-error loss functions.
 */
public final class SoftMaxActivationFunction implements ActivationFunction {

  @Override
  public String toString() {
    return "SoftMax: exp(v[i]) / sum(exp[v])";
  }

  @Override
  public float[] apply(float[] v) {
    var max = v[0];
    for (var i = 1; i < v.length; i++) {
      if (v[i] > max) {
        max = v[i];
      }
    }

    var s = 0f;
    var h = new float[v.length];
    for (var i = 0; i < v.length; i++) {
      s += (h[i] = (float) Math.exp(v[i] - max));
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
      // range of true derivative is actually a matrix,
      // but we can use the derivative of the sigmoid as approximation
      d[i] = h[i] * (1f - h[i]);
    }
    return d;
  }

  @Override
  public float[] derivative(float[] v) {
    var d = apply(v);
    for (var i = 0; i < v.length; i++) {
      // range of true derivative is actually a matrix,
      // but we can use the derivative of the sigmoid as approximation
      d[i] *= (1f - d[i]);
    }
    return d;
  }

  @Override
  public float[] initialize(Random random, int count, int outputSize) {
    return Initialization.shell(random, count, outputSize, UniformInitializer::xavier);
  }


}
