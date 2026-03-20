package edu.yaprnn.events;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import edu.yaprnn.networks.MultiLayerNetwork;
import jakarta.enterprise.event.Event;
import java.lang.reflect.Field;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OnMultiLayerNetworkWeightsPreviewModifiedRouterTest {

  @Mock
  Event<OnMultiLayerNetworkWeightsPreviewModified> event;
  @Mock
  MultiLayerNetwork network;
  @Captor
  ArgumentCaptor<OnMultiLayerNetworkWeightsPreviewModified> eventCaptor;

  OnMultiLayerNetworkWeightsPreviewModifiedRouter router;

  @BeforeEach
  void setUp() throws Exception {
    router = new OnMultiLayerNetworkWeightsPreviewModifiedRouter();
    Field field = OnMultiLayerNetworkWeightsPreviewModifiedRouter.class.getDeclaredField(
        "onMultiLayerNetworkWeightsPreviewModifiedEvent");
    field.setAccessible(true);
    field.set(router, event);
  }

  @Nested
  class Defaults {

    @Test
    void shouldHaveDefaultZoom() {
      assertThat(router.getZoom()).isEqualTo(1f);
    }

    @Test
    void shouldHaveDefaultGamma() {
      assertThat(router.getGamma()).isEqualTo(-1f);
    }
  }

  @Nested
  class SetWeights {

    @Test
    void shouldUpdateWeightsAndFireEvent() {
      router.setWeights(network, 3);

      assertThat(router.getMultiLayerNetwork()).isSameAs(network);
      assertThat(router.getWeightsIndex()).isEqualTo(3);
      verify(event).fire(eventCaptor.capture());
      assertThat(eventCaptor.getValue().multiLayerNetwork()).isSameAs(network);
      assertThat(eventCaptor.getValue().weightsIndex()).isEqualTo(3);
    }

    @Test
    void shouldHandleObservedEvent() {
      var weightsSelected = new OnMultiLayerNetworkWeightsSelected(network, 5);

      router.setWeights(weightsSelected);

      assertThat(router.getMultiLayerNetwork()).isSameAs(network);
      assertThat(router.getWeightsIndex()).isEqualTo(5);
    }
  }

  @Nested
  class SetZoom {

    @Test
    void shouldUpdateZoomAndFireEvent() {
      router.setZoom(4.0);

      assertThat(router.getZoom()).isEqualTo(4f);
      verify(event).fire(eventCaptor.capture());
      assertThat(eventCaptor.getValue().zoom()).isEqualTo(4f);
    }
  }

  @Nested
  class SetGamma {

    @Test
    void shouldUpdateGammaAndFireEvent() {
      router.setGamma(2.5);

      assertThat(router.getGamma()).isEqualTo(2.5f);
      verify(event).fire(eventCaptor.capture());
      assertThat(eventCaptor.getValue().gamma()).isEqualTo(2.5f);
    }
  }

  @Nested
  class Current {

    @Test
    void shouldReturnCurrentState() {
      router.setWeights(network, 2);
      router.setZoom(8.0);
      router.setGamma(0.5);

      var current = router.current();

      assertThat(current.multiLayerNetwork()).isSameAs(network);
      assertThat(current.weightsIndex()).isEqualTo(2);
      assertThat(current.zoom()).isEqualTo(8f);
      assertThat(current.gamma()).isEqualTo(0.5f);
    }
  }
}
