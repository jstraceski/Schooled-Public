package schooled.entities;

import java.io.Serializable;

/**
 * A list of general entity states.
 * <p></p>
 * Program written by Joseph Straceski
 * <p>
 * contact: straceski.joseph@gmail.com, https://github.com/Crepox
 */
public enum State implements Serializable {
  ON, STARTING, OFF, STOPPING, BROKEN, DEFAULT, IN, OUT,
  UP, DOWN, LEFT, RIGHT, RIGHT_RUNNING, WALK_UP, WALK_DOWN, WALK_LEFT, WALK_RIGHT, LYING_DOWN,
  SITTING, SITTING_DESK, SIT_RIGHT, SIT_RIGHT_DESK, SIT_RIGHT_DESK_VAR,
  SIT_LEFT, SIT_LEFT_DESK, SIT_LEFT_DESK_VAR,
  ERROR,
  HOP,
  SIT_UP_DESK, SIT_UP_DESK_VAR,
  LEFT_LOOP, LEFT_STARTING, LEFT_STOPPING,
  PUSHED_IN, PULLED_OUT,
  LYING_DOWN_PILLOW, FACE_DOWN, DOWN_TO_KNEEL,
  GET_UP, KNEEL, KNEEL_TO_STAND, SIT_UP,
  NO_PILLOW;

  /**
   * Shortcut of getting an enumeration from a string.
   *
   * @param s the enumeration's name
   * @return the enumerations value
   */
  public static State get(String s) {
    return State.valueOf(s.toUpperCase());
  }
}
