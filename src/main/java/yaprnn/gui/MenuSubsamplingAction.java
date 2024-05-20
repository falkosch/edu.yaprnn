package yaprnn.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import yaprnn.dvv.Data;
import yaprnn.dvv.NoSuchFileException;
import yaprnn.gui.view.SubsamplingView;
import yaprnn.mlp.ActivationFunction;
import yaprnn.mlp.Linear;

/**
 * This eventhandler opens a new SubsamplingView that will ask for the parameters and then calls the
 * subsampling method in the core.
 */
class MenuSubsamplingAction implements ActionListener {

  private final static ImageIcon ICON_PROCESSALL = ImagesMacros.loadIcon(22, 22,
      "/yaprnn/gui/view/iconProcessAll.png");
  private final GUI gui;

  MenuSubsamplingAction(GUI gui) {
    this.gui = gui;
    setEnabled(false);
    gui.getView().getMenuSubsampling().addActionListener(this);
  }

  void setEnabled(boolean enabled) {
    gui.getView().getMenuSubsampling().setEnabled(enabled);
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    // Es wird eine Skalierungsfunktion bentigt, die lineare darf nicht
    // verwendet werden.
    Vector<ActivationFunction> scaleFuns = new Vector<>(gui.getCore().getAllActivationFunctions());
    for (ActivationFunction f : scaleFuns) {
      if (f instanceof Linear) {
        scaleFuns.remove(f);
        break;
      }
    }

    Data data = gui.getSelectedData();
    SubsamplingInfo si = new SubsamplingInfo(gui, new SubsamplingView(), data);

    new OverlapChange(si);
    new ResolutionChange(si);
    new ZoomAction(si);
    new ProcessAction(si);

    // Preview Handler fr Audio-Daten
    new PreviewPlayAudioListener(si.sv.getLabelPreview()).setData(data);

    si.sv.getToolProcess().setIcon(ICON_PROCESSALL);
    si.sv.getOptionScaleFun().setModel(new DefaultComboBoxModel<>(scaleFuns));
    si.sv.getOptionScaleFun().setEditable(false);

    MenuSubsamplingAction.createSubsampledPreview(si);

    si.sv.setVisible(true);
  }

  private static void createSubsampledPreview(SubsamplingInfo si) {
    Data data = si.previewData;
    if (data == null) {
      return;
    }
    si.sv.getLabelPreview().setImage(ImagesMacros.createPreview(data, si.zoom, false, 0, 0));
    si.sv.getLabelPreviewSubsampled()
        .setImage(ImagesMacros.createPreview(data, si.zoom, true, si.resolution, si.overlap));
  }

  /**
   * Used to hold required parameters and view objects.
   */
  private static class SubsamplingInfo {

    final GUI gui;
    final SubsamplingView sv;
    final Data previewData;
    double zoom = 1.0, overlap = 0.4;
    int resolution = 16;

    SubsamplingInfo(GUI gui, SubsamplingView sv, Data previewData) {
      this.gui = gui;
      this.sv = sv;
      this.previewData = previewData;
    }

  }

  private record OverlapChange(SubsamplingInfo si) implements ChangeListener {

    private OverlapChange(SubsamplingInfo si) {
      this.si = si;
      si.sv.getOptionOverlap().addChangeListener(this);
    }

    @Override
    public void stateChanged(ChangeEvent e) {
      si.overlap = (Double) si.sv.getOptionOverlap().getValue();
      MenuSubsamplingAction.createSubsampledPreview(si);
    }

  }

  private record ResolutionChange(SubsamplingInfo si) implements ChangeListener {

    private ResolutionChange(SubsamplingInfo si) {
      this.si = si;
      si.sv.getOptionResolution().addChangeListener(this);
    }

    @Override
    public void stateChanged(ChangeEvent e) {
      si.resolution = (Integer) si.sv.getOptionResolution().getValue();
      MenuSubsamplingAction.createSubsampledPreview(si);
    }

  }

  private record ZoomAction(SubsamplingInfo si) implements ActionListener {

    private ZoomAction(SubsamplingInfo si) {
      this.si = si;
      si.sv.getOptionZoom().addActionListener(this);
      si.sv.getOptionZoom().addKeyListener(new OnlyNumbersKeyAdapter(true, false));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      Object item = si.sv.getOptionZoom().getSelectedItem();
      if (item instanceof String) {
        try {
          si.zoom = Double.parseDouble((String) item);
          MenuSubsamplingAction.createSubsampledPreview(si);
        } catch (Exception ex) {
          JOptionPane.showMessageDialog(si.sv,
              "The value you have entered cannot be parsed to floating point value. Please enter a correct zoom value.",
              "Parsing error", JOptionPane.ERROR_MESSAGE);
        }
      }
    }

  }

  private class ProcessAction implements ActionListener {

    private final SubsamplingInfo si;

    ProcessAction(SubsamplingInfo si) {
      this.si = si;
      si.sv.getToolProcess().addActionListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      si.sv.setVisible(false);
      si.sv.dispose();

      // Vorher versuchen etwas Speicher frei zu machen
      GUI.tryFreeMemory();
      try {
        si.gui.getCore().preprocess(si.resolution, si.overlap,
            (ActivationFunction) si.sv.getOptionScaleFun().getSelectedItem());
        JOptionPane.showMessageDialog(gui.getView(), "Finished.", "Subsampling",
            JOptionPane.INFORMATION_MESSAGE);
      } catch (NoSuchFileException ex) {
        JOptionPane.showMessageDialog(gui.getView(),
            "Subsampling failed!\n" + "Resolution too large for image_" + ex.getFilename(),
            "An error occured", JOptionPane.ERROR_MESSAGE);
      }
    }

  }

}
