package edu.yaprnn.support;

public final class Floats {

  public static int indexOf(float[] array, float target) {
    for (var i = 0; i < array.length; i++) {
      if (array[i] == target) {
        return i;
      }
    }
    return -1;
  }

  public static int argMax(float[] array) {
    assert array.length > 0;

    var argMax = 0;
    var max = array[0];
    for (var i = 1; i < array.length; i++) {
      if (array[i] > max) {
        max = array[argMax = i];
      }
    }
    return argMax;
  }

  public static float max(float[] array) {
    return array[argMax(array)];
  }

  public static boolean haveMaxAtSameIndex(float[] h, float[] target) {
    assert h.length >= 1;
    assert h.length == target.length;

    var argMaxH = 0;
    var maxH = h[0];
    var argMaxT = 0;
    var maxT = target[0];

    for (var i = 1; i < h.length; i++) {
      if (h[i] > maxH) {
        maxH = h[i];
        argMaxH = i;
      }
      if (target[i] >= maxT) {
        maxT = target[i];
        argMaxT = i;
      }
    }

    return argMaxH == argMaxT;
  }
}
