package edu.yaprnn.gui.services;

import edu.yaprnn.events.OnMultiLayerNetworkWeightsPreviewModifiedRouter;
import edu.yaprnn.events.OnSamplePreviewModifiedRouter;
import edu.yaprnn.gui.model.NetworksTreeModel;
import edu.yaprnn.gui.model.nodes.ModelNode;
import edu.yaprnn.networks.functions.ActivationFunction;
import edu.yaprnn.networks.functions.TangentHyperbolicActivationFunction;
import edu.yaprnn.training.ClassifierDataSelector;
import edu.yaprnn.training.DataSelector;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.awt.Color;
import java.awt.Component;
import java.awt.SystemColor;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.Function;
import java.util.function.IntConsumer;
import javax.swing.AbstractButton;
import javax.swing.AbstractListModel;
import javax.swing.BorderFactory;
import javax.swing.BoundedRangeModel;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultBoundedRangeModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JMenuItem;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.tree.TreeCellEditor;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

@Singleton
public class ControlsService {

  @Inject
  DialogsService dialogsService;
  @Inject
  FilesService filesService;
  @Inject
  IconsService iconsService;
  @Inject
  NetworksTreeModel networksTreeModel;
  @Inject
  OnSamplePreviewModifiedRouter onSamplePreviewModifiedRouter;
  @Inject
  OnMultiLayerNetworkWeightsPreviewModifiedRouter onMultiLayerNetworkWeightsPreviewModifiedRouter;

  @Any
  @Inject
  Instance<ActivationFunction> activationFunctionInstance;
  @Any
  @Inject
  Instance<DataSelector> dataSelectorInstance;

  public <T> String toTitlePart(T part, Function<T, String> toText) {
    return Optional.ofNullable(part).map(toText).orElse("N/A");
  }

  public Color validationColor(boolean labelsExists) {
    return labelsExists ? SystemColor.text : new Color(255, 160, 160);
  }

  public JMenuItem loadMenuItem(Component parent, String title,
      FileNameExtensionFilter fileExtension, Consumer<String> pathConsumer) {
    return selectFileMenuItem(parent, title, iconsService.loadIcon(), fileExtension, false,
        pathConsumer);
  }

  public JMenuItem selectFileMenuItem(Component parent, String title, Icon icon,
      FileNameExtensionFilter fileExtension, boolean isSaveAction, Consumer<String> pathConsumer) {
    return actionMenuItem(title, icon,
        () -> filesService.selectFile(parent, fileExtension, isSaveAction, path -> {
          try {
            pathConsumer.accept(path);
            if (isSaveAction) {
              dialogsService.showFinished(parent, title);
            }
          } catch (Throwable throwable) {
            dialogsService.showError(parent, title, throwable);
          }
        }));
  }

  public JMenuItem actionMenuItem(String title, Icon icon, Runnable action) {
    return actionComponent(new JMenuItem(title, icon), action);
  }

  public <T extends AbstractButton> T actionComponent(T component, Runnable action) {
    component.addActionListener(nus -> action.run());
    component.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
    component.setOpaque(false);
    return component;
  }

  public JMenuItem saveMenuItem(Component parent, String title,
      FileNameExtensionFilter fileExtension, Consumer<String> pathConsumer) {
    return selectFileMenuItem(parent, title, iconsService.saveIcon(), fileExtension, true,
        pathConsumer);
  }

  public JButton actionButton(String title, Icon icon, Runnable action) {
    return actionComponent(new JButton(title, icon), action);
  }

  public JComboBox<String> zoomComboBox(DoubleConsumer consumer) {
    return zoomComboBox(zoomComboBoxModel(), consumer);
  }

  public JComboBox<String> zoomComboBox(ComboBoxModel<String> model, DoubleConsumer consumer) {
    var comboBox = new JComboBox<>(model);
    comboBox.addActionListener(event -> {
      try {
        var text = String.valueOf(comboBox.getSelectedItem());
        consumer.accept(Double.parseDouble(text));
      } catch (Throwable throwable) {
        dialogsService.showError(comboBox, "Zoom", throwable);
      }
    });
    comboBox.addKeyListener(onlyNumbersKeyListener(true, false));
    return comboBox;
  }

