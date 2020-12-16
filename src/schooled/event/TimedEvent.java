package schooled.event;

/**
 * An Event wrapper that implements a timed function.
 * <p>
 * The TimedEvent is used by testing the difference in time (dt), each tick. If the total time
 * difference (timeDiff) has exceeded the time value (time) act upon the event. If the event has
 * occurred this tick, return true to signal the Event has been preformed.
 * <p></p>
 * Program written by Joseph Straceski
 * <p>
 * contact: straceski.joseph@gmail.com, https://github.com/Crepox
 */
public class TimedEvent {

  private float timeDiff = 0;
  private float time;
  private Event e;

  public TimedEvent(float length, Event e) {
    this.time = length;
    this.e = e;
  }

  /**
   * Add time to the internal clock, and preform the event if the specified time has passed.
   *
   * @param dt time difference to add to the clock
   * @return true if the event occurred, false otherwise.
   */
  public boolean test(float dt) {
    timeDiff += dt;

    if (timeDiff >= time) {
      timeDiff = 0;
      if (e != null) {
        e.act();
      }

      return true;
    }
    return false;
  }

  public float getTime() {
    return time;
  }

  public float getTimeDiff() {
    return timeDiff;
  }

  /**
   * Get the event that that is preformed when time the timer is done.
   *
   * @return event that will be preformed
   */
  public Event getEvent() {
    return e;
  }

}
