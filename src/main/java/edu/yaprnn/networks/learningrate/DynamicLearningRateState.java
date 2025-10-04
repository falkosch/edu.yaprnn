package edu.yaprnn.networks.learningrate;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

/**
 * Increases the learning rate when the error resulting from the last training iteration increased.
 * Decreases the learning rate when the error decreased.
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class DynamicLearningRateState implements LearningRateState {

  private final DynamicLearningRate config;

  private final float lastTrainingError;

  private final float current;

  public static DynamicLearningRateState from(float ascend, float descend, float initial) {
    return new DynamicLearningRateState(new DynamicLearningRate(ascend, descend), Float.MAX_VALUE,
        initial);
  }

  public float current() {
    return current;
  }

  @Override
  public LearningRateState updateRate(float trainingError) {
    return new DynamicLearningRateState(config, trainingError, nextRateFrom(trainingError));
  }

  private float nextRateFrom(float trainingError) {
    if (lastTrainingError > trainingError) {
      return current * config.ascend();
    } else if (lastTrainingError < trainingError) {
      return current * config.descend();
    }
    return current;
  }
}
