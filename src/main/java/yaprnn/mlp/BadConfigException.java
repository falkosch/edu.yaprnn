package yaprnn.mlp;

import java.io.Serial;

/**
 * This exception will be thrown by the {@link MLP} if a incorrect configuration is set.
 */
public class BadConfigException extends Exception {

  @Serial
  private static final long serialVersionUID = 1L;

  /**
   * This constructor sets the error message and a identifer describing the message.
   *
   * @param message The error message.
   */
  public BadConfigException(String message) {
    super(message);
  }

}
