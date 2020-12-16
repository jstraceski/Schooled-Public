/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controls;

import java.util.ArrayList;
import java.util.Arrays;
import schooled.Game;

/**
 * Reference to one or more keyboard / mouse buttons.
 * <p>
 * Stores a list of buttons.
 * <p></p>
 * Program written by Joseph Straceski
 * <p>
 * contact: straceski.joseph@gmail.com, https://github.com/Crepox
 */
public class Button {

  private ArrayList<ButtonData> buttonList = new ArrayList<>();

  /**
   * Create a keyboard Button from the id given.
   *
   * @param newButton button id
   */
  public Button(int newButton) {
    this(newButton, ButtonType.Keyboard);
  }

  /**
   * Create a button reference from an id and a button type.
   * <p>
   * Buttons can be of type Keyboard or Mouse as of 8-10-19
   *
   * @param newButton button id
   * @param newType   button type
   */
  public Button(int newButton, ButtonType newType) {
    setButton(newButton, newType);
  }

  @Override
  public String toString() {
    return "button[keys=" + Game.toStr(buttonList) + "]";
  }

  /**
   * Reset and replace the values with a single button type and id.
   *
   * @param newButton button id
   * @param type button type
   */
  public void setButton(int newButton, ButtonType type) {
    this.buttonList.clear();
    addButton(newButton, type);
  }

  /**
   * Add the data from a referenced button to the current data.
   *
   * @param button referenced button
   */
  public void addButton(Button button) {
    for (ButtonData bd : button.getButtons()) {
      addButton(bd.button, bd.type);
    }
  }

  /**
   * Add a new button with a given id and type to the current data.
   *
   * @param newButton button id
   * @param type button type
   */
  public void addButton(int newButton, ButtonType type) {
    ButtonData data = new ButtonData();
    data.button = newButton;
    data.type = type;
    buttonList.add(data);
  }

  /**
   * Get a list of data representing any referenced buttons.
   *
   * @return list of button references
   */
  public ArrayList<ButtonData> getButtons() {
    return buttonList;
  }

  /**
   * Describes types of input devices.
   */
  public enum ButtonType {
    Mouse, Keyboard
  }
}
