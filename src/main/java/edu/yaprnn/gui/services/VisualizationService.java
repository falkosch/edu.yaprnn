package edu.yaprnn.gui.services;

import edu.yaprnn.networks.Layer;
import edu.yaprnn.networks.WeightsDimension;
import edu.yaprnn.support.swing.Images;
import jakarta.inject.Singleton;
import java.awt.Image;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.stream.IntStream;
import javax.swing.table.DefaultTableModel;

@Singleton
public class VisualizationService {

  private static final DecimalFormat TABLE_VALUE_FORMAT = new DecimalFormat("0.###E0");

  public Image from(float[] weights, int outputSize, float zoom, float gamma) {
    var height = inputSizeWithBias(weights, outputSize);
    var image = new BufferedImage(outputSize, height, BufferedImage.TYPE_INT_RGB);

    var gammaAsDouble = (double) gamma;

    try {
      for (int y = 0, w = 0; y < height; y++) {
        for (var x = 0; x < outputSize; x++, w++) {
          var weight = weights[w];
          var sign = Math.signum(weight);
          var positiveGammaAdjusted = Math.pow(sign * weight, gammaAsDouble);
          var intensity = (int) Math.clamp(255d - positiveGammaAdjusted, 0d, 255d);
          image.setRGB(x, y, intensity << (sign < 0 ? 0 : 8) | intensity << (sign > 0 ? 16 : 8));
        }
      }

      var clampedZoom = Math.clamp(zoom, 0.5f, 100f);
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

  public String valueAtOrDefault(float[] array, int index) {
    return index >= 0 && index < array.length ? formatTableValue(array[index]) : "";
  }

  public String formatTableValue(float value) {
    return TABLE_VALUE_FORMAT.format(value);
  }
}
