package edu.yaprnn.networks;

public record WeightsDimension(int rows, int cols) {

  public static WeightsDimension from(float[] weights, int outputSize) {
    return new WeightsDimension(weights.length / outputSize, outputSize);
  }

  public int inputSize() {
    return rows - 1;
  }

  public int inputSizeWithBias() {
    return rows;
  }
}
