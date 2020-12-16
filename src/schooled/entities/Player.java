package schooled.entities;

import java.util.ArrayList;
import schooled.Game;
import schooled.Window;
import schooled.containers.EntityHolder;
import schooled.containers.Room;
import schooled.engines.RenderEngine;
import schooled.event.Event;
import schooled.event.events.Remove;
import schooled.physics.BoundingBox;
import schooled.physics.Manifold;
import schooled.physics.PolygonShape;
import schooled.physics.Shape;
import schooled.physics.StateShape;
import schooled.physics.Vector;
import schooled.visuals.sprite.Animation;
import schooled.visuals.sprite.Sprite;
import schooled.visuals.sprite.StateSprite;

/**
 * An Entity to represent a Player.
 * <p></p>
 * Program written by Joseph Straceski
 * <p>
 * contact: straceski.joseph@gmail.com, https://github.com/Crepox
 */
public class Player extends NPC {

  // A shape to represent the default player footprint
  private static final Shape DEFAULT_FOOTPRINT = new PolygonShape(16.0f, 6.0f);
  // The default mass of the player
  private static final float DEFAULT_MASS = 60f; // average-ish human weight in kg
  // The default mass of the player
  private static final Vector DEFAULT_SPRITE_SHIFT = new Vector(0, -20);
  // The default walk speed of the player
  private final float walkSpeed = 0.10f;
  // The default sprinting speed of the player
  private final float sprintSpeed = 0.18f;
  // The direction the player is facing
  private Vector facing = new Vector(0.0f, 1.0f);
  // The previous direction the player was facing
  private Vector oldFacing = new Vector(0.0f, 0.0f);
  // A boolean to store if the player is sprinting or not
  private boolean sprinting = false;
  // A boolean to store if the player is moving or not
  private boolean moving = false;
  // A boolean to store if the player was moving the last tick or not
  private boolean oldMoving = false;
  // The current movement direction of the player
  private Vector moveDirection = new Vector(0, 0);

  // A boolean to store if the player can moveDirection or not
  private boolean canMove = true;
  // A boolean to store if the player is frozen in time or not
  private boolean frozen = false;

  // The current pose of the player
  private Pose pose = Pose.STANDING;
  // The last pose of the player
  private Pose lastPose = Pose.STANDING;
  private EntityArea interactArea;

  /**
   * A constructor for a player.
   *
   * @param g the game instance
   */
  public Player(Game g) {
    super(g, new Vector(0.0f, 0.0f), DEFAULT_FOOTPRINT, DEFAULT_MASS);
    interactArea = new EntityArea(getGame(), new PolygonShape(7, 7), this::addInteraction);
    interactArea.setCollides(false);
    interactArea.setInteractAll(true);
    addChild(interactArea);
  }

  /**
   * Set if the player can moveDirection or not.
   *
   * @param value if true the player can moveDirection, if false the player can't moveDirection
   */
  public void setCanMove(boolean value) {
    canMove = value;
  }

  /**
   * Get the direction the player is facing.
   *
   * @return the facing direction
   */
  public Vector getFacing() {
    return facing;
  }

  /**
   * Is the player facing the direction v.
   *
   * @param v the input direction
   * @return if the input vector roughly equals the facing vector return true
   */
  public boolean isFacing(Vector v) {
    return facing.roughEquals(v, 0.1f);
  }

  /**
   * Set the sprinting status of the player.
   *
   * @param newSprint if set true the player is sprinting, if false the player is walking
   */
  public void setSprint(boolean newSprint) {
    sprinting = newSprint;
    if (getSprite() != null && getSprite().isAnimated()) {
      if (newSprint) {
        // getSprite().getAnimation().setDefaultDelay(10000000L);
      } else {
        // getSprite().getAnimation().setDefaultDelay(20000000L);
      }
    }
  }

  /**
   * Get the current speed of the player.
   *
   * @return the current speed of the player
   */
  public float getSpeed() {
    if (sprinting) {
      return sprintSpeed;
    }
    return walkSpeed;
  }

