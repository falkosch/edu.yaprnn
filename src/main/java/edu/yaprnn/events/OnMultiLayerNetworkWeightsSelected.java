package edu.yaprnn.events;

import edu.yaprnn.networks.MultiLayerNetwork;

public record OnMultiLayerNetworkWeightsSelected(MultiLayerNetwork multiLayerNetwork,
                                                 int weightsIndex) {

}
