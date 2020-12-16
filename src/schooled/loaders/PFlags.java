package schooled.loaders;

import org.json.JSONObject;

/**
 * A PFlags object. Stores flag objects for parsing data.
 * <p></p>
 * Program written by Joseph Straceski
 * <p>
 * contact: straceski.joseph@gmail.com, https://github.com/Crepox
 */
public class PFlags {

  public ParseData multiLineObj = null; // an object that is added to over multiple lines
  public String string = ""; // a commanded loaded over multiple lines
  public float lineCount = 0; // line index
  public boolean noload = false; // don't load entities into a room
  public String fileName; // name of the file being loaded
  public PairedData endData = new PairedData(); // line end data
  public JSONObject lastObj = null;

}