  /**
   * Set the vector direction that the player facing.
   *
   * @param v the input vector
   */
  public void setFacingVector(Vector v) {
    if (v.roughEquals(Vector.zero, 0.00001f)) {
      return;
    }

    facing = Vector.getDirectionVector(v);
  }

  /**
   * Set the movement direction of the player.
   *
   * @param v the input vector
   */
  public void setMoveDirection(Vector v) {
    moveDirection = v;
  }

  /**
   * Get the movement direction of the player.
   *
   * @return  the input vector
   */
  public Vector getMoveDirection() {
    return moveDirection;
  }

  /**
   * Set the pose of the player.
   *
   * @param pose the player's pose
   */
  public void setPose(Pose pose) {
    this.pose = pose;
  }

  /**
   * Set the sitting state of the player.
   *
   * @param bool if true set the player's state to sitting, if false set the player's state to
   *             standing
   */
  public void setSitting(boolean bool, Pose pose) {
    if (bool) {
      setCanMove(false);
      setPose(pose);
      setPhysics(false);
      setVelocity(Vector.zero.clone());
    } else {
      setCanMove(true);
      setPose(Pose.STANDING);
      setPhysics(true);
    }
    updateSprite();
  }

  /**
   * Set the sleeping state of the player.
   *
   * @param bool if true set the player's state to sleeping, if false set the player's state to
   *             standing
   */
  public void setSleeping(boolean bool) {
    if (bool) {
      setCanMove(false);
      setPose(Pose.LYING_DOWN);
      setVelocity(Vector.zero.clone());
    } else {
      setCanMove(true);
      setPose(Pose.STANDING);
    }
    updateSprite();
  }

  /**
   * Add to the velocity of the entity. Checks if the player is moving and not frozen before adding
   * the velocity.
   *
   * @param v the input vector
   */
  public void addVelocity(Vector v) {
    if (!frozen && canMove) {
      super.addVelocity(v);
    }
  }

  @Override
  public void setPosition(Vector v) {
    super.setPosition(v);
  }

  @Override
  public void updateCycle(float t) {
    if (!frozen) {
      // Convert the movement direction to a unit vector
      Vector moveVelocity = moveDirection.normalizei();
      // Scale the unit vector by the current speed and the current time difference
      moveVelocity.scale(t * 60 * this.getSpeed());
      // Add the movement vector to the velocity of the entity
      addVelocity(moveVelocity);
    }

    // update the moving status of the player
    moving = !getLocalVelocity().roughEquals(new Vector(0, 0), 0.01f);

    // if the player isn't moving and is currently walking finish the walking
    //  animation and then set the pose of the player to standing
    if (!moving && pose == Pose.WALKING) {
      if (hasSprite() && getSprite().isAnimated() && !getSprite().isFinished()) {
        getSprite().setEnd(true);
      } else {
        setPose(Pose.STANDING);
      }
    } else if (moving && hasSprite() && getSprite().isEnding() && getSprite().isAnimated()) {
      // check if a player has ended a run and then restarted it before it finished
      getSprite().setEnd(false); // set the animation to not end.
    }

    // if the player is moving and isn't frozen then set the pose to walking
    if (moving && !frozen) {
      setPose(Pose.WALKING);
    }

    updateSprite(); // update the current sprite of the player
    updateAnimationSpeed(); // update the speed of the animated sprites
    oldFacing = facing; // set the previous facing vector
    oldMoving = moving; // set the old moving flag
    lastPose = pose; // set the last pose

    super.updateCycle(t);
  }

