package edu.yaprnn.samples;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.File;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class AudiosImportServiceTest {

  final AudiosImportService service = new AudiosImportService();

  @Nested
  class FromAiff {

    @Test
    void shouldThrowWhenFileNameIsEmpty() {
      var file = new File("");

      assertThatThrownBy(() -> service.fromAiff(new File[]{file}))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("Audio file has no name");
    }
  }
}
