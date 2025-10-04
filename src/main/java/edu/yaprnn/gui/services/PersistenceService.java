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
import java.net.URL;
import lombok.SneakyThrows;

@Singleton
public class PersistenceService {

  @Named(JacksonConfigurer.YAPRNN_OBJECT_MAPPER_BEAN)
  @Inject
  ObjectMapper objectMapper;

  @SneakyThrows
  public void saveTrainingData(TrainingData trainingData, String path) {
    objectMapper.writeValue(new File(path), trainingData);
  }

  @SneakyThrows
  public TrainingData loadTrainingData(String path) {
    return objectMapper.readValue(new File(path), TrainingData.class);
  }

  @SneakyThrows
  public TrainingData loadTrainingData(URL url) {
    return objectMapper.readValue(url, TrainingData.class);
  }

  @SneakyThrows
  public void saveMultiLayerNetworkTemplate(MultiLayerNetworkTemplate multiLayerNetworkTemplate,
      String path) {
    objectMapper.writeValue(new File(path), multiLayerNetworkTemplate);
  }

  @SneakyThrows
  public MultiLayerNetworkTemplate loadMultiLayerNetworkTemplate(String path) {
    return objectMapper.readValue(new File(path), MultiLayerNetworkTemplate.class);
  }

  @SneakyThrows
  public MultiLayerNetworkTemplate loadMultiLayerNetworkTemplate(URL url) {
    return objectMapper.readValue(url, MultiLayerNetworkTemplate.class);
  }

  @SneakyThrows
  public void saveMultiLayerNetwork(MultiLayerNetwork multiLayerNetwork, String path) {
    objectMapper.writeValue(new File(path), multiLayerNetwork);
  }

  @SneakyThrows
  public MultiLayerNetwork loadMultiLayerNetwork(String path) {
    return objectMapper.readValue(new File(path), MultiLayerNetwork.class);
  }

  @SneakyThrows
  public MultiLayerNetwork loadMultiLayerNetwork(URL url) {
    return objectMapper.readValue(url, MultiLayerNetwork.class);
  }
}
