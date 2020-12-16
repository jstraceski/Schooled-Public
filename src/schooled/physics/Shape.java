package schooled.physics;

import java.awt.Color;
import schooled.entities.State;

/**
 * Abstract Shape class.
 * <p></p>
 * Program written by <a href="mailto:straceski.joseph@gmail.com">Joseph Straceski</a>
 * <p>
 * web: <a href="https://github.com/Crepox">https://github.com/Crepox</a>
 */
public interface Shape {

  /**
   * Create a shallow copy of the given shape.
   *
   * @return copy of the shape
   */
  Shape clone();

  void setDebugColor(Color color);

  Color getDebugColor();

  void update(float t);

  void setState(State state);

  BoundingBox getBoundingBox();

  void reset();
}
