package edu.yaprnn.events;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

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
class OnSamplePreviewModifiedRouterTest {

  @Mock
  Event<OnSamplePreviewModified> event;
  @Captor
  ArgumentCaptor<OnSamplePreviewModified> eventCaptor;

  OnSamplePreviewModifiedRouter router;

  @BeforeEach
  void setUp() throws Exception {
    router = new OnSamplePreviewModifiedRouter();
    Field field = OnSamplePreviewModifiedRouter.class.getDeclaredField(
        "onSamplePreviewModifiedEvent");
    field.setAccessible(true);
    field.set(router, event);
  }

  @Nested
  class Defaults {

    @Test
    void shouldHaveDefaultZoom() {
      assertThat(router.getZoom()).isEqualTo(4f);
    }

    @Test
    void shouldHaveDefaultResolution() {
      assertThat(router.getResolution()).isEqualTo(784);
    }

    @Test
    void shouldHaveDefaultOverlap() {
      assertThat(router.getOverlap()).isCloseTo(0.1f, within(0.001f));
    }
  }

  @Nested
  class SetZoom {

    @Test
    void shouldUpdateZoomAndFireEvent() {
      router.setZoom(8.0);

      assertThat(router.getZoom()).isEqualTo(8f);
      verify(event).fire(eventCaptor.capture());
      assertThat(eventCaptor.getValue().zoom()).isEqualTo(8f);
    }

    @Test
    void shouldCastDoubleToFloat() {
      router.setZoom(2.5);

      assertThat(router.getZoom()).isEqualTo(2.5f);
    }
  }

  @Nested
  class SetResolution {

    @Test
    void shouldUpdateResolutionAndFireEvent() {
      router.setResolution(256);

      assertThat(router.getResolution()).isEqualTo(256);
      verify(event).fire(eventCaptor.capture());
      assertThat(eventCaptor.getValue().resolution()).isEqualTo(256);
    }
  }

  @Nested
  class SetOverlap {

    @Test
    void shouldUpdateOverlapAndFireEvent() {
      router.setOverlap(0.5);

      assertThat(router.getOverlap()).isCloseTo(0.5f, within(0.001f));
      verify(event).fire(eventCaptor.capture());
      assertThat(eventCaptor.getValue().overlap()).isCloseTo(0.5f, within(0.001f));
    }
  }

  @Nested
  class Current {

    @Test
    void shouldReturnCurrentState() {
      router.setZoom(2.0);
      router.setResolution(100);
      router.setOverlap(0.2);

      var current = router.current();

      assertThat(current.zoom()).isEqualTo(2f);
      assertThat(current.resolution()).isEqualTo(100);
      assertThat(current.overlap()).isCloseTo(0.2f, within(0.001f));
    }
  }
}
