# Critical Issues

## C1: Audio stream resource leak if clip.open() throws

**File**: `src/main/java/edu/yaprnn/gui/views/SampleDetailsView.java:261-275`
**Category**: Java

**Problem**: In `PlayAudioSampleMouseAdapter.from()`, the `AudioInputStream` is created at line 262 but only closed inside the `LineListener` callback (line 268). If `clip.open(stream)` at line 275 throws an exception, the stream is never closed because the line listener is never triggered.

**Fix**: Use try-with-resources or a try-catch to close the stream on failure:

```java
private Clip from(Sample sample) throws Exception {
  if (sample == null || sample.getFile() == null) {
    return null;
  }

  var clip = AudioSystem.getClip();
  var stream = AudioSystem.getAudioInputStream(sample.getFile());

  try {
    clip.addLineListener(event -> {
      if (event.getType() == Type.STOP) {
        clip.close();
        try {
          stream.close();
        } catch (java.io.IOException ignored) {
        }
        clipRef.compareAndSet(clip, null);
      }
    });

    clip.open(stream);
    clip.start();
    return clip;
  } catch (Exception e) {
    stream.close();
    clip.close();
    throw e;
  }
}
```
