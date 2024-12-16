package edu.yaprnn.networks.loss;

import edu.yaprnn.networks.activation.ActivationFunction;
import java.util.Arrays;

/**
 * Computes the Mean Squared Error (MSE) and gradients for training neural networks. The MSE is
 * defined as: {@code E = 1/len(v) * sum([activation(v) - y]^2)}.
 */
public final class MeanSquaredErrorLossFunction implements LossFunction {

  @Override
  public float[] computeOutputError(float[] v, float[] h, float[] target,
      ActivationFunction activationFunction) {
    var error = activationFunction.derivative(h, v);

    var normalization = 2f / h.length;
    var i = 0;
    for (var minLength = Math.min(h.length, target.length); i < minLength; i++) {
      error[i] *= normalization * (h[i] - target[i]);
    }
    // for any remaining h (when target is the smaller array), assume target=0
    for (; i < h.length; i++) {
      error[i] *= normalization * h[i];
    }

    return error;
  }

  @Override
  public float computeNetworkError(float[] h, float[] target) {
    var maxLength = Math.max(h.length, target.length);
    var safeH = Arrays.copyOf(h, maxLength);
    var safeT = Arrays.copyOf(target, maxLength);

    var sumSquaredError = 0f;
    for (var i = 0; i < maxLength; i++) {
      var residual = safeT[i] - safeH[i];
      sumSquaredError += residual * residual;
    }
    return sumSquaredError / maxLength;
  }

  @Override
  public String toString() {
    return "MeanSquaredError: 1/n * sum([h - y]^2)";
  }
}
