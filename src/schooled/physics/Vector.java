package schooled.physics;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.Serializable;
import schooled.Game;
import schooled.menu.Origin;
import schooled.visuals.sprite.Sprite;

/**
 * Object to describe a vector and commonly used vector-math operations.
 * <p>
 * Stores x and y values as Floats, with (0, 0) default values respectively. Also contains a name
 * string for debugging purposes.
 * <p></p>
 * Program written by Joseph Straceski
 * <p>
 * contact: straceski.joseph@gmail.com, https://github.com/Crepox
 */
public class Vector implements Serializable {

  // static unit vectors
  public static final Vector zero = new Vector(0, 0);
  public static final Vector up = new Vector(0, -1);
  public static final Vector down = new Vector(0, 1);
  public static final Vector left = new Vector(-1, 0);
  public static final Vector right = new Vector(1, 0);

  private float x, y;
  private String name;

  // <editor-fold defaultstate="collapsed" desc="Static Methods">

  // <editor-fold defaultstate="collapsed" desc="Production Methods">

  /**
   * Calculate the magnitude of vector a.
   * <p>
   * Uses Math.sqrt and truncates to a float.
   *
   * @param a vector a
   * @return magnitude
   */
  public static float mag(Vector a) {
    return (float) Math.sqrt(a.x * a.x + a.y * a.y);
  }

  /**
   * Calculate the squared magnitude of vector a.
   *
   * @param a vector a
   * @return squared magnitude
   */
  public static float magSqr(Vector a) {
    return a.x * a.x + a.y * a.y;
  }

  /**
   * Calculate the dot product of vectors a and b.
   * <p>
   * f(a, b) = a.x * b.y + a.y * b.x
   *
   * @param a vector a
   * @param b vector b
   * @return dot product
   */
  public static float dot(Vector a, Vector b) {
    return a.x * b.x + a.y * b.y;
  }

  /**
   * Calculate the perpendicular dot product of vectors a and b.
   * <p>
   * f(a, b) = a.y * b.x - a.x * b.y
   *
   * @param a vector a
   * @param b vector b
   * @return dot product
   */
  public static float pdot(Vector a, Vector b) {
    return a.y * b.x - a.x * b.y;
  }

  /**
   * Find the cross product of vectors a and b.
   * <p>
   * f(a, b) = a.x * b.y - a.y * b.x
   *
   * @param a input vector a
   * @param b input vector b
   * @return cross product
   */
  public static float cross(Vector a, Vector b) {
    return a.x * b.y - a.y * b.x;
  }

  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Modification Methods">

  /**
   * Negate vector a.
   * <p>
   * x = -x, y = -y
   *
   * @param a vector a
   * @return negated vector
   */
  public static Vector negate(Vector a) {
    return new Vector(-a.x, -a.y);
  }

  /**
   * Add vectors a and b together.
   * <p>
   * x = a.x + b.x, y = a.y + b.y
   *
   * @param a input vector a
   * @param b input vector b
   * @return summation vector
   */
  public static Vector add(Vector a, Vector b) {
    if (b == null) {
      return new Vector(a);
    } else if (a == null) {
      return new Vector(b);
    }

    return add(a, b.getX(), b.getY());
  }

  /**
   * Add vectors a and b together.
   * <p>
   * x = a.x + b.x, y = a.y + b.y
   *
   * @param a input vector a
   * @param x input b.x
   * @param y input b.y
   * @return summation vector
   */
  public static Vector add(Vector a, float x, float y) {
    if (a == null) {
      return new Vector(x, y);
    }

    return new Vector(a.x + x, a.y + y);
  }

  /**
   * Subtract vector b from a.
   * <p>
   * x = a.x - b.x, y = a.y - b.y
   *
   * @param a vector a
   * @param b vector b
   * @return difference vector
   */
  public static Vector sub(Vector a, Vector b) {
    if (b == null) {
      return new Vector(a);
    } else if (a == null) {
      return (new Vector(b)).negatei();
    }

    return new Vector(a.x - b.x, a.y - b.y);
  }

  /**
   * Scale vector a by scalar b.
   * <p>
   * x = a.x * b, y = a.y * b
   *
   * @param a vector
   * @param b scalar
   * @return scaled vector
   */
  public static Vector scale(Vector a, float b) {
    return new Vector(a.x * b, a.y * b);
  }

