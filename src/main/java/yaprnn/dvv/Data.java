package yaprnn.dvv;

import yaprnn.mlp.ActivationFunction;

public abstract class Data {

  private Use use;

  public abstract double[] getData();

  public abstract String getName();

  public abstract String getLabel();

  public abstract int getTarget();

  public abstract String getFilename();

  public abstract String getPath();

  public abstract String getSubsamplingOptions();

  public abstract void subsample(int resolution, double overlap, ActivationFunction function)
      throws NoSuchFileException;

  public abstract boolean isAudio();

  public abstract boolean isPicture();

  public abstract Object previewRawData();

  public abstract Object previewSubsampledData(int resolution, double overlap);

  /**
   * Marks this Data object as a part of the training data set.
   */
  public void setTraining() {
    use = Use.TRAINING;
  }

  /**
   * Marks this Data object as a part of the test data set.
   */
  public void setTest() {
    use = Use.TEST;
  }

  /**
   * Marks this Data object as neither test nor training data.
   */
  public void setNotUsed() {
    use = Use.NOT_USED;
  }

  /**
   * Returns true if this object is part of the training data set, false otherwise.
   *
   * @return true if the object is training data; false otherwise.
   */
  public boolean isTraining() {
    return use == Use.TRAINING;
  }

  /**
   * Returns true if this object is part of the test data set, false otherwise.
   *
   * @return true if the object is test data; false otherwise.
   */
  public boolean isTest() {
    return use == Use.TEST;
  }

  /**
   * Returns true if this object is part of the not-used data set, false otherwise.
   *
   * @return true if the object is neither test nor training data; false otherwise.
   */
  public boolean isNotUsed() {
    return use == Use.NOT_USED;
  }

  private enum Use {TRAINING, TEST, NOT_USED}

  public enum Type {AUDIO, PICTURE}

}
