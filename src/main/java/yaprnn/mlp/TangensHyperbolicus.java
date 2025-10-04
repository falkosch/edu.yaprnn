package yaprnn.mlp;

import java.io.Serial;

/**
 * This object implements the hyperbolic tangent (value range [-1,1]) and the interface
 * {@link ActivationFunction}.
 */
public class TangensHyperbolicus implements ActivationFunction {

  @Serial
  private static final long serialVersionUID = -7432193650916186201L;

  /**
   * Computes the corresponding value of the function to the parameter x
   *
   * @return the function's value with x
   */
  public double compute(double x) {
    return Math.tanh(x);
  }

  /**
   * Computes the corresponding value if the function's derivation to the parameter x
   *
   * @return the function's derivation with value x.
   */
  public double derivation(double x) {
    double v = Math.tanh(x);
    return 1 - v * v;
  }

  /**
   * The function's string presentation.
   *
   * @return the name
   */
  public String toString() {
    return "Tangens hyperbolicus";
  }

}
