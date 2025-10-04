package edu.yaprnn.support;

import jakarta.enterprise.inject.Produces;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import java.security.SecureRandom;
import java.util.Random;

@Singleton
public class RandomConfigurer {

  public static final String YAPRNN_RANDOM_BEAN = "yaprnnRandom";

  private final byte[] seed = new byte[]{};

  @Named(YAPRNN_RANDOM_BEAN)
  @Produces
  public Random getRandom() {
    return new SecureRandom(seed);
  }
}
