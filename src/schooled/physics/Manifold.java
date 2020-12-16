package schooled.physics;

import java.util.ArrayList;
import schooled.entities.BasicEntity;
import schooled.entities.Entity;

/**
 * Manifold data type storing entity collision data.
 * <pre>
 * normals: list of collision normals generated from the collision
 * vectors: list of vectors representing the collision depth at each normal.
 * a: entity a
 * b: entity b </pre>
 * Normals are usually pointing from a to b.
 * <p></p>
 * Program written by Joseph Straceski
 * <p>
 * contact: straceski.joseph@gmail.com, https://github.com/Crepox
 */
public class Manifold {

  // manifold data
  public ArrayList<Vector> normals = new ArrayList<>();
  public ArrayList<Vector> vectors = new ArrayList<>();
  public BasicEntity a = null, b = null;
  public int aEulerCount = 0, bEulerCount = 0;

  /**
   * Empty constructor.
   */
  public Manifold() {
  }


  @Override
  public String toString() {
    return "Manifold[a=" + a + ", b=" + b + ", n=" + normals + "]";
  }
}
