package edu.yaprnn.functions;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import edu.yaprnn.networks.WeightsDimension;
import java.util.Random;

/**
 * Scales values of an output of a layer so that differences to excessively big values are mitigated
 * and do not dominate classification.
 *
 * <p>Denotation:
 * <ul>
 *   <li>{@code v} - outputs before activation and after matrix-transform by weights between two layers</li>
 *   <li>{@code h} - outputs after activation</li>
 * </ul>
 */
@JsonTypeInfo(use = Id.CLASS)
public interface ActivationFunction {

  /**
   * @param v outputs before activation
   * @return {@code h} outputs after activation
   */
  float[] apply(float[] v);

  /**
   * @param h outputs after activation
   * @param v outputs before activation
   * @return derivative of outputs
   */
  float[] derivative(float[] h, float[] v);

  /**
   * @param v outputs before activation
   * @return derivative of outputs
   */
  float[] derivative(float[] v);

  default float[] randomWeights(Random random, int count, int outputSize) {
    var weights = new float[count];
    var inputSize = WeightsDimension.from(weights, outputSize).inputSize();
    var xavier = (float) Math.sqrt(6.0 / (inputSize + outputSize));

    // from-bias weights are zeroed/excluded
    for (int row = 0, w = 0; row < inputSize; row++) {
      for (var col = 0; col < outputSize; col++, w++) {
        weights[w] = random.nextFloat(-xavier, xavier);
      }
    }
    return weights;
  }
}
