package edu.yaprnn.gui.model.nodes;

import edu.yaprnn.gui.services.IconsService;
import edu.yaprnn.samples.model.Sample;
import edu.yaprnn.support.Providers;
import java.util.Collections;
import java.util.function.Supplier;
import lombok.Getter;

/**
 * Represents a {@link Sample} in the tree model.
 */
@Getter
public class SampleNode extends DefaultNode {

  private final Supplier<Sample> sampleSupplier;

  public SampleNode(Supplier<Sample> sampleSupplier) {
    super(Providers.constant(IconsService.ICON_SAMPLE),
        Providers.mapped(sampleSupplier, SampleNode::labelFrom), Collections::emptyList);
    this.sampleSupplier = sampleSupplier;
  }

  private static String labelFrom(Sample sample) {
    return "%s: %s".formatted(sample.getLabel(), sample.getName());
  }
}
