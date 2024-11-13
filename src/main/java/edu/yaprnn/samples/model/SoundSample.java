package edu.yaprnn.samples.model;

import edu.yaprnn.support.Floats;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public final class SoundSample implements Sample {

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
    return subSample(resolution, overlap, 1.005f);
  }

  /**
   * @param resolution new resolution of data
   * @param overlap    the overlap between adjacent windows in the range [0, 1)
   * @param lambda     increase of the window width
   * @return sampled version
   */
  public SoundSample subSample(int resolution, float overlap, float lambda) {
    assert overlap >= 0f && overlap < 1f : "Overlap must be in the range [0, 1)";
    assert lambda > 1f : "Window sizing 'lambda' must be greater than 1";

    var windowWidth = initialWindowWidthFrom(resolution, lambda);
    var windowStart = 0f;

    var subSampled = new float[resolution];
    for (var i = 0; i < subSampled.length; i++) {
      subSampled[i] = averageInWindow(windowStart, windowWidth, overlap);
      windowStart += windowWidth;
      windowWidth *= lambda;
    }
    return new SoundSample(file, name, label, target, subSampled, original);
  }

  private float initialWindowWidthFrom(int resolution, float lambda) {
    return original.length * (1f - lambda) / (1f - (float) Math.pow(lambda, resolution));
  }

  private float averageInWindow(float windowStart, float windowWidth, float overlap) {
    var windowOverlap = windowWidth * overlap;
    var windowLeft = windowStart - windowOverlap;
    var windowRight = windowStart + windowWidth + windowOverlap;

    var leftIndex = Math.clamp((int) windowLeft, 0, original.length - 1);
    var rightIndex = Math.clamp(1 + (int) windowRight, leftIndex + 1, original.length);

    if (rightIndex - leftIndex < 2) {
      return original[leftIndex] * (windowRight - windowLeft);
    }

    var sum = original[leftIndex] * (1f + leftIndex - windowLeft);
    for (var i = leftIndex + 1; i < rightIndex - 1; i++) {
      sum += original[i];
    }
    sum += original[rightIndex - 1] * (1f + windowRight - rightIndex);
    return sum / (windowRight - windowLeft);
  }

  @Override
  public String[] getLabels() {
    return LABELS.toArray(String[]::new);
  }

  public Image createPreview() {
    var image = new BufferedImage(input.length, PREVIEW_HEIGHT, BufferedImage.TYPE_INT_RGB);
    var scale = PREVIEW_HEIGHT / Floats.max(input);

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
