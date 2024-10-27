package edu.yaprnn.functions;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@AllArgsConstructor
public class ThresholdActivationFunction implements ActivationFunction {

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
  public String toString() {
    return "Threshold: v > T ? v : C";
  }
}
