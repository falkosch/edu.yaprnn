package yaprnn.mlp;

import java.util.Arrays;

public class TestMLP {

  protected static Layer[] layer;

  public static void main(String[] args) {
    System.out.print("Erstelle neues MLP...");
    int[] hL = {2};
    ActivationFunction[] f = new ActivationFunction[3];
    Arrays.fill(f, new TangensHyperbolicus());
    double[] b = {1};

    try {
      MLP(2, 2, hL, f, b, true);
    } catch (BadConfigException e) {
      e.printStackTrace();
    }
    System.out.println("fertig");

    System.out.print("Erstelle Eingabedaten...");

    int[] target = new int[2];
    double[][] inputValues = new double[2][2];

    target[0] = 1;

    inputValues[0][0] = 1;
    inputValues[0][1] = 0;
    inputValues[1][0] = 0;
    inputValues[1][1] = 1;

    System.out.println("fertig");

    System.out.print("Erstelle Testeingabedaten...");

    int[] ttarget = new int[2];
    double[][] tinputValues = new double[2][2];

    ttarget[0] = 1;

    tinputValues[0][0] = 1;
    tinputValues[0][1] = 0;
    tinputValues[1][0] = 0;
    tinputValues[1][1] = 1;

    System.out.println("fertig");

    System.out.println("TrainOnline...");
    double error = 1;
    while (error > 0.00001) {
      try {
        error = runOnline(target, inputValues, 0.01);
        System.out.println(
            "Der Fehler ist: " + error + "; Der TestFehler: " + runTest(ttarget, tinputValues));

      } catch (BadConfigException e) {
        e.printStackTrace();
        break;
      }

    }

    double[] asd;

    // Eingabedaten setzen
    layer[0].setInput(inputValues[1]);

    // Ausgabe berechnen
    asd = layer[layer.length - 1].getOutput();

    System.out.println(
        "\nDas Ergebnis ist: " + asd[0] + " | " + asd[1] + " mit " + inputValues[1][0] + " | "
            + inputValues[1][1]);

  }

  public static void MLP(int inputNeurons, int outputNeurons, int[] hiddenLayers,
      ActivationFunction[] functions, double[] bias, boolean autoencoder)
      throws BadConfigException {

    // Konfiguration überprüfen
    if (inputNeurons < 1) {
      throw new BadConfigException("Ungültige Anzahl für Neuronen in der Eingabeschicht!");
    }
    if (outputNeurons < 1) {
      throw new BadConfigException("Ungültige Anzahl für Neuronen in der Ausgabeschicht!");
    }
    if (functions.length != hiddenLayers.length + 2) {
      throw new BadConfigException(
          "Anzahl der Aktivierungsfunktionen stimmt nicht mit den Layern überein!");
    }
    if (bias.length != hiddenLayers.length) {
      throw new BadConfigException("Anzahl der Bias(se?) stimmt nicht mit den Layern überein!");
    }

    // inputLayer erzeugen
    layer = new Layer[hiddenLayers.length + 2];
    layer[0] = new Layer(null, inputNeurons, functions[0], 0);

    // hiddenLayer erzeugen
    for (int i = 0; i < hiddenLayers.length; i++) {
      layer[i + 1] = new Layer(layer[i], hiddenLayers[i], functions[i], bias[i]);
    }

    // outputLayer erzeugen
    layer[layer.length - 1] = new Layer(layer[layer.length - 2], outputNeurons,
        functions[functions.length - 1], 0);

    if (!autoencoder) {
      return;
    }

    // Als Autoencoder trainieren
    layer[layer.length - 1].makeAutoencoder(0.5, 1000, 0.00001, 0.2);
  }

  public static double runOnline(int[] intTarget, double[][] input, double eta)
      throws BadConfigException {
    if (layer == null) {
      return 0;
    }

    if (eta < 0) {
      throw new BadConfigException("Eta is negative!");
    }

    double[] out;
    double[] target = new double[layer[layer.length - 1].getSize()];

    for (int i = 0; i < intTarget.length; i++) {
      int theData = intTarget[i];

      // Zielwert erzeugen
      Arrays.fill(target, 0);
      if (theData > target.length) {
        throw new BadConfigException("Invalid Target: " + theData);
      }
      target[theData] = 1;

      // Eingabedaten setzen
      layer[0].setInput(input[i]);

      // Ausgabe berechnen
      out = layer[layer.length - 1].getOutput();

      // Den Fehler an der Ausgabeschicht berechnen
      double[] errVec = new double[target.length];
      for (int h = 0; h < target.length; h++) {
        errVec[h] = out[h] - target[h];
      }

      // Fehler zurückpropagieren
      layer[layer.length - 1].backPropagate(errVec);

      // Gewichte anpassen
      layer[layer.length - 1].update(eta, 0.3);
    }

    // Mittelwert berechnen  und Fehler zurückgeben
    return runTest(intTarget, input);
  }

  /**
   * Diese Methode fuehrt einen Test mit den uebergebenen Daten durch.
   *
   * @return den Testfehler
   */
  public static double runTest(int[] intTarget, double[][] input) throws BadConfigException {
    double err = 0;
    double[] out;
    double[] target = new double[layer[layer.length - 1].getSize()];

    for (int i = 0; i < intTarget.length; i++) {
      int theData = intTarget[i];

      // Zielwert erzeugen
      Arrays.fill(target, 0);
      if (theData > target.length) {
        throw new BadConfigException("Invalid Target: " + theData);
      }
      target[theData] = 1;

      // Eingabedaten setzen
      layer[0].setInput(input[i]);

      // Ausgabe berechnen
      out = layer[layer.length - 1].getOutput();

      for (int h = 0; h < target.length; h++) {
        err += Math.pow(out[h] - target[h], 2);
      }

    }

    // Mittelwert berechnen  und Fehler zurückgeben
    return 0.5 * err / intTarget.length;
  }

}
