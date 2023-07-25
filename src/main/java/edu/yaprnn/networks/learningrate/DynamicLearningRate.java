package edu.yaprnn.networks.learningrate;

/**
 * @param ascend  modifier of increase, should be greater than 1.0
 * @param descend modifier of decrease, should be greater than 0.0 and smaller than 1.0
 */
public record DynamicLearningRate(float ascend, float descend) {

}
