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

  private static final DecimalFormat TABLE_VALUE_FORMAT = new DecimalFormat(" 0.0###;-0.0###");
  private static final float[][] SCIENTIFIC_COLORS_BERLIN_LOOKUP = {
      // big negative
      {-1.25f, 158f, 176f, 255f},
      // small negative
      {-0.5f, 40f, 104f, 134f},
      // near zero
      {+0f, 28f, 11f, 6f},
      // small positive
      {+0.5f, 128f, 54f, 32f},
      // big positive
      {+1.25f, 255f, 173f, 173f}};
  private static final float[][] HEAT_LOOKUP = SCIENTIFIC_COLORS_BERLIN_LOOKUP;
  private static final int HEAT_LOOKUP_INTENSITY = 0;
  private static final int HEAT_LOOKUP_RED = 1;
  private static final int HEAT_LOOKUP_GREEN = 2;
  private static final int HEAT_LOOKUP_BLUE = 3;

  private static int toHeatColor(float intensity) {
    var startHeat = HEAT_LOOKUP[intensity < 0f ? 0 : HEAT_LOOKUP.length - 1];
    var redFloat = startHeat[HEAT_LOOKUP_RED];
    var greenFloat = startHeat[HEAT_LOOKUP_GREEN];
    var blueFloat = startHeat[HEAT_LOOKUP_BLUE];

    for (int minIndex = 0, maxIndex = 1; maxIndex < HEAT_LOOKUP.length; minIndex++, maxIndex++) {
      var minHeat = HEAT_LOOKUP[minIndex];
      var minHeatIntensity = minHeat[HEAT_LOOKUP_INTENSITY];
      var maxHeat = HEAT_LOOKUP[maxIndex];

      var maxHeatIntensity = maxHeat[HEAT_LOOKUP_INTENSITY];
      if (intensity < minHeatIntensity || intensity >= maxHeatIntensity) {
        continue;
      }

      var minHeatRed = minHeat[HEAT_LOOKUP_RED];
      var minHeatGreen = minHeat[HEAT_LOOKUP_GREEN];
      var minHeatBlue = minHeat[HEAT_LOOKUP_BLUE];

      var factor = (intensity - minHeatIntensity) / (maxHeatIntensity - minHeatIntensity);
      redFloat = minHeatRed + factor * (maxHeat[HEAT_LOOKUP_RED] - minHeatRed);
      greenFloat = minHeatGreen + factor * (maxHeat[HEAT_LOOKUP_GREEN] - minHeatGreen);
      blueFloat = minHeatBlue + factor * (maxHeat[HEAT_LOOKUP_BLUE] - minHeatBlue);
      break;
    }

    var red = Math.clamp(Math.round(redFloat), 0, 255);
    var green = Math.clamp(Math.round(greenFloat), 0, 255);
    var blue = Math.clamp(Math.round(blueFloat), 0, 255);
    return (red << 16) | (green << 8) | blue;
  }

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
      return Images.resize(image, newWidth, newHeight, AffineTransformOp.TYPE_BICUBIC);
    } finally {
      image.flush();
    }
  }

  public int inputSizeWithBias(float[] weights, int outputSize) {
    return WeightsDimension.from(weights, outputSize).inputSizeWithBias();
  }

  public Image fromWeights(float[] weights, int outputSize, float zoom, float gamma) {
    var height = inputSizeWithBias(weights, outputSize);
    var image = new BufferedImage(outputSize, height, BufferedImage.TYPE_INT_RGB);

    try {
      var gammaAsDouble = (double) -gamma;
      for (int y = 0, w = 0; y < height; y++) {
        for (var x = 0; x < outputSize; x++, w++) {
          var weight = weights[w];
          var sign = Math.signum(weight);
          var absoluteWeight = sign * weight;
          var gammaAdjusted =
              absoluteWeight > 0f ? (float) Math.pow(sign * weight, gammaAsDouble) : 0f;
          image.setRGB(x, y, toHeatColor(sign * gammaAdjusted));
        }
      }

      var newWidth = (int) (outputSize * zoom);
      var newHeight = (int) (height * zoom);
      return Images.resize(image, newWidth, newHeight, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
    } finally {
      image.flush();
    }
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
