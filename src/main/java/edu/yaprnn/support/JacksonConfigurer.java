package edu.yaprnn.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Named;
import jakarta.inject.Singleton;

@Singleton
public class JacksonConfigurer {

  public static final String YAPRNN_OBJECT_MAPPER_BEAN = "yaprnnObjectMapper";

  private ObjectMapper objectMapper;

  @PostConstruct
  void postConstruct() {
    objectMapper = new ObjectMapper().disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
  }

  @Named(YAPRNN_OBJECT_MAPPER_BEAN)
  @Produces
  public ObjectMapper getObjectMapper() {
    return objectMapper;
  }
}
