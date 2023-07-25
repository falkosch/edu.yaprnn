package edu.yaprnn.samples;

import edu.yaprnn.samples.model.SoundSample;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import javax.sound.sampled.AudioSystem;
import lombok.SneakyThrows;
import org.jtransforms.fft.FloatFFT_1D;

@Singleton
public class AudiosImportService {

  @Inject
  ImportService importService;

  public List<SoundSample> fromAiff(File[] files) {
    return Arrays.stream(files).map(this::fromAiff).toList();
  }

  @SneakyThrows
  private SoundSample fromAiff(File file) {
    var name = file.getName();
    var label = name.toUpperCase().substring(0, 1);
    var target = importService.toTarget(label, SoundSample.LABELS);

    try (var audioInput = AudioSystem.getAudioInputStream(file)) {
      var readSize = audioInput.getFormat().getFrameSize() * audioInput.getFrameLength();
      var rawData = new byte[(int) readSize];
      var readTotal = audioInput.read(rawData);
      if (readTotal < readSize) {
        throw new IllegalArgumentException(
            "File %s seems to be an invalid AIFF file".formatted(file.getPath()));
      }

      var input = process(toWaveSamples(rawData));
      return new SoundSample(file, name, label, target, input, input);
    }
  }

  private float[] process(float[] data) {
    var processed = alignLengthToPowerOfTwo(data);
    var fftLength = processed.length / 2;
    new FloatFFT_1D(fftLength).realForwardFull(processed);

    var absolute = new float[fftLength];
    for (int i = 0, j = 0; i < fftLength; i++, j += 2) {
      var real = processed[j];
      var imaginary = processed[j + 1];
      absolute[i] = (float) Math.sqrt(real * real + imaginary * imaginary);
    }
    return absolute;
  }

  private float[] toWaveSamples(byte[] rawData) {
    var waveSamples = new float[rawData.length / 2];
    for (int i = 0, j = 0; j < rawData.length; i++, j += 2) {
      waveSamples[i] = rawData[j] << 8 | rawData[j + 1] & 0xff;
    }
    return waveSamples;
  }

  private float[] alignLengthToPowerOfTwo(float[] data) {
    var powerOfTwo = 1;
    while (powerOfTwo < data.length) {
      powerOfTwo *= 2;
    }
    return powerOfTwo == data.length ? data : Arrays.copyOf(data, powerOfTwo);
  }
}
