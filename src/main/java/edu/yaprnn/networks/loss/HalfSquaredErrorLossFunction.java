package edu.yaprnn.networks.loss;

import edu.yaprnn.networks.activation.ActivationFunction;
import java.util.Arrays;


/**
 * Computes the loss and gradients for the half-squared error function used in training neural
 * networks. This loss function represents the squared difference between the predicted and actual
 * values, normalized by a factor of 0.5.
 * <p>
 * The half-squared error is defined as: {@code E = 1/2 * sum([activation(v) - y]^2)}
 * <p>
 * This implementation calculates both the network error and the gradient of the output layer error
 * for use in backpropagation.
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
    for (; i < h.length; i++) {
      error[i] *= h[i];
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
    return 0.5f * sumSquaredError;
  }

  @Override
  public String toString() {
    return "HalfSquaredError: 1/2 * sum([activation(v) - y]^2)";
  }
}
