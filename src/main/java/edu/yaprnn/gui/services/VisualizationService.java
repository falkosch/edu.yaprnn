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

  public Image fromOutput(float[] output, int width, float zoom, float gamma) {
    var height = Math.max(1, inputSizeWithBias(output, width));
    var safeOutput = Arrays.copyOf(output, width * height);
    var image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

    try {
      var gammaAsDouble = (double) -gamma;
      for (var y = 0; y < height; y++) {
        for (var x = 0; x < width; x++) {
          var gammaAdjusted = Math.pow(safeOutput[y * width + x], gammaAsDouble);
          var pixelValue = Math.clamp(Math.round(255d * gammaAdjusted), 0, 255);
          image.setRGB(x, y, pixelValue | (pixelValue << 8) | (pixelValue << 16));
        }
      }

      var newWidth = (int) (width * zoom);
      var newHeight = (int) (height * zoom);
      return Images.resize(image, newWidth, newHeight, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
    } finally {
      image.flush();
    }
  }

  public Image fromWeights(float[] weights, int outputSize, float zoom, float gamma) {
    var height = inputSizeWithBias(weights, outputSize);
    var image = new BufferedImage(outputSize, height, BufferedImage.TYPE_INT_RGB);

    try {
      var gammaAsDouble = (double) gamma;
      for (int y = 0, w = 0; y < height; y++) {
        for (var x = 0; x < outputSize; x++, w++) {
          var weight = weights[w];
          var sign = Math.signum(weight);
          var positiveGammaAdjusted = Math.pow(sign * weight, gammaAsDouble);
          var intensity = Math.clamp(Math.round(255d - positiveGammaAdjusted), 0, 255);
          image.setRGB(x, y, intensity << (sign < 0 ? 0 : 8) | intensity << (sign > 0 ? 16 : 8));
        }
      }

      var newWidth = (int) (outputSize * zoom);
      var newHeight = (int) (height * zoom);
      return Images.resize(image, newWidth, newHeight, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
    } finally {
      image.flush();
    }
  }

  public int inputSizeWithBias(float[] weights, int outputSize) {
    return WeightsDimension.from(weights, outputSize).inputSizeWithBias();
  }

  public DefaultTableModel classificationTableModel(String[] labels, Layer[] layers,
      float[] output) {
    var rowsCount = getRowsCount(labels, layers);
    var columns = makeClassificationColumns(layers.length);
    var tableData = new Object[rowsCount][columns.length];

    for (var row = 0; row < rowsCount; row++) {
      tableData[row][0] = "[%d]".formatted(row);
      tableData[row][columns.length - 1] = valueAtOrDefault(labels, row, "");
      tableData[row][columns.length - 2] = valueAtOrDefault(output, row);

      for (int i = 0, column = 1; i < layers.length; i++, column += 2) {
        var layer = layers[i];
        tableData[row][column] = valueAtOrDefault(layer.v(), row);
        tableData[row][column + 1] = valueAtOrDefault(layer.h(), row);
      }
    }

    return new DefaultTableModel(tableData, columns);
  }

  private int getRowsCount(String[] labels, Layer[] layers) {
    var layersSizes = Arrays.stream(layers)
        .flatMapToInt(layer -> IntStream.of(layer.v().length, layer.h().length));
    return IntStream.concat(IntStream.of(labels.length), layersSizes).max().orElseThrow();
  }

  private Object[] makeClassificationColumns(int layersCount) {
    var columnsCount = 3 + layersCount * 2;
    return IntStream.range(0, columnsCount).mapToObj(c -> {
      if (c == 0) {
        return "Index";
      }
      if (c == columnsCount - 1) {
        return "Label";
      }
      if (c == columnsCount - 2) {
        return "Output";
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
