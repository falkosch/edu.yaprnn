package edu.yaprnn.samples;

import edu.yaprnn.events.OnSamplePreviewModifiedRouter;
import edu.yaprnn.samples.model.SoundSample;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Arrays;
import java.util.List;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import org.jtransforms.fft.FloatFFT_1D;

@Singleton
public final class AudiosImportService {

  private static final float SHORT_NORM = -1f / Short.MIN_VALUE;

  @Inject
  OnSamplePreviewModifiedRouter onSamplePreviewModifiedRouter;
  @Inject
  ImportService importService;

  public List<SoundSample> fromAiff(File[] files) {
    return Arrays.stream(files).map(this::fromAiff).toList();
  }

  private SoundSample fromAiff(File file) {
    var name = file.getName();
    if (name.isEmpty()) {
      throw new IllegalArgumentException("Audio file has no name: %s".formatted(file.getPath()));
    }
    var label = name.toUpperCase().substring(0, 1);
    var target = importService.toTarget(label, SoundSample.LABELS);

    try (var audioInput = AudioSystem.getAudioInputStream(file)) {
      var format = audioInput.getFormat();
      if (format.getFrameSize() != 2) {
        throw new IllegalArgumentException(
            "Expected 16-bit audio (frame size 2), got frame size %d for file %s".formatted(
                format.getFrameSize(), file.getPath()));
      }
      if (!format.isBigEndian()) {
        throw new IllegalArgumentException(
            "Expected big-endian audio format for file %s".formatted(file.getPath()));
      }
      var readSize = format.getFrameSize() * audioInput.getFrameLength();
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
    } catch (UnsupportedAudioFileException e) {
      throw new IllegalArgumentException(
          "File %s is not a supported audio format".formatted(file.getPath()), e);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
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
