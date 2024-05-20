package yaprnn.dvv;

import java.io.Serial;

public class NoSuchFileException extends Exception {

  /**
   *
   */
  @Serial
  private static final long serialVersionUID = -9011975069079364930L;

  private final String filename;

  public NoSuchFileException(String filename) {
    this.filename = filename;
  }

  public String getFilename() {
    return filename;
  }

}
