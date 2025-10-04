package edu.yaprnn.networks;

import edu.yaprnn.networks.activation.ActivationFunction;
import jdk.incubator.vector.FloatVector;
import jdk.incubator.vector.VectorOperators;
import jdk.incubator.vector.VectorSpecies;

public class SimdSupport {

  public static final VectorSpecies<Float> SPECIES = FloatVector.SPECIES_128;

  public static float[] addLaneWise(float[] a, float[] b) {
    assert a.length == b.length;

    var vectorLength = SPECIES.length();
    var loopBound = SPECIES.loopBound(a.length);

    var result = new float[a.length];
    for (var i = 0; i < loopBound; i += vectorLength) {
      FloatVector.fromArray(SPECIES, a, i)
          .add(FloatVector.fromArray(SPECIES, b, i))
          .intoArray(result, i);
    }
    for (var i = loopBound; i < a.length; i++) {
      result[i] = a[i] + b[i];
    }
    return result;
  }

  public static void sumScalarMultiplicationLaneWise(float[] target, float[] source, float scalar,
      int targetStart, int sourceStart, int length) {
    assert targetStart >= 0;
    assert sourceStart >= 0;
    assert length > 0;
    assert targetStart + length <= target.length;
    assert sourceStart + length <= source.length;

    var vectorLength = SPECIES.length();
    var loopBound = SPECIES.loopBound(length);
    var targetLoopBound = targetStart + loopBound;
    var scalarBroadcast = FloatVector.broadcast(SPECIES, scalar);
    var sourceIndex = sourceStart;
    var targetLength = targetStart + length;

    for (var i = targetStart; i < targetLoopBound; i += vectorLength, sourceIndex += vectorLength) {
      FloatVector.fromArray(SPECIES, target, i)
          .add(FloatVector.fromArray(SPECIES, source, sourceIndex).mul(scalarBroadcast))
          .intoArray(target, i);
    }

    for (var i = targetLoopBound; i < targetLength; i++, sourceIndex++) {
      target[i] += source[sourceIndex] * scalar;
    }
  }

  public static float sumSquaredError(float[] actual, float[] prediction) {
    assert actual.length == prediction.length;

    var vectorLength = SPECIES.length();
    var loopBound = SPECIES.loopBound(actual.length);

    var partialSumVector = FloatVector.zero(SPECIES);
    for (var i = 0; i < loopBound; i += vectorLength) {
      var residualVector = FloatVector.fromArray(SPECIES, actual, i)
          .sub(FloatVector.fromArray(SPECIES, prediction, i));
      partialSumVector = partialSumVector.add(residualVector.mul(residualVector));
    }
    var sum = partialSumVector.reduceLanes(VectorOperators.ADD);
    for (var i = loopBound; i < actual.length; i++) {
      var residual = actual[i] - prediction[i];
      sum += residual * residual;
    }
    return sum;
  }

  public static float[] computeMeanSquaredErrorGradient(float[] actual, float[] prediction,
      float[] preActivation, ActivationFunction activationFunction) {
    assert actual.length == prediction.length;
    assert actual.length == preActivation.length;

    var vectorLength = SPECIES.length();
    var loopBound = SPECIES.loopBound(actual.length);

    var error = activationFunction.derivative(prediction, preActivation);

    for (var i = 0; i < loopBound; i += vectorLength) {
      FloatVector.fromArray(SPECIES, error, i)
          .mul(FloatVector.fromArray(SPECIES, prediction, i)
              .sub(FloatVector.fromArray(SPECIES, actual, i)))
          .intoArray(error, i);
    }

    for (var i = loopBound; i < actual.length; i++) {
      error[i] *= prediction[i] - actual[i];
    }

    return error;
  }

