package edu.yaprnn.gui.services;

import edu.yaprnn.events.OnSamplePreviewModifiedRouter;
import edu.yaprnn.support.swing.DialogsService;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.function.DoubleConsumer;
import java.util.function.IntConsumer;
import javax.swing.JEditorPane;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;

@Singleton
public class SampleControlsService {

  @Inject
  ControlsService controlsService;
  @Inject
  DialogsService dialogsService;
  @Inject
  OnSamplePreviewModifiedRouter onSamplePreviewModifiedRouter;

  public JEditorPane sampleMetaEditorPane() {
    var editorPane = new JEditorPane();
    editorPane.setContentType("text/html");
    editorPane.setEditable(false);
    editorPane.setOpaque(false);
    return editorPane;
  }

  public JSpinner resolutionSpinner(IntConsumer consumer) {
    return resolutionSpinner(resolutionSpinnerModel(), consumer);
  }

  public JSpinner resolutionSpinner(SpinnerModel model, IntConsumer consumer) {
    var spinner = new JSpinner(model);
    spinner.addChangeListener(_ -> {
      try {
        var value = spinner.getValue();
        if (value instanceof Integer resolution) {
          consumer.accept(resolution);
        } else {
          consumer.accept(Integer.parseInt(String.valueOf(value)));
        }
      } catch (Throwable throwable) {
        dialogsService.showError(spinner, "Resolution", throwable);
      }
    });
    spinner.addKeyListener(controlsService.onlyNumbersKeyListener(true, true));
    return spinner;
  }

  public SpinnerModel resolutionSpinnerModel() {
    return new SpinnerNumberModel(onSamplePreviewModifiedRouter.getResolution(), 1, 784, 1);
  }

  public JSpinner overlapSpinner(DoubleConsumer consumer) {
    return overlapSpinner(overlapSpinnerModel(), consumer);
  }

  public JSpinner overlapSpinner(SpinnerModel model, DoubleConsumer consumer) {
    var spinner = new JSpinner(model);
    spinner.addChangeListener(_ -> {
      try {
        var value = spinner.getValue();
        if (value instanceof Double overlap) {
          consumer.accept(overlap);
        } else {
          consumer.accept(Double.parseDouble(String.valueOf(value)));
        }
      } catch (Throwable throwable) {
        dialogsService.showError(spinner, "Overlap", throwable);
      }
    });
    spinner.addKeyListener(controlsService.onlyNumbersKeyListener(true, false));
    return spinner;
  }

  public SpinnerModel overlapSpinnerModel() {
    return new SpinnerNumberModel(onSamplePreviewModifiedRouter.getOverlap(), 0.0d, 0.95d, 0.05d);
  }
}
