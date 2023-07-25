package edu.yaprnn.networks;

import com.google.common.primitives.Floats;

public record AccuracyResult(float error, int count, float hits) {

  public static AccuracyResult from(float[] h, float[] target, float error) {
    var argMax = Floats.indexOf(h, Floats.max(h));
    if (argMax >= 0) {
      var t = target[argMax];
      var hit = t == 1f ? 1f : 0f;
      return new AccuracyResult(error, 1, hit);
    }
    return new AccuracyResult(error, 1, 0f);
  }

  public static AccuracyResult sum(AccuracyResult left, AccuracyResult right) {
    return new AccuracyResult(left.error + right.error, left.count + right.count,
        left.hits + right.hits);
  }

  public static AccuracyResult average(AccuracyResult result) {
    var count = result.count;
    return new AccuracyResult(result.error / count, 1, result.hits / count);
  }
}
