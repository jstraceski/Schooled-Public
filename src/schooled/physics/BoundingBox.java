package schooled.physics;

import java.awt.Color;
import java.io.Serializable;
import schooled.Game;
import schooled.entities.State;
import schooled.visuals.sprite.Sprite;

/**
 * A Shape type that implements a BoundingBox. This specific implementation is an axis-alligned
 * BoundingBox. The values are stored in floats that represent the point locations of the maximum
 * point and minimum point as: xMax, yMax, xMin and yMin.
 * <p></p>
 * Program written by Joseph Straceski
 * <p>
 * contact: straceski.joseph@gmail.com, https://github.com/Crepox
 */
public class BoundingBox implements Shape, Serializable {

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

  @Override
  public BoundingBox getBoundingBox() {
    return null;
  }

  // bounding box information
  public float xMax = -Float.MAX_VALUE;
  public float yMax = -Float.MAX_VALUE;
  public float xMin = Float.MAX_VALUE;
  public float yMin = Float.MAX_VALUE;

  /**
   * Empty Constructor
   */
  private BoundingBox() {
  }

  public BoundingBox(float xMax, float yMax, float xMin, float yMin) {
    this.xMax = xMax;
    this.yMax = yMax;
    this.xMin = xMin;
    this.yMin = yMin;
  }

  /**
   * Create a BoundingBox from Shape data.
   *
   * @param s shape to convert
   */
  public BoundingBox(Shape s) {
    if (s instanceof Circle) {
      xMax = ((Circle) s).getRadius();
      yMax = ((Circle) s).getRadius();
      xMin = -((Circle) s).getRadius();
      yMin = -((Circle) s).getRadius();
    } else if (s instanceof PolygonShape) {
      for (Vector v : ((PolygonShape) s).getVertices()) {
        xMax = Math.max(v.getX(), xMax);
        xMin = Math.min(v.getX(), xMin);
        yMax = Math.max(v.getY(), yMax);
        yMin = Math.min(v.getY(), yMin);
      }
    } else if (s instanceof MultiShape) {
      for (int index = 0; index < ((MultiShape) s).size(); index++) {
        Shape shape = ((MultiShape) s).getShape(index);
        Vector vector = ((MultiShape) s).getShift(index);
        BoundingBox box = shape.getBoundingBox();

        box = box.addShift(vector);

        xMax = Math.max(box.xMax, xMax);
        xMin = Math.min(box.xMin, xMin);
        yMax = Math.max(box.yMax, yMax);
        yMin = Math.min(box.yMin, yMin);
      }
    }
  }

  public BoundingBox(Vector min, Vector max) {
    this.xMin = min.getX();
    this.yMin = min.getY();

    this.xMax = max.getX();
    this.yMax = max.getY();

  }

  public BoundingBox(Vector vector) {
    this.xMin = 0;
    this.yMin = 0;

    this.xMax = vector.getX();
    this.yMax = vector.getY();
  }

  /**
   * Create a clone of this BoundingBox. The box data is stored in floats and the values are
   * copied.
   *
   * @return BoundingBox clone
   */
  public BoundingBox clone() {
    BoundingBox box = new BoundingBox();
    box.xMax = xMax;
    box.yMax = yMax;
    box.xMin = xMin;
    box.yMin = yMin;
    return box;
  }

  public float getXMax() {
    return xMax;
  }

  public float getYMax() {
    return yMax;
  }

  public float getXMin() {
    return xMin;
  }

  public float getYMin() {
    return yMin;
  }

  /**
   * Get the maximum vector. The maximum x and y values combined.
   *
   * @return maximum vector
   */
  public Vector getMax() {
    return new Vector(xMax, yMax);
  }

  /**
   * Get the minimum vector. The minimum x and y values combined.
   *
   * @return minimum vector
   */
  public Vector getMin() {
    return new Vector(xMin, yMin);
  }

  /**
   * Get the width of the BoundingBox. Using the difference between xMax and xMin calculate the
   * width of the BoundingBox.
   *
   * @return width
   */
  public float getWidth() {
    return xMax - xMin;
  }

  /**
   * Get the height of the BoundingBox. Using the difference between yMax and yMin calculate the
   * height of the BoundingBox.
   *
   * @return height
   */
  public float getHeight() {
    return yMax - yMin;
  }

  /**
   * Shift the BoundingBox by a vector. Add a shifting vector x to all the x values and shifting
   * vector y to the y values.
   */
  public BoundingBox addShift(Vector vector) {
    return new BoundingBox(xMax + vector.getX(), yMax + vector.getY(),
        xMin + vector.getX(), yMin + vector.getY());
  }

  @Override
  public String toString() {
    return "BoundingBox[xMin=" + xMin + ", yMin=" + yMin + ", xMax=" + xMax + ", yMax=" + yMax + "]";
  }
}
