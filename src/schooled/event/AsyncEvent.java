package schooled.event;

import schooled.entities.Entity;

public class AsyncEvent extends Event {

  public AsyncEvent(EventAB ab) {
    super(ab);
  }

  public AsyncEvent(EventA a) {
    super(a);
  }

  public AsyncEvent(EventNone e) {
    super(e);
  }

  /**
   * Preform the event with a sender and receiver entities as an input.
   *
   * @param sender   sender entity
   * @param receiver receiver entity
   */
  public void act(Entity sender, Entity receiver) {
    if (ab != null) {
      Thread thread = new Thread(() -> {
        try {
          ab.act(sender, receiver);
        } catch (Exception e) {
          e.printStackTrace();
        }
      });
      thread.start();
    } else {
      act(sender);
    }
  }

  /**
   * Preform the event with an sender entity.
   *
   * @param sender the sender entity
   */
  public void act(Entity sender) {
    if (a != null) {
      Thread thread = new Thread(() -> {
        try {
          a.act(sender);
        } catch (Exception e) {
          e.printStackTrace();
        }
      });
      thread.start();
    } else {
      act();
    }
  }

  /**
   * Preform the event with no entity.
   */
  public void act() {
    if (e != null) {
      Thread thread = new Thread(() -> {
        try {
          e.act();
        } catch (Exception e) {
          e.printStackTrace();
        }
      });
      thread.start();
    } else {
      System.out.println(" " + ab + " " + a + " " + e);
      throw new Error("Not Implemented");
    }
  }
}
