package edu.yaprnn.gui.views;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;
import edu.yaprnn.events.OnSamplePreviewModifiedRouter;
import edu.yaprnn.gui.services.ControlsService;
import edu.yaprnn.gui.services.SampleControlsService;
import edu.yaprnn.gui.services.SamplesService;
import edu.yaprnn.gui.services.ZoomControlsService;
import edu.yaprnn.samples.model.Sample;
import edu.yaprnn.support.swing.DialogsService;
import edu.yaprnn.support.swing.ImagePanel;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.sound.sampled.AudioSystem;
import java.util.concurrent.atomic.AtomicReference;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineEvent.Type;
import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.SwingWorker;
import lombok.Getter;
import lombok.extern.java.Log;

@Log
public class SampleDetailsView {

  public static final String TITLE = "Sample Details";

  private static final String META_TEMPLATE = """
      <style>
        * {
          font-family: "Helvetica Neue", sans-serif;
        }
        table {
          border-collapse: collapse;
          border-spacing: 4px;
        }
        table, td, tr, th {
          padding: 0;
          margin: 0;
        }
      </style>
      %s
      """;

  @Inject
  ControlsService controlsService;
  @Inject
  DialogsService dialogsService;
  @Inject
  SamplesService samplesService;
  @Inject
  SampleControlsService sampleControlsService;
  @Inject
  ZoomControlsService zoomControlsService;
  @Inject
  OnSamplePreviewModifiedRouter onSamplePreviewModifiedRouter;

  @Getter
  private JPanel content;

  private ImagePanel previewImagePanel;
  private ImagePanel subSampledPreviewImagePanel;
  private JEditorPane sampleMetaEditorPane;
  private JSplitPane previewSplitPane;

  private PlayAudioSampleMouseAdapter playAudioAdapter;
  private Sample sample;
  private float zoom;
  private int resolution;
  private float overlap;

  @PostConstruct
  void initialize() {
    var zoomSampleLabel = new JLabel("Zoom");
    var resolutionLabel = new JLabel("Resolution");
    var overlapLabel = new JLabel("Overlap");
    var subSampledTitledBorder = BorderFactory.createTitledBorder("Preview");
    var sampleMetaTitledBorder = BorderFactory.createTitledBorder("Sample Meta");

    var zoomSampleComboBox = zoomControlsService.zoomComboBox(
        onSamplePreviewModifiedRouter::setZoom);
    var resolutionSpinner = sampleControlsService.resolutionSpinner(
        onSamplePreviewModifiedRouter::setResolution);
    var overlapSpinner = sampleControlsService.overlapSpinner(
        onSamplePreviewModifiedRouter::setOverlap);

    previewImagePanel = new ImagePanel();
    playAudioAdapter = new PlayAudioSampleMouseAdapter();
    previewImagePanel.addMouseListener(playAudioAdapter);
    subSampledPreviewImagePanel = new ImagePanel();
    sampleMetaEditorPane = sampleControlsService.sampleMetaEditorPane();

    var previewImageScrollPane = new JScrollPane(previewImagePanel);
    var subSampledPreviewScrollPane = new JScrollPane(subSampledPreviewImagePanel);
    previewSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, previewImageScrollPane,
        subSampledPreviewScrollPane);

    var subSampledPanel = new JPanel();
    var subSampledGroupLayout = new GroupLayout(subSampledPanel);
    subSampledGroupLayout.setHorizontalGroup(subSampledGroupLayout.createParallelGroup()
        .addGroup(subSampledGroupLayout.createSequentialGroup()
            .addGroup(subSampledGroupLayout.createParallelGroup()
                .addComponent(zoomSampleLabel)
                .addComponent(zoomSampleComboBox, PREFERRED_SIZE, DEFAULT_SIZE, 100))
            .addGroup(subSampledGroupLayout.createParallelGroup()
                .addComponent(resolutionLabel)
                .addComponent(resolutionSpinner, PREFERRED_SIZE, DEFAULT_SIZE, 100))
            .addGroup(subSampledGroupLayout.createParallelGroup()
                .addComponent(overlapLabel)
                .addComponent(overlapSpinner, PREFERRED_SIZE, DEFAULT_SIZE, 100)))
        .addComponent(previewSplitPane));
    subSampledGroupLayout.setVerticalGroup(subSampledGroupLayout.createSequentialGroup()
        .addGroup(subSampledGroupLayout.createParallelGroup()
            .addGroup(subSampledGroupLayout.createSequentialGroup()
                .addComponent(zoomSampleLabel)
                .addComponent(zoomSampleComboBox, PREFERRED_SIZE, DEFAULT_SIZE, 28))
            .addGroup(subSampledGroupLayout.createSequentialGroup()
                .addComponent(resolutionLabel)
                .addComponent(resolutionSpinner, PREFERRED_SIZE, DEFAULT_SIZE, 28))
            .addGroup(subSampledGroupLayout.createSequentialGroup()
                .addComponent(overlapLabel)
                .addComponent(overlapSpinner, PREFERRED_SIZE, DEFAULT_SIZE, 28)))
        .addComponent(previewSplitPane));
    subSampledGroupLayout.setAutoCreateContainerGaps(true);
    subSampledGroupLayout.setAutoCreateGaps(true);
    subSampledPanel.setLayout(subSampledGroupLayout);
    subSampledPanel.setBorder(subSampledTitledBorder);
    subSampledPanel.setOpaque(false);

