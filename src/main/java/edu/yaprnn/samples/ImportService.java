package edu.yaprnn.samples;

import jakarta.inject.Singleton;
import java.util.List;

@Singleton
public final class ImportService {

  public float[] toTarget(String label, List<String> labels) {
    var target = new float[labels.size()];
    target[labels.indexOf(label)] = 1f;
    return target;
  }
}
