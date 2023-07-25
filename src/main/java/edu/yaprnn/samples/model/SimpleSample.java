package edu.yaprnn.samples.model;

import com.google.common.primitives.Floats;
import edu.yaprnn.functions.Functions;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Builder
@Getter
@RequiredArgsConstructor
public class SimpleSample implements Sample {

  private final String name;
  private final String[] labels;
  private final float[] target;
  private final float[] input;

  @Override
  public File getFile() {
    return null;
  }

  @Override
  public String getLabel() {
    return labels[Floats.indexOf(target, 1f)];
  }

  @Override
  public Image createPreview() {
    var image = new BufferedImage(input.length, 1, BufferedImage.TYPE_BYTE_GRAY);
    for (var x = 0; x < input.length; x++) {
      image.setRGB(x, 0, (int) Functions.clamp(255f * input[x], 0f, 255f));
    }
    return image;
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
