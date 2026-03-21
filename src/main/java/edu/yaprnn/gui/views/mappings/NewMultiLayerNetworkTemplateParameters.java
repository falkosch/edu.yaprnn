package edu.yaprnn.gui.views.mappings;

import edu.yaprnn.networks.activation.ActivationFunction;
import edu.yaprnn.networks.loss.LossFunction;

public record NewMultiLayerNetworkTemplateParameters(String name, int layersCount, int layersSize,
    float bias, ActivationFunction activationFunction, LossFunction lossFunction) {

}
