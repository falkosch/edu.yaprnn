package yaprnn.dvv;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import yaprnn.mlp.ActivationFunction;

/**
 * IdxPicture is the class for holding a single image loaded from an IDX file. It provides
 * funcionality for previewing raw and subsampled data and for subsampling and scaling the data.
 */
public class IdxPicture extends Data {

  private final static int DATA_MAGIC_NUMBER = 2051;
  private final static int LABEL_MAGIC_NUMBER = 2049;
  private final byte[][] rawData;
  private final String label;
  private final int target;
  private final String filename;
  private final int fileIndex;
  private double[] data;
  private String subsamplingOptions;

  /**
   * Constructs an IdxPicture object from the specified data.
   *
   * @param rawData   the image data
   * @param label     the classifying label
   * @param filename  the file this picture was loaded from
   * @param fileIndex the index this picture had in the file
   */
  public IdxPicture(byte[][] rawData, String label, String filename, int fileIndex) {
    this.rawData = rawData;
    this.label = label;
    this.filename = filename;
    this.fileIndex = fileIndex;
    target = Integer.parseInt(label);
    this.subsamplingOptions = "Not yet subsampled";
  }

  /**
   * Reads several images from the specified files and returns them as a collection.
   *
   * @param dataFilename  the file holding the actual image data
   * @param labelFilename the file holding the data labels
   * @return a collection of the loaded images
   * @throws InvalidFileException  if one of the files does not have the required format
   * @throws FileMismatchException if the files do not appear to belong to the same dataset
   * @throws NoSuchFileException   if one of the files does not exist
   */
  public static Collection<Data> readFromFile(String dataFilename, String labelFilename)
      throws NoSuchFileException, InvalidFileException, FileMismatchException, IOException {
    DataInputStream dataInput = null, labelInput = null;
    int numImagesData, numImagesLabel;
    try {
      try {
        dataInput = new DataInputStream(new FileInputStream(dataFilename));
      } catch (FileNotFoundException e) {
        throw new NoSuchFileException(dataFilename);
      }
      try {
        labelInput = new DataInputStream(new FileInputStream(labelFilename));
      } catch (FileNotFoundException e) {
        throw new NoSuchFileException(labelFilename);
      }
      try {
        final int dataMagicNumber = dataInput.readInt();
        if (dataMagicNumber != DATA_MAGIC_NUMBER) {
          throw new InvalidFileException(dataFilename);
        }
        numImagesData = dataInput.readInt();
      } catch (EOFException e) {
        throw new InvalidFileException(dataFilename);
      }
      try {
        final int labelMagicNumber = labelInput.readInt();
        if (labelMagicNumber != LABEL_MAGIC_NUMBER) {
          throw new InvalidFileException(labelFilename);
        }
        numImagesLabel = labelInput.readInt();
      } catch (EOFException e) {
        throw new InvalidFileException(labelFilename);
      }
      if (numImagesData != numImagesLabel) {
        throw new FileMismatchException(dataFilename, labelFilename);
      }
      return readDataFromFile(dataInput, labelInput, numImagesData, dataFilename, labelFilename);
    } finally {
      if (dataInput != null) {
        dataInput.close();
      }
      if (labelInput != null) {
        labelInput.close();
      }
    }
  }

  private static Collection<Data> readDataFromFile(DataInputStream dataInput,
      DataInputStream labelInput, int numImages, String dataFilename, String labelFilename)
      throws InvalidFileException, IOException {
    Collection<Data> result = new ArrayList<>(numImages);
    try {
      final int numRows = dataInput.readInt();
      final int numCols = dataInput.readInt();
      for (int i = 0; i < numImages; i++) {
        byte[][] image = new byte[numRows][numCols];
        for (int j = 0; j < numRows; j++) {
          int bytesRead = 0;
          while (bytesRead != numCols) {
            final int read = dataInput.read(image[j], bytesRead, numCols - bytesRead);
            if (read == -1) {
              throw new InvalidFileException(dataFilename);
            }
            bytesRead += read;
          }
        }
        final byte target = labelInput.readByte();
        if (target < 0 || target > 9) {
          throw new InvalidFileException(labelFilename);
        }
        result.add(new IdxPicture(image, String.valueOf(target), dataFilename, i));
      }
    } catch (EOFException e) {
      throw new InvalidFileException(labelFilename);
    }
    return result;
  }