  /**
   * Scale vector a by vector b.
   * <p>
   * x = a.x * b.x, y = a.y * b.y
   *
   * @param a vector a
   * @param b vector b
   * @return scaled vector
   */
  public static Vector scale(Vector a, Vector b) {
    return new Vector(a.x * b.x, a.y * b.y);
  }

  /**
   * Add vector b scaled by scalar c to vector a.
   * <p>
   * x = a.x + b.x * c, y = a.y + b.y * c
   *
   * @param a vector a
   * @param b vector b
   * @param c scalar c
   * @return result vector
   */
  public static Vector addScaled(Vector a, Vector b, float c) {
    return add(a, scale(b, c));
  }

  /**
   * Invert vector a.
   * <p>
   * x = 1/x y = 1/y
   * <p>
   * Invert a vectors components by replacing them with one over the given value. If any of the
   * components are 0, do nothing.
   *
   * @param a vector a
   * @return inverted vector
   */
  public static Vector invert(Vector a) {
    return new Vector(a.x == 0 ? 0 : (1 / a.x), a.y == 0 ? 0 : (1 / a.y));
  }

  /**
   * Find the perpendicular vector made by the difference between vectors a and b.
   * <p>
   * x = -(a.y - b.y), y = (a.x - b.x)
   * <p>
   * One use case for this function would be if a and b referred to points in space, this function
   * will produce a perpendicular vector to the line from point b to point a. The vector is situated
   * 90 degrees counter-clockwise from the difference vector.
   *
   * @param a vector a
   * @param b vector b
   * @return perpendicular vector
   */
  public static Vector perp(Vector a, Vector b) {
    return new Vector(-(a.y - b.y), (a.x - b.x));
  }

  /**
   * Calculate a vector perpendicular to vector a.
   * <p>
   * x = -a.y, y = a.x
   * <p>
   * The perpendicular vector is situated 90 degrees counter-clockwise from vector a.
   *
   * @param a vector a
   * @return perpendicular vector
   */
  public static Vector perp(Vector a) {
    return perp(a, Vector.zero);
  }

  /**
   * Divide vector a by vector b.
   * <p>
   * x = a.x / b.x, y = a.y / b.y
   *
   * @param a vector a
   * @param b vector b
   * @return divided vector
   */
  public static Vector div(Vector a, Vector b) {
    return new Vector(a.x / b.x, a.y / b.y);
  }

  /**
   * Calculate the unit vector of vector a.
   * <p>
   * Unit vectors are vectors of length 1 with the same value ratio as the original vector.
   *
   * @param a vector a
   * @return unit vector
   */
  public static Vector normal(Vector a) {
    return normal(a, mag(a));
  }

  /**
   * Calculate the unit vector of vector a.
   * <p>
   * Unit vectors are vectors of length 1 with the same value ratio as the original vector.
   *
   * @param a vector a
   * @return unit vector
   */
  public static Vector normal(Vector a, float magnitude) {
    if (magnitude == 0) {
      return new Vector(a);
    }
    return new Vector(scale(a, 1.0f / magnitude));
  }

  /**
   * Rotate the Vector a by radians rad
   * @param a Vector
   * @param rad angle
   * @return rotated Vector
   */
  public static Vector rot(Vector a, float rad) {
    return new Vector(a.getX() * (float) Math.cos(rad) - a.getY() * (float) Math.sin(rad),
        a.getX() * (float) Math.sin(rad) + a.getY() * (float) Math.cos(rad));
  }

  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="General Methods">

  /**
   * Create a new vector with the smallest x and y value of vector a and vector b.
   *
   * @param a vector a
   * @param b vector b
   * @return minimized vector
   */
  public static Vector min(Vector a, Vector b) {
    return new Vector(Math.min(a.getX(), b.getX()), Math.min(a.getY(), b.getY()));
  }

  /**
   * Create a new vector with the largest x and y value of vector a and vector b.
   *
   * @param a vector a
   * @param b vector b
   * @return maximized vector
   */
  public static Vector max(Vector a, Vector b) {
    return new Vector(Math.max(a.getX(), b.getX()), Math.max(a.getY(), b.getY()));
  }

  /**
   * Find if vector a equals vector b.
   *
   * @param a vector a
   * @param b vector b
   * @return if vector a equals vector b return true, otherwise false
   */
  public static boolean equals(Vector a, Vector b) {
    return (a.x == b.x) && (a.y == b.y);
  }

