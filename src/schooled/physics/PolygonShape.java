package schooled.physics;


import java.awt.Color;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;
import schooled.entities.State;

/**
 * Shape describing a Polygon.
 * <p>
 * Stores a list of vertices. Has an additional PolygonShape value to store a shape traced from the
 * original. The height and width are calculated on the fly and reset by changing the polygon's
 * structure.
 * <p></p>
 * Program written by Joseph Straceski
 * <p>
 * contact: straceski.joseph@gmail.com, https://github.com/Crepox
 */
public class PolygonShape implements Shape, Serializable {

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
  public void update(float t) {}

  @Override
  public void setState(State state) {}

  @Override
  public void reset() {}

  private final ArrayList<Vector> vertices = new ArrayList<>();
  private PolygonShape traceShape = null;
  private float height = -1, width = -1;

  /**
   * Empty Constructor
   */
  public PolygonShape() {
  }

  /**
   * Creates a square polygon with edge length d.
   *
   * @param d edge length of polygon
   */
  public PolygonShape(float d) {
    this(d, d);
  }

  /**
   * Creates a polygon with a width and height. Vertices are centered around Vector(0,0).
   *
   * @param width  polygon width
   * @param height polygon height
   */
  public PolygonShape(float width, float height) {
    this(width / 2.0f, height / 2.0f, -width / 2.0f, -height / 2.0f);
  }

  /**
   * Creates a polygon based on a axis-aligned rectangle with vertices on points defined by minimum
   * and maximum values.
   *
   * @param xMax x maximum
   * @param yMax y maximum
   * @param xMin x minimum
   * @param yMin y minimum
   */
  public PolygonShape(float xMax, float yMax, float xMin, float yMin) {
    addVertex(new Vector(xMin, yMax));
    addVertex(new Vector(xMax, yMax));
    addVertex(new Vector(xMax, yMin));
    addVertex(new Vector(xMin, yMin));

    width = xMax - xMin;
    height = yMax - yMin;
  }

  /**
   * Create a rectangle described by a size Vector. size.x is the width and size.y is the height.
   *
   * @param size size Vector
   */
  public PolygonShape(Vector size) {
    this(size.getX(), size.getY());
  }

  /**
   * Create a rectangle described by a size Vector and a shift Vector. places a rectangle with width
   * size.x and height size.y shifted in relative space by the pos Vector.
   *
   * @param size size Vector
   * @param pos  shift Vector
   */
  public PolygonShape(Vector size, Vector pos) {
    this(size.getX(), size.getY());
    addShift(pos);
  }

  /**
   * Create a polygon from a list of vertices.
   *
   * @param v list of vertices
   */
  public PolygonShape(ArrayList<Vector> v) {
    addVertices(v);
  }

  /**
   * Create a polygon from a bounding box.
   *
   * @param boundingBox box
   */
  public PolygonShape(BoundingBox boundingBox) {
    this(boundingBox.xMax, boundingBox.yMax, boundingBox.xMin, boundingBox.yMin);
  }

  @Override
  public PolygonShape clone() {
    PolygonShape polygonShape = new PolygonShape(vertices);
    polygonShape.height = height;
    polygonShape.width = width;
    polygonShape.traceShape = traceShape;
    return polygonShape;
  }

  public schooled.physics.BoundingBox getBoundingBox() {
    if (box == null) {
      box = new BoundingBox(this);
    }
    return box;
  }

  /**
   * Add a list of vertex Vectors to the list of Vectors. Resets the width and height.
   *
   * @param v list of vertices
   */
  public void addVertices(ArrayList<Vector> v) {
    for (Vector vector : v) {
      addVertex(vector.clone());
    }
  }

  /**
   * Add a vertex to the list of vertices. Resets the width and height.
   *
   * @param v vertex to add
   */
  public void addVertex(Vector v) {
    vertices.add(v);
    height = -1;
    width = -1;
    box = null;
  }

  /**
   * Add a vertex to the list of vertices with a vector made from x and y.
   *
   * @param x x position
   * @param y y position
   */
  public void addVertex(float x, float y) {
    vertices.add(new Vector(x, y));
  }


