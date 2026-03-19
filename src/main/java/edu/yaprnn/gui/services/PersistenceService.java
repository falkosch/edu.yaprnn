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
    try {
      objectMapper.writeValue(new File(path), trainingData);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  public TrainingData loadTrainingData(String path) {
    try {
      return objectMapper.readValue(new File(path), TrainingData.class);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  public TrainingData loadTrainingData(URL url) {
    try {
      return objectMapper.readValue(url, TrainingData.class);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  public void saveMultiLayerNetworkTemplate(MultiLayerNetworkTemplate multiLayerNetworkTemplate,
      String path) {
    try {
      objectMapper.writeValue(new File(path), multiLayerNetworkTemplate);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  public MultiLayerNetworkTemplate loadMultiLayerNetworkTemplate(String path) {
    try {
      return objectMapper.readValue(new File(path), MultiLayerNetworkTemplate.class);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  public MultiLayerNetworkTemplate loadMultiLayerNetworkTemplate(URL url) {
    try {
      return objectMapper.readValue(url, MultiLayerNetworkTemplate.class);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  public void saveMultiLayerNetwork(MultiLayerNetwork multiLayerNetwork, String path) {
    try {
      objectMapper.writeValue(new File(path), multiLayerNetwork);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  public MultiLayerNetwork loadMultiLayerNetwork(String path) {
    try {
      return objectMapper.readValue(new File(path), MultiLayerNetwork.class);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  public MultiLayerNetwork loadMultiLayerNetwork(URL url) {
    try {
      return objectMapper.readValue(url, MultiLayerNetwork.class);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }
}
