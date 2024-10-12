package edu.yaprnn.samples.model;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class SoundSample implements Sample {

  public static final List<String> LABELS = List.of("A", "E", "I", "O", "U");

  private static final int COLOR_BASE = 16765440;
  private static final int PREVIEW_HEIGHT = 256;
  private static final float PREVIEW_DESCALE = 210f / PREVIEW_HEIGHT;

  private final File file;
  private final String name;
  private final String label;
  private final float[] target;
  private final float[] input;
  private final float[] original;

  public SoundSample subSample(int resolution, float overlap) {
    return subSample(resolution, overlap, 1.02f);
  }

  /**
   * @param resolution new resolution of data
   * @param overlap    the overlap between adjacent windows in the range [0, 0.95]
   * @param lambda     increase of the window width
   * @return sampled version
   */
  public SoundSample subSample(int resolution, float overlap, float lambda) {
    var index = 0f;
    var width = firstWindowWidth(resolution, overlap, lambda);
    var subSampled = new float[resolution];
    for (var i = 0; i < resolution; i++) {
      subSampled[i] = averageInWindow(width, index, overlap);
      index += (1f - overlap) * width;
      width *= lambda;
    }
    return new SoundSample(file, name, label, target, subSampled, original);
  }

  private float firstWindowWidth(int resolution, float overlap, float lambda) {
    var weight = 0f;
    for (var i = 0; i < resolution; i++) {
      weight += (float) Math.pow(lambda, i);
    }
    return this.original.length / ((1f - overlap) * weight);
  }

  private float averageInWindow(float width, float index, float overlap) {
    if (width == 0f) {
      return 0f;
    }
    var start = Math.round(index - width * overlap);
    var leftIndex = Math.max(0, start);
    var rightIndex = Math.min(this.original.length - 1, Math.round(start + width));
    var sum = 0f;
    for (var i = leftIndex; i <= rightIndex; i++) {
      sum += this.original[i];
    }
    return sum / (rightIndex - leftIndex);
  }

  @Override
  public String[] getLabels() {
    return LABELS.toArray(String[]::new);
  }

  public Image createPreview() {
    var image = new BufferedImage(input.length, PREVIEW_HEIGHT, BufferedImage.TYPE_INT_RGB);
    var maximum = 0f;
    for (var v : input) {
      maximum = Math.max(maximum, v);
    }
    var scale = PREVIEW_HEIGHT / maximum;
    for (var x = 0; x < input.length; x++) {
      var valueAtX = (int) (scale * input[x]);
      for (var y = PREVIEW_HEIGHT - valueAtX; y < PREVIEW_HEIGHT; y++) {
        image.setRGB(x, y,
            COLOR_BASE - PREVIEW_HEIGHT * Math.round(PREVIEW_DESCALE * (PREVIEW_HEIGHT - y)));
      }
    }

    return image;
  }

  @Override
  public String getMetaDescription() {
    return """
        <table>
          <tr> <th>Audio File</th> <td>%s</td> </tr>
          <tr> <th>Name</th> <td>%s</td> </tr>
          <tr> <th>Label</th> <td>%s</td> </tr>
          <tr> <th>Original Resolution</th> <td>%d</td> </tr>
          <tr> <th>Input Resolution</th> <td>%d</td> </tr>
        </table>
        """.formatted(file.getPath(), name, label, original.length, input.length);
  }

  @Override
  public String toString() {
    return "%s (%s)".formatted(name, label);
  }
}
