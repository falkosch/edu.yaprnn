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
  public static final Icon ICON_DEV_TEST_SAMPLES_NODE = smallIconFromResource(
      "/edu/yaprnn/gui/views/old/dev-test-set-node.png");
  public static final Icon ICON_LAYER_SIZE = smallIconFromResource(
      "/edu/yaprnn/gui/views/old/layer-size.png");
  public static final Icon ICON_LAYER = smallIconFromResource(
      "/edu/yaprnn/gui/views/symbols/afif-fudin-layer.png");
  public static final Icon ICON_LAYER_TEMPLATE = smallIconFromResource(
      "/edu/yaprnn/gui/views/symbols/mayor-icons-layer-template.png");
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

  public Icon visitIcon() {
    return smallIconFromResource("/edu/yaprnn/gui/views/action/srip-visit.png");
  }

  public Icon addIcon() {
    return smallIconFromResource("/edu/yaprnn/gui/views/action/srip-add.png");
  }

  public Icon editIcon() {
    return smallIconFromResource("/edu/yaprnn/gui/views/action/srip-edit.png");
  }

  public Icon removeIcon() {
    return smallIconFromResource("/edu/yaprnn/gui/views/action/srip-remove.png");
  }

  public Icon subSampleIcon() {
    return smallIconFromResource("/edu/yaprnn/gui/views/action/srip-sub-sample.png");
  }

  public Icon randomizeTrainingDataIcon() {
    return smallIconFromResource("/edu/yaprnn/gui/views/action/buandesign-randomize.png");
  }

  public Icon classifyIcon() {
    return smallIconFromResource("/edu/yaprnn/gui/views/action/srip-classify.png");
  }

  public Icon trainIcon() {
    return smallIconFromResource("/edu/yaprnn/gui/views/action/srip-train.png");
  }

  public Icon resetIcon() {
    return smallIconFromResource("/edu/yaprnn/gui/views/action/srip-reset.png");
  }

  public Icon loadIcon() {
    return smallIconFromResource("/edu/yaprnn/gui/views/action/srip-open.png");
  }

  public Icon saveIcon() {
    return smallIconFromResource("/edu/yaprnn/gui/views/action/srip-save.png");
  }

  public Icon importAudioIcon() {
    return smallIconFromResource("/edu/yaprnn/gui/views/action/srip-import-audio.png");
  }

  public Icon importImagesIcon() {
    return smallIconFromResource("/edu/yaprnn/gui/views/action/srip-import-images.png");
  }

  public Icon sampleSetNodeIcon() {
    return smallIconFromResource("/edu/yaprnn/gui/views/old/sample-set-node.png");
  }
}
