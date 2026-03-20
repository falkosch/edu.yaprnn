package edu.yaprnn.gui.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.yaprnn.networks.MultiLayerNetwork;
import edu.yaprnn.networks.templates.MultiLayerNetworkTemplate;
import edu.yaprnn.support.JacksonConfigurer;
import edu.yaprnn.training.TrainingData;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;

@Singleton
public class PersistenceService {

  @Named(JacksonConfigurer.YAPRNN_OBJECT_MAPPER_BEAN)
  @Inject
  ObjectMapper objectMapper;

  public void saveTrainingData(TrainingData trainingData, String path) {
    write(() -> objectMapper.writeValue(new File(path), trainingData));
  }

  public TrainingData loadTrainingData(String path) {
    return read(() -> objectMapper.readValue(new File(path), TrainingData.class));
  }

  public TrainingData loadTrainingData(URL url) {
    return read(() -> objectMapper.readValue(url, TrainingData.class));
  }

  public void saveMultiLayerNetworkTemplate(MultiLayerNetworkTemplate multiLayerNetworkTemplate,
      String path) {
    write(() -> objectMapper.writeValue(new File(path), multiLayerNetworkTemplate));
  }

  public MultiLayerNetworkTemplate loadMultiLayerNetworkTemplate(String path) {
    return read(() -> objectMapper.readValue(new File(path), MultiLayerNetworkTemplate.class));
  }

  public MultiLayerNetworkTemplate loadMultiLayerNetworkTemplate(URL url) {
    return read(() -> objectMapper.readValue(url, MultiLayerNetworkTemplate.class));
  }

  public void saveMultiLayerNetwork(MultiLayerNetwork multiLayerNetwork, String path) {
    write(() -> objectMapper.writeValue(new File(path), multiLayerNetwork));
  }

  public MultiLayerNetwork loadMultiLayerNetwork(String path) {
    return read(() -> objectMapper.readValue(new File(path), MultiLayerNetwork.class));
  }

  public MultiLayerNetwork loadMultiLayerNetwork(URL url) {
    return read(() -> objectMapper.readValue(url, MultiLayerNetwork.class));
  }

  private <T> T read(IOSupplier<T> supplier) {
    try {
      return supplier.get();
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  private void write(IORunnable runnable) {
    try {
      runnable.run();
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  @FunctionalInterface
  interface IOSupplier<T> {

    T get() throws IOException;
  }

  @FunctionalInterface
  interface IORunnable {

    void run() throws IOException;
  }
}
