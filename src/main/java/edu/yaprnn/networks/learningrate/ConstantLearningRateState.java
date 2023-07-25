package edu.yaprnn.networks.learningrate;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ConstantLearningRateState implements LearningRateState {

  private final ConstantLearningRate config;

  public static ConstantLearningRateState from(float learningRate) {
    return new ConstantLearningRateState(new ConstantLearningRate(learningRate));
  }

  public float current() {
    return config.value();
  }

  @Override
  public LearningRateState updateRate(float trainingError) {
    return this;
  }
}
