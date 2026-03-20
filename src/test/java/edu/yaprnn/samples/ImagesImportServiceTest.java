package edu.yaprnn.samples;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class ImagesImportServiceTest {

  final ImagesImportService service = new ImagesImportService();

  @Nested
  class Process {

    @Test
    void convertsUnsignedBytesToNormalizedFloats() throws Exception {
      var data = new byte[]{0, 127, (byte) 128, (byte) 255};

      var result = invokeProcess(data);

      assertThat(result).containsExactly(0f / 255f, 127f / 255f, 128f / 255f, 255f / 255f);
    }

    @Test
    void emptyInputReturnsEmptyArray() throws Exception {
      var result = invokeProcess(new byte[0]);

      assertThat(result).isEmpty();
    }

    private float[] invokeProcess(byte[] data) throws Exception {
      Method method = ImagesImportService.class.getDeclaredMethod("process", byte[].class);
      method.setAccessible(true);
      return (float[]) method.invoke(service, data);
    }
  }
}
