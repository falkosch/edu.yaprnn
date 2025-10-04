package edu.yaprnn.networks.weights;

import edu.yaprnn.networks.WeightsDimension;
import java.util.Random;
import lombok.experimental.UtilityClass;

@UtilityClass
public final class Initialization {

  public static float[] shell(Random random, int count, int outputSize, Initializer initializer) {
    var weights = new float[count];
    var dimension = WeightsDimension.from(weights, outputSize);
    initializer.apply(random, weights, dimension.inputSize(), outputSize);
    return weights;
  }
}
