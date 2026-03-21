package edu.yaprnn.gui.views.mappings;

import edu.yaprnn.training.selectors.DataSelector;

public record RandomizeTrainingDataParameters(String name, float trainingPercentage,
    float devTestPercentage, DataSelector dataSelector) {

}
