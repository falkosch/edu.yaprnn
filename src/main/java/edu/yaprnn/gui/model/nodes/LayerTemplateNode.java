package edu.yaprnn.gui.model.nodes;

import edu.yaprnn.gui.services.IconsService;
import edu.yaprnn.networks.templates.LayerTemplate;
import edu.yaprnn.networks.templates.MultiLayerNetworkTemplate;
import edu.yaprnn.support.Providers;
import java.util.List;
import java.util.function.IntSupplier;
import java.util.function.Supplier;
import lombok.Getter;

/**
 * Represents a {@link LayerTemplate} in a {@link MultiLayerNetworkTemplate}.
 */
@Getter
public class LayerTemplateNode extends DefaultNode {

  private final Supplier<MultiLayerNetworkTemplate> multiLayerNetworkTemplateSupplier;
  private final IntSupplier layerIndexSupplier;
  private final Supplier<LayerTemplate> layerTemplateSupplier;

  public LayerTemplateNode(Supplier<MultiLayerNetworkTemplate> multiLayerNetworkTemplateSupplier,
      IntSupplier layerIndexSupplier) {
    super(Providers.constant(IconsService.ICON_LAYER_TEMPLATE),
        () -> labelFrom(multiLayerNetworkTemplateSupplier.get(), layerIndexSupplier.getAsInt()),
        () -> childrenFrom(multiLayerNetworkTemplateSupplier.get(), layerIndexSupplier.getAsInt()));
    this.multiLayerNetworkTemplateSupplier = multiLayerNetworkTemplateSupplier;
    this.layerIndexSupplier = layerIndexSupplier;
    this.layerTemplateSupplier = Providers.mapped(multiLayerNetworkTemplateSupplier,
        multiLayerNetworkTemplate -> multiLayerNetworkTemplate.getLayers()
            .get(layerIndexSupplier.getAsInt()));
  }

  private static String labelFrom(MultiLayerNetworkTemplate multiLayerNetworkTemplate,
      int layerIndex) {
    if (layerIndex == 0) {
      return "Input layer";
    }
    if (layerIndex + 1 == multiLayerNetworkTemplate.getLayers().size()) {
      return "Output layer";
    }
    return "Layer %d".formatted(layerIndex);
  }

  private static List<? extends ModelNode> childrenFrom(
      MultiLayerNetworkTemplate multiLayerNetworkTemplate, int layerIndex) {
    var multiLayerNetworkTemplateSupplier = Providers.constant(multiLayerNetworkTemplate);
    var layerTemplateSupplier = Providers.constant(
        multiLayerNetworkTemplate.getLayers().get(layerIndex));
    return List.of(new LayerSizeNode(multiLayerNetworkTemplateSupplier, layerTemplateSupplier),
        new ActivationFunctionNode(multiLayerNetworkTemplateSupplier, layerTemplateSupplier));
  }
}
