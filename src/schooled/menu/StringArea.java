/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package schooled.menu;

import schooled.Game;
import schooled.physics.Vector;

/**
 * Menu entity with a built in string constructor.
 * <p></p>
 * Program written by Joseph Straceski
 * <p>
 * contact: straceski.joseph@gmail.com, https://github.com/Crepox
 */
public class StringArea extends MenuEntity {

  /**
   * Create a menu entity with a string.
   *
   * @param g game instance
   * @param v position
   * @param s string to display
   */
  public StringArea(Game g, Vector v, String s) {
    super(g, v, null);
    addText(s);
    updateCycle();
  }

  public StringArea(Game g, Vector v) {
    super(g, v, null);
    updateCycle();
  }
}
