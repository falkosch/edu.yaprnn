package yaprnn.mlp;

import java.io.Serial;

/**
 * This {@link ActivationFunction} simply returns the intput value. Therefor the derivation is
 * always 1.
 */
public class Linear implements ActivationFunction {

  @Serial
  private static final long serialVersionUID = -7102325169146600623L;

  /**
   * Computes the corresponding value of the function to the parameter x
   *
   * @return the function's value with x
   */
  public double compute(double x) {
    return x;
  }

  /**
   * Computes the corresponding value if the function's derivation to the parameter x
   *
   * @return the function's derivation with value x.
   */
  public double derivation(double x) {
    return 1;
  }

  /**
   * The function's string presentation.
   *
   * @return the name
   */
  public String toString() {
    return "Linear";
  }

}
