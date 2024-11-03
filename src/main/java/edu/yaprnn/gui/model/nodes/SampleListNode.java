package edu.yaprnn.gui.model.nodes;

import edu.yaprnn.gui.model.NetworksTreeModel;
import edu.yaprnn.samples.model.Sample;
import edu.yaprnn.support.Providers;
import java.util.List;
import java.util.function.Supplier;
import javax.swing.Icon;
import lombok.Getter;

/**
 * Represents a set of {@link Sample} in the {@link NetworksTreeModel}.
 */
@Getter
public class SampleListNode extends DefaultNode {

  private final Supplier<List<Sample>> sampleListSupplier;

  public SampleListNode(Supplier<Icon> iconSupplier, Supplier<String> labelSupplier,
      Supplier<List<Sample>> sampleListSupplier) {
    super(iconSupplier, labelSupplier,
        Providers.mapped(sampleListSupplier, SampleListNode::childrenFrom));
    this.sampleListSupplier = sampleListSupplier;
  }

  private static List<? extends ModelNode> childrenFrom(List<Sample> sampleList) {
    return sampleList.stream().map(Providers::constant).map(SampleNode::new).toList();
  }
}