  /**
   * Find the direction vector closest in angle to vector v.
   *
   * @param v vector v
   * @return direction vector
   */
  public static Vector getDirectionVector(Vector v) {
    if (v.equals(new Vector(0.0f, 0.0f))) {
      return null;
    }

    float up = v.dot(Vector.up);
    float down = v.dot(Vector.down);
    float left = v.dot(Vector.left);
    float right = v.dot(Vector.right);
    if (up > down && up > left && up > right) {
      return Vector.up;
    } else if (down > left && down > right) {
      return Vector.down;
    } else if (left > right) {
      return Vector.left;
    } else {
      return Vector.right;
    }
  }

  // </editor-fold>

  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Object Methods">

  // <editor-fold defaultstate="collapsed" desc="Constructors">

  /**
   * Empty constructor. Sets the x and y value to 0,0.
   */
  public Vector() {
    x = 0;
    y = 0;
  }

  /**
   * Create a vector from two doubles.
   *
   * @param x x value
   * @param y y value
   */
  public Vector(double x, double y) {
    this.x = (float) x;
    this.y = (float) y;
  }

  /**
   * Create a vector from two floats.
   *
   * @param x x value
   * @param y y value
   */
  public Vector(float x, float y) {
    if (Float.isNaN(x) || Float.isNaN(y)) {
      Game.log(Thread.currentThread().getStackTrace());
      System.exit(0);
    }

    this.x = x;
    this.y = y;
  }


  /**
   * Creates a new Vector from a pre-existing one.
   * <p>
   * Copies the input's x and y values to create an unlinked clone of Vector v.
   *
   * @param v input vector
   */
  public Vector(Vector v) {
    if (v == null) {
      this.x = 0;
      this.y = 0;
    } else {
      this.x = v.x;
      this.y = v.y;
      this.name = v.name;
    }
  }

  /**
   * Converts a Sprite's width and height to a Vector.
   * <p>
   * Width is set to the x value and Height is set to the y value.
   *
   * @param i input Sprite
   */
  public Vector(Sprite i) {
    x = i.getWidth();
    y = i.getHeight();
  }

  /**
   * Create a vector from a java.awt.Component.
   * <p>
   * The Component's width is the x value and the Component's height is the y.
   *
   * @param c java.awt.Component
   */
  public Vector(Component c) {
    this.x = c.getWidth();
    this.y = c.getHeight();
  }

  /**
   * Create a vector from a java.awt.Dimension.
   * <p>
   * The Dimension's width is the x value and the Dimension's height is the y.
   *
   * @param d java.awt.Dimension
   */
  public Vector(Dimension d) {
    this.x = (float) d.getWidth();
    this.y = (float) d.getHeight();
  }

  /**
   * Create a vector from a java.awt.image.BufferedImage.
   * <p>
   * The BufferedImage's width is the x value and the BufferedImage's height is the y.
   *
   * @param i java.awt.image.BufferedImage
   */
  public Vector(BufferedImage i) {
    x = i.getWidth();
    y = i.getHeight();
  }

  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="General Methods">

  @Override
  public Vector clone() {
    return new Vector(x, y);
  }

  @Override
  public String toString() {
    return "(" + x + ", " + y + ")";
  }

  /**
   * Find if vector a is equal to this.
   * <p>
   * Works with null values.
   *
   * @param a vector a
   * @return if vector a equals this return true, otherwise false
   */
  public boolean equals(Vector a) {
    return a != null && (x == a.x) && (y == a.y);
  }

  /**
   * Calculate if vector a is within distance f from this.
   * <p>
   * Works with null values.
   *
   * @param a vector a
   * @param f distance f
   * @return
   */
  public boolean roughEquals(Vector a, float f) {
    return a != null && (((x - a.x) * (x - a.x)) + ((y - a.y) * (y - a.y))) < f * f;
  }

  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Getters and Setters">

  public float getX() {
    return x;
  }

  public void setX(float x) {
    this.x = x;
  }

  public float getY() {
    return y;
  }

  public void setY(float y) {
    this.y = y;
  }

  /**
   * Get the value of x rounded by Math.floor().
   * <p>
   * This is the fastest rounding method.
   *
   * @return rounded value
   */
  public int getXi() {
    return (int) Math.floor(x + 0.5f);
  }

  /**
   * Get the value of x rounded by Math.round().
   *
   * @return rounded value
   */
  public int getXi2() {
    return Math.round(x);
  }