  public PolygonShape getTraceShape() {
    return traceShape;
  }

  public void setTraceShape(PolygonShape ps) {
    this.traceShape = ps;
  }

  /**
   * Does the polygon have a traced shape.
   *
   * @return true if the trace shape isn't null otherwise false
   */
  public boolean hasTraceShape() {
    return traceShape != null;
  }


  /**
   * Mirror the polygon's vertices around the y axis.
   */
  public void flipHorizontal() {
    // swaps the relative locations and vertex order to prevent turning the polygon inside out
    for (int i = 0; i < ((int) Math.round(vertices.size() / 2.0)); i++) {
      Vector pointa = new Vector(vertices.get(i));
      pointa.setX(-pointa.getX());

      Vector pointb = new Vector(vertices.get(vertices.size() - i - 1));
      pointb.setX(-pointb.getX());

      vertices.set(vertices.size() - i - 1, pointa);
      vertices.set(i, pointb);
    }
  }

  /**
   * Mirror the polygon's vertices around the x axis.
   */
  public void flipVertical() {
    // swaps the relative locations and vertex order to prevent turning the polygon inside out
    for (int i = 0; i < ((int) Math.round(vertices.size() / 2.0)); i++) {
      Vector pointa = new Vector(vertices.get(i));
      pointa.setY(-pointa.getY());

      Vector pointb = new Vector(vertices.get(vertices.size() - i - 1));
      pointb.setY(-pointb.getY());

      vertices.set(vertices.size() - i - 1, pointa);
      vertices.set(i, pointb);
    }
  }

  /**
   * Add a shift Vector to all vertices.
   *
   * @param shift shift Vector
   */
  public void addShift(Vector shift) {
    for (Vector vert : vertices) {
      vert.add(shift);
    }
  }

  /**
   * Scale the all polygon's vertices by a scalar value.
   *
   * @param scale scalar value
   * @return scaled shape
   */
  public Shape scale(float scale) {
    for (Vector vert : vertices) {
      vert.scale(scale);
    }

    width *= scale;
    height *= scale;
    return this;
  }

  /**
   * Calculate the width and height of the polygon from the vertices.
   */
  public void calculateSize() {
    if (width == -1) {
      float xMin = Float.MAX_VALUE,
          yMin = Float.MAX_VALUE,
          xMax = -1, yMax = -1;

      for (Vector vertex : vertices) {
        xMin = Math.min(xMin, vertex.getX());
        yMin = Math.min(yMin, vertex.getY());
        xMax = Math.min(xMax, vertex.getX());
        yMax = Math.min(yMax, vertex.getY());
      }

      width = xMax - xMin;
      height = yMax - yMin;
    }
  }


  public float getWidth() {

    if (width == -1) {
      calculateSize();
    }

    return width;
  }

  public float getHeight() {

    if (height == -1) {
      calculateSize();
    }

    return height;
  }

  /**
   * Get the vertex at a given index in the list of vertices.
   *
   * @param index index of the vertex
   * @return vertex Vector
   */
  public Vector getVertex(int index) {
    return vertices.get(index);
  }

  /**
   * Get the number of vertices.
   *
   * @return number of vertices
   */
  public int getSize() {
    return vertices.size();
  }

  public void addPoint(int a, int b) {
    addVertex(a, b);
  }

  public String toString() {
    return "Polygon[vertices=" + Arrays.toString(vertices.toArray()) + "]";
  }

  /**
   * Returns a shallow copy of the vertex list.
   *
   * @return copied vertex list
   */
  public ArrayList<Vector> getVertices() {
    return (ArrayList<Vector>) vertices.clone();
  }

  /**
   * Clear all the vertices and size values.
   */
  public void clear() {
    vertices.clear();
    width = -1;
    height = -1;
  }

  /**
   * Returns true if the polygon is valid. A valid polygon is defined as one with more than two
   * vertices.
   */
  public boolean isValid() {
    return getSize() > 2;
  }
}
