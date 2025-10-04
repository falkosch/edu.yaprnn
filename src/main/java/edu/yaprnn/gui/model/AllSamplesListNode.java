package edu.yaprnn.gui.model;

import edu.yaprnn.functions.Providers;
import edu.yaprnn.gui.model.nodes.SampleListNode;
import edu.yaprnn.gui.services.IconsService;
import edu.yaprnn.model.Repository;
import edu.yaprnn.samples.model.Sample;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.List;

@Singleton
public class AllSamplesListNode extends SampleListNode {

  @Inject
  public AllSamplesListNode(Repository repository, IconsService iconsService) {
    super(Providers.constant(iconsService.sampleSetNodeIcon()),
        Providers.mapped(repository::getSamples, AllSamplesListNode::labelFrom),
        repository::getSamples);
  }

  private static String labelFrom(List<Sample> samples) {
    return "Samples (%d)".formatted(samples.size());
  }
}
