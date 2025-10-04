package yaprnn.dvv;

import java.io.Serial;

public class InvalidFileException extends Exception {

  /**
   *
   */
  @Serial
  private static final long serialVersionUID = 8045507416358627917L;

  private final String filename;

  public InvalidFileException(String filename) {
    this.filename = filename;
  }

  public String getFilename() {
    return filename;
  }

}
