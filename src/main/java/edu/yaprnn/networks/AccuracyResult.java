package edu.yaprnn.networks;

import edu.yaprnn.support.Floats;

public record AccuracyResult(float error, int count, float hits) {

  public static AccuracyResult from(float[] h, float[] target, float error) {
    var hit = Floats.haveMaxAtSameIndex(h, target) ? 1f : 0f;
    return new AccuracyResult(error, 1, hit);
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
