package edu.yaprnn.networks.weights;

import java.util.Random;

public interface Initializer {

  void apply(Random random, float[] weights, int inputSize, int outputSize);
}
