package edu.yaprnn.events;

import edu.yaprnn.networks.MultiLayerNetwork;
import jakarta.enterprise.event.Event;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.Getter;

@Singleton
public final class OnMultiLayerNetworkWeightsPreviewModifiedRouter {

  @Inject
  Event<OnMultiLayerNetworkWeightsPreviewModified> onMultiLayerNetworkWeightsPreviewModifiedEvent;

  @Getter
  private MultiLayerNetwork multiLayerNetwork;
  @Getter
  private int weightsIndex;
  @Getter
  private float zoom = 1f;
  @Getter
  private float gamma = -1f;

  void setWeights(@Observes OnMultiLayerNetworkWeightsSelected event) {
    setWeights(event.multiLayerNetwork(), event.weightsIndex());
  }

  public void setWeights(MultiLayerNetwork multiLayerNetwork, int weightsIndex) {
    this.multiLayerNetwork = multiLayerNetwork;
    this.weightsIndex = weightsIndex;
    fireEvent();
  }

  /** Public so TrainingFrame can trigger a preview refresh after each training iteration. */
  public void fireEvent() {
    onMultiLayerNetworkWeightsPreviewModifiedEvent.fire(current());
  }

  public OnMultiLayerNetworkWeightsPreviewModified current() {
    return new OnMultiLayerNetworkWeightsPreviewModified(multiLayerNetwork, weightsIndex, zoom,
        gamma);
  }

  public void setZoom(double value) {
    zoom = (float) value;
    fireEvent();
  }

  public void setGamma(double value) {
    gamma = (float) value;
    fireEvent();
  }
}
