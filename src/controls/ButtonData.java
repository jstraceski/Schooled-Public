package controls;

import controls.Button.ButtonType;

/**
 * A data container for a solitary button on the keyboard or mouse.
 * <p></p>
 * Program written by Joseph Straceski
 * <p>
 * contact: straceski.joseph@gmail.com, https://github.com/Crepox
 */
public class ButtonData {

  public ButtonType type; // type of the button ie. keyboard/ mouse
  public int button; // the button id

  @Override
  public String toString() {
    return "<" + type.toString() + ": " + button + ">";
  }
}
