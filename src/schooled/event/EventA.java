/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package schooled.event;

import schooled.entities.Entity;

/**
 * An interface for event that take in an entity as an input.
 * <p></p>
 * Program written by Joseph Straceski
 * <p>
 * contact: straceski.joseph@gmail.com, https://github.com/Crepox
 */
public interface EventA {

  /**
   * Preform an action with a sender entity.
   *
   * @param sender sender entity
   */
  void act(Entity sender) throws Exception;
}
