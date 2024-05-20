package yaprnn.gui;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * Forces the user to just enter numbers.
 */
class OnlyNumbersKeyAdapter extends KeyAdapter {

  private final boolean onlyPositive;
  private final boolean onlyIntegers;

  OnlyNumbersKeyAdapter(boolean onlyPositive, boolean onlyIntegers) {
    this.onlyPositive = onlyPositive;
    this.onlyIntegers = onlyIntegers;
  }

  @Override
  public void keyTyped(KeyEvent e) {
    char c = e.getKeyChar();

    if ((c == '.' || c == 'E' || c == 'e') && onlyIntegers) {
      e.consume();
      return;
    }

    if (c == '-' && onlyPositive) {
      e.consume();
      return;
    }

    if (!(Character.isDigit(c) || c == '-' || c == '.' || c == 'E' || c == 'e')) {
      e.consume();
    }
  }

}
