package edu.yaprnn.networks.learningrate;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

/**
 * Updates the learning rate at the end of a training epoch.
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class EpochLearningRateState implements LearningRateState {

  private final EpochLearningRate config;

  private final int iterations;

  private final float current;

  public static EpochLearningRateState from(int interval, float descend, float initial) {
    return new EpochLearningRateState(new EpochLearningRate(interval, descend), 0, initial);
  }

  public float current() {
    return current;
  }

  @Override
  public LearningRateState updateRate(float trainingError) {
    return new EpochLearningRateState(config, iterations + 1, nextRate());
  }

  private float nextRate() {
    if (iterations > 0 && iterations % config.interval() == 0) {
      return current * config.descend();
    }
    return current;
  }
}
