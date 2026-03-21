package edu.yaprnn.gui.services;

import jakarta.inject.Singleton;

@Singleton
public class IconsService {

  // Resource paths for tree node icons (toolkit-agnostic)
  public static final String ICON_ACTIVATION_FUNCTION = "/edu/yaprnn/gui/views/old/activation-function.png";
  public static final String ICON_BIAS = "/edu/yaprnn/gui/views/old/layer-size.png";
  public static final String ICON_DEV_TEST_SAMPLES_NODE = "/edu/yaprnn/gui/views/old/dev-test-set-node.png";
  public static final String ICON_LAYER_SIZE = "/edu/yaprnn/gui/views/old/layer-size.png";
  public static final String ICON_LAYER = "/edu/yaprnn/gui/views/symbols/afif-fudin-layer.png";
  public static final String ICON_LAYER_TEMPLATE = "/edu/yaprnn/gui/views/symbols/mayor-icons-layer-template.png";
  public static final String ICON_LOSS_FUNCTION = "/edu/yaprnn/gui/views/old/configuration.png";
  public static final String ICON_MULTI_LAYER_NETWORK = "/edu/yaprnn/gui/views/symbols/vectorslab-network.png";
  public static final String ICON_MULTI_LAYER_NETWORK_TEMPLATE = "/edu/yaprnn/gui/views/symbols/mayor-icons-network-template.png";
  public static final String ICON_NODE = "/edu/yaprnn/gui/views/symbols/srip-node.png";
  public static final String ICON_SAMPLE = "/edu/yaprnn/gui/views/old/sample.png";
  public static final String ICON_TRAINING_DATA_NODE = "/edu/yaprnn/gui/views/old/training-data-node.png";
  public static final String ICON_TRAINING_SAMPLES_NODE = "/edu/yaprnn/gui/views/old/training-set-node.png";
  public static final String ICON_SAMPLE_SET_NODE = "/edu/yaprnn/gui/views/old/sample-set-node.png";

  public String sampleSetNodeIconResourcePath() {
    return ICON_SAMPLE_SET_NODE;
  }
}
