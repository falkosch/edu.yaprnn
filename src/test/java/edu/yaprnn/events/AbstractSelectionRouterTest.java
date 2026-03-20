package edu.yaprnn.events;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import edu.yaprnn.gui.model.nodes.DefaultNode;
import edu.yaprnn.gui.model.nodes.ModelNode;
import jakarta.enterprise.event.Event;
import java.util.List;
import java.util.function.Function;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AbstractSelectionRouterTest {

  @Mock
  Event<String> event;
  @Mock
  Function<ModelNode, String> mapper;
  @Captor
  ArgumentCaptor<String> eventCaptor;

  AbstractSelectionRouter<String, String> createRouter() {
    return new AbstractSelectionRouter<>() {
      @Override
      protected Class<String> elementType() {
        return String.class;
      }

      @Override
      protected String createEvent(String selected) {
        return selected;
      }

      @Override
      protected Event<String> event() {
        return event;
      }

      @Override
      protected Function<ModelNode, String> modelNodeMapper() {
        return mapper;
      }
    };
  }

  @Nested
  class SetSelected {

    @Test
    void shouldUpdateSelectedAndFireEvent() {
      var router = createRouter();

      router.setSelected("item");

      assertThat(router.getSelected()).isEqualTo("item");
      verify(event).fire("item");
    }

    @Test
    void shouldSetNullAndFireEvent() {
      var router = createRouter();
      router.setSelected("item");

      router.setSelected((String) null);

      assertThat(router.getSelected()).isNull();
      verify(event).fire(null);
    }
  }

  @Nested
  class UnselectIfRemoved {

    @Test
    void shouldUnselectWhenSelectedItemIsRemoved() {
      var router = createRouter();
      router.setSelected("item");

      router.unselectIfRemoved(new OnRepositoryElementsRemoved(String.class, List.of("item")));

      assertThat(router.getSelected()).isNull();
    }

    @Test
    void shouldNotUnselectWhenDifferentItemIsRemoved() {
      var router = createRouter();
      router.setSelected("item");

      router.unselectIfRemoved(new OnRepositoryElementsRemoved(String.class, List.of("other")));

      assertThat(router.getSelected()).isEqualTo("item");
    }

    @Test
    void shouldNotUnselectWhenDifferentTypeIsRemoved() {
      var router = createRouter();
      router.setSelected("item");

      router.unselectIfRemoved(new OnRepositoryElementsRemoved(Integer.class, List.of(1)));

      assertThat(router.getSelected()).isEqualTo("item");
    }

    @Test
    void shouldDoNothingWhenNothingIsSelected() {
      var router = createRouter();

      router.unselectIfRemoved(new OnRepositoryElementsRemoved(String.class, List.of("item")));

      assertThat(router.getSelected()).isNull();
    }
  }

  @Nested
  class SetSelectedFromModelNode {

    @Mock
    DefaultNode modelNode;

    @Test
    void shouldMapModelNodeAndSetSelected() {
      var router = createRouter();
      org.mockito.Mockito.when(mapper.apply(modelNode)).thenReturn("mapped");

      router.setSelected(new OnModelNodeSelected(modelNode));

      assertThat(router.getSelected()).isEqualTo("mapped");
      verify(event).fire("mapped");
    }
  }
}
