/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package schooled.menu;

import schooled.Game;
import schooled.entities.BasicEntity;
import schooled.entities.Entity;
import schooled.event.Event;
import schooled.physics.Vector;

/**
 * String area that denotes a pressable button.
 * <p></p>
 * Program written by Joseph Straceski
 * <p>
 * contact: straceski.joseph@gmail.com, https://github.com/Crepox
 */
public class MenuButton extends MenuEntity {

  private boolean hoverOver, hoverOverLast;
  private Event buttonEvent;

  /**
   * Create an empty button.
   *
   * @param g game instance
   */
  public MenuButton(Game g) {
    super(g);
    hoverOver = false;
    setPhysics(false);
  }

  public MenuButton(MenuEntity g) {
    super(g);
    hoverOver = false;
    setPhysics(false);
  }

  /**
   * Create a button out of the game instance, a position vector, and a string.
   *
   * The string is used as the text of the button.
   *
   * @param g game instance
   * @param v position
   * @param s button text
   */
  public MenuButton(Game g, Vector v, String s) {
    this(g);
    setPosition(v);
    addText(s);
  }

  /**
   * Create a button out of the game instance, a position vector, and a text context.
   *
   * The text context is used as the text of the button.
   *
   * @param g game instance
   * @param v position
   * @param s button text
   */
  public MenuButton(Game g, Vector v, TextContext s) {
    this(g);
    setPosition(v);
    addText(s);
  }

  @Override
  public boolean collision(BasicEntity e) {
    if (!hoverOver && (getGame() == null || getGame().getGameEntity() == e)) {
      hoverOver = true;
      if (!hoverOverLast) {
        setTextBold(true);
      }
    }
    return true;
  }

  @Override
  public boolean interact(Entity e) {
    if (buttonEvent != null) {
      buttonEvent.act();
      return true;
    }
    return false;
  }

  @Override
  public void updateCycle(float f) {
    super.updateCycle(f);
    if (!hoverOver && hoverOverLast) {
      setTextBold(false);
    }

    hoverOverLast = hoverOver;

    if (hoverOver) {
      hoverOver = false;
    }
  }

  /**
   * Get the event preformed when clicking the button.
   *
   * @return button event
   */
  public Event getButtonEvent() {
    return buttonEvent;
  }

  /**
   * Set the event preformed when clicking the button.
   *
   * @param event button event
   */
  public void setButtonEvent(Event event) {
    this.buttonEvent = event;
  }
}
