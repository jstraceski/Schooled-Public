package schooled.event;

import java.util.function.BooleanSupplier;

/**
 * An Object that combines a conditional test and an event.
 * <p>
 * Calling test will test the conditional event. If the test passes, preform the action event.
 * Calling test will also return the result of the conditional event.
 * <p></p>
 * Program written by Joseph Straceski
 * <p>
 * contact: straceski.joseph@gmail.com, https://github.com/Crepox
 */
public class ConditionalEvent {

  private BooleanSupplier bs; // A functional test that returns a boolean
  private Event e; // An event to preform

  /**
   * Basic constructor for a conditional event.
   * <p>
   * When test() is called. It preforms the BooleanSupplier bs and if it is true it preforms the
   * event e. It then returns the result of bs.
   *
   * @param bs conditional event
   * @param e  action event
   */
  public ConditionalEvent(BooleanSupplier bs, Event e) {
    this.bs = bs;
    this.e = e;
  }

  /**
   * Test the boolean supplier.
   * <p>
   * If the boolean supplier returns true, preform the event.
   *
   * @return the result of the boolean supplier
   */
  public boolean test() {
    if (bs == null) {
      throw new RuntimeException("BooleanSupplier is null, ConditionalEvent not set up correctly");
    }

    if (bs.getAsBoolean()) {
      e.act();
      return true;
    }

    return false;
  }

  /**
   * Get the contained BooleanSupplier that is tested in the test() function.
   *
   * @return conditional event
   */
  public BooleanSupplier getBooleanSupplier() {
    return bs;
  }

  /**
   * Get the Event that is preformed after the test function is checked.
   *
   * @return action event
   */
  public Event getEvent() {
    return e;
  }
}
