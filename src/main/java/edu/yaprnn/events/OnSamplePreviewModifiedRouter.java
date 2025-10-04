package edu.yaprnn.events;

import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.Getter;

@Singleton
public class OnSamplePreviewModifiedRouter {

  @Inject
  Event<OnSamplePreviewModified> onSamplePreviewModifiedEvent;

  @Getter
  private float zoom = 4f;
  @Getter
  private int resolution = 784;
  @Getter
  private float overlap = 0.1f;

  public void setZoom(double value) {
    setZoom((float) value);
  }

  public void setZoom(float value) {
    zoom = value;
    fireEvent();
  }

  private void fireEvent() {
    onSamplePreviewModifiedEvent.fire(current());
  }

  public OnSamplePreviewModified current() {
    return new OnSamplePreviewModified(zoom, resolution, overlap);
  }

  public void setResolution(int value) {
    resolution = value;
    fireEvent();
  }

  public void setOverlap(double value) {
    setOverlap((float) value);
  }

  public void setOverlap(float value) {
    overlap = value;
    fireEvent();
  }
}
