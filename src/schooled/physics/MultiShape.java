/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package schooled.physics;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import schooled.Game;
import schooled.entities.State;

/**
 * A list of Shapes that describe a combined shape.
 * <p>
 * Contains a list of vectors that describe the relative positions of all of the shapes.
 * <p></p>
 * Program written by Joseph Straceski
 * <p>
 * contact: straceski.joseph@gmail.com, https://github.com/Crepox
 */
public class MultiShape implements Shape {

  Color debugColor = null;
  BoundingBox box = null;

  @Override
  public void setDebugColor(Color color) {
    debugColor = color;
  }

  @Override
  public Color getDebugColor() {
    return debugColor;
  }

  @Override
  public void update(float t) {
    for (Shape shape : shapes) {
      shape.update(t);
    }
  }

  @Override
  public void setState(State state) {
    for (Shape shape : shapes) {
      shape.setState(state);
    }
  }

  @Override
  public void reset() {
    for (Shape shape : shapes) {
      shape.reset();
    }
  }

  public MultiShape setClone(MultiShape multiShape) {
    if (shapes != null) {
      ArrayList<Shape> nShapeList = new ArrayList<>();
      ArrayList<Vector> nShiftList = new ArrayList<>();

      for (Shape shape : shapes) {
        nShapeList.add(shape.clone());
      }

      for (Vector shift : shifts) {
        nShiftList.add(shift.clone());
      }

      multiShape.shapes = nShapeList;
      multiShape.shifts = nShiftList;
    }

    multiShape.debugColor = debugColor;
    if (box != null) {
      multiShape.box = box.clone();
    }

    return multiShape;
  }

  @Override
  public BoundingBox getBoundingBox() {
    if (box == null) {
      box = new BoundingBox(this);
    }
    return box;
  }

  // MultiShape data
  private ArrayList<Shape> shapes;
  private ArrayList<Vector> shifts;

  public MultiShape() {
    shapes = new ArrayList<>();
    shifts = new ArrayList<>();
  }

  /**
   * Clone the current MultiShape. Creates clones of the shape list and shift list.
   *
   * @return MultiShape clone
   */
  @Override
  public MultiShape clone(){
    MultiShape s = new MultiShape();
    return setClone(s);
  }

  /**
   * Number of shapes in the MultiShape.
   *
   * @return size of the shape list
   */
  public int size() {
    return shapes.size();
  }

  /**
   * Add a shift shape pair. Add add a shape to the shape-list and its corresponding position or
   * shift vector to the vector-list.
   *
   * @param shape  shape to add
   * @param vector shift to add
   */
  public void add(Shape shape, Vector vector) {
    shapes.add(shape);
    shifts.add(vector);
    box = null;
  }

  /**
   * Add a shift to all of the shape positions in the larger multi-shape.
   *
   * @param vector shift to add
   */
  public void shiftAll(Vector vector) {
    shifts.forEach(listVector -> listVector.add(vector));
    box = null;
  }

  /**
   * Remove the Shape from the MultiShape. Removes the Shape and its corresponding shift position.
   *
   * @param shape Shape to remove
   */
  public int remove(Shape shape) {
    int i = shapes.indexOf(shape);
    if (i > 0) {
      shapes.remove(i);
      shifts.remove(i);
      box = null;
    }
    return i;
  }

  /**
   * Find if a shape Shape is contained in the list of shapes.
   *
   * @param shape shape to search for
   * @return if the shape is in the shape-list return true otherwise, false
   */
  public boolean contains(Shape shape) {
    return shapes.contains(shape);
  }

  /**
   * Get shape at index i in the shape-list.
   *
   * @param i index i
   * @return shape at index i
   */
  public Shape getShape(int i) {
    return shapes.get(i);
  }

  /**
   * Get shift at index i in the shape-list.
   *
   * @param i index i
   * @return shift at index i
   */
  public Vector getShift(int i) {
    return shifts.get(i);
  }

  @Override
  public String toString() {
    return "MultiShape[" + Game.toStr(shifts) + " " + Game.toStr(shapes) + "]";
  }
}
