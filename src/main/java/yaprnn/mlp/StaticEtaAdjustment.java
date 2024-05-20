package yaprnn.mlp;

/**
 * This object adjusts the learning rate with a static factor and implements {@link Eta}.
 */

public class StaticEtaAdjustment implements Eta {

  private final double iterarorBorder;
  private final double multiplier;
  private double eta;
  private int iterations = 0;

  /**
   * This constructor sets the multiplier, which is used for the learning rate adjustment, the
   * learning rate and the number of getEta can be called before an adjustment.
   *
   * @param eta        The learning rate.
   * @param multiplier The multiplier used for the adjustments (betwee 0 and 1)
   * @param iterations Number of getEta can be called before an adjustment is done.
   */
  public StaticEtaAdjustment(double eta, double multiplier, int iterations) {
    this.eta = eta;
    this.iterarorBorder = iterations;
    this.multiplier = multiplier;
  }

  /**
   * Computes the new learning rate, if necessary.
   *
   * @param lastError Not needed.
   * @return The learning rate.
   */

  public double getEta(double lastError) {
    if (iterations % iterarorBorder == 0 && iterations != 0) {
      eta *= multiplier;
    }

    iterations++;
    return eta;
  }

  /**
   * Returns a string-representation of the learning rate.
   *
   * @return The learning rate as string.
   */
  @Override
  public String toString() {
    return Double.toString(eta);
  }
}
