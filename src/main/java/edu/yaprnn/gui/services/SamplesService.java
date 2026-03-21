package edu.yaprnn.gui.services;

import edu.yaprnn.samples.model.ImageSample;
import edu.yaprnn.samples.model.Sample;
import edu.yaprnn.samples.model.SimpleSample;
import edu.yaprnn.samples.model.SoundSample;
import jakarta.inject.Singleton;

@Singleton
public class SamplesService {

  public Sample subSample(Sample sample, int resolution, float overlap) {
    return switch (sample) {
      case ImageSample imageSample -> imageSample.subSample(resolution, overlap);
      case SoundSample soundSample -> soundSample.subSample(resolution, overlap);
      case SimpleSample simpleSample -> simpleSample;
      case null ->
          throw new UnsupportedOperationException("Unknown sample type: null");
    };
  }
}
