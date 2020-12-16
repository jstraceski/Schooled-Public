package schooled.physics;

import java.awt.Color;
import java.util.ArrayList;
import schooled.entities.State;

public class AnimatedShape extends MultiShape implements Shape {


  private ArrayList<Float> delays = new ArrayList<>();

  private int index;
  private float collector;
  private float speed = 1;
  private boolean loop = true;


  public AnimatedShape() {
    reset();
  }

  public void reset() {
    index = 0;
    collector = 0.0f;
  }

  public AnimatedShape setClone(AnimatedShape multiShape) {
    super.setClone(multiShape);

    multiShape.delays = new ArrayList<>(delays);
    multiShape.index = index;
    multiShape.collector = collector;
    multiShape.speed = speed;
    multiShape.loop = loop;

    return multiShape;
  }

  @Override
  public AnimatedShape clone() {
    AnimatedShape animatedShape = new AnimatedShape();
    return setClone(animatedShape);
  }

  @Override
  public void setDebugColor(Color color) {

  }

  @Override
  public Color getDebugColor() {
    return null;
  }

  @Override
  public void update(float t) {
    animate(t);
  }

  public void animate(float t) {
    collector += t * speed;

    if (collector > delays.get(index)) {
      incrementIndex();
    }
  }

  /**
   * Increase the frame index of the animation by one.
   */
  public void incrementIndex() {
    setIndex(index + 1);
  }

  /**
   * Set the frame index.
   *
   * @param i index
   */
  public void setIndex(int i) {
    if (index + 1 >= delays.size()) {
      if (loop) {
        index = 0;
      } else {
        index = delays.size() - 1;
      }
    } else {
      index = Math.max(i, 0);
    }

    collector = 0;
  }

  @Override
  public void setState(State state) {

  }

  @Override
  public BoundingBox getBoundingBox() {
    Shape shape = getShape();
    if (shape != null) {
      return shape.getBoundingBox();
    }
    return null;
  }


  public void add(Shape shape, Vector vector, float delay) {
    super.add(shape, vector);
    delays.add(delay);
  }


  @Override
  public int remove(Shape shape) {
    int i = super.remove(shape);

    if (i > 0) {
      delays.remove(i);
    }

    return i;
  }

  @Override
  public Shape getShape(int i) {
    return this.getShape();
  }

  public Shape getShape() {
    return super.getShape(index);
  }

  @Override
  public int size() {
    return super.size() > 0 ? 1 : 0;
  }

  @Override
  public Vector getShift(int i) {
    return this.getShift();
  }

  public Vector getShift() {
    return super.getShift(index);
  }
}

