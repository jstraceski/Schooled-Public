/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package schooled.entities;

import java.util.ArrayList;
import schooled.Game;
import schooled.engines.Engine;
import schooled.event.Event;
import schooled.physics.Circle;
import schooled.physics.Shape;
import schooled.physics.Vector;
import schooled.visuals.sprite.Sprite;

/**
 * An Entity to represent an Item.
 * <p></p>
 * Program written by Joseph Straceski
 * <p>
 * contact: straceski.joseph@gmail.com, https://github.com/Crepox
 */
public class ItemEntity extends Entity {

  private boolean removeOnPickup = true; // remove the entity on pickup
  private boolean takeable = true; // is the item takeable
  private boolean holdable = false;
  private Event pickupEvent = null; // event to preform when the item is picked


  /**
   * Create a blank item entity.
   *
   * @param g game instance
   */
  public ItemEntity(Game g) {
    this(g, new Vector(100, 100), new Circle(10), 10);
  }

  /**
   * Construct an entity with the basic data types.
   *
   * @param g the game instance
   * @param v the position
   * @param s the shape
   * @param m the mass
   */
  public ItemEntity(Game g, Vector v, Shape s, float m) {
    super(g, v, s, m);
  }

  /**
   * Construct an entity with a BasicEntity
   *
   * @param b the basic entity
   */
  public ItemEntity(Entity b) {
    super(b);
  }

  @Override
  public Entity clone() {

    return new ItemEntity(this);
  }

  @Override
  public BasicEntity setClone(BasicEntity entity) {
    if (entity.getClass().isInstance(this)) {
      return setClone(this.getClass().cast(entity));
    } else {
      return super.setClone(entity);
    }
  }

  @Override
  public boolean collision(BasicEntity basicEntity) {
    if (basicEntity instanceof Player && takeable) {
      if (pickupEvent != null) {
        pickupEvent.act((Player) basicEntity, this);
      }

      if (removeOnPickup) {
        removeFromContainer();
      }

      if (holdable) {
        ((Player) basicEntity).items.add(this);
        ArrayList<ItemEntity> held = ((Player) basicEntity).held;

        if (held.size() == 0) {
          held.add(this);
        } else if (held.size() == 1) {
          held.add(this);
        }
      }

      cancelCollision();
    }

    return true;
  }

  /**
   * Set if the item should be removed on pickup.
   *
   * @param remove set if it should be removed
   */
  public void setRemoveOnPickup(boolean remove) {
    this.removeOnPickup = remove;
  }

  /**
   * Set if the item is takeable or not.
   *
   * @param takeable set whether or not the item can be taken
   */
  public void setTakeable(boolean takeable) {
    this.takeable = takeable;
  }

  public boolean isHoldable() {
    return holdable;
  }

  public void setHoldable(boolean holdable) {
    this.holdable = holdable;
  }

  /**
   * Set the pickup event
   *
   * @param pickupEvent the pickup event
   */
  public void setPickupEvent(Event pickupEvent) {
    this.pickupEvent = pickupEvent;
  }

  @Override
  public Sprite getSprite() {
    if (hasSprite()) {
      return super.getSprite();
    }
    return getGame().getSprite("Default_Item");
  }

  float throwSpeed = 5f;
  float velocityBonus = 2f;
  boolean leftHand = false;
  float outTime = 0;
  float grabTime = 2f;
  float maxGrabTime = 5f;

  public boolean use(Player user) {
    if (!hasParent()) {
      Vector velocity = user.getLocalVelocity().clone();
      Vector throwDir = user.getMoveDirection().normalizei();

      Vector throwVector = velocity.scalei(velocityBonus).addScaledi(throwDir, throwSpeed);

      user.addChild(this);
      setVelocity(throwVector);
      setPos(Vector.zero.clone());
      leftHand = false;
      outTime = 0;
      return true;
    }
    return false;
  }

  @Override
  public void updateCycle(float t) {
    super.updateCycle(t);

    Entity p = getParent();
    if (hasParent() && p instanceof Player && Engine.collides(p.getShape(), p.getPos(), getShape(), getPos())) {
      if (leftHand) {
        p.removeChild(this);
        this.removeFromContainer();
      }
    } else {
      leftHand = true;
    }

    outTime += t;

    if (outTime > grabTime) {
      leftHand = true;
    }

    if (leftHand && outTime > maxGrabTime && p != null) {
      p.removeChild(this);
      this.removeFromContainer();
    }
  }
}
