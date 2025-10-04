package edu.yaprnn.networks.learningrate;

/**
 * @param interval iterations until change of learning rate
 * @param descend  modifier of decrease when interval ends, should be greater than 0.0 and smaller
 *                 than 1.0
 */
public record EpochLearningRate(int interval, float descend) {

}
