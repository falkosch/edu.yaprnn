package edu.yaprnn.networks;

import edu.yaprnn.networks.functions.ActivationFunction;

/**
 * Tracks the inputs/outputs in the layers of the network while feeding a sample to it. Denotation
 * of parameters and values is similar to the one in {@link ActivationFunction}.
 *
 * @param v outputs before activation and after matrix-transform by weights between two layers
 * @param h outputs after activation
 */
public record Layer(float[] v, float[] h) {

  public static Layer output(Layer[] layers) {
    return layers[outputIndex(layers)];
  }

  public static int outputIndex(Layer[] layers) {
    return layers.length - 1;
  }
}
