package schooled.physics;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.lwjgl.system.CallbackI.V;
import schooled.Game;
import schooled.entities.State;
import schooled.gameobjects.Stat;

public class StateShape extends MultiShape {

  private HashMap<State, Shape> shapes;
  private HashMap<State, Vector> shifts;
  private State state;

  public StateShape() {
    shapes = new HashMap<>();
    shifts = new HashMap<>();

    reset();
  }

  public void reset() {
    state = State.DEFAULT;
  }

  @Override
  public StateShape clone() {
    StateShape stateShape = new StateShape();
    return setClone(stateShape);
  }


  public StateShape setClone(StateShape stateShape) {
    super.setClone(stateShape);
    HashMap<State, Shape> nShapeList = new HashMap<>();
    HashMap<State, Vector> nShiftList = new HashMap<>();

    for (State state : shapes.keySet()) {
      nShapeList.put(state, shapes.get(state).clone());
    }

    for (State state : shifts.keySet()) {
      nShiftList.put(state, shifts.get(state).clone());
    }

    stateShape.shapes = nShapeList;
    stateShape.shifts = nShiftList;
    stateShape.state = state;
    return stateShape;
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
    Shape shape = getShape();
    if (shape != null) {
      shape.update(t);
    }
  }

  @Override
  public void setState(State state) {
    this.state = state;
  }

  @Override
  public BoundingBox getBoundingBox() {
    Shape shape = getShape();
    if (shape != null) {
      return shape.getBoundingBox();
    }

    return null;
  }

  public void add(Shape shape) {
    add(shape, State.DEFAULT);
  }

  public void add(Shape shape, State state) {
    shapes.put(state, shape);
    shifts.put(state, new Vector(0, 0));
  }

  public void add(Shape shape, Vector vector, State state) {
    shapes.put(state, shape);
    shifts.put(state, vector);
  }

  public int remove(Shape shape) {
    throw new UnsupportedOperationException();
  }

  public int remove(State state) {
    shapes.remove(state);
    shifts.remove(state);

    return state.ordinal();
  }

  @Override
  public Shape getShape(int i) {
    return this.getShape();
  }

  public Shape getShape(State state) {
    return shapes.get(state);
  }

  public Shape getShape() {
    Shape shape = shapes.get(state);

    if (shape == null) {
      shape = shapes.get(State.DEFAULT);
    }

    return shape;
  }

  @Override
  public int size() {
    return getShape() != null ? 1 : 0;
  }

  @Override
  public Vector getShift(int i) {
    return this.getShift();
  }

  public Vector getShift() {
    Vector shift = shifts.get(state);

    if (shift == null) {
      shift = shifts.get(State.DEFAULT);
    }

    return shift;
  }

  public State getState() {
    return state;
  }

  @Override
  public String toString() {

    List<String> strings = shapes.keySet().stream().map((key) -> key + ": " + Game.toStr(shapes.get(key))).collect(Collectors.toList());
    ArrayList<String> strList = new ArrayList<>(strings);
    strList = (ArrayList<String>) strList.stream().map(s -> ("\t" + s)).collect(Collectors.toList());

    ArrayList<String> data = new ArrayList<>(strList);
    Optional<String> dataStr = data.stream().reduce((s, s2) -> s + "\n" + s2);
    return "StateShape[" + dataStr.get() + "]";
  }
}
