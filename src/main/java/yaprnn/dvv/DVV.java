package yaprnn.dvv;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Vector;
import yaprnn.mlp.ActivationFunction;

/**
 * DVV is the main data managment and preprocessing class. It provides methods for reading picture
 * and sound files and for preprocessing loaded files.
 */
public class DVV {

  private final Collection<Data> allData;
  private final int numOutputNeurons;
  private final Data.Type type;
  private Collection<Data> trainingData;
  private Collection<Data> testData;
  private int numInputNeurons;

  /**
   * Constructs a DVV for holding {@link IdxPicture} data with the specified filenames.
   *
   * @param dataFilename  the name of the file containing the image data
   * @param labelFilename the name of the file containing the labels/targets
   * @throws InvalidFileException  if one of the files does not have the required format
   * @throws FileMismatchException if the files do not appear to belong to the same dataset
   * @throws NoSuchFileException   if one of the files does not exist
   */
  public DVV(String dataFilename, String labelFilename)
      throws InvalidFileException, FileMismatchException, NoSuchFileException, IOException {
    allData = IdxPicture.readFromFile(dataFilename, labelFilename);
    numOutputNeurons = 10;
    type = Data.Type.PICTURE;
  }

  /**
   * Constructs a DVV for holding {@link AiffSound} data with the specified filenames.
   *
   * @param filenames the names of the file containing the sound data
   * @throws NoSuchFileException if one of the files does not exist
   */
  public DVV(Collection<String> filenames) throws InvalidFileException, NoSuchFileException {
    allData = AiffSound.readFromFile(filenames);
    numOutputNeurons = 5;
    type = Data.Type.AUDIO;
  }

  /**
   * Returns the whole data set.
   *
   * @return the data set
   */
  public Collection<Data> getDataSet() {
    return allData;
  }

  /**
   * Returns the training data set.
   *
   * @return the training data
   */
  public Collection<Data> getTrainingData() {
    if (trainingData == null) {
      selectTrainingData();
    }
    shuffle(trainingData);
    return trainingData;
  }

  /**
   * Selects training and test data and stores them in the appropriate collections.
   */
  private void selectTrainingData() {
    trainingData = new LinkedList<>();
    testData = new LinkedList<>();
    for (Data data : allData) {
      if (data.isTraining()) {
        trainingData.add(data);
      } else if (data.isTest()) {
        testData.add(data);
      }
    }
  }

  /**
   * Shuffles the specified data set.
   */
  private void shuffle(Collection<Data> input) {
    Data[] dataset = input.toArray(new Data[0]);
    input.clear();
    Random random = new Random();
    int n = dataset.length;
    while (n > 1) {
      final int k = random.nextInt(n);
      --n;
      final Data tmp = dataset[n];
      dataset[n] = dataset[k];
      dataset[k] = tmp;
    }
    Collections.addAll(input, dataset);
  }

  /**
   * Returns the test data set.
   *
   * @return the test data
   */
  public Collection<Data> getTestData() {
    if (testData == null) {
      selectTrainingData();
    }
    shuffle(testData);
    return testData;
  }

  /**
   * Randomly selects data for the training or test data set, according to the specified
   * percentages.
   *
   * @param trainingDataPercentage the percentage of data to be used for training
   * @param testDataPercentage     the percentage of data to be used for testing
   */
  public void chooseRandomTrainingData(double trainingDataPercentage, double testDataPercentage) {
    shuffle(allData);
    int numTraining = (int) (allData.size() * trainingDataPercentage);
    int numTest = (int) (allData.size() * testDataPercentage);
    for (Data data : allData) {
      if (numTraining > 0) {
        data.setTraining();
        --numTraining;
      } else if (numTest > 0) {
        data.setTest();
        --numTest;
      } else {
        data.setNotUsed();
      }
    }
    trainingData = null;
    testData = null;
  }

  /**
   * Preprocesses the whole data set.
   *
   * @param resolution      the data is to be sampled to
   * @param overlap         the overlap used when determining the window sizes
   * @param scalingFunction the function used to scale the subsampled data
   */
  public void preprocess(int resolution, double overlap, ActivationFunction scalingFunction)
      throws NoSuchFileException {
    if (allData.size() > 0) {
      try {
        for (Data data : allData) {
          data.subsample(resolution, overlap, scalingFunction);
        }
      } catch (NoSuchFileException ex) {
        throw new NoSuchFileException(ex.getFilename());
      }
      if (allData.iterator().next().isAudio()) {
        numInputNeurons = resolution;
      } else {
        numInputNeurons = resolution * resolution;
      }
    }
  }

  /**
   * Returns the size of the input vector.
   *
   * @return the size of the input vector
   */
  public int getNumInputNeurons() {
    return numInputNeurons;
  }

  /**
   * Retuns the size of the output vector.
   *
   * @return the size of the output vector
   */
  public int getNumOutputNeurons() {
    return numOutputNeurons;
  }

  /**
   * Returns the type of the data.
   *
   * @return the type of the data
   */
  public Data.Type getDataType() {
    return type;
  }

  /**
   * Saves the training and test set to a set file.
   *
   * @param fileName the file that receives the training and the test set
   */
  public void saveDataSet(String fileName) throws Exception {
    // Wir merken uns nur die Kurznamen.
    List<String> trainingSet = new Vector<>();
    List<String> testSet = new Vector<>();
    for (Data d : allData) {
      if (d.isTraining()) {
        trainingSet.add(d.getName());
      } else if (d.isTest()) {
        testSet.add(d.getName());
      }
    }
    ObjectOutputStream out;
    try {
      out = new ObjectOutputStream(new FileOutputStream(fileName));
      out.writeObject(trainingSet);
      out.writeObject(testSet);
      out.flush();
      out.close();
    } catch (FileNotFoundException e) {
      throw new NoSuchFileException(fileName);
    }
  }

  /**
   * Loads a training and test set from a set file.
   *
   * @param fileName the file that contains the training and the test set
   */
  @SuppressWarnings("unchecked")
  public void loadDataSet(String fileName) throws Exception {
    List<String> trainingSet;
    List<String> testSet;
    ObjectInputStream in;
    try {
      in = new ObjectInputStream(new FileInputStream(fileName));
    } catch (FileNotFoundException e) {
      throw new NoSuchFileException(fileName);
    }
    trainingSet = (List<String>) in.readObject();
    testSet = (List<String>) in.readObject();
    in.close();
    for (Data d : allData) {
      if (trainingSet.contains(d.getName())) {
        d.setTraining();
      } else if (testSet.contains(d.getName())) {
        d.setTest();
      } else {
        d.setNotUsed();
      }
    }
  }

}
