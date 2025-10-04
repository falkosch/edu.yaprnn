package edu.yaprnn.gui.services;

import edu.yaprnn.functions.Functions;
import edu.yaprnn.gui.images.Images;
import edu.yaprnn.networks.Layer;
import edu.yaprnn.networks.WeightsDimension;
import jakarta.inject.Singleton;
import java.awt.Image;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.stream.IntStream;
import javax.swing.table.DefaultTableModel;

@Singleton
public class WeightsService {

  public Image from(float[] weights, int outputSize, float zoom, float gamma) {
    var height = inputSizeWithBias(weights, outputSize);
    var image = new BufferedImage(outputSize, height, BufferedImage.TYPE_BYTE_GRAY);

    try {
      for (int y = 0, w = 0; y < height; y++) {
        for (var x = 0; x < outputSize; x++, w++) {
          var value = (float) (16.0 * (Math.pow(weights[w], gamma) + 1.0));
          var pixelValue = (int) Functions.clamp(value, 0, 255);
          image.setRGB(x, y, pixelValue | (pixelValue << 8) | (pixelValue << 16));
        }
      }

      var clampedZoom = Functions.clamp(zoom, 0.5f, 100f);
      var newWidth = (int) (outputSize * clampedZoom);
      var newHeight = (int) (height * clampedZoom);
      return Images.resize(image, newWidth, newHeight, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
    } finally {
      image.flush();
    }
  }

  public int inputSizeWithBias(float[] weights, int outputSize) {
    return WeightsDimension.from(weights, outputSize).inputSizeWithBias();
  }

  public DefaultTableModel classificationTableModel(String[] labels, Layer[] layers) {
    var layersMaxSize = Arrays.stream(layers)
        .map(Layer::h)
        .mapToInt(h -> h.length)
        .max()
        .orElseThrow();
    var rowsCount = Math.max(labels.length, layersMaxSize);
    var columns = classificationColumns(layers.length);
    var tableData = new Object[rowsCount][columns.length];

    for (var row = 0; row < rowsCount; row++) {
      tableData[row][0] = "[%d]".formatted(row);
      tableData[row][columns.length - 1] = valueAtOrDefault(labels, row, "");

      for (int i = 0, column = 1; i < layers.length; i++, column += 2) {
        var layer = layers[i];
        tableData[row][column] = valueAtOrDefault(layer.v(), row);
        tableData[row][column + 1] = valueAtOrDefault(layer.h(), row);
      }
    }

    return new DefaultTableModel(tableData, columns);
  }

  public Object[] classificationColumns(int layersCount) {
    var columnsCount = 2 + layersCount * 2;
    return IntStream.range(0, columnsCount).mapToObj(c -> {
      if (c == 0) {
        return "Index";
      }
      if (c == columnsCount - 1) {
        return "Label";
      }
      return "%s[%d]".formatted((c - 1) % 2 == 0 ? "v" : "h", (c - 1) / 2);
    }).toArray();
  }

  public String valueAtOrDefault(String[] array, int index, String defaultValue) {
    return index >= 0 && index < array.length ? array[index] : defaultValue;
  }

  public Object valueAtOrDefault(float[] array, int index) {
    return index >= 0 && index < array.length ? roundFractions(array[index]) : "";
  }

  public float roundFractions(float value) {
    return Math.round(1_000_000f * value) / 1_000_000f;
  }
}
