package edu.yaprnn.networks.learningrate;

/**
 * Determines the learning rate in training a network.
 */
public interface LearningRateState {

  /**
   * @return the current learning rate in this {@link LearningRateState}
   */
  float current();

  /**
   * @param trainingError error given by the training iteration that updates this
   *                      {@link LearningRateState}
   * @return the updated {@link LearningRateState}
   */
  LearningRateState updateRate(float trainingError);
}
