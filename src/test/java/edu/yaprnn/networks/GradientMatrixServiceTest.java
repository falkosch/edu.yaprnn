package edu.yaprnn.networks;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import edu.yaprnn.networks.activation.ActivationFunction;
import edu.yaprnn.networks.activation.LinearActivationFunction;
import edu.yaprnn.networks.activation.SigmoidActivationFunction;
import java.util.Random;
import org.junit.jupiter.api.Test;

class GradientMatrixServiceTest {

  final GradientMatrixService service = new GradientMatrixService();

  @Test
  void shouldCreateZeroMatricesWithCorrectDimensions() {
    var layerSizes = new int[]{3, 4, 2};

    var result = service.zeroMatrices(layerSizes);

    assertThat(result.length).isEqualTo(2);
    // (3+1)*4 = 16
    assertThat(result[0]).hasSize(16);
    // (4+1)*2 = 10
    assertThat(result[1]).hasSize(10);
    assertThat(result[0]).containsOnly(0f);
    assertThat(result[1]).containsOnly(0f);
  }

  @Test
  void shouldCopyMatrices() {
    var source = new float[][]{
        {1f, 2f},
        {3f, 4f, 5f}
    };

    var result = service.copyMatrices(source);

    assertThat(result.length).isEqualTo(2);
    assertThat(result[0]).containsExactly(1f, 2f);
    assertThat(result[1]).containsExactly(3f, 4f, 5f);
    // Should be independent copies
    assertThat(result[0]).isNotSameAs(source[0]);
    assertThat(result[1]).isNotSameAs(source[1]);
  }

  @Test
  void shouldResetLayerWeightsWithExplicitRandom() {
    var layerSizes = new int[]{2, 3, 1};
    var activationFunctions = new ActivationFunction[]{
        new LinearActivationFunction(),
        new SigmoidActivationFunction(),
        new LinearActivationFunction()
    };

    var result = service.resetLayerWeights(new Random(42L), layerSizes, activationFunctions);

    assertThat(result.length).isEqualTo(2);
    // (2+1)*3 = 9
    assertThat(result[0]).hasSize(9);
    // (3+1)*1 = 4
    assertThat(result[1]).hasSize(4);
  }

  @Test
  void shouldThrowWhenLayerSizesAndActivationFunctionsMismatch() {
    var layerSizes = new int[]{2, 3};
    var activationFunctions = new ActivationFunction[]{
        new LinearActivationFunction(),
        new SigmoidActivationFunction(),
        new LinearActivationFunction()
    };

    assertThatThrownBy(() -> service.resetLayerWeights(new Random(42L), layerSizes,
        activationFunctions))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("same length");
  }

  @Test
  void shouldAccumulateGradientsInPlace() {
    var accumulator = new float[][]{
        {1f, 2f, 3f},
        {4f, 5f}
    };
    var gradients = new float[][]{
        {0.1f, 0.2f, 0.3f},
        {0.4f, 0.5f}
    };

    service.accumulateGradientsInPlace(accumulator, gradients);

    assertThat(accumulator[0]).containsExactly(1.1f, 2.2f, 3.3f);
    assertThat(accumulator[1]).containsExactly(4.4f, 5.5f);
  }

  @Test
  void shouldThrowWhenInPlaceAccumulatorAndGradientsMismatchLength() {
    var accumulator = new float[][]{{1f, 2f}};
    var gradients = new float[][]{{1f}, {2f}};

    assertThatThrownBy(() -> service.accumulateGradientsInPlace(accumulator, gradients))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("same length");
  }

  @Test
  void shouldThrowWhenInPlaceAccumulatorAndGradientsMismatchInnerLength() {
    var accumulator = new float[][]{{1f, 2f}};
    var gradients = new float[][]{{1f, 2f, 3f}};

    assertThatThrownBy(() -> service.accumulateGradientsInPlace(accumulator, gradients))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("same length");
  }

  @Test
  void shouldZeroFillMatrices() {
    var matrices = new float[][]{
        {1f, 2f, 3f},
        {4f, 5f}
    };

    service.zeroFillMatrices(matrices);

    assertThat(matrices[0]).containsOnly(0f);
    assertThat(matrices[1]).containsOnly(0f);
  }

  @Test
  void shouldZeroFillEmptyMatrices() {
    var matrices = new float[0][];

    service.zeroFillMatrices(matrices);

    assertThat(matrices).isEmpty();
  }
}
