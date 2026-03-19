package edu.yaprnn.networks.templates;

import static org.assertj.core.api.Assertions.assertThat;

import edu.yaprnn.networks.activation.LinearActivationFunction;
import edu.yaprnn.networks.activation.ReLUActivationFunction;
import edu.yaprnn.networks.activation.SigmoidActivationFunction;
import java.util.List;
import org.junit.jupiter.api.Test;

class MultiLayerNetworkTemplateTest {

  final LinearActivationFunction linear = new LinearActivationFunction();
  final ReLUActivationFunction relu = new ReLUActivationFunction();
  final SigmoidActivationFunction sigmoid = new SigmoidActivationFunction();

  final MultiLayerNetworkTemplate template = MultiLayerNetworkTemplate.builder()
      .name("TestNet")
      .layers(List.of(
          LayerTemplate.builder().size(4).activationFunction(linear).build(),
          LayerTemplate.builder().size(8).activationFunction(relu).build(),
          LayerTemplate.builder().size(2).activationFunction(sigmoid).build()
      ))
      .bias(1f)
      .build();

  @Test
  void shouldCollectLayerSizes() {
    assertThat(template.collectLayerSizes()).containsExactly(4, 8, 2);
  }

  @Test
  void shouldCollectActivationFunctions() {
    var functions = template.collectActivationFunctions();

    assertThat(functions).hasSize(3);
    assertThat(functions[0]).isInstanceOf(LinearActivationFunction.class);
    assertThat(functions[1]).isInstanceOf(ReLUActivationFunction.class);
    assertThat(functions[2]).isInstanceOf(SigmoidActivationFunction.class);
  }

  @Test
  void shouldFormatToString() {
    assertThat(template.toString()).isEqualTo("TestNet (3)");
  }

  @Test
  void shouldHandleEmptyLayers() {
    var empty = MultiLayerNetworkTemplate.builder().name("Empty").build();

    assertThat(empty.collectLayerSizes()).isEmpty();
    assertThat(empty.collectActivationFunctions()).isEmpty();
    assertThat(empty.toString()).isEqualTo("Empty (0)");
  }
}
