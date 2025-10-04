package edu.yaprnn.gui.views;

import edu.yaprnn.events.OnSamplePreviewModifiedRouter;
import edu.yaprnn.gui.model.NetworksTreeModel;
import edu.yaprnn.gui.services.ControlsService;
import edu.yaprnn.gui.services.IconsService;
import edu.yaprnn.gui.services.SamplesService;
import edu.yaprnn.model.Repository;
import edu.yaprnn.samples.model.Sample;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import java.awt.BorderLayout;
import java.awt.event.ItemEvent;
import java.util.List;
import java.util.Objects;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JToolBar;
import lombok.Getter;

@Getter
public class SamplePreprocessingFrame extends JFrame {

  public static final String TITLE = "Sample Preprocessing";

  @Inject
  ControlsService controlsService;
  @Inject
  IconsService iconsService;
  @Inject
  OnSamplePreviewModifiedRouter onSamplePreviewModifiedRouter;
  @Inject
  NetworksTreeModel networksTreeModel;
  @Inject
  Repository repository;
  @Inject
  SamplesService samplesService;

  @Inject
  Instance<SampleDetailsView> sampleDetailsViewInstance;

  private JButton processButton;
  private DefaultComboBoxModel<Sample> samplesComboBoxModel;
  private SampleDetailsView sampleDetailsView;

  public void removeSamplesFromSelectionControls(List<Sample> removed) {
    controlsService.silenceListModelListenersDuringRunnable(samplesComboBoxModel,
        () -> removed.forEach(samplesComboBoxModel::removeElement));
  }

  @PostConstruct
  void initializeComponents() {
    setTitle(TITLE);

    processButton = controlsService.actionButton("Process", iconsService.subSampleIcon(),
        this::process);

    samplesComboBoxModel = new DefaultComboBoxModel<>(
        repository.getSamples().toArray(Sample[]::new));
    var samplesComboBox = new JComboBox<>(samplesComboBoxModel);
    samplesComboBox.addItemListener(this::syncViewOnSampleSelectionChanged);

    var toolBar = new JToolBar();
    toolBar.add(processButton);
    toolBar.add(samplesComboBox);

    sampleDetailsView = sampleDetailsViewInstance.get();

    var sampleDetailsViewContent = sampleDetailsView.getContent();

    getContentPane().add(toolBar, BorderLayout.NORTH);
    getContentPane().add(sampleDetailsViewContent);
    setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    pack();
    sampleDetailsView.doLayout();
  }

  private void process() {
    var resolution = onSamplePreviewModifiedRouter.getResolution();
    var overlap = onSamplePreviewModifiedRouter.getOverlap();

    var originalSamples = List.copyOf(repository.getSamples());
    var processedSamples = originalSamples.stream()
        .map(sample -> samplesService.subSample(sample, resolution, overlap)).toList();

    networksTreeModel.removeSamples(originalSamples);
    networksTreeModel.addSamples(processedSamples);
  }

  private void syncViewOnSampleSelectionChanged(ItemEvent event) {
    var selectedSample = (Sample) (event.getStateChange() == ItemEvent.SELECTED ? event.getItem()
        : null);
    sampleDetailsView.setSamplePreview(selectedSample);
    pack();
  }

  public void prepareSampleSelectionControls(List<Sample> samples) {
    samplesComboBoxModel.addAll(samples);
  }

  public void setSelectedSample(Sample sample) {
    if (Objects.nonNull(sample)) {
      samplesComboBoxModel.setSelectedItem(sample);
    }
  }

  public void setSamplePreview(float zoom, int resolution, float overlap) {
    sampleDetailsView.setSamplePreview(zoom, resolution, overlap);
  }
}