  public static void applyGradients(float[] weights, float learningRate, float momentum,
      float decayL1, float decayL2, float[] gradients, float[] thisWeights,
      float[] thisPreviousGradients) {
    var vectorLength = SPECIES.length();
    var loopBound = SPECIES.loopBound(weights.length);

    var learningRateVector = FloatVector.broadcast(SPECIES, learningRate);
    var momentumVector = FloatVector.broadcast(SPECIES, momentum);
    var decayL1Vector = FloatVector.broadcast(SPECIES, decayL1);

    var decayL2Times2 = decayL2 * 2f;
    var decayL2Times2Vector = FloatVector.broadcast(SPECIES, decayL2Times2);
    var oneAddMomentum = 1f + momentum;
    var oneAddMomentumVector = FloatVector.broadcast(SPECIES, oneAddMomentum);

    for (var i = 0; i < loopBound; i += vectorLength) {
      // weight = weights[i];
      var weightsVector = FloatVector.fromArray(SPECIES, weights, i);
      // decay = decayL1 * Math.signum(weight) + decayL2 * 2f * weight;
      var decayVector = decayL1Vector.mul(SimdSupport.signum(weightsVector))
          .add(decayL2Times2Vector.mul(weightsVector));
      // thisPreviousGradient = thisPreviousGradients[i];
      var thisPreviousGradientsVector = FloatVector.fromArray(SPECIES, thisPreviousGradients, i);
      // gradientUpdate = momentum * thisPreviousGradient - learningRate * (gradients[i] + decay);
      var gradientUpdateVector = momentumVector.mul(thisPreviousGradientsVector)
          .sub(learningRateVector.mul(
              FloatVector.fromArray(SPECIES, gradients, i).add(decayVector)));
      // thisWeights[i] += (1f + momentum) * gradientUpdate - momentum * thisPreviousGradient;
      FloatVector.fromArray(SPECIES, thisWeights, i)
          .add(oneAddMomentumVector.mul(gradientUpdateVector)
              .sub(momentumVector.mul(thisPreviousGradientsVector)))
          .intoArray(thisWeights, i);
      // thisPreviousGradients[i] = gradientUpdate;
      gradientUpdateVector.intoArray(thisPreviousGradients, i);
    }

    // Process remaining elements
    for (var i = loopBound; i < weights.length; i++) {
      var weight = weights[i];
      var decay = decayL1 * Math.signum(weight) + decayL2Times2 * weight;
      var thisPreviousGradient = thisPreviousGradients[i];
      var gradientUpdate = momentum * thisPreviousGradient - learningRate * (gradients[i] + decay);
      thisWeights[i] += oneAddMomentum * gradientUpdate - momentum * thisPreviousGradient;
      thisPreviousGradients[i] = gradientUpdate;
    }
  }

  private static FloatVector signum(FloatVector values) {
    assert values.species() == SPECIES;

    var zeros = FloatVector.zero(SPECIES);
    var ones = FloatVector.broadcast(SPECIES, 1f);
    return zeros.blend(ones, zeros.lt(values)).blend(ones.neg(), values.lt(zeros));
  }

  public static float[] computeLayerError(Layer inputLayer, float[] outputErrors, float[] weights,
      ActivationFunction activationFunction) {
    var vectorLength = SPECIES.length();
    var loopBound = SPECIES.loopBound(outputErrors.length);

    var inputErrors = activationFunction.derivative(inputLayer.h(), inputLayer.v());
    var w = 0;
    for (var row = 0; row < inputErrors.length; row++) {

      var errorSumVector = FloatVector.zero(SPECIES);
      for (var col = 0; col < loopBound; col += vectorLength, w += vectorLength) {
        errorSumVector = errorSumVector.add(FloatVector.fromArray(SPECIES, weights, w)
            .mul(FloatVector.fromArray(SPECIES, outputErrors, col)));
      }

      var errorSum = errorSumVector.reduceLanes(VectorOperators.ADD);
      for (var col = loopBound; col < outputErrors.length; col++, w++) {
        errorSum += weights[w] * outputErrors[col];
      }

      inputErrors[row] *= errorSum;
    }

    return inputErrors;
  }
}
