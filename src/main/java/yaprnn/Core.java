package yaprnn;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import yaprnn.dvv.DVV;
import yaprnn.dvv.Data;
import yaprnn.dvv.DataTypeMismatchException;
import yaprnn.dvv.FileMismatchException;
import yaprnn.dvv.InvalidFileException;
import yaprnn.dvv.NoSuchFileException;
import yaprnn.mlp.ActivationFunction;
import yaprnn.mlp.Eta;
import yaprnn.mlp.Linear;
import yaprnn.mlp.MLPStub;
import yaprnn.mlp.NeuralNetwork;
import yaprnn.mlp.Sigmoid;
import yaprnn.mlp.TangensHyperbolicus;

/**
 * Core is the main control class. It contains the methods for opening files, preprocessing,
 * training and classifying, which are called by the GUI.
 */
public class Core {

  private final List<ActivationFunction> activations;
  private MLPStub mlp;
  private DVV dvv;
  private GUIInterface gui;
  private List<Double> trainingErrors;
  private List<Double> testErrors;
  private boolean run = true;

  /**
   * Constructs a new Core Object.
   */
  public Core() {
    activations = new LinkedList<>();
    activations.add(new TangensHyperbolicus());
    activations.add(new Sigmoid());
    activations.add(new Linear());
  }

  /**
   * Classifies the given data item. This method returns a vector of percentages, the size of which
   * is equal to the number of classes into which the data is classified. An entry
   * classify(input)[k] == p means that input has a chance of p to belong to class k.
   *
   * @param input the input data item
   * @return the vector of percentages
   */
  public double[] classify(Data input) throws DataTypeMismatchException {
    if (mlp == null) {
      return null;
    }
    if (input.getData() == null) {
      throw new DataTypeMismatchException();
    }
    if (mlp.setNumInputNeurons(input.getData().length) || mlp.setNumOutputNeurons(
        dvv.getNumOutputNeurons()) || mlp.setDataType(dvv.getDataType())) {
      throw new DataTypeMismatchException();
    }
    return mlp.classify(input.getData());
  }

  /**
   * Opens an IdxPicture data set contained in the specified filenames.
   *
   * @param dataFilename  the file containing the image data
   * @param labelFilename the file containing the image labels
   * @throws NoSuchFileException   if one of the files does not exist or could not be opened
   * @throws InvalidFileException  if one of the files does not have the expected format
   * @throws FileMismatchException if the two files appear to belong to distinct data sets
   */
  public void openIdxPicture(String dataFilename, String labelFilename)
      throws NoSuchFileException, InvalidFileException, FileMismatchException, IOException {
    dvv = new DVV(dataFilename, labelFilename);
    gui.setDataSet(dvv.getDataSet());
  }

  /**
   * Opens an AiffSound data set contained in the stated filenames.
   *
   * @param filenames the collection containing the sound data
   * @throws InvalidFileException if one of the files is not a supported format
   * @throws NoSuchFileException  if one of the files does not exist
   */
  public void openAiffSound(Collection<String> filenames)
      throws InvalidFileException, NoSuchFileException {
    dvv = new DVV(filenames);
    gui.setDataSet(dvv.getDataSet());
  }

  /**
   * Creates a new MLP using the specified parameters and returns an interface to it.
   *
   * @param bias an array holding the bias for each layer
   * @return an interface to the new mlp
   */
  public NeuralNetwork newMLP(String name, int numLayers, int numNeurons, int activation,
      double bias) {
    mlp = new MLPStub(name, numLayers, numNeurons, activation, bias, activations);
    return mlp;
  }

  public NeuralNetwork newMLP(String name, int numLayers, int numNeurons, int activation,
      double bias, int maxIterations, double maxError, double eta) {
    mlp = new MLPStub(name, numLayers, numNeurons, activation, bias, activations);
    mlp.setAutoencoder(maxIterations, maxError, eta);
    return mlp;
  }

  /**
   * Loads a MLP from the specified file.
   *
   * @param filename the name of the file from which to read
   * @return a NeuralNetwork interface representing the loaded mlp
   * @throws NoSuchFileException if the specified file does not exist or could not be opened
   */
  public NeuralNetwork loadMLP(String filename)
      throws NoSuchFileException, IOException, ClassNotFoundException {
    ObjectInputStream in;
    try {
      in = new ObjectInputStream(new FileInputStream(filename));
    } catch (FileNotFoundException e) {
      throw new NoSuchFileException(filename);
    }
    mlp = (MLPStub) in.readObject();
    in.close();
    return mlp;
  }

  /**
   * Saves the current MLP to the specified filename.
   *
   * @param filename the name of the file where the MLP is to be stored
   */
  public void saveMLP(String filename) throws NoSuchFileException, IOException {
    ObjectOutputStream out;
    try {
      out = new ObjectOutputStream(new FileOutputStream(filename));
      out.writeObject(mlp);
      out.flush();
      out.close();
    } catch (FileNotFoundException e) {
      throw new NoSuchFileException(filename);
    }
  }

