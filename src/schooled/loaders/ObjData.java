package schooled.loaders;

import java.util.ArrayList;
import schooled.entities.Entity;
import schooled.event.Event;
import schooled.physics.Shape;
import schooled.visuals.sprite.Sprite;

/**
 * Parse-data class that stores an object literal.
 * <p></p>
 * Program written by Joseph Straceski
 * <p>
 * contact: straceski.joseph@gmail.com, https://github.com/Crepox
 */
public class ObjData extends ParseData {

  private Object object = null; // the parsed object

  /**
   * Create a ObjData with an object type
   *
   * @param objType name of the object
   */
  public ObjData(String objType) {
    super(objType);
  }

  /**
   * Create a ObjData with an object type, and object data
   *
   * @param objType name of the object
   */
  public ObjData(String objType, Object object) {
    super(objType);
    this.object = object;
  }

  /**
   * Create a ObjData with an object type, object name, and object data
   *
   * @param objType name of the object
   */
  public ObjData(String objType, String objName, Object object) {
    super(objType);
    setName(objName);
    this.object = object;
  }

  /**
   * Create a ObjData with an object type and data.
   *
   * @param objType object type identifier
   * @param data    a list of extra string data
   */
  public ObjData(String objType, ArrayList<String> data) {
    super(objType, data);
  }

  /**
   * Create a ObjData with a type, name, and data.
   *
   * @param objType data type
   * @param name    lookup name of the data piece
   * @param data    a list of extra string data
   */
  public ObjData(String objType, String name, ArrayList<String> data) {
    super(objType, name, data);
  }

  /**
   * Create a ObjData with a type, name, object, and data.
   *
   * @param objType data type
   * @param name    lookup name of the data piece
   * @param object  java object representation of the ObjData
   * @param data    a list of extra string data
   */
  public ObjData(String objType, String name, Object object, ArrayList<String> data) {
    super(objType, name, data);
    this.object = object;
  }

  @Override
  public boolean isObj() {
    return true;
  }

  @Override
  public boolean isType(String objectType) {
    return checkType(objectType) || checkObj(objectType);
  }

  /**
   * Check the objectType String against the manually set dataType. Splits up some labels into
   * multiple strings.
   *
   * @param objectType string to check against
   * @return if the strings are equal or not
   */
  private boolean checkType(String objectType) {
    if (objectType.equals("number")) {
      return (getDataType().equalsIgnoreCase("float")
          || getDataType().equalsIgnoreCase("integer"));
    }
    return getDataType().equalsIgnoreCase(objectType);
  }

  /**
   * Check the objectType String against the the class name of the Object. Splits up some labels
   * into multiple objects.
   *
   * @param objectType string to check against
   * @return if the string equals the Object's class name
   */
  private boolean checkObj(String objectType) {
    if (object == null) {
      return false;
    }

    if (objectType.equals("entity")) {
      return object instanceof Entity;
    } else if (objectType.equals("sprite")) {
      return object instanceof Sprite;
    } else if (objectType.equals("shape")) {
      return object instanceof Shape;
    } else if (objectType.equals("number")) {
      return object instanceof Float || object instanceof Integer;
    } else if (objectType.equals("event")) {
      return object instanceof Event;
    }

    return object.toString().equalsIgnoreCase(objectType);
  }

  /**
   * Set the ObjData's object representation.
   *
   * @param obj java object data of the ObjData
   */
  public void setObj(Object obj) {
    this.object = obj;
  }

  /**
   * Get the ObjData's object representation.
   *
   * @return java object data of the ObjData
   */
  public Object getObj() {
    return object;
  }
}
