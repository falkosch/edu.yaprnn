package edu.yaprnn.samples.model;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ImageSample implements Sample {

  public static final List<String> LABELS = List.of("0", "1", "2", "3", "4", "5", "6", "7", "8",
      "9");

  private final File imagesPackageFile;
  private final File labelsPackageFile;
  private final int indexInPackage;
  private final String name;
  private final String label;

  private final float[] target;

  private final int originalWidth;
  private final int originalHeight;
  private final float[] original;

  private final int inputWidth;
  private final int inputHeight;
  private final float[] input;

  /**
   * @param resolution new resolution of data
   * @param overlap    the overlap between adjacent windows in the range [0, 0.95]
   * @return sampled version
   */
  public ImageSample subSample(int resolution, float overlap) {
    var clampedResolution = Math.max(1, Math.min(resolution, originalWidth * originalHeight));
    var subSampledSize = getSubSampledSize(clampedResolution);
    var subSampled = subSample(subSampledSize, clampedResolution, overlap);

    return new ImageSample(imagesPackageFile, labelsPackageFile, indexInPackage, name, label,
        target, originalWidth, originalHeight, original, subSampledSize.width,
        subSampledSize.height, subSampled);
  }

  private Dimension getSubSampledSize(int resolution) {
    var scale = Math.sqrt(resolution / (float) (originalWidth * originalHeight));
    var width = (int) (scale * originalWidth);
    var height = (int) (scale * originalHeight);
    return new Dimension(width, height);
  }

  private float[] subSample(Dimension subSampledSize, int resolution, float overlap) {
    var subSampledStepX = originalWidth / (float) subSampledSize.width;
    var subSampledStepY = originalHeight / (float) subSampledSize.height;
    var stepX = originalWidth * overlap;
    var stepY = originalHeight * overlap;

    var subSampled = new float[resolution];
    for (var subSampledY = 0; subSampledY < subSampledSize.height; subSampledY++) {
      var windowY = subSampledY * subSampledStepY - 0.5f * stepY;
      var startY = Math.max(0, (int) windowY);
      var endY = Math.max(startY + 1, Math.min(originalHeight, (int) (windowY + stepY)));

      for (var subSampledX = 0; subSampledX < subSampledSize.width; subSampledX++) {
        var i = subSampledY * subSampledSize.width + subSampledX;

        var windowX = subSampledX * subSampledStepX - 0.5f * stepX;
        var startX = Math.max(0, (int) windowX);
        var endX = Math.max(startX + 1, Math.min(originalWidth, (int) (windowX + stepX)));

        for (var y = startY; y < endY; y++) {
          for (var x = startX; x < endX; x++) {
            subSampled[i] += original[y * originalWidth + x];
          }
        }
        subSampled[i] /= (endX - startX) * (endY - startY);
      }
    }

    return subSampled;
  }

  @Override
  public File getFile() {
    return this.imagesPackageFile;
  }

  @Override
  public String[] getLabels() {
    return LABELS.toArray(String[]::new);
  }

  @Override
  public Image createPreview() {
    var image = new BufferedImage(inputWidth, inputHeight, BufferedImage.TYPE_INT_RGB);

    for (var y = 0; y < inputHeight; y++) {
      for (var x = 0; x < inputWidth; x++) {
        var pixelValue = Math.round(255f * input[y * inputWidth + x]);
        image.setRGB(x, y, pixelValue | (pixelValue << 8) | (pixelValue << 16));
      }
    }

    return image;
  }

  @Override
  public String getMetaDescription() {
    return """
        <table>
          <tr> <th>Images Package File</th> <td>%s</td> </tr>
          <tr> <th>Labels Package File</th> <td>%s</td> </tr>
          <tr> <th>Name</th> <td>%s</td> </tr>
          <tr> <th>Label</th> <td>%s</td> </tr>
          <tr> <th>Original Resolution</th> <td>%d</td> </tr>
          <tr> <th>Input Resolution</th> <td>%d</td> </tr>
        </table>
        """.formatted(imagesPackageFile.getPath(), labelsPackageFile.getPath(), name, label,
        original.length, input.length);
  }

  @Override
  public String toString() {
    return "%s (%s)".formatted(name, label);
  }
}