  /**
   * Performs online training with the specified parameters, using the current data set and mlp.
   *
   * @param eta           the learning rate
   * @param maxIterations the maximum number of iterations (epochs) to perform
   * @param maxError      training stops if the test error falls below maxError
   */

  public void trainOnline(Eta eta, int maxIterations, double maxError, double momentum)
      throws DataTypeMismatchException {
    trainingErrors = new LinkedList<>();
    testErrors = new LinkedList<>();
    double trainingErr = Double.MAX_VALUE;
    double testErr;
    run = true;
    if (mlp.setNumInputNeurons(dvv.getNumInputNeurons()) || mlp.setNumOutputNeurons(
        dvv.getNumOutputNeurons()) || mlp.setDataType(dvv.getDataType())) {
      throw new DataTypeMismatchException();
    }
    for (int i = 0; i < maxIterations && run; i++) {
      Collection<Data> test = dvv.getTestData();
      Collection<Data> train = dvv.getTrainingData();

      trainingErr = mlp.runOnline(train, eta.getEta(trainingErr), momentum);
      testErr = mlp.runTest(test);

      trainingErrors.add(trainingErr);
      testErrors.add(testErr);

      gui.setTrainingError(trainingErrors);
      gui.setTestError(testErrors);

      System.out.println(
          "Trainingsfehler: " + trainingErr + "   Testfehler " + testErr + " Eta: " + eta);
      if (trainingErr <= maxError) {
        break;
      }
    }
  }

  /**
   * Performs batch training with the specified parameters, using the current data set and mlp.
   *
   * @param eta           the learning rate
   * @param maxIterations the maximum number of iterations (epochs) to perform
   * @param maxError      training stops if the test error falls below maxError
   */

  public void trainBatch(Eta eta, int maxIterations, double maxError, int batchSize,
      double momentum) throws DataTypeMismatchException {
    trainingErrors = new LinkedList<>();
    testErrors = new LinkedList<>();
    double trainingErr = Double.MAX_VALUE;
    double testErr;
    run = true;
    if (mlp.setNumInputNeurons(dvv.getNumInputNeurons()) || mlp.setNumOutputNeurons(
        dvv.getNumOutputNeurons()) || mlp.setDataType(dvv.getDataType())) {
      throw new DataTypeMismatchException();
    }
    mlp.resetIterations();
    for (int i = 0; i < maxIterations && run; i++) {
      Collection<Data> test = dvv.getTestData();
      Collection<Data> train = dvv.getTrainingData();

      trainingErr = mlp.runBatch(train, batchSize, eta.getEta(trainingErr), momentum);
      testErr = mlp.runTest(test);

      trainingErrors.add(trainingErr);
      testErrors.add(testErr);

      gui.setTrainingError(trainingErrors);
      gui.setTestError(testErrors);

      System.out.println(
          "Trainingsfehler: " + trainingErr + "   Testfehler " + testErr + " Eta: " + eta);
      if (trainingErr <= maxError) {
        break;
      }
    }
  }

  /**
   * Preprocesses the currently loaded data set using the specified parameters. If no data set is
   * loaded, this method does nothing.
   *
   * @param resolution      the desired resolution of the result
   * @param overlap         the window overlap used for subsampling. Must be a value between 0 and
   *                        0.95
   * @param scalingFunction the function used to scale (e.g. to the range [0, 1]) the subsampled
   *                        data
   */
  public void preprocess(int resolution, double overlap, ActivationFunction scalingFunction)
      throws NoSuchFileException {
    if (dvv != null) {
      try {
        dvv.preprocess(resolution, overlap, scalingFunction);
      } catch (NoSuchFileException ex) {
        throw new NoSuchFileException(ex.getFilename());
      }
    }
  }

  /**
   * Randomly selects data for the training or test data set, according to the specified
   * percentages.
   *
   * @param trainingDataPercentage the percentage of data to be used for training
   * @param testDataPercentage     the percentage of data to be used for testing
   */
  public void chooseRandomTrainingData(double trainingDataPercentage, double testDataPercentage) {
    if (dvv != null) {
      dvv.chooseRandomTrainingData(trainingDataPercentage, testDataPercentage);
    }
  }

  /**
   * Returns a list of all available activation functions.
   *
   * @return the list of activationFunctions
   */
  public List<ActivationFunction> getAllActivationFunctions() {
    return activations;
  }

  /**
   * Sets the GUI for the Core.
   */
  public void setGUI(GUIInterface gui) {
    this.gui = gui;
  }

  /**
   * Stops a running learning process.
   */
  public void stopLearning() {
    run = false;
  }

  public void loadDataSet(String fileName) throws Exception {
    dvv.loadDataSet(fileName);
  }

  public void saveDataSet(String fileName) throws Exception {
    dvv.saveDataSet(fileName);
  }

}
