package edu.yaprnn.functions;

import lombok.experimental.UtilityClass;

@UtilityClass
public class Functions {

  public static float clamp(float value, float min, float max) {
    return Math.max(Math.min(value, max), min);
  }
}
