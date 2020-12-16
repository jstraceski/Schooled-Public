package schooled.physics;

import java.awt.Color;
import java.io.Serializable;
import schooled.entities.State;

/**
 * Program written by Joseph Straceski.
 *
 * @author Joseph Straceski, web: <https://github.com/Crepox>, e-mail: straceski.joseph@gmail.com
 * <p>
 * A Shape type that implements a Circle.
 */
public class Circle implements Shape, Serializable  {

  // circle data
  private float radius;
  BoundingBox box;

  /**
   * Create a circle with radius r.
   *
   * @param r Circle's radius
   */
  public Circle(float r) {
    radius = r;
    box = new BoundingBox(this);
  }

  @Override
  public BoundingBox getBoundingBox() {
    return box;
  }

  /**
   * Create a clone of this Circle.
   *
   * @return cloned Circle
   */
  @Override
  public Circle clone() {
    return new Circle(radius);
  }


  Color debugColor = null;

  @Override
  public void setDebugColor(Color color) {
    debugColor = color;
  }

  @Override
  public Color getDebugColor() {
    return debugColor;
  }

  @Override
  public void update(float t) {}

  @Override
  public void setState(State state) {}

  @Override
  public void reset() {}

  /**
   * Get  Circle's radius.
   *
   * @return radius in float form
   */
  public float getRadius() {
    return radius;
  }

  /**
   * Set the Circle's radius.
   *
   * @param radius radius in float form
   */
  public void setRadius(float radius) {
    this.radius = radius;
  }

}