  /**
   * Update the speed of the animation based on the velocity of the entity.
   */
  public void updateAnimationSpeed() {
    if (getSprite() != null) {
      // calculate the relative animation speed of the entity
      float animationSpeed = getLocalVelocity().mag() / (walkSpeed * 8.0f);
      // cap the animation speed to a certain value
      animationSpeed = Math.min(animationSpeed, 2.0f);
      // scale the speed by an exponential factor to give the speed up a more gradual change.
      animationSpeed = (float) Math.pow(animationSpeed, 1.5f);
      animationSpeed = Math.max(animationSpeed, 0.5f);
      // set the animation speed
      getSprite().setSpeed(animationSpeed);
    }
  }

  @Override
  public void applyInteractions() {
    super.applyInteractions();
  }

  @Override
  public void preRender() {
    updateSprite();
  }

  /**
   * Using the state of the player and movement flags, set the current sprite. If the player changes
   * movement directions update the movement direction.
   */
  public void updateSprite() {
    if (stateEquals(State.GET_UP)) {
      return;
    }
    if (pose == Pose.SITTING) {
      setState(State.SIT_RIGHT);
    } else if (pose == Pose.SITTING_DESK) {
      setState(State.SIT_RIGHT_DESK);
    } else if (pose == Pose.LYING_DOWN) {
      setState(State.LYING_DOWN);
    } else if (pose == Pose.WALKING) {
      if (!oldFacing.equals(facing) || lastPose != Pose.WALKING) {
        if (facing.equals(Vector.up)) {
          setState(State.WALK_UP);
        } else if (facing.equals(Vector.down)) {
          setState(State.WALK_DOWN);
        } else if (facing.equals(Vector.left)) {
          setState(State.WALK_LEFT);
        } else if (facing.equals(Vector.right)) {
          setState(State.WALK_RIGHT);
        }
      }
    } else if (pose == Pose.STANDING) {
      if (!oldFacing.equals(facing) || lastPose != Pose.STANDING) {
        if (facing.equals(Vector.up)) {
          setState(State.UP);
        } else if (facing.equals(Vector.down)) {
          setState(State.DOWN);
        } else if (facing.equals(Vector.left)) {
          setState(State.LEFT);
        } else {
          setState(State.RIGHT);
        }
      }
    }
  }

  /**
   * Selector constructor that takes in an entity and creates a selection area based on its shape.
   * <p>
   * The selectors are defined by a square that's the average of both side lengths. The selectors
   * side lengths are then capped by the entities respective side lengths to prevent the selector
   * from extending past the upper and lower bounds of the entity.
   * <p>
   * <pre>{@code
   *   Entity        Selector (Stage 1)   Selector (Final)
   *                       ______
   *  ________            |      |            ______
   * |        |           |      |           |      |
   * |________|     +     |______|     =     |______|
   *
   * length: 4            length: 3          length: 3
   * width:  2            width:  3          width:  2
   * }</pre>
   *
   */

  @Override
  public void processSelection(EntityHolder entityHolder) {
    super.processSelection(entityHolder);

    BoundingBox bb = getBoundingBox();
    float height = bb.getHeight();
    float width = bb.getWidth();

    if (isFacing(Vector.up) || isFacing(Vector.down)) {
      interactArea.setPos(getFacing().scalei(height / 2 + interactArea.getBB().getHeight() / 2));
    } else {
      interactArea.setPos(getFacing().scalei(width / 2 + interactArea.getBB().getWidth() / 2));
    }

    interactArea.collideThisTick();
  }

  /**
   * Set the direction vector of the player
   *
   * @param v the direction vector
   */
  public void setDirection(Vector v) {
    if (v == null) {
      System.out.println("Direction cannot be set to null");
    } else {
      facing = v.clone();
    }
  }

  ArrayList<ItemEntity> items = new ArrayList<>();
  ArrayList<ItemEntity> held = new ArrayList<>();

  public boolean useItem(int hand) {
    if (held.size() > hand) {
      return held.get(hand).use(this);
    }
    return false;
  }

  /**
   * An enumeration to represent the current pose of the player.
   */
  public enum Pose {
    SITTING, SITTING_DESK, STANDING, LYING_DOWN, WALKING
  }
}