    var sampleMetaPanel = new JPanel();
    var sampleMetaGroupLayout = new GroupLayout(sampleMetaPanel);
    sampleMetaGroupLayout.setHorizontalGroup(sampleMetaGroupLayout.createParallelGroup()
        .addComponent(sampleMetaEditorPane, 100, PREFERRED_SIZE, PREFERRED_SIZE));
    sampleMetaGroupLayout.setVerticalGroup(sampleMetaGroupLayout.createParallelGroup()
        .addComponent(sampleMetaEditorPane, PREFERRED_SIZE, PREFERRED_SIZE, PREFERRED_SIZE));
    sampleMetaGroupLayout.setAutoCreateContainerGaps(true);
    sampleMetaGroupLayout.setAutoCreateGaps(true);
    sampleMetaPanel.setLayout(sampleMetaGroupLayout);
    sampleMetaPanel.setBorder(sampleMetaTitledBorder);
    sampleMetaPanel.setOpaque(false);

    content = new JPanel();
    var sampleDetailsGroupLayout = new GroupLayout(content);
    sampleDetailsGroupLayout.setHorizontalGroup(sampleDetailsGroupLayout.createSequentialGroup()
        .addGroup(sampleDetailsGroupLayout.createParallelGroup()
            .addComponent(sampleMetaPanel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
            .addComponent(subSampledPanel)));
    sampleDetailsGroupLayout.setVerticalGroup(sampleDetailsGroupLayout.createSequentialGroup()
        .addComponent(sampleMetaPanel, DEFAULT_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
        .addComponent(subSampledPanel));
    sampleDetailsGroupLayout.setAutoCreateContainerGaps(true);
    sampleDetailsGroupLayout.setAutoCreateGaps(true);
    content.setLayout(sampleDetailsGroupLayout);
    content.setOpaque(false);
  }

  public void setSamplePreview(float zoom, int resolution, float overlap) {
    this.zoom = zoom;
    this.resolution = resolution;
    this.overlap = overlap;
    setSamplePreview();
  }

  private void setSamplePreview() {
    if (sample == null) {
      clear();
      return;
    }

    var currentSample = sample;
    var currentZoom = zoom;
    var currentResolution = resolution;
    var currentOverlap = overlap;

    new SwingWorker<Void, Void>() {
      private String metaDescription;
      private Image originalPreview;
      private Image inputPreview;

      @Override
      protected Void doInBackground() {
        var subSampled = samplesService.subSample(currentSample, currentResolution, currentOverlap);
        metaDescription = META_TEMPLATE.formatted(subSampled.getMetaDescription());
        originalPreview = samplesService.from(subSampled::createPreviewFromOriginal, currentZoom);
        inputPreview = samplesService.from(subSampled::createPreviewFromInput, currentZoom);
        return null;
      }

      @Override
      protected void done() {
        try {
          get();
          sampleMetaEditorPane.setText(metaDescription);
          previewImagePanel.setImage(originalPreview);
          subSampledPreviewImagePanel.setImage(inputPreview);
        } catch (Exception exception) {
          log.log(java.util.logging.Level.WARNING, "Sample preview failed", exception);
        }
      }
    }.execute();
  }

  public void clear() {
    previewImagePanel.setImage(null);
    subSampledPreviewImagePanel.setImage(null);
    sampleMetaEditorPane.setText(null);
  }

  public void setSamplePreview(Sample sample) {
    this.sample = sample;
    setSamplePreview();
  }

  public void dispose() {
    playAudioAdapter.stopClip();
  }

  public void doLayout() {
    previewSplitPane.setDividerLocation(0.5);
  }

  private class PlayAudioSampleMouseAdapter extends MouseAdapter {

    private final AtomicReference<Clip> clipRef = new AtomicReference<>();

    void stopClip() {
      var c = clipRef.getAndSet(null);
      if (c != null) {
        c.stop();
        c.close();
      }
    }

    @Override
    public void mouseClicked(MouseEvent event) {
      stopClip();

      var currentSample = SampleDetailsView.this.sample;
      try {
        clipRef.set(from(currentSample));
      } catch (Exception exception) {
        dialogsService.showError(SampleDetailsView.this.content, TITLE, exception);
      }
    }

    private Clip from(Sample sample) throws Exception {
      if (sample == null || sample.getFile() == null) {
        return null;
      }

      var clip = AudioSystem.getClip();
      var stream = AudioSystem.getAudioInputStream(sample.getFile());

      clip.addLineListener(event -> {
        if (event.getType() == Type.STOP) {
          clip.close();
          try {
            stream.close();
          } catch (java.io.IOException ignored) {
          }
          clipRef.compareAndSet(clip, null);
        }
      });

      clip.open(stream);
      clip.start();
      return clip;
    }
  }
}
