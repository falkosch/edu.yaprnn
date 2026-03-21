package edu.yaprnn.gui.views;

import edu.yaprnn.gui.services.SamplesService;
import edu.yaprnn.samples.model.Sample;
import edu.yaprnn.support.swt.ImageCanvas;
import edu.yaprnn.support.swt.SwtImages;
import java.awt.image.BufferedImage;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineEvent.Type;
import lombok.extern.java.Log;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;

/**
 * SWT composite for displaying sample details: HTML meta info and original/sub-sampled image
 * previews. Replaces the Swing SampleDetailsView.
 */
@Log
public class SwtSampleDetailsComposite extends Composite {

  private static final String HTML_WRAPPER = """
      <html><head><style>
        * { font-family: "Segoe UI", "Helvetica Neue", sans-serif; font-size: 12px; }
        table { border-collapse: collapse; }
        th { text-align: left; padding-right: 8px; }
        td, th { padding: 2px 4px; }
      </style></head><body>%s</body></html>
      """;

  private final Display display;
  private final SamplesService samplesService;

  private Browser metaBrowser;
  private ImageCanvas originalCanvas;
  private ImageCanvas subSampledCanvas;
  private Spinner zoomSpinner;
  private Spinner resolutionSpinner;
  private Spinner overlapSpinner;

  private final AtomicReference<Clip> clipRef = new AtomicReference<>();

  private Sample sample;
  private Image originalImage;
  private Image subSampledImage;

  public SwtSampleDetailsComposite(Composite parent, Display display,
      SamplesService samplesService) {
    super(parent, SWT.NONE);
    this.display = display;
    this.samplesService = samplesService;
    setLayout(new GridLayout(1, false));
    createContent();
  }

