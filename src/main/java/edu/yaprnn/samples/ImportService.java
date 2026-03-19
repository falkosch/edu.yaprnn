package edu.yaprnn.samples;

import jakarta.inject.Singleton;
import java.util.List;

@Singleton
public final class ImportService {

  public float[] toTarget(String label, List<String> labels) {
    var index = labels.indexOf(label);
    if (index < 0) {
      throw new IllegalArgumentException("Unknown label: " + label);
    }
    var target = new float[labels.size()];
    target[index] = 1f;
    return target;
  }
}
