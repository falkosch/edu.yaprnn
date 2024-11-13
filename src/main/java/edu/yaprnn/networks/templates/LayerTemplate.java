package edu.yaprnn.networks.templates;

import edu.yaprnn.networks.MultiLayerNetwork;
import edu.yaprnn.networks.functions.ActivationFunction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

/**
 * Represents a template of a layer in a {@link MultiLayerNetwork} topology.
 */
@RequiredArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder(toBuilder = true)
public final class LayerTemplate {

  /**
   * Count of neurons in this layer.
   */
  private int size;
  /**
   * Scales the values that should be propagated as output.
   */
  private ActivationFunction activationFunction;
}
