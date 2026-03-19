package edu.yaprnn.networks.loss;

import edu.yaprnn.networks.activation.ActivationFunction;

/**
 * Computes the Half-Squared Error and gradients for training neural networks. It is defined as:
 * {@code E = 1/2 * sum([h - y]^2)}.
 */
public final class HalfSquaredErrorLossFunction implements LossFunction {

  @Override
  public float[] computeOutputError(float[] v, float[] h, float[] target,
      ActivationFunction activationFunction) {
    var error = activationFunction.derivative(h, v);

    var i = 0;
    for (var minLength = Math.min(h.length, target.length); i < minLength; i++) {
      error[i] *= h[i] - target[i];
    }
    // for any remaining h (when target is the smaller array), assume target=0
    for (; i < h.length; i++) {
      error[i] *= h[i];
    }

    return error;
  }

  @Override
  public float computeNetworkError(float[] h, float[] target) {
    var aligned = AlignedArrays.of(h, target);

    var sumSquaredError = 0f;
    for (var i = 0; i < aligned.length(); i++) {
      var residual = aligned.target()[i] - aligned.h()[i];
      sumSquaredError += residual * residual;
    }
    return 0.5f * sumSquaredError;
  }

  @Override
  public String toString() {
    return "HalfSquaredError: 1/2 * sum([h - y]^2)";
  }
}
