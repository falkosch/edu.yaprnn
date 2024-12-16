package edu.yaprnn.networks.loss;

import edu.yaprnn.networks.activation.ActivationFunction;
import java.util.Arrays;

/**
 * Computes the Mean Binary Cross-Entropy (BCE) loss and gradients for binary classification tasks.
 * It is defined as: {@code E = -1/n * sum(y * log(h) + (1 - y) * log(1 - h))}.
 */
public final class MeanBinaryCrossEntropyLossFunction implements LossFunction {

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
    var maxLength = Math.max(h.length, target.length);
    var safeH = Arrays.copyOf(h, maxLength);
    var safeT = Arrays.copyOf(target, maxLength);

    var loss = 0f;
    for (var i = 0; i < maxLength; i++) {
      var x = safeH[i];
      var y = safeT[i];
      loss -= y * (float) Math.log(x) + (1f - y) * (float) Math.log(1f - x);
    }

    return Float.isNaN(loss) ? Float.POSITIVE_INFINITY : loss / maxLength;
  }

  @Override
  public String toString() {
    return "MeanBinaryCrossEntropy: -1/n * sum(y * log(h) + (1 - y) * log(1 - h))";
  }
}
