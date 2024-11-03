package edu.yaprnn.networks.functions;

import java.util.Random;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@AllArgsConstructor
public final class ThresholdActivationFunction implements ActivationFunction {

  private float threshold = 0.1f;
  private float value = 0f;

  @Override
  public float[] apply(float[] v) {
    var h = new float[v.length];
    for (var i = 0; i < v.length; i++) {
      var x = v[i];
      h[i] = x > threshold ? x : value;
    }
    return h;
  }

  @Override
  public float[] derivative(float[] h, float[] v) {
    return derivative(v);
  }

  @Override
  public float[] derivative(float[] v) {
    var d = new float[v.length];
    for (var i = 0; i < v.length; i++) {
      d[i] = v[i] > threshold ? 1f : 0f;
    }
    return d;
  }

  @Override
  public float[] initialize(Random random, int count, int outputSize) {
    return Initialization.shell(random, count, outputSize, Initialization::heUniform);
  }

  @Override
  public String toString() {
    return "Threshold: v > T ? v : C";
  }
}
