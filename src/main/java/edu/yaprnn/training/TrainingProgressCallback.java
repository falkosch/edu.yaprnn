package edu.yaprnn.training;

import edu.yaprnn.networks.AccuracyResult;

/**
 * Callback for training progress updates. Called after each epoch.
 */
@FunctionalInterface
public interface TrainingProgressCallback {

  void onEpochComplete(int iteration, float learningRate, AccuracyResult training,
      AccuracyResult devTest);
}
