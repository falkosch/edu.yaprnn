package edu.yaprnn.support;

import static org.assertj.core.api.Assertions.assertThat;

import java.security.SecureRandom;
import java.util.Random;
import org.junit.jupiter.api.Test;

class RandomConfigurerTest {

  final RandomConfigurer configurer = new RandomConfigurer();

  @Test
  void shouldProduceSecureRandom() {
    Random random = configurer.getRandom();

    assertThat(random).isInstanceOf(SecureRandom.class);
  }

  @Test
  void shouldReturnNonNull() {
    assertThat(configurer.getRandom()).isNotNull();
  }

  @Test
  void shouldDefineBeanName() {
    assertThat(RandomConfigurer.YAPRNN_RANDOM_BEAN).isEqualTo("yaprnnRandom");
  }
}
