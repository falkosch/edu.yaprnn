package edu.yaprnn.gui.model.nodes;

import edu.yaprnn.functions.Providers;
import edu.yaprnn.gui.services.IconsService;
import java.util.Collections;
import java.util.function.Supplier;
import lombok.Getter;

@Getter
public class SampleNameNode extends DefaultNode {

  private final Supplier<String> sampleNameSupplier;

  public SampleNameNode(Supplier<String> sampleNameSupplier) {
    super(Providers.constant(IconsService.ICON_SAMPLE), sampleNameSupplier, Collections::emptyList);
    this.sampleNameSupplier = sampleNameSupplier;
  }
}
