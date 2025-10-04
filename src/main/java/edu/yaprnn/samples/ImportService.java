package edu.yaprnn.samples;

import jakarta.inject.Singleton;
import java.util.List;

@Singleton
public class ImportService {

  public float[] toTarget(String label, List<String> labels) {
    var targetIndex = labels.indexOf(label);
    var target = new float[labels.size()];
    for (var i = 0; i < target.length; i++) {
      target[i] = i == targetIndex ? 1f : 0f;
    }
    return target;
  }
}
