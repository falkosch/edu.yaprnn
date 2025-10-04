package yaprnn.mlp;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collection;
import yaprnn.dvv.Data;

/**
 * This class represents the MLP and makes heavy use of the class {@link Layer}.
 */
public class MLP implements Serializable {

  @Serial
  private static final long serialVersionUID = -5212835785366190139L;

  private final Layer[] layer;
  int iterations = 0;

  /**
   * Builds the Network and sets all necessary variables.
   *
   * @param hiddenLayers array with hidden layers.
   * @param functions    array with activation functions.
   * @param bias         Array with biases.
   */
  public MLP(int inputNeurons, int outputNeurons, int[] hiddenLayers,
      ActivationFunction[] functions, double[] bias) throws BadConfigException {

    // Tests the configuration
    if (inputNeurons < 1) {
      throw new BadConfigException("Invalid number of neurons in the input layer!");
    }
    if (outputNeurons < 1) {
      throw new BadConfigException("Invalid number of neurons in the output layer!");
    }
    if (functions.length != hiddenLayers.length + 2) {
      throw new BadConfigException(
          "The number of activiation functions is not equal to the number of layers!");
    }
    if (bias.length != hiddenLayers.length) {
      throw new BadConfigException(
          "The number of bias values is not equal to the number of layers!");
    }

    // Creates the input layer
    layer = new Layer[hiddenLayers.length + 2];
    layer[0] = new Layer(null, inputNeurons, functions[0], 0);

    // creates  hidden layers
    for (int i = 0; i < hiddenLayers.length; i++) {
      layer[i + 1] = new Layer(layer[i], hiddenLayers[i], functions[i], bias[i]);
    }

    // creates the outputlayer
    layer[layer.length - 1] = new Layer(layer[layer.length - 2], outputNeurons,
        functions[functions.length - 1], 0);

  }

