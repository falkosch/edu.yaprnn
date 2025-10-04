package edu.yaprnn.networks.loss;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import edu.yaprnn.networks.activation.ActivationFunction;
import java.util.Comparator;

/**
 * Quantifies the difference between actual and predicted outputs of a model, serving as a basis for
 * optimization during training. Implementations of this interface provide specific loss calculation
 * algorithms.
 */
@JsonTypeInfo(use = Id.CLASS)
public sealed interface LossFunction permits HalfSquaredErrorLossFunction {

  Comparator<LossFunction> COMPARATOR = Comparator.comparing(LossFunction::toString);

  /**
   * Computes the error gradient for the output layer of a neural network based on the given
   * predicted values, target values, and activation function.
   *
   * @param v                  predicted values before activation
   * @param h                  predicted values after activation
   * @param target             actual output values
   * @param activationFunction the activation function in the output layer
   * @return the error gradient with respect to the output layer
   */
  float[] computeOutputError(float[] v, float[] h, float[] target,
      ActivationFunction activationFunction);

  /**
   * Computes the network error between the predicted values after activation and the actual output
   * values for a single sample. This function is used to quantify the overall error in training the
   * neural network.
   *
   * @param h      predicted values after activation
   * @param target actual output values
   * @return the network error
   */
  float computeNetworkError(float[] h, float[] target);
}
