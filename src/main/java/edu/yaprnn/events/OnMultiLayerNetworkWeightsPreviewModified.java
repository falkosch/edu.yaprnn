package edu.yaprnn.events;

import edu.yaprnn.networks.MultiLayerNetwork;

public record OnMultiLayerNetworkWeightsPreviewModified(MultiLayerNetwork multiLayerNetwork,
                                                        int weightsIndex, float zoom, float gamma) {

}