  /**
   * This function trains the MLP as autoencoder by using the function
   * {@link Layer#makeAutoencoder(double, int, double, double) makeAutoencoder}
   *
   * @param maxIterations Break condition for the training. Training will stop if maxIterations is reached.
   * @param maxError      If the net's error falls below this border, training will be aborted.
   * @param eta           the learning rate
   */
  public void makeAutoencoder(int maxIterations, double maxError, double eta) {
    try {
      layer[layer.length - 1].makeAutoencoder(Math.random(), maxIterations, maxError, eta);
    } catch (BadConfigException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * This function performs the online calculation with the the network.
   *
   * @param dataCollection A collection of the type {@link Data} with input and target values.
   * @param eta            The learning rate to be used.
   */
  public double runOnline(Collection<Data> dataCollection, double eta, double momentum) {

    Layer outLayer = layer[layer.length - 1];
    ActivationFunction outAVF = outLayer.getActivationFunction();
    int outLayerSize = outLayer.getSize();

    double[] errVec = new double[outLayerSize];

    for (Data theData : dataCollection) {
      // get the index where the target value is 1
      int target = theData.getTarget();

      // Sets the input data
      if (!layer[0].setInput(theData.getData())) {
        System.out.println("Can't set input data!");
      }

      // Calculate the output
      double[] out = outLayer.getOutput();

      // Calculates the error of the output layer
      for (int h = 0; h < outLayerSize; h++) {
        if (h == target) {
          errVec[h] = (out[h] - 1) * outAVF.derivation(outLayer.layerInput[h]);
        } else {
          errVec[h] = out[h] * outAVF.derivation(outLayer.layerInput[h]);
        }
      }

      // Error backpropagation
      /*System.out.println("Mittlerer Gadient: " + */
      outLayer.backPropagate(errVec)
      /* / layer.length)*/;

      // Adjust the weights
      if (momentum > 0) {
        outLayer.update(eta, momentum);
      } else {
        outLayer.update(eta);
      }

    }

    return runTest(dataCollection);
  }

  /**
   * This method performs a test using delivered data.
   *
   * @param dataCollection The data, tu be used for the test.
   * @return The test error. If an error occurse, returns 0.
   */
  public double runTest(Collection<Data> dataCollection) {

    Layer outLayer = layer[layer.length - 1];
    ActivationFunction outAVF = outLayer.getActivationFunction();
    int outLayerSize = outLayer.getSize();

    double err = 0;

    for (Data theData : dataCollection) {
      // get the index where the target value is 1
      int target = theData.getTarget();

      // Sets the input data
      if (!layer[0].setInput(theData.getData())) {
        return 0;
      }

      // Calculate the output
      double[] out = outLayer.getOutput();

      // Calculates the error of the output layer
      for (int h = 0; h < outLayerSize; h++) {
        double v = h == target
            ? (out[h] - 1) * outAVF.derivation(outLayer.layerInput[h])
            : out[h] * outAVF.derivation(outLayer.layerInput[h]);

        err += v * v;
      }

    }

    return (0.5 * err) / dataCollection.size();
  }

  /**
   * This function performs the batch calculation  with the Network
   *
   * @param dataCollection A collection of the type {@link Data}a with input and target values.
   * @param eta            The learning rate to be used.
   * @return The Training error computed by the function {@link #runTest(Collection)}. In case of an error returns 0.
   */

  public double runBatch(Collection<Data> dataCollection, int batchSize, double eta,
      double momentum) {

    Layer outLayer = layer[layer.length - 1];
    ActivationFunction outAVF = outLayer.getActivationFunction();
    int outLayerSize = outLayer.getSize();

    double[] errVec = new double[outLayerSize];

    for (Data theData : dataCollection) {
      // get the index where the target value is 1
      int target = theData.getTarget();

      // Sets the input data
      if (!layer[0].setInput(theData.getData())) {
        System.out.println("Can't set input data!");
      }

      // Calculate the output
      double[] out = outLayer.getOutput();

      // Calculates the error of the output layer
      for (int h = 0; h < outLayerSize; h++) {
        if (h == target) {
          errVec[h] = (out[h] - 1) * outAVF.derivation(outLayer.layerInput[h]);
        } else {
          errVec[h] = out[h] * outAVF.derivation(outLayer.layerInput[h]);
        }
      }

      // Error backpropagation
      System.out.println("Mittlerer Gadient: " + outLayer.backPropagate(errVec) / layer.length);
      iterations++;

      if (iterations % batchSize == 0) {
        // Adjust the weights
        if (momentum > 0) {
          outLayer.update(eta, momentum);
        } else {
          outLayer.update(eta);
        }
      }

    }
    return runTest(dataCollection);
  }

  /**
   * This method is used to classify a given input vector.
   *
   * @param input The input to classify with a dimension equal to the output neurons.
   * @return The output of neurons in percents.
   */
  public double[] classify(double[] input) {
    layer[0].setInput(input);
    double[] netOutput = layer[layer.length - 1].getOutput();
    double[] retVal = new double[netOutput.length];

    for (int i = 0; i < retVal.length; i++) {
      retVal[i] = Math.exp(10 * netOutput[i]);
    }
    // Sum up
    double sum = 0;
    for (double v : retVal) {
      sum += v;
    }

    for (int i = 0; i < netOutput.length; i++) {
      retVal[i] = retVal[i] * 100 / sum;
    }

    return retVal;
  }

  /**
   * String-representation of the neuronal network
   *
   * @return An ascii presentation of the network's weights.
   */
  public String toString() {
    StringBuilder buffer = new StringBuilder();
    for (int i = 0; i < layer.length; i++) {
      buffer.append("Layer ").append(i);
      buffer.append("\n").append(layer[i].toString()).append("\n");
    }
    return buffer.toString();
  }

  /**
   * Returns the WeightMatrix of the {@link Layer}.
   *
   * @param layer The layer's index.
   * @return The layer's WeightMatrix.
   */
  public double[][] getWeights(int layer) {
    if (layer > (this.layer.length - 1)) {
      return null;
    }

    return this.layer[layer].getWeightMatrix();
  }

  /**
   * This function resets the integrated iteration-counter.
   */
  public void resetIterations() {
    iterations = 0;
  }
}
