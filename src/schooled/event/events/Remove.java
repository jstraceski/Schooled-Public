/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package schooled.event.events;

import schooled.entities.Entity;
import schooled.event.Event;

/**
 * A pre-built Event that removes an entity from a room. Imutable.
 * <p></p>
 * Program written by Joseph Straceski
 * <p>
 * contact: straceski.joseph@gmail.com, https://github.com/Crepox
 */
public class Remove extends Event {

  /**
   * Construct a pre-built remove event.
   * <p>
   * Removes Entity: entity from its current room.
   *
   * @param entity entity being removed
   */
  public Remove(Entity entity) {
    super(entity::removeFromContainer);
  }
}
