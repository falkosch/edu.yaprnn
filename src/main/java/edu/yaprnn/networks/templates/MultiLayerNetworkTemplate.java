package edu.yaprnn.networks.templates;

import edu.yaprnn.networks.MultiLayerNetwork;
import edu.yaprnn.networks.activation.ActivationFunction;
import edu.yaprnn.networks.loss.LossFunction;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

/**
 * Represents a template that can instantiate a {@link MultiLayerNetwork} that can be trained.
 *
 * <p>
 * The template specifies the topology of the {@link MultiLayerNetwork}, whereby:
 * <ul>
 * <li>Layers are indexed from <code>0</code> through <code>n-1</code>, given <code>n</code> Layers.</li>
 * <li>There must be always at least <code>n>=2</code> Layers.</li>
 * <li>Layer at <code>0</code> is the input layer.</li>
 * <li>Layer at <code>n-1</code> is the output layer.</li>
 * </ul>
 */
@RequiredArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder(toBuilder = true)
public final class MultiLayerNetworkTemplate {

  /**
   * Readable name of the multiLayerNetwork template.
   */
  private String name;
  /**
   * Ordered layer templates.
   */
  @Builder.Default
  private List<LayerTemplate> layers = new ArrayList<>();
  /**
   * Loss function for predicted values of the model.
   */
  private LossFunction lossFunction;

  public LayerTemplate getLayer(int layerIndex) {
    return layers.get(layerIndex);
  }

  public int indexOfLayer(LayerTemplate layerTemplate) {
    return layers.indexOf(layerTemplate);
  }

  public void addLayer(LayerTemplate value) {
    this.layers.add(value);
  }

  public void setLayerSize(int layerIndex, int value) {
    layers.get(layerIndex).setSize(value);
  }

  public void setActivationFunction(int layerIndex, ActivationFunction value) {
    layers.get(layerIndex).setActivationFunction(value);
  }

  /**
   * @return each layer size not accounting for 1 bias node
   */
  public int[] collectLayerSizes() {
    return layers.stream().mapToInt(LayerTemplate::getSize).toArray();
  }

  public ActivationFunction[] collectActivationFunctions() {
    return layers.stream()
        .map(LayerTemplate::getActivationFunction)
        .toArray(ActivationFunction[]::new);
  }

  @Override
  public String toString() {
    return "%s (%d)".formatted(name, layers.size());
  }
}