  public ComboBoxModel<String> zoomComboBoxModel() {
    var model = new DefaultComboBoxModel<>(zoomLevels());
    model.setSelectedItem(onSamplePreviewModifiedRouter.getZoom());
    return model;
  }

  public KeyListener onlyNumbersKeyListener(boolean onlyPositive, boolean onlyIntegers) {
    return new KeyAdapter() {

      @Override
      public void keyTyped(KeyEvent event) {
        var key = event.getKeyChar();
        var floating = key == '.' || key == 'E' || key == 'e';
        var sign = key == '-';
        if ((floating && onlyIntegers) || (sign && onlyPositive) || !(Character.isDigit(key) || sign
            || floating)) {
          event.consume(); // swallow it
        }
      }
    };
  }

  public String[] zoomLevels() {
    return new String[]{"0.5", "1.0", "2.0", "4.0", "8.0", "16.0"};
  }

  public JSlider gammaSlider(DoubleConsumer consumer) {
    return gammaSlider(gammaBoundedRangeModel(), consumer);
  }

  public JSlider gammaSlider(BoundedRangeModel model, DoubleConsumer consumer) {
    var slider = new JSlider(model);
    slider.addChangeListener(_ -> consumer.accept(toGammaValue(model, slider.getValue())));
    slider.setMajorTickSpacing(model.getExtent());
    slider.setOpaque(false);
    slider.setPaintTicks(true);
    return slider;
  }

  public BoundedRangeModel gammaBoundedRangeModel() {
    var model = new DefaultBoundedRangeModel(-1, 0, -1000, 0);
    model.setValue(
        fromGammaValue(model, onMultiLayerNetworkWeightsPreviewModifiedRouter.getGamma()));
    return model;
  }

  private float toGammaValue(BoundedRangeModel model, int sliderValue) {
    return sliderValue / gammaRange(model);
  }

  private int fromGammaValue(BoundedRangeModel model, float gammaValue) {
    return (int) (gammaValue * gammaRange(model));
  }

  private float gammaRange(BoundedRangeModel model) {
    return 0.5f * (model.getMaximum() - model.getMinimum());
  }

  public JComboBox<ActivationFunction> activationFunctionsComboBox(
      Consumer<ActivationFunction> consumer) {
    return activationFunctionsComboBox(activationFunctionsComboBoxModel(), consumer);
  }

  public JComboBox<ActivationFunction> activationFunctionsComboBox(
      ComboBoxModel<ActivationFunction> model, Consumer<ActivationFunction> consumer) {
    var comboBox = new JComboBox<>(model);
    comboBox.addActionListener(event -> {
      try {
        if (comboBox.getSelectedItem() instanceof ActivationFunction value) {
          consumer.accept(value);
        }
      } catch (Throwable throwable) {
        dialogsService.showError(comboBox, "Activation Function", throwable);
      }
    });
    return comboBox;
  }

  public ComboBoxModel<ActivationFunction> activationFunctionsComboBoxModel() {
    var model = new DefaultComboBoxModel<>(activationFunctions());
    model.setSelectedItem(
        activationFunctionInstance.select(TangentHyperbolicActivationFunction.class).get());
    return model;
  }

  public ActivationFunction[] activationFunctions() {
    return activationFunctionInstance.stream()
        .sorted(ActivationFunction.COMPARATOR)
        .toArray(ActivationFunction[]::new);
  }

  public JComboBox<DataSelector> dataSelectorsComboBox(Consumer<DataSelector> consumer) {
    return dataSelectorsComboBox(dataSelectorsComboBoxModel(), consumer);
  }

  public JComboBox<DataSelector> dataSelectorsComboBox(ComboBoxModel<DataSelector> model,
      Consumer<DataSelector> consumer) {
    var comboBox = new JComboBox<>(model);
    comboBox.addActionListener(event -> {
      try {
        if (comboBox.getSelectedItem() instanceof DataSelector value) {
          consumer.accept(value);
        }
      } catch (Throwable throwable) {
        dialogsService.showError(comboBox, "Data Selector", throwable);
      }
    });
    return comboBox;
  }

