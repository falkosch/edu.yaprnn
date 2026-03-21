package edu.yaprnn.training;

import edu.yaprnn.networks.MultiLayerNetwork;
import lombok.Builder;

@Builder
public record TrainingParameters(
    int maxIterations,
    float maxTrainingError,
    int batchSize,
    int maxParallelism,
    float learningRate,
    LearningRateModifier learningRateModifier,
    int learningRateChangeInterval,
    float learningRateAscend,
    float learningRateDescend,
    float momentum,
    float decayL1,
    float decayL2,
    TrainingData trainingData,
    MultiLayerNetwork multiLayerNetwork) {
}
