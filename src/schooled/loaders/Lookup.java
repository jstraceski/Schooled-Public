package schooled.loaders;

import java.awt.font.GlyphMetrics;
import java.util.HashMap;
import schooled.Game;

/**
 * A wrapper for a hash-map that contains StringObjects and their name Id's.
 * <p></p>
 * Program written by Joseph Straceski
 * <p>
 * contact: straceski.joseph@gmail.com, https://github.com/Crepox
 */
public class Lookup {

  private HashMap<String, ObjData> data = new HashMap<>();
  private HashMap<String, Object> other = new HashMap<>();


  /**
   * Puts a string object in the lookup table.
   *
   * @param name name Id of the string object
   * @param so string object
   * @return string object
   */
  public ObjData put(String name, ObjData so) {
    return data.put(name, so);
  }

  public void put(String name, Object so) {
    other.put(name, so);
  }

  /**
   * Checks if the lookup table has a certain name Id.
   *
   * @param name the name Id
   * @return true if the lookup contains the name Id false if not
   */
  public boolean containsKey(String name) {
    return data.containsKey(name) || other.containsKey(name);
  }

  /**
   * Gets a string object from the lookup table.
   *
   * @param name the name Id of the string object
   * @return the string object
   */
  public Object get(String name) throws RuntimeException{
    Object obj = other.get(name);
    if (obj == null){
      if (data.containsKey(name)) {
        return data.get(name).getObj();
      } else {
        throw new RuntimeException("Object with name <" + name + "> not in lookup table.");
      }
    }
    return obj;
  }

  @Override
  public String toString() {
    return Game.toStr(data) + "\n" + Game.toStr(other);
  }
}
