package edu.yaprnn.support;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ProvidersTest {

  @Test
  void shouldReturnConstantInt() {
    var supplier = Providers.constant(42);
    assertThat(supplier.getAsInt()).isEqualTo(42);
    assertThat(supplier.getAsInt()).isEqualTo(42);
  }

  @Test
  void shouldReturnConstantObject() {
    var supplier = Providers.constant("hello");
    assertThat(supplier.get()).isEqualTo("hello");
  }

  @Test
  void shouldReturnConstantNull() {
    var supplier = Providers.constant(null);
    assertThat(supplier.get()).isNull();
  }

  @Test
  void shouldMapSupplierValue() {
    var supplier = Providers.mapped(() -> 5, x -> x * 2);
    assertThat(supplier.get()).isEqualTo(10);
  }

  @Test
  void shouldComposeWithStringMapper() {
    var supplier = Providers.mapped(() -> 42, Object::toString);
    assertThat(supplier.get()).isEqualTo("42");
  }
}
