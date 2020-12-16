package schooled.event;

import schooled.entities.Entity;

/**
 * A structured passable function.
 * <p>
 * An Event is a specialized passable function that can be customized in three diffrent ways. An
 * Event can be created to take in three diffrent types of inputs. Sender and Reciever: EventAB,
 * Sender: EventA, or No Actors: EventNone.
 * <p></p>
 * Program written by Joseph Straceski
 * <p>
 * contact: straceski.joseph@gmail.com, https://github.com/Crepox
 */
public class Event {

  protected EventAB ab = null; // A function that takes in an sender and receiver entity
  protected EventA a = null; // A function that takes in an actor entity
  protected EventNone e = null; // A function that takes in no actors
  private boolean repeat = false; // a flag to store if the Event repeats or not

  public int getLevel() {
    return ab != null ? 3 : a != null ? 2 : e != null ? 1 : 0;
  }

  /**
   * The sender receiver Event constructor.
   *
   * @param ab function that takes in a sender and receiver entity
   */
  public Event(EventAB ab) {
    this.ab = ab;
  }

  /**
   * The sender Event constructor.
   *
   * @param a function that takes in an sender entity
   */
  public Event(EventA a) {
    this.a = a;
  }

  /**
   * The actor-less Event constructor.
   *
   * @param e function with no arguments
   */
  public Event(EventNone e) {
    this.e = e;
  }

  @Override
  public Event clone() {
    Event event = new Event((EventNone) null);
    event.ab = ab;
    event.a = a;
    event.e = e;
    event.repeat = repeat;
    return event;
  }


  /**
   * Add the Event to preform the provided sender receiver function.
   *
   * @param ab function that takes in a sender and receiver entity
   */
  public void add(EventAB ab) {
    this.ab = ab;
  }

  /**
   * Add the Event to preform the provided sender receiver function.
   *
   * @param a function that takes in a sender and receiver entity
   */
  public void add(EventA a) {
    this.a = a;
  }

  /**
   * Add the Event to preform the provided actor-less function.
   *
   * @param e function with no arguments
   */
  public void add(EventNone e) {
    this.e = e;
  }


  /**
   * Set the Event to preform the provided sender receiver function.
   *
   * @param ab function that takes in a sender and receiver entity
   */
  public void set(EventAB ab) {
    this.ab = ab;
    a = null;
    e = null;
  }

  /**
   * Set the Event to preform the provided sender function.
   *
   * @param a function that takes in an sender entity
   */
  public void set(EventA a) {
    this.a = a;
    ab = null;
    e = null;
  }

  /**
   * Set the Event to preform the provided actor-less function.
   *
   * @param e function with no arguments
   */
  public void set(EventNone e) {
    this.e = e;
    ab = null;
    a = null;
  }

  /**
   * Set the event to repeat or not.
   *
   * @param b repeat or not
   */
  public void setRepeat(boolean b) {
    repeat = b;
  }

  /**
   * Should the event repeat.
   *
   * @return does the event repeat
   */
  public boolean isRepeat() {
    return repeat;
  }

  /**
   * Preform the event with a sender and receiver entities as an input.
   *
   * @param sender   sender entity
   * @param receiver receiver entity
   */
  public void act(Entity sender, Entity receiver) {
    try {
      if (ab != null) {
        ab.act(sender, receiver);
        //act(sender);
      } else {
        act(sender);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Preform the event with an sender entity.
   *
   * @param sender the sender entity
   */
  public void act(Entity sender) {
    try {
      if (a != null) {
        a.act(sender);
        //act();
      } else {
        act();
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Preform the event with no entity.
   */
  public void act() {
    try {
      if (e != null) {
        e.act();
      } else {
      System.out.println(" " + ab + " " + a + " " + e);
      throw new Error("Not Implemented");
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
