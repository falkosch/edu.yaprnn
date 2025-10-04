package edu.yaprnn.gui.model.nodes;

import edu.yaprnn.functions.Providers;
import java.util.List;
import java.util.function.Supplier;
import javax.swing.Icon;
import lombok.Getter;

@Getter
public class SampleNameListNode extends DefaultNode {

  private final Supplier<List<String>> sampleNameListSupplier;

  public SampleNameListNode(Supplier<Icon> iconSupplier, Supplier<String> labelSupplier,
      Supplier<List<String>> sampleNameListSupplier) {
    super(iconSupplier, labelSupplier,
        Providers.mapped(sampleNameListSupplier, SampleNameListNode::childrenFrom));
    this.sampleNameListSupplier = sampleNameListSupplier;
  }

  private static List<? extends ModelNode> childrenFrom(List<String> sampleNameList) {
    return sampleNameList.stream().map(Providers::constant).map(SampleNameNode::new).toList();
  }
}
