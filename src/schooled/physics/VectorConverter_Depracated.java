package schooled.physics;

import java.awt.Component;
import java.awt.image.BufferedImage;

/**
 * Deprecated Vector converter.
 * <p></p>
 * Program written by Joseph Straceski
 * <p>
 * contact: straceski.joseph@gmail.com, https://github.com/Crepox
 */
public class VectorConverter_Depracated {

  public static Vector toVector(BufferedImage i) {
    return new Vector(i.getWidth(), i.getHeight());
  }

  public static Vector toVector(Component c) {
    return new Vector(c.getWidth(), c.getHeight());
  }
}
