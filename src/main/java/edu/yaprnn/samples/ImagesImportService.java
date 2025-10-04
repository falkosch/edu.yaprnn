package edu.yaprnn.samples;

import edu.yaprnn.samples.model.ImageSample;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.nio.file.Paths;
import java.util.List;
import lombok.SneakyThrows;

@Singleton
public class ImagesImportService {

  private static final int IMAGES_PACKAGE_HEADER_SIGNATURE = 2051;
  private static final int LABELS_PACKAGE_HEADER_SIGNATURE = 2049;

  @Inject
  ImportService importService;

  @SneakyThrows
  public List<ImageSample> fromImagesLabelsPackage(URL imagesPackageUrl, URL labelsPackageUrl) {
    var labelsPackageFile = Paths.get(labelsPackageUrl.toURI()).toFile();
    return List.of(readImages(Paths.get(imagesPackageUrl.toURI()).toFile(), labelsPackageFile,
        readLabels(labelsPackageFile)));
  }

  @SneakyThrows
  private ImageSample[] readImages(File imagesPackageFile, File labelsPackageFile,
      String[] labels) {
    try (var fileInputStream = new FileInputStream(imagesPackageFile)) {
      try (var stream = new DataInputStream(fileInputStream)) {
        if (stream.readInt() != IMAGES_PACKAGE_HEADER_SIGNATURE) {
          throw new IllegalArgumentException(
              "File %s is not an image package".formatted(imagesPackageFile.getPath()));
        }

        var countImages = stream.readInt();
        if (countImages != labels.length) {
          throw new IllegalArgumentException(
              "Count of images %d does not match count of labels %d".formatted(countImages,
                  labels.length));
        }

        var height = stream.readInt();
        var width = stream.readInt();
        return readImages(stream, imagesPackageFile, labelsPackageFile, labels, height, width);
      }
    }
  }

  @SneakyThrows
  private String[] readLabels(File labelsPackageFile) {
    try (var fileInputStream = new FileInputStream(labelsPackageFile)) {
      try (var stream = new DataInputStream(fileInputStream)) {
        if (stream.readInt() != LABELS_PACKAGE_HEADER_SIGNATURE) {
          throw new IllegalArgumentException(
              "File %s is not a label package".formatted(labelsPackageFile.getPath()));
        }

        var labels = new String[stream.readInt()];
        for (var i = 0; i < labels.length; i++) {
          labels[i] = String.valueOf(stream.readByte());
        }
        return labels;
      }
    }
  }

  private ImageSample[] readImages(DataInputStream stream, File imagesPackageFile,
      File labelsPackageFile, String[] labels, int height, int width) {
    var imageSize = height * width;
    var samples = new ImageSample[labels.length];
    for (int i = 0; i < samples.length; i++) {
      var original = process(readRawImage(stream, imagesPackageFile, imageSize));
      var name = "%s_%d".formatted(imagesPackageFile.getName(), i);
      var label = labels[i];
      var target = importService.toTarget(label, ImageSample.LABELS);
      samples[i] = new ImageSample(imagesPackageFile, labelsPackageFile, i, name, label, target,
          width, height, original, width, height, original);
    }
    return samples;
  }

  private float[] process(byte[] data) {
    var processed = new float[data.length];
    for (var i = 0; i < data.length; i++) {
      processed[i] = (data[i] >= 0 ? data[i] : 128 + (data[i] & 0x7F)) / 255f;
    }
    return processed;
  }

  @SneakyThrows
  private byte[] readRawImage(DataInputStream stream, File imagesPackageFile, int imageSize) {
    var data = new byte[imageSize];
    var offset = 0;
    while (offset != data.length) {
      var read = stream.read(data, offset, data.length - offset);
      if (read == -1) {
        throw new IllegalArgumentException(
            "End of file. Images package %s is malformed".formatted(imagesPackageFile.getPath()));
      }
      offset += read;
    }
    return data;
  }

  public List<ImageSample> fromImagesLabelsPackage(String pathImagesPackage,
      String pathLabelsPackage) {
    var labelsPackageFile = new File(pathLabelsPackage);
    return List.of(
        readImages(new File(pathImagesPackage), labelsPackageFile, readLabels(labelsPackageFile)));
  }
}
