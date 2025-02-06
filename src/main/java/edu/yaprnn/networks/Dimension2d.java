package edu.yaprnn.networks;

public record Dimension2d(int rows, int cols) {

  public static Dimension2d from(float[] weights, int outputSize) {
    return new Dimension2d(weights.length / outputSize, outputSize);
  }

  public int inputSize() {
    return rows - 1;
  }

  public int inputSizeWithBias() {
    return rows;
  }
}
