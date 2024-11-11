package edu.yaprnn.gui.services;

import edu.yaprnn.events.OnSamplePreviewModifiedRouter;
import edu.yaprnn.support.swing.DialogsService;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.function.DoubleConsumer;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;

@Singleton
public class ZoomControlsService {

  private static final String[] ZOOM_LEVELS = {"0.5", "1.0", "2.0", "4.0", "8.0", "16.0"};

  @Inject
  ControlsService controlsService;
  @Inject
  DialogsService dialogsService;
  @Inject
  OnSamplePreviewModifiedRouter onSamplePreviewModifiedRouter;

  public JComboBox<String> zoomComboBox(DoubleConsumer consumer) {
    return zoomComboBox(zoomComboBoxModel(), consumer);
  }

  public JComboBox<String> zoomComboBox(ComboBoxModel<String> model, DoubleConsumer consumer) {
    var comboBox = new JComboBox<>(model);
    comboBox.addActionListener(_ -> {
      try {
        var text = String.valueOf(comboBox.getSelectedItem());
        consumer.accept(Double.parseDouble(text));
      } catch (Throwable throwable) {
        dialogsService.showError(comboBox, "Zoom", throwable);
      }
    });
    comboBox.addKeyListener(controlsService.onlyNumbersKeyListener(true, false));
    return comboBox;
  }

  public ComboBoxModel<String> zoomComboBoxModel() {
    var model = new DefaultComboBoxModel<>(ZOOM_LEVELS);
    model.setSelectedItem(onSamplePreviewModifiedRouter.getZoom());
    return model;
  }
}
