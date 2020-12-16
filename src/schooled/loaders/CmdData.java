package schooled.loaders;

import java.util.ArrayList;

/**
 * ParseData wrapper used to differentiate data types.
 * <p></p>
 * Program written by Joseph Straceski
 * <p>
 * contact: straceski.joseph@gmail.com, https://github.com/Crepox
 */
public class CmdData extends ParseData {

  /**
   * Create a CmdData with an object type
   *
   * @param cmdType name of the object
   */
  public CmdData(String cmdType) {
    super(cmdType);
  }

  /**
   * Create a CmdData with an object type and data.
   *
   * @param cmdType object type identifier
   * @param data    a list of extra string data
   */
  public CmdData(String cmdType, ArrayList<String> data) {
    super(cmdType, data);
  }

  /**
   * Create a CmdData with a type, data, and a command flag.
   *
   * @param cmdType data type
   * @param name    lookup name of the data piece
   * @param data    a list of extra string data
   */
  public CmdData(String cmdType, String name, ArrayList<String> data) {
    super(cmdType, name, data);
  }

  public boolean isObj() {
    return false;
  }
}
