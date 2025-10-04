package edu.yaprnn.support;

import java.util.function.Function;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

public final class Providers {

  public static IntSupplier constant(int value) {
    return () -> value;
  }

  public static <T> Supplier<T> constant(T value) {
    return () -> value;
  }

  public static <T, R> Supplier<R> mapped(Supplier<T> supplier, Function<T, R> mapper) {
    return () -> mapper.apply(supplier.get());
  }
}
