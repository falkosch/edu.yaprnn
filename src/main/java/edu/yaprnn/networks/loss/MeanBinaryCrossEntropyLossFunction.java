package edu.yaprnn.networks.loss;

import edu.yaprnn.networks.activation.ActivationFunction;

/**
 * Computes the Mean Binary Cross-Entropy (BCE) loss and gradients for binary classification tasks.
 * It is defined as: {@code E = -1/n * sum(y * log(h) + (1 - y) * log(1 - h))}.
 */
public final class MeanBinaryCrossEntropyLossFunction implements LossFunction {

  private static final float EPS = 1e-7f;

  @Override
  public float[] computeOutputError(float[] v, float[] h, float[] target,
      ActivationFunction activationFunction) {
    var error = activationFunction.derivative(h, v);

    var normalization = -1f / h.length;
    var i = 0;
    for (var minLength = Math.min(h.length, target.length); i < minLength; i++) {
      var x = h[i];
      var xMxx = x - x * x;
      var y = target[i];
      error[i] *= xMxx == 0f ? 0f : normalization * (x - y) / xMxx;
    }
    // for any remaining h (when target is the smaller array), assume target=0
    for (; i < h.length; i++) {
      var x = h[i];
      var xMxx = x - x * x;
      error[i] *= xMxx == 0f ? 0f : normalization * x / xMxx;
    }

    return error;
  }

  @Override
  public float computeNetworkError(float[] h, float[] target) {
    var aligned = AlignedArrays.of(h, target);

    var loss = 0f;
    for (var i = 0; i < aligned.length(); i++) {
      var x = Math.max(EPS, Math.min(1f - EPS, aligned.h()[i]));
      var y = aligned.target()[i];
      loss -= y * (float) Math.log(x) + (1f - y) * (float) Math.log(1f - x);
    }

    return loss / aligned.length();
  }

  @Override
  public String toString() {
    return "MeanBinaryCrossEntropy: -1/n * sum(y * log(h) + (1 - y) * log(1 - h))";
  }
}