  /**
   * Get the value of x rounded by Math.floor().
   * <p>
   * This is the fastest rounding method.
   *
   * @return rounded value
   */
  public int getYi() {
    return (int) Math.floor(y + 0.5f);
  }

  /**
   * Get the value of y rounded by Math.round().
   *
   * @return rounded value
   */
  public int getYi2() {
    return Math.round(y);
  }

  /**
   * Set the x and y values to the x and y values of vector v.
   *
   * @param v vector v
   */
  public void set(Vector v) {
    setX(v.getX());
    setY(v.getY());
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Production Methods">

  /**
   * Calculate the magnitude.
   *
   * @return magnitude
   */
  public float mag() {
    return mag(this);
  }

  /**
   * Calculate the squared magnitude.
   *
   * @return magnitude
   */
  public float magSqr() {
    return magSqr(this);
  }

  /**
   * Calculate the dot product of this and vector a.
   * <p>
   * f(a, b) = x * b.y + y * b.x
   *
   * @param a vector a
   * @return dot product
   */
  public float dot(Vector a) {
    return dot(this, a);
  }

  /**
   * Calculate the dot product of this and vector a.
   * <p>
   * f(a, b) = x * b.y + y * b.x
   *
   * @param a vector a
   * @return dot product
   */
  public float pdot(Vector a) {
    return pdot(this, a);
  }

  /**
   * Calculate the cross product of this and vector a.
   * <p>
   * f(a, b) = x * b.y - y * b.x
   *
   * @param a vector a
   * @return cross product
   */
  public float cross(Vector a) {
    return cross(this, a);
  }

  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Modification Methods">

  /**
   * Negate this vector.
   * <p>
   * x = -x, y = -y
   */
  public void negate() {
    x = -x;
    y = -y;
  }

  /**
   * Add vector a to this.
   * <p>
   * x = x + a.x, y = y + a.y
   *
   * @param a vector a
   */
  public Vector add(Vector a) {
    x += a.x;
    y += a.y;
    return this;
  }

  /**
   * Subtract vector a from this.
   * <p>
   * x = x - a.x, y = y - a.y
   *
   * @param a vector a
   */
  public void sub(Vector a) {
    x -= a.x;
    y -= a.y;
  }

  /**
   * Scale this by scalar a.
   * <p>
   * x = x * a, y = y * a
   *
   * @param a scalar
   */
  public void scale(float a) {
    x *= a;
    y *= a;
  }

  /**
   * Multiply this by vector a.
   * <p>
   * x = x * a.x, y = y * a.y
   *
   * @param a vector a
   */
  public void scale(Vector a) {
    x *= a.x;
    y *= a.y;
  }

  /**
   * Add vector v multiplied by scalar a to this.
   * <p>
   * x = x + v.x * a, y = y + v.y * a
   *
   * @param v vector v
   * @param a scalar a
   */
  public void addScaled(Vector v, float a) {
    add(scale(v, a));
  }

  /**
   * Invert this vector.
   * <p>
   * x = 1/x, y = 1/y
   * <p>
   * If any components of the vector are 0 keep it as 0 instead.
   */
  public void invert() {
    if (x != 0) {
      x = 1 / x;
    }
    if (y != 0) {
      y = 1 / y;
    }
  }

  /**
   * Make this vector a unit vector.
   */
  public void normalize() {
    float a = mag();
    if (a != 0) {
      scale(1 / mag());
    }
  }

  /**
   * Round this vector.
   * <p>
   * Uses getXi() and getYi().
   */
  public void round() {
    x = getXi();
    y = getYi();
  }


  /**
   * Rotate this vector by rad radians.
   * @param rad
   */
  public void rot(float rad) {
    float xx = x * (float) Math.cos(rad) - y * (float) Math.sin(rad);
    float yy = x * (float) Math.sin(rad) - y * (float) Math.cos(rad);

    x = xx;
    y = yy;
  }

  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Instance Creation Methods">

  /**
   * Instance version of {@link Vector#negate()}.
   *
   * @return negated vector
   */
  public Vector negatei() {
    return Vector.negate(this);
  }

  /**
   * Instance version of {@link Vector#add(Vector a)}.
   *
   * @return added vector
   */
  public Vector addi(float x, float y) {
    return Vector.add(this, x, y);
  }

  /**
   * Instance version of {@link Vector#add(Vector a)}.
   *
   * @return added vector
   */
  public Vector addi(Vector a) {
    return Vector.add(this, a);
  }

  /**
   * Instance version of {@link Vector#sub(Vector a)}.
   *
   * @return subtracted vector
   */
  public Vector subi(Vector a) {
    return Vector.sub(this, a);
  }

  /**
   * Instance version of {@link Vector#scale(float a)}.
   *
   * @return scaled vector
   */
  public Vector scalei(float a) {
    return Vector.scale(this, a);
  }

  /**
   * Instance version of {@link Vector#scale(Vector a)}.
   *
   * @return scaled vector
   */
  public Vector scalei(Vector a) {
    return Vector.scale(this, a);
  }

  /**
   * Instance version of {@link Vector#addScaled(Vector v, float a)}.
   *
   * @return summed vector
   */
  public Vector addScaledi(Vector v, float a) {
    return Vector.addScaled(this, v, a);
  }

  /**
   * Instance version of {@link Vector#invert()}.
   *
   * @return inverted vector
   */
  public Vector inverti() {
    return Vector.invert(this);
  }

  /**
   * Instance version of {@link Vector#normalize()}.
   *
   * @return normalized vector
   */
  public Vector normalizei() {
    return Vector.normal(this);
  }

  /**
   * Instance version of {@link Vector#normalize()}.
   *
   * @return normalized vector
   */
  public Vector normalizei(float magnitude) {
    return Vector.normal(this, magnitude);
  }

  /**
   * Instance version of {@link Vector#round()}.
   *
   * @return rounded vector
   */
  public Vector roundi() {
    return new Vector(getXi(), getYi());
  }

  /**
   * Instance version of {@link Vector#perp(Vector v)} with this as vector v.
   *
   * @return perpendicular vector
   */
  public Vector perpi() {
    return perp(this);
  }

  /**
   * Instance version of {@link Vector#perp(Vector a, Vector b)} with this as vector a.
   *
   * @return perpendicular vector 
   */
  public Vector perpi(Vector v) {
    return perp(this, v);
  }

  /**
   * Instance version of {@link Vector#div(Vector a, Vector b)} with this as vector a.
   *
   * @return divided vector
   */
  public Vector divi(Vector v) {
    return div(this, v);
  }

  /**
   * Instance version of {@link Vector#rot(Vector, float)} with this as vector a.
   *
   * @return rotated vector
   */
  public Vector roti(float f) {
    return rot(this, f);
  }

  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Special Functions"
  /**
   * Offset the location of an box object by its size and origin
   * <p>
   * The origin position and the origin enum work in unison in this method. The origin position is a
   * position relative to the center of a shape that defines where the shape's raw
   * position references itself. Imagine if your latitude and longitude was always defined as where
   * your nose is located. Your nose would be your origin and all your cells would define their
   * location as relative to your nose. An origin position of (10, 10) and an input position of
   * (100, 100) will draw the shape centered at (110, 110) as if it were being drawn at (100, 100)
   * from a point (10, 10) relative to the center of the shape. The origin enum is a way to add an
   * origin position relative to the size of the shape. The origin enum calculation is added to
   * the origin position.
   *
   * @param pos object position
   * @param size width and height of the object or rectangle that encloses the object
   * @param origin relative origin
   * @param oPos custom origin position, can be null
   * @return relative drawing position
   */
  public static Vector originShift(Vector pos, Vector size, Origin origin, Vector oPos, float scale) {
    if (oPos != null) {
      pos = pos.addi(oPos);
    }

    Vector hSize = size.scalei(0.5f);

    switch (origin) {
      case CENTER:
        return pos.addi(Vector.zero);
      case TOP_LEFT:
        return pos.addi(hSize);
      case TOP_RIGHT:
        return pos.addi(new Vector(-hSize.getX(), hSize.getY()));
      case TOP:
        return pos.addi(new Vector(0, hSize.getY()));
      case LEFT:
        return pos.addi(new Vector(hSize.getX(), 0));
      case RIGHT:
        return pos.addi(new Vector(-hSize.getX(), 0));
      case BOTTOM_LEFT:
        return pos.addi(new Vector(hSize.getX(), -hSize.getY()));
      case BOTTOM:
        return pos.addi(new Vector(0, -hSize.getY()));
      default: // BOTTOM_RIGHT
        return pos.addi(new Vector(-hSize.getX(), -hSize.getY()));
    }
  }
  // </editor-fold>

  // </editor-fold>
}