  public ComboBoxModel<DataSelector> dataSelectorsComboBoxModel() {
    var model = new DefaultComboBoxModel<>(dataSelectors());
    model.setSelectedItem(dataSelectorInstance.select(ClassifierDataSelector.class).get());
    return model;
  }

  public DataSelector[] dataSelectors() {
    return dataSelectorInstance.stream().toArray(DataSelector[]::new);
  }

  public JSpinner resolutionSpinner(IntConsumer consumer) {
    return resolutionSpinner(resolutionSpinnerModel(), consumer);
  }

  public JSpinner resolutionSpinner(SpinnerModel model, IntConsumer consumer) {
    var spinner = new JSpinner(model);
    spinner.addChangeListener(event -> {
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
    spinner.addKeyListener(onlyNumbersKeyListener(true, true));
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
    spinner.addChangeListener(event -> {
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
    spinner.addKeyListener(onlyNumbersKeyListener(true, false));
    return spinner;
  }

  public SpinnerModel overlapSpinnerModel() {
    return new SpinnerNumberModel(onSamplePreviewModifiedRouter.getOverlap(), 0.0d, 0.95d, 0.05d);
  }

  public JTree networksTree(TreeCellEditor treeCellEditor, TreeCellRenderer treeCellRenderer,
      BiConsumer<ModelNode, TreePath> consumer) {
    return networksTree(treeCellEditor, treeCellRenderer, consumer,
        () -> consumer.accept(null, null));
  }

  public JTree networksTree(TreeCellEditor treeCellEditor, TreeCellRenderer treeCellRenderer,
      BiConsumer<ModelNode, TreePath> onSelect, Runnable onUnselect) {
    var tree = new JTree(networksTreeModel);
    tree.addTreeSelectionListener(event -> {
      if (event.isAddedPath()) {
        var treePath = event.getPath();
        if (treePath.getLastPathComponent() instanceof ModelNode modelNode) {
          onSelect.accept(modelNode, treePath);
          return;
        }
      }
      onUnselect.run();
    });
    tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
    tree.setCellEditor(treeCellEditor);
    tree.setCellRenderer(treeCellRenderer);
    tree.setEditable(true);
    tree.setLargeModel(true);
    tree.setRootVisible(false);
    tree.setShowsRootHandles(true);
    return tree;
  }

  public JTable valuesTable() {
    var table = new JTable(emptyTableModel());
    table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    table.setRowSelectionAllowed(false);
    return table;
  }

  public TableModel emptyTableModel() {
    return new DefaultTableModel(new Object[][]{}, new Object[]{});
  }

  public JEditorPane sampleMetaEditorPane() {
    var editorPane = new JEditorPane();
    editorPane.setContentType("text/html");
    editorPane.setEditable(false);
    editorPane.setOpaque(false);
    return editorPane;
  }

  public SpinnerNumberModel trainingSizeSpinnerNumberModel() {
    return new SpinnerNumberModel(60, 1, 100, 1);
  }

  public SpinnerNumberModel devTestSizeSpinnerNumberModel() {
    return new SpinnerNumberModel(20, 1, 100, 1);
  }

  public SpinnerNumberModel layersCountSpinnerNumberModel() {
    return new SpinnerNumberModel(10, 2, Integer.MAX_VALUE, 1);
  }

  public SpinnerNumberModel layerSizeSpinnerNumberModel() {
    return new SpinnerNumberModel(10, 2, Integer.MAX_VALUE, 1);
  }

  public <T> void silenceListModelListenersDuringRunnable(AbstractListModel<T> model,
      Runnable runnable) {
    var dataListeners = List.of(model.getListDataListeners());
    dataListeners.forEach(model::removeListDataListener);
    runnable.run();
    dataListeners.forEach(model::addListDataListener);
  }
}
