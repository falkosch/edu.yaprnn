package edu.yaprnn.samples;

import edu.yaprnn.events.OnSamplePreviewModifiedRouter;
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

  private static final float SHORT_NORM = -1f / Short.MIN_VALUE;

  @Inject
  OnSamplePreviewModifiedRouter onSamplePreviewModifiedRouter;
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

      var input = toFrequencyAmplitudes(toWaveSamples(rawData));
      return new SoundSample(file, name, label, target, input, input).subSample(
          onSamplePreviewModifiedRouter.getResolution(),
          onSamplePreviewModifiedRouter.getOverlap());
    }
  }

  private float[] toFrequencyAmplitudes(float[] samples) {
    var inputToFFTBuffer = Arrays.copyOf(samples, 2 * samples.length);
    new FloatFFT_1D(samples.length).realForwardFull(inputToFFTBuffer);

    var frequencyAmplitudes = new float[inputToFFTBuffer.length / 2];
    for (int i = 0, j = 0; j < inputToFFTBuffer.length; i++, j += 2) {
      var real = inputToFFTBuffer[j];
      var imaginary = inputToFFTBuffer[j + 1];
      frequencyAmplitudes[i] = (float) Math.sqrt(real * real + imaginary * imaginary);
    }

    return frequencyAmplitudes;
  }

  private float[] toWaveSamples(byte[] rawData) {
    var samples = new float[rawData.length / 2];

    for (int i = 0, j = 0; j < rawData.length; i++, j += 2) {
      samples[i] = SHORT_NORM * (rawData[j] << 8 | rawData[j + 1] & 0xff);
    }

    return samples;
  }
}
