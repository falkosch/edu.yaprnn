package yaprnn.dvv;

import java.io.Serial;

public class FileMismatchException extends Exception {

  /**
   *
   */
  @Serial
  private static final long serialVersionUID = 1281496297181827375L;

  private final String dataFilename;
  private final String labelFilename;

  public FileMismatchException(String dataFilename, String labelFilename) {
    this.dataFilename = dataFilename;
    this.labelFilename = labelFilename;
  }

  public String getDataFilename() {
    return dataFilename;
  }

  public String getLabelFilename() {
    return labelFilename;
  }

}
