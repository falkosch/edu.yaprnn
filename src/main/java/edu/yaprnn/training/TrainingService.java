package edu.yaprnn.training;

import edu.yaprnn.model.Repository;
import edu.yaprnn.networks.GradientMatrixService;
import edu.yaprnn.networks.learningrate.ConstantLearningRateState;
import edu.yaprnn.networks.learningrate.DynamicLearningRateState;
import edu.yaprnn.networks.learningrate.EpochLearningRateState;
import edu.yaprnn.networks.learningrate.LearningRateState;
import edu.yaprnn.samples.model.Sample;
import edu.yaprnn.training.selectors.DataSelector;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import lombok.extern.java.Log;

/**
 * Executes the training loop for a MultiLayerNetwork. Toolkit-agnostic — does not depend on Swing
 * or SWT.
 */
@Log
@Singleton
public class TrainingService {

  @Inject
  GradientMatrixService gradientMatrixService;
  @Inject
  Repository repository;
  @Inject
  ShuffleService shuffleService;

  /**
   * Runs the training loop. Blocks until training completes or the calling thread is interrupted.
   *
   * @param params   training hyperparameters and data/network selection
   * @param callback called after each epoch with accuracy results (may be called from any thread)
   */
  public void train(TrainingParameters params, TrainingProgressCallback callback) {
    var trainingData = Objects.requireNonNull(params.trainingData(),
        "Training data must be selected");
    var multiLayerNetwork = Objects.requireNonNull(params.multiLayerNetwork(),
        "Network must be selected");
    var dataSelector = Objects.requireNonNull(trainingData.getDataSelector(),
        "Training data has no data selector configured");

    var devTestSamples = repository.querySamplesByName(trainingData.getDevTestSampleNames());
    var trainingSamples = repository.querySamplesByName(trainingData.getTrainingSampleNames());

    if (trainingSamples.isEmpty()) {
      throw new IllegalStateException(
          "Training sample set is empty. None of the configured sample names match loaded samples");
    }
    if (devTestSamples.isEmpty()) {
      throw new IllegalStateException(
          "Dev/test sample set is empty. None of the configured sample names match loaded samples");
    }
    if (params.learningRate() <= 0f) {
      throw new IllegalStateException("Learning rate must be greater than 0");
    }

    try (var executor = Executors.newFixedThreadPool(params.maxParallelism(),
        Thread.ofVirtual().factory())) {

      var learningRateState = createLearningRateState(params);
      var trainingError = trackError(executor, params, multiLayerNetwork, -1, params.learningRate(),
          trainingSamples, devTestSamples, dataSelector, callback);

      for (var i = 0;
          i < params.maxIterations() && trainingError > params.maxTrainingError()
              && !Thread.currentThread().isInterrupted(); i++) {
        var samples = shuffleService.shuffleList(trainingSamples);
        var currentLearningRate = learningRateState.current();

        var iteration = i;
        measureIterationTime(
            () -> multiLayerNetwork.learnMiniBatch(gradientMatrixService, executor, samples,
                dataSelector, params.maxParallelism(), params.batchSize(), currentLearningRate,
                params.momentum(), params.decayL1(), params.decayL2()));

        learningRateState = learningRateState.updateRate(trainingError);
        trainingError = trackError(executor, params, multiLayerNetwork, iteration,
            currentLearningRate, samples, devTestSamples, dataSelector, callback);
      }
    }
  }

  private LearningRateState createLearningRateState(TrainingParameters params) {
    return switch (params.learningRateModifier()) {
      case PERIODIC -> EpochLearningRateState.from(params.learningRateChangeInterval(),
          params.learningRateDescend(), params.learningRate());
      case ADAPTIVE -> DynamicLearningRateState.from(params.learningRateAscend(),
          params.learningRateDescend(), params.learningRate());
      case CONSTANT -> ConstantLearningRateState.from(params.learningRate());
    };
  }

  private float trackError(java.util.concurrent.ExecutorService executor,
      TrainingParameters params,
      edu.yaprnn.networks.MultiLayerNetwork multiLayerNetwork, int iteration, float learningRate,
      List<Sample> samples, List<Sample> devTestSamples, DataSelector dataSelector,
      TrainingProgressCallback callback) {
    var trainingAccuracy = multiLayerNetwork.computeAccuracy(executor, samples, dataSelector,
        params.maxParallelism());
    var devTestAccuracy = multiLayerNetwork.computeAccuracy(executor, devTestSamples, dataSelector,
        params.maxParallelism());

    callback.onEpochComplete(iteration, learningRate, trainingAccuracy, devTestAccuracy);

    log.info(
        "[%s] lr=%s | training: accuracy=%s, error=%s | test: accuracy=%s, error=%s".formatted(
            iteration, learningRate, trainingAccuracy.hits(), trainingAccuracy.error(),
            devTestAccuracy.hits(), devTestAccuracy.error()));

    return trainingAccuracy.error();
  }

  private void measureIterationTime(Runnable runnable) {
    var t = System.nanoTime();
    runnable.run();
    var delta = System.nanoTime() - t;
    var iterationTime = delta / (float) Duration.ofSeconds(1).toNanos();
    log.info(() -> "Iteration time: %s".formatted(iterationTime));
  }
}
