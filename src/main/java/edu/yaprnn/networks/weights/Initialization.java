package edu.yaprnn.networks.weights;

import edu.yaprnn.networks.Dimension2d;
import java.util.Random;

public final class Initialization {

  public static float[] shell(Random random, int count, int outputSize, Initializer initializer) {
    var weights = new float[count];
    var dimension = Dimension2d.from(weights, outputSize);
    initializer.apply(random, weights, dimension.inputSize(), outputSize);
    return weights;
  }
}
