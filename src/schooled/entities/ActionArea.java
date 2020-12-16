package schooled.entities;

import schooled.Game;
import schooled.event.Event;
import schooled.physics.Shape;
import schooled.physics.Vector;

/**
 * An area that preforms an event when another object is inside of it.
 * <p></p>
 * Program written by Joseph Straceski
 * <p>
 * contact: straceski.joseph@gmail.com, https://github.com/Crepox
 */
public class ActionArea extends Entity {

  private Event event; // the event to preform

  /**
   * A Constructor for an Action Area.
   * <p>
   * The shape is used to defile the area's bounds. The event is the action that occurs when the
   * area's bounds are broken.
   *
   * @param g        a game instance
   * @param s        the areas shape
   * @param newEvent the action event
   */
  public ActionArea(Game g, Shape s, Event newEvent) {
    super(g, new Vector(0.0f, 0.0f), s, 0.0f);
    event = newEvent;
    setPhysics(false);
  }

  @Override
  public boolean collision(BasicEntity e) {
    if (e instanceof Entity) {
      event.act((Entity) e);
    }
    return true;
  }

  public Event getEvent() {
    return event;
  }

  public void setEvent(Event event) {
    this.event = event;
  }
}