  private void createContent() {
    // Controls bar
    var controlsComposite = new Composite(this, SWT.NONE);
    controlsComposite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
    controlsComposite.setLayout(new GridLayout(6, false));

    new Label(controlsComposite, SWT.NONE).setText("Zoom:");
    zoomSpinner = new Spinner(controlsComposite, SWT.BORDER);
    zoomSpinner.setValues(1, 1, 20, 0, 1, 5);
    zoomSpinner.addListener(SWT.Selection, e -> updatePreview());

    new Label(controlsComposite, SWT.NONE).setText("Resolution:");
    resolutionSpinner = new Spinner(controlsComposite, SWT.BORDER);
    resolutionSpinner.setValues(256, 1, 1024, 0, 1, 10);
    resolutionSpinner.addListener(SWT.Selection, e -> updatePreview());

    new Label(controlsComposite, SWT.NONE).setText("Overlap:");
    overlapSpinner = new Spinner(controlsComposite, SWT.BORDER);
    overlapSpinner.setValues(100, 0, 1000, 3, 1, 10);
    overlapSpinner.addListener(SWT.Selection, e -> updatePreview());

    // Main content split
    var sash = new SashForm(this, SWT.VERTICAL);
    sash.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

    // Top: HTML meta
    metaBrowser = new Browser(sash, SWT.NONE);
    metaBrowser.setText("<html><body>Select a sample to see details.</body></html>");

    // Bottom: image previews side by side
    var imagesSash = new SashForm(sash, SWT.HORIZONTAL);

    var originalScroll = new ScrolledComposite(imagesSash,
        SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
    originalScroll.setExpandHorizontal(true);
    originalScroll.setExpandVertical(true);
    originalCanvas = new ImageCanvas(originalScroll, SWT.NONE);
    originalCanvas.addListener(SWT.MouseUp, e -> playAudio());
    originalScroll.setContent(originalCanvas);

    var subSampledScroll = new ScrolledComposite(imagesSash,
        SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
    subSampledScroll.setExpandHorizontal(true);
    subSampledScroll.setExpandVertical(true);
    subSampledCanvas = new ImageCanvas(subSampledScroll, SWT.NONE);
    subSampledScroll.setContent(subSampledCanvas);

    imagesSash.setWeights(50, 50);
    sash.setWeights(30, 70);
  }

  public int getResolution() {
    return resolutionSpinner.getSelection();
  }

  public float getOverlap() {
    return overlapSpinner.getSelection() / 1000f;
  }

  public void setSample(Sample sample) {
    this.sample = sample;
    updatePreview();
  }

  public void clear() {
    this.sample = null;
    metaBrowser.setText("<html><body>Select a sample to see details.</body></html>");
    disposeImages();
    originalCanvas.setImage(null);
    subSampledCanvas.setImage(null);
  }

  private void updatePreview() {
    if (sample == null) {
      clear();
      return;
    }

    var currentSample = sample;
    var zoom = zoomSpinner.getSelection();
    var resolution = resolutionSpinner.getSelection();
    var overlap = overlapSpinner.getSelection() / 1000f;

    Thread.ofVirtual().name("sample-preview").start(() -> {
      try {
        var subSampled = samplesService.subSample(currentSample, resolution, overlap);
        var metaHtml = HTML_WRAPPER.formatted(subSampled.getMetaDescription());

        var origAwtImage = subSampled.createPreviewFromOriginal();
        var inputAwtImage = subSampled.createPreviewFromInput();

        var origSwtData = toSwtImageData(origAwtImage, zoom);
        var inputSwtData = toSwtImageData(inputAwtImage, zoom);

        display.asyncExec(() -> {
          if (isDisposed()) {
            return;
          }
          metaBrowser.setText(metaHtml);
          disposeImages();

          if (origSwtData != null) {
            originalImage = new Image(display, origSwtData);
            originalCanvas.setImage(originalImage);
          }
          if (inputSwtData != null) {
            subSampledImage = new Image(display, inputSwtData);
            subSampledCanvas.setImage(subSampledImage);
          }
        });
      } catch (Exception e) {
        log.log(Level.WARNING, "Failed to update sample preview", e);
      }
    });
  }

  private org.eclipse.swt.graphics.ImageData toSwtImageData(java.awt.Image awtImage, int zoom) {
    if (awtImage == null) {
      return null;
    }
    var buffered = toBufferedImage(awtImage);
    var data = SwtImages.toSwtImageData(buffered);
    if (zoom > 1) {
      data = data.scaledTo(data.width * zoom, data.height * zoom);
    }
    buffered.flush();
    awtImage.flush();
    return data;
  }

  private BufferedImage toBufferedImage(java.awt.Image awtImage) {
    if (awtImage instanceof BufferedImage bi) {
      return bi;
    }
    var w = awtImage.getWidth(null);
    var h = awtImage.getHeight(null);
    var bi = new BufferedImage(Math.max(w, 1), Math.max(h, 1), BufferedImage.TYPE_INT_RGB);
    var g = bi.createGraphics();
    g.drawImage(awtImage, 0, 0, null);
    g.dispose();
    return bi;
  }

  private void playAudio() {
    stopClip();
    if (sample == null || sample.getFile() == null) {
      return;
    }
    try {
      var clip = AudioSystem.getClip();
      try (var stream = AudioSystem.getAudioInputStream(sample.getFile())) {
        clip.addLineListener(event -> {
          if (event.getType() == Type.STOP) {
            clip.close();
            clipRef.compareAndSet(clip, null);
          }
        });
        clip.open(stream);
        clip.start();
        clipRef.set(clip);
      }
    } catch (Exception e) {
      log.log(Level.WARNING, "Audio playback failed", e);
    }
  }

  private void stopClip() {
    var c = clipRef.getAndSet(null);
    if (c != null) {
      c.stop();
      c.close();
    }
  }

  private void disposeImages() {
    if (originalImage != null && !originalImage.isDisposed()) {
      originalImage.dispose();
      originalImage = null;
    }
    if (subSampledImage != null && !subSampledImage.isDisposed()) {
      subSampledImage.dispose();
      subSampledImage = null;
    }
  }

  @Override
  public void dispose() {
    stopClip();
    disposeImages();
    super.dispose();
  }
}
