package yaprnn.gui;

import java.awt.Component;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.swing.JOptionPane;
import yaprnn.dvv.Data;

class PreviewPlayAudioListener implements MouseListener {

  private static Clip curClip = null;
  private final Component target;
  private Data data = null;

  PreviewPlayAudioListener(Component target) {
    this.target = target;
    target.addMouseListener(this);
  }

  void setData(Data data) {
    this.data = data;
  }

  @Override
  public void mouseClicked(MouseEvent e) {
    // Letzten Clip stoppen
    if (curClip != null) {
      curClip.stop();
      curClip.close();
    }

    if (data == null || !data.isAudio()) {
      return;
    }

    try {
      AudioInputStream ais = AudioSystem.getAudioInputStream(new File(data.getPath()));
      AudioFormat format = ais.getFormat();
      int bufferSize = (int) (ais.getFrameLength() * format.getFrameSize());
      DataLine.Info info = new DataLine.Info(Clip.class, format, bufferSize);
      curClip = (Clip) AudioSystem.getLine(info);
      curClip.open(ais);
      curClip.start();
    } catch (Exception ex) {
      JOptionPane.showMessageDialog(target,
          "An error occured while trying to play the audio preview:\n" + ex.getMessage(),
          "Playing audio preview", JOptionPane.ERROR_MESSAGE);
    }
  }

  @Override
  public void mousePressed(MouseEvent e) {
  }

  @Override
  public void mouseReleased(MouseEvent e) {
  }

  @Override
  public void mouseEntered(MouseEvent e) {
  }

  @Override
  public void mouseExited(MouseEvent e) {
  }

}
