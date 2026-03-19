package edu.yaprnn.networks.loss;

import java.util.Arrays;

record AlignedArrays(float[] h, float[] target, int length) {

  static AlignedArrays of(float[] h, float[] target) {
    var length = Math.max(h.length, target.length);
    return new AlignedArrays(Arrays.copyOf(h, length), Arrays.copyOf(target, length), length);
  }
}
