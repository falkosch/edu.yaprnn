package yaprnn.mlp;

import java.io.Serial;

/**
 * This class implements {@link ActivationFunction} with a sigmoid function. The range is from 0 to
 * 1.
 */
public class Sigmoid implements ActivationFunction {

  @Serial
  private static final long serialVersionUID = -7520433725747069333L;

  /**
   * Computes the corresponding value of the function to the parameter x
   *
   * @return the function's value with x
   */
  public double compute(double x) {
    return (1.0 / (1.0 + Math.exp(-x)));
  }

  /**
   * Computes the corresponding value if the function's derivation to the parameter x
   *
   * @return the function's derivation with value x.
   */
  public double derivation(double x) {
    double e = Math.exp(x);
    double e2 = e + 1;
    return e / (e2 * e2);
  }

  /**
   * The function's string presentation.
   *
   * @return the name
   */
  public String toString() {
    return "Sigmoid";
  }

}