  /**
   * Returns the completely preprocessed data of this image.
   *
   * @return the preprocessed data
   */
  @Override
  public double[] getData() {
    return data;
  }

  /**
   * Returns the name of this image. The name is constructed from the filename and the file index of
   * this image.
   *
   * @return the name of this image
   */
  @Override
  public String getName() {
    return "%s_%d".formatted((new File(filename)).getName(), fileIndex);
  }

  /**
   * Returns the label for this image.
   *
   * @return the label for this image
   */
  @Override
  public String getLabel() {
    return label;
  }

  /**
   * Returns the target for this image.
   *
   * @return the target for this image
   */
  @Override
  public int getTarget() {
    return target;
  }

  /**
   * Returns the filename this image was read from.
   *
   * @return the filename this object was read from
   */
  @Override
  public String getFilename() {
    return filename;
  }

  @Override
  public String getPath() {
    return filename;
  }

  /**
   * Returns the used subsampling options
   *
   * @return the used subsampling options
   */
  @Override
  public String getSubsamplingOptions() {
    return this.subsamplingOptions;
  }

  /**
   * Performs the subsampling and scaling of the data.
   *
   * @param overlap         the overlap between adjacent windows in the range [0, 0.95]
   * @param scalingFunction the function used to scale the subsampled data
   */
  @Override
  public void subsample(int resolution, double overlap, ActivationFunction scalingFunction)
      throws NoSuchFileException {
    if (resolution <= 0 || resolution > rawData.length || overlap < 0.0 || overlap > 0.95) {
      throw new NoSuchFileException(Integer.toString(this.fileIndex));
    }
    final int[][] subData = subsample(resolution, overlap);
    data = new double[resolution * resolution];
    for (int i = 0; i < resolution; i++) {
      for (int j = 0; j < resolution; j++) {
        data[i * resolution + j] = scalingFunction.compute(subData[i][j]);
      }
    }
    DecimalFormat f = new DecimalFormat("#0.00");
    this.subsamplingOptions = "Resolution: " + resolution + "       Overlap: " + f.format(overlap);
  }

  /**
   * Is it an audio?
   *
   * @return Returns false.
   */
  @Override
  public boolean isAudio() {
    return false;
  }

  /**
   * Is it a picture?
   *
   * @return Returns true.
   */

  @Override
  public boolean isPicture() {
    return true;
  }

  /**
   * Returns the raw data for previewing.
   *
   * @return the raw data
   */
  @Override
  public Object previewRawData() {
    return rawData;
  }

  /**
   * Returns the data subsampled with the specified parameters (not yet scaled).
   *
   * @param resolution the desired resolution
   * @param overlap    the overlap between adjacent windows in the range [0, 0.95]
   */
  @Override
  public Object previewSubsampledData(int resolution, double overlap) {
    if (resolution <= 0 || resolution > rawData.length || overlap < 0.0 || overlap > 0.95) {
      return null;
    }
    final int[][] subData = subsample(resolution, overlap);
    final byte[][] result = new byte[resolution][resolution];
    for (int i = 0; i < resolution; i++) {
      for (int j = 0; j < resolution; j++) {
        result[i][j] = (byte) subData[i][j];
      }
    }
    return result;
  }

  private int[][] subsample(int resolution, double overlap) {
    int[][] subData = new int[resolution][resolution];
    double scaling = rawData.length / (double) resolution;
    int windowSize = (int) Math.round(scaling * (1 + overlap));
    for (int i = 0; i < resolution; i++) {
      for (int j = 0; j < resolution; j++) {
        int x0 = (int) (j * scaling);
        int y0 = (int) (i * scaling);
        int xsize = x0 + windowSize < rawData[0].length ? windowSize : rawData[0].length - x0;
        int ysize = y0 + windowSize < rawData.length ? windowSize : rawData.length - y0;
        int size = Math.min(xsize, ysize);
        subData[i][j] = 0;
        for (int k = y0; k < y0 + size; k++) {
          for (int l = x0; l < x0 + size; l++) {
            subData[i][j] += uByteToInt(rawData[k][l]);
          }
        }
        subData[i][j] /= size * size;
      }
    }

    return subData;
  }

  private int uByteToInt(byte b) {
    return (int) b >= 0 ? (int) b : 128 + ((int) b & 0x7F);
  }
}
