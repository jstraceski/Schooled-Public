/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package schooled.event;

import schooled.entities.Entity;

/**
 * An interface for event that have a sending and receiving entity as inputs.
 * <p></p>
 * Program written by Joseph Straceski
 * <p>
 * contact: straceski.joseph@gmail.com, https://github.com/Crepox
 */
public interface EventAB {

  /**
   * Preform an action with a sender and receiver entity.
   *
   * @param sender   sender entity
   * @param receiver receiver entity
   */
  void act(Entity sender, Entity receiver) throws Exception;
}
