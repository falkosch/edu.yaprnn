package edu.yaprnn.networks.activation;

final class Sigmoid {

  static float of(float x) {
    return 1f / (1f + (float) Math.exp(-x));
  }
}
