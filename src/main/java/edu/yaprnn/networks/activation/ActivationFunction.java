package edu.yaprnn.networks.activation;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import java.util.Comparator;
import java.util.Random;

/**
 * Linear or non-linear transformation to scale values of an output of a layer. Depending on the
 * implementation, differences to excessively big values are mitigated.
 *
 * <p>Denotation:
 * <ul>
 *   <li>{@code v} - outputs before activation and after matrix-transform by weights between two layers</li>
 *   <li>{@code h} - outputs after activation</li>
 * </ul>
 */
@JsonTypeInfo(use = Id.CLASS)
public sealed interface ActivationFunction permits BinaryStepActivationFunction,
    GeLUActivationFunction, LinearActivationFunction, QuickGeLUActivationFunction,
    ReLUActivationFunction, SigmoidActivationFunction, SignumActivationFunction,
    SoftMaxActivationFunction, TangentHyperbolicActivationFunction,
    TangentHyperbolicHardActivationFunction, ThresholdActivationFunction {

  Comparator<ActivationFunction> COMPARATOR = Comparator.comparing(ActivationFunction::toString);

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

  float[] initialize(Random random, int count, int outputSize);
}
