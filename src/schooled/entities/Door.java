/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package schooled.entities;

import schooled.Game;
import schooled.containers.World;
import schooled.event.events.Move;
import schooled.physics.Shape;
import schooled.physics.Vector;
import schooled.visuals.sprite.Sprite;

/**
 * An Entity to represent a Door.
 * <p></p>
 * Program written by Joseph Straceski
 * <p>
 * contact: straceski.joseph@gmail.com, https://github.com/Crepox
 */
public class Door extends Entity {

  Door otherDoor; // this is the door that Players interacting with this door will exit from.
  Vector exitVector; // the offset vector applied to entities exiting from the door.

  /**
   * Constructor for a door object
   *
   * @param g        the game instance
   * @param position the door position
   * @param shape    the shape
   * @param mass     the mass
   */
  public Door(Game g, Vector position, Shape shape, float mass) {
    super(g, position, shape, mass);
    setup();
  }

  /**
   * Constructor for a door object using a BasicEntity
   *
   * @param be the basic entity
   */
  public Door(Entity be) {
    super(be);
    setup();
  }

  public Door(Game be) {
    super(be);
    setup();
  }

  @Override
  public Entity clone() {
    return new Door(this);
  }

  @Override
  public BasicEntity setClone(BasicEntity entity) {
    if (entity instanceof Door) {
      return setClone(this.getClass().cast(entity));
    } else {
      return super.setClone(entity);
    }
  }

  @Override
  public Entity setClone(Entity entity) {
    super.setClone(entity);
    if (entity instanceof Door) {
      ((Door) entity).otherDoor = otherDoor;
      ((Door) entity).exitVector = exitVector;
    }

    return entity;
  }

  /**
   * Set up the physical interactions for the door. (gets called when constructing a Door)
   */
  private void setup() {
    setPhysics(false);
    setInteractOverride(true);
  }

  @Override
  public Sprite getSprite() {
    if (super.getSprite() != null) {
      return super.getSprite();
    }
    return getGame().getImage("door");
  }

  /**
   * Get the targeted door.
   *
   * @return the targeted door
   */
  public Door getTargetDoor() {
    return otherDoor;
  }

  /**
   * Set the targeted door.
   *
   * @param d the targeted door
   */
  public void setTargetDoor(Door d) {
    this.otherDoor = d;
  }


  /**
   * Get the exit vector.
   *
   * @return the exit vector
   */
  public Vector getExitVector() {
    return exitVector;
  }

  /**
   * Set the exit vector.
   *
   * @param exitVector the exit vector
   */
  public void setExitVector(Vector exitVector) {
    this.exitVector = exitVector;
  }

  @Override
  public boolean interact(Entity e) {
    super.interact(e);

    // if the interacting entity is a player.
    if (e instanceof Player) {
      // if the other door doesn't exist cancel the interaction.
      if (otherDoor == null) {
        return false;
      }

      // if the exit vector doesn't exist use the diff from the current door as the exit vector
      Vector eVector = otherDoor.getExitVector() != null
          ? otherDoor.getExitVector()
          : Vector.sub(e.getPosition(), getPosition());

      // move the interacting entity in cycle to the new room
      World world = getGame().getWorld();
      e.setVelocity(Vector.zero.clone());
      world.addSyncedEvent(new Move(e, otherDoor.getPosition().addi(eVector), otherDoor.getRoom(), true));

      return true;
    }
    return false;
  }
}
