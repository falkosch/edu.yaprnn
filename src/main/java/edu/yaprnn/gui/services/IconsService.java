package edu.yaprnn.gui.services;

import edu.yaprnn.support.swing.Images;
import jakarta.inject.Singleton;
import java.awt.image.AffineTransformOp;
import java.util.Objects;
import javax.swing.Icon;
import javax.swing.ImageIcon;

@Singleton
public class IconsService {

  private static final int SMALL_ICON_SIZE = 28;

  public static final Icon ICON_ACTIVATION_FUNCTION = smallIconFromResource(
      "/edu/yaprnn/gui/views/old/activation-function.png");
  public static final Icon ICON_BIAS = smallIconFromResource(
      "/edu/yaprnn/gui/views/old/layer-size.png");
  public static final Icon ICON_DEV_TEST_SAMPLES_NODE = smallIconFromResource(
      "/edu/yaprnn/gui/views/old/dev-test-set-node.png");
  public static final Icon ICON_LAYER_SIZE = smallIconFromResource(
      "/edu/yaprnn/gui/views/old/layer-size.png");
  public static final Icon ICON_LAYER = smallIconFromResource(
      "/edu/yaprnn/gui/views/symbols/afif-fudin-layer.png");
  public static final Icon ICON_LAYER_TEMPLATE = smallIconFromResource(
      "/edu/yaprnn/gui/views/symbols/mayor-icons-layer-template.png");
  public static final Icon ICON_LOSS_FUNCTION = smallIconFromResource(
      "/edu/yaprnn/gui/views/old/configuration.png");
  public static final Icon ICON_MULTI_LAYER_NETWORK = smallIconFromResource(
      "/edu/yaprnn/gui/views/symbols/vectorslab-network.png");
  public static final Icon ICON_MULTI_LAYER_NETWORK_TEMPLATE = smallIconFromResource(
      "/edu/yaprnn/gui/views/symbols/mayor-icons-network-template.png");
  public static final Icon ICON_NODE = smallIconFromResource(
      "/edu/yaprnn/gui/views/symbols/srip-node.png");
  public static final Icon ICON_SAMPLE = smallIconFromResource(
      "/edu/yaprnn/gui/views/old/sample.png");
  public static final Icon ICON_TRAINING_DATA_NODE = smallIconFromResource(
      "/edu/yaprnn/gui/views/old/training-data-node.png");
  public static final Icon ICON_TRAINING_SAMPLES_NODE = smallIconFromResource(
      "/edu/yaprnn/gui/views/old/training-set-node.png");

  private final Icon visitIcon = smallIconFromResource(
      "/edu/yaprnn/gui/views/action/srip-visit.png");
  private final Icon addIcon = smallIconFromResource(
      "/edu/yaprnn/gui/views/action/srip-add.png");
  private final Icon editIcon = smallIconFromResource(
      "/edu/yaprnn/gui/views/action/srip-edit.png");
  private final Icon removeIcon = smallIconFromResource(
      "/edu/yaprnn/gui/views/action/srip-remove.png");
  private final Icon subSampleIcon = smallIconFromResource(
      "/edu/yaprnn/gui/views/action/srip-sub-sample.png");
  private final Icon randomizeTrainingDataIcon = smallIconFromResource(
      "/edu/yaprnn/gui/views/action/buandesign-randomize.png");
  private final Icon classifyIcon = smallIconFromResource(
      "/edu/yaprnn/gui/views/action/srip-classify.png");
  private final Icon trainIcon = smallIconFromResource(
      "/edu/yaprnn/gui/views/action/srip-train.png");
  private final Icon resetIcon = smallIconFromResource(
      "/edu/yaprnn/gui/views/action/srip-reset.png");
  private final Icon loadIcon = smallIconFromResource(
      "/edu/yaprnn/gui/views/action/srip-open.png");
  private final Icon saveIcon = smallIconFromResource(
      "/edu/yaprnn/gui/views/action/srip-save.png");
  private final Icon importAudioIcon = smallIconFromResource(
      "/edu/yaprnn/gui/views/action/srip-import-audio.png");
  private final Icon importImagesIcon = smallIconFromResource(
      "/edu/yaprnn/gui/views/action/srip-import-images.png");
  private final Icon sampleSetNodeIcon = smallIconFromResource(
      "/edu/yaprnn/gui/views/old/sample-set-node.png");

  public Icon visitIcon() {
    return visitIcon;
  }

  private static Icon smallIconFromResource(String location) {
    return fromResource(location, SMALL_ICON_SIZE, SMALL_ICON_SIZE);
  }

  private static Icon fromResource(String location, int width, int height) {
    var resource = IconsService.class.getResource(location);
    Objects.requireNonNull(resource, () -> "%s not found".formatted(location));

    var image = new ImageIcon(resource).getImage();
    try {
      return new ImageIcon(Images.resize(image, width, height, AffineTransformOp.TYPE_BICUBIC));
    } finally {
      image.flush();
    }
  }

  public Icon addIcon() {
    return addIcon;
  }

  public Icon editIcon() {
    return editIcon;
  }

  public Icon removeIcon() {
    return removeIcon;
  }

  public Icon subSampleIcon() {
    return subSampleIcon;
  }

  public Icon randomizeTrainingDataIcon() {
    return randomizeTrainingDataIcon;
  }

  public Icon classifyIcon() {
    return classifyIcon;
  }

  public Icon trainIcon() {
    return trainIcon;
  }

  public Icon resetIcon() {
    return resetIcon;
  }

  public Icon loadIcon() {
    return loadIcon;
  }

  public Icon saveIcon() {
    return saveIcon;
  }

  public Icon importAudioIcon() {
    return importAudioIcon;
  }

  public Icon importImagesIcon() {
    return importImagesIcon;
  }

  public Icon sampleSetNodeIcon() {
    return sampleSetNodeIcon;
  }
}
