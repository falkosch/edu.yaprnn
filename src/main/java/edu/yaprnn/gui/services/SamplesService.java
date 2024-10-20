package edu.yaprnn.gui.services;

import edu.yaprnn.gui.images.Images;
import edu.yaprnn.gui.views.ImportImagesPanel;
import edu.yaprnn.samples.AudiosImportService;
import edu.yaprnn.samples.ImagesImportService;
import edu.yaprnn.samples.model.ImageSample;
import edu.yaprnn.samples.model.Sample;
import edu.yaprnn.samples.model.SoundSample;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.awt.Component;
import java.awt.Image;
import java.awt.image.AffineTransformOp;
import java.net.URL;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.JMenuItem;

@Singleton
public class SamplesService {

  @Inject
  AudiosImportService audiosImportService;
  @Inject
  ControlsService controlsService;
  @Inject
  DialogsService dialogsService;
  @Inject
  FilesService filesService;
  @Inject
  IconsService iconsService;
  @Inject
  ImagesImportService imagesImportService;

  @Inject
  Instance<ImportImagesPanel> importImagesPanelInstance;

  public JMenuItem importAudioMenuItem(Component parent,
      Consumer<List<SoundSample>> soundSamplesConsumer) {
    var title = "Import Audio";
    return controlsService.actionMenuItem(title, iconsService.importAudioIcon(),
        () -> filesService.selectFiles(parent, filesService.audioAiffFileExtension(), true, false,
            files -> {
              try {
                soundSamplesConsumer.accept(audiosImportService.fromAiff(files));
              } catch (Throwable throwable) {
                dialogsService.showError(parent, title, throwable);
              }
            }));
  }

  public JMenuItem importImagesMenuItem(Component parent,
      Consumer<List<ImageSample>> imageSamplesConsumer) {
    return controlsService.actionMenuItem(ImportImagesPanel.TITLE, iconsService.importImagesIcon(),
        () -> importImagesPanelInstance.get()
            .show(parent, (parameters) -> importImages(parent, imageSamplesConsumer,
                parameters.imagesPackage(), parameters.labelsPackage())));
  }

  public void importImages(Component parent, Consumer<List<ImageSample>> imageSamplesConsumer,
      String imagesPackage, String labelsPackage) {
    try {
      imageSamplesConsumer.accept(
          imagesImportService.fromImagesLabelsPackage(imagesPackage, labelsPackage));
    } catch (Throwable throwable) {
      dialogsService.showError(parent, ImportImagesPanel.TITLE, throwable);
    }
  }

  public void importImages(Component parent, Consumer<List<ImageSample>> imageSamplesConsumer,
      URL imagesPackage, URL labelsPackage) {
    try {
      imageSamplesConsumer.accept(
          imagesImportService.fromImagesLabelsPackage(imagesPackage, labelsPackage));
    } catch (Throwable throwable) {
      dialogsService.showError(parent, ImportImagesPanel.TITLE, throwable);
    }
  }

  public Image from(Sample sample, float zoom) {
    var image = sample.createPreview();

    try {
      var newWidth = (int) (image.getWidth(null) * zoom);
      var newHeight = (int) (image.getHeight(null) * zoom);
      return Images.resize(image, newWidth, newHeight, AffineTransformOp.TYPE_BICUBIC);
    } finally {
      image.flush();
    }
  }

  public Sample subSample(Sample sample, int resolution, float overlap) {
    if (sample instanceof ImageSample imageSample) {
      return imageSample.subSample(resolution, overlap);
    }
    if (sample instanceof SoundSample soundSample) {
      return soundSample.subSample(resolution, overlap);
    }
    throw unknownSampleType(sample);
  }

  private RuntimeException unknownSampleType(Sample sample) {
    return new UnsupportedOperationException("Unknowns sample type: %s".formatted(sample));
  }
}
