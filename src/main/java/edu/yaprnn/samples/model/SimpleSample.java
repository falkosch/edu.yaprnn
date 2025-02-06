package edu.yaprnn.samples.model;

import edu.yaprnn.support.Floats;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Builder
@Getter
@RequiredArgsConstructor
public final class SimpleSample implements Sample {

  private final String name;
  private final String[] labels;
  private final float[] target;
  private final float[] input;

  private static Image createPreviewFrom(float[] values) {
    var image = new BufferedImage(values.length, 1, BufferedImage.TYPE_BYTE_GRAY);
    for (var x = 0; x < values.length; x++) {
      image.setRGB(x, 0, Math.clamp(Math.round(255f * values[x]), 0, 255));
    }
    return image;
  }

  @Override
  public File getFile() {
    return null;
  }

  @Override
  public String getLabel() {
    return labels[Floats.indexOf(target, 1f)];
  }

  @Override
  public float[] getOriginal() {
    return input;
  }

  @Override
  public Image createPreviewFromOriginal() {
    return createPreviewFrom(input);
  }

  @Override
  public Image createPreviewFromInput() {
    return createPreviewFrom(input);
  }


  @Override
  public String getMetaDescription() {
    return """
        <table>
          <tr> <th>Name</th> <td>%s</td> </tr>
          <tr> <th>Label</th> <td>%s</td> </tr>
          <tr> <th>Resolution</th> <td>%d</td> </tr>
        </table>
        """.formatted(name, getLabel(), input.length);
  }
}
