package schooled.entities;

import java.util.ArrayList;
import java.util.function.BiFunction;
import schooled.Game;
import schooled.event.Event;
import schooled.physics.Manifold;
import schooled.physics.Shape;
import schooled.physics.Vector;

/**
 * An area that preforms an event when another object is inside of it.
 * <p></p>
 * Program written by Joseph Straceski
 * <p>
 * contact: straceski.joseph@gmail.com, https://github.com/Crepox
 */
public class EntityArea extends Entity {

  private BiFunction<Entity, ArrayList<Manifold>, Boolean> func = null;
  private int turnCollisionOff = 0;
  private boolean playerOnly = false;

  /**
   * A Constructor for an Entity Area.
   * <p>
   * The shape is used to define the area's bounds.
   *
   * @param g        a game instance
   * @param s        the areas shape
   */
  public EntityArea(Game g, Shape s, BiFunction<Entity, ArrayList<Manifold>, Boolean> nFunction) {
    super(g, new Vector(0.0f, 0.0f), s, 0.0f);
    setPhysics(false);
    setVisible(false);
    if (nFunction != null) {
      func = nFunction;
    }
  }

  public void setFunc(BiFunction<Entity, ArrayList<Manifold>, Boolean> func) {
    this.func = func;
  }

  /**
   * A Constructor for an Entity Area.
   * <p>
   * The shape is used to define the area's bounds.
   *
   * @param g        a game instance
   * @param s        the areas shape
   */
  public EntityArea(Game g, Shape s) {
    this(g, s, null);
  }

  /**
   * A Constructor for an Entity Area.
   * <p>
   * The shape is used to define the area's bounds.
   *
   * @param g        a game instance
   */
  public EntityArea(Game g) {
    this(g, null);
  }

  @Override
  public boolean collision(BasicEntity e, ArrayList<Manifold> m) {

    if (((e instanceof Player) || (!playerOnly && e instanceof Entity)) && !isParent(e)) {
      Event interactEvent = getInteractEvent();
      if (interactEvent != null) {
        interactEvent.act((Entity) e);
        return true;
      }

      if (func != null) {
        return func.apply((Entity) e, m);
      }
    }

    return false;
  }

  @Override
  public boolean interact(Entity e) {
    return false;
  }

  public void collideThisTick() {
    turnCollisionOff = 2;
    setCollides(true);
  }

  @Override
  public void updateCycle() {
    super.updateCycle();
    if (turnCollisionOff == 1) {

      setCollides(false);
    }

    if (turnCollisionOff > 0) {
      turnCollisionOff--;
    }
  }

  public void setPlayerOnly(boolean playerOnly) {
    this.playerOnly = playerOnly;
  }
}
