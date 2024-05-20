package yaprnn.mlp;

/**
 * This object adjusts the learning rate in against last computed error and implements the interface
 * {@link Eta}
 */

public class DynamicEtaAdjustment implements Eta {

  final double signum;
  final double roh;
  double eta;
  double lastError;

  /**
   * Constructor which sets the learning rate, signum and roh. Look at getEta for further
   * information.
   *
   * @param eta    The learning rate.
   * @param signum Multiplier for a smaller learning rate.
   * @param roh    Multiplier for a bigger learning rate.
   */
  public DynamicEtaAdjustment(double eta, double signum, double roh) {
    this.eta = eta;
    this.signum = signum;
    this.roh = roh;
  }

  /**
   * Returns a adjusted learning rate. If the delivered error is bigger than the saved one, the
   * learning rate is adjusted with signum (mult.). With a smaller error, than the saved one, the
   * learning rate will be multiplied with roh. Otherwise the learning rate won't be touched.
   *
   * @param lastError The last computed error.
   * @return The adjusted learning rate.
   */
  public double getEta(double lastError) {

    if (this.lastError > lastError) {
      eta *= signum;
    } else if (this.lastError < lastError) {
      eta *= roh;
    }

    this.lastError = lastError;
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
