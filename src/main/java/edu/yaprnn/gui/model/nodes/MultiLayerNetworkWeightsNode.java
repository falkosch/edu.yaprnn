package edu.yaprnn.gui.model.nodes;

import edu.yaprnn.functions.Providers;
import edu.yaprnn.gui.services.IconsService;
import edu.yaprnn.networks.MultiLayerNetwork;
import java.util.Collections;
import java.util.function.IntSupplier;
import java.util.function.Supplier;
import lombok.Getter;

@Getter
public class MultiLayerNetworkWeightsNode extends DefaultNode {

  private final Supplier<MultiLayerNetwork> multiLayerNetworkSupplier;
  private final IntSupplier weightsIndexSupplier;

  public MultiLayerNetworkWeightsNode(Supplier<MultiLayerNetwork> multiLayerNetworkSupplier,
      IntSupplier weightsIndexSupplier) {
    super(Providers.constant(IconsService.ICON_LAYER),
        () -> labelFrom(multiLayerNetworkSupplier.get(), weightsIndexSupplier.getAsInt()),
        Collections::emptyList);
    this.multiLayerNetworkSupplier = multiLayerNetworkSupplier;
    this.weightsIndexSupplier = weightsIndexSupplier;
  }

  private static String labelFrom(MultiLayerNetwork multiLayerNetwork, int weightsIndex) {
    var targetLayerIndex = weightsIndex + 1;
    return targetLayerIndex == multiLayerNetwork.getLayerWeights().length ? "Weights to output"
        : "Weights to layer %d".formatted(targetLayerIndex);
  }
}
