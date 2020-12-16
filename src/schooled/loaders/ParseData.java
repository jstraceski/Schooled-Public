/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package schooled.loaders;

import java.util.ArrayList;
import schooled.Game;

/**
 * An object for storing parsed data.
 * <p></p>
 * Program written by Joseph Straceski
 * <p>
 * contact: straceski.joseph@gmail.com, https://github.com/Crepox
 */
public abstract class ParseData {

  private String dataType; // the name of the command or Object
  private String name; // the name Id of the data
  private ArrayList<String> data; // un-parsed data of the ParseData
  private ArrayList<String> tags; // tags on the ParseData

  /**
   * ParseData constructor. Sets all of the data to clear and all the flags to false.
   */
  public ParseData() {
    data = new ArrayList<String>();
    tags = new ArrayList<String>();
    dataType = "";
    name = "";
  }

  /**
   * Create a ParseData with an data type.
   *
   * @param dataType data type identifier
   */
  public ParseData(String dataType) {
    this();
    this.dataType = dataType;
  }

  /**
   * Create a ParseData with a data type and string data.
   *
   * @param dataType data type identifier
   * @param data     list of string data
   */
  public ParseData(String dataType, ArrayList<String> data) {
    this();
    setDataType(dataType);
    if (!data.isEmpty()) {
      setData(data);
    }
  }

  /**
   * Create a ParseData with a type, name, and data.
   *
   * @param type data type identifier
   * @param name lookup name of the data piece
   * @param data list of extra string data
   */
  public ParseData(String type, String name, ArrayList<String> data) {
    this();
    setDataType(type);
    if (name != null && !name.isEmpty()) {
      setName(name);
    }
    if (data != null && !data.isEmpty()) {
      setData(data);
    }
  }

  /**
   * Get the ParseData object name.
   *
   * @return the object name
   */
  public String getDataType() {
    return dataType;
  }

  /**
   * Set the data type of the ParseData.
   *
   * @param dataType the object name
   */
  public void setDataType(String dataType) {
    this.dataType = dataType;
  }

  /**
   * Get the lookup name of the ParseData.
   *
   * @return lookup name string
   */
  public String getName() {
    return name;
  }

  /**
   * Set the lookup name of the string object.
   *
   * @param name lookup name string
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Does the ParseData have a lookup name.
   *
   * @return if the ParseData has a name return true
   */
  public boolean hasName() {
    return name != null && !name.isEmpty();
  }

  /**
   * Get data string list of the ParseData.
   *
   * @return the list of data strings
   */
  public ArrayList<String> getData() {
    return data;
  }

  /**
   * Get the number of data entries in the ParseData.
   *
   * @return the number of data entries
   */
  public int getSize() {
    return data.size();
  }

  /**
   * Get the ith data string of the ParseData.
   *
   * @param i the index of the data
   * @return the data string
   */
  public String get(int i) {

    if (i >= data.size()) {
      return "";
    }

    return data.get(i);
  }

  /**
   * Set the list of data strings.
   *
   * @param data the data string list
   */
  public void setData(ArrayList<String> data) {
    this.data = data;
  }

  /**
   * Add data strings to the data string list.
   *
   * @param data the data strings
   */
  public void addData(ArrayList<String> data) {
    this.data.addAll(data);
  }

  /**
   * Add a string to the data string list.
   *
   * @param data the data string
   */
  public void addData(String data) {
    this.data.add(data);
  }

  /**
   * Does the ParseData have data.
   *
   * @return if the ParseData has data return true
   */
  public boolean hasData() {
    return data != null && !data.isEmpty();
  }

  /**
   * Does the ParseData have data at the given index.
   *
   * @param index the data index
   * @return if the data exists
   */
  public boolean hasData(int index) {
    return data.size() > index && index >= 0;
  }

  /**
   * Set the tags of the ParseData.
   *
   * @param tags the tag strings
   */
  public void setTags(ArrayList<String> tags) {
    this.tags = tags;
  }


  /**
   * Add a tag to the ParseData.
   *
   * @param tag tag string
   */
  public void addTag(String tag) {
    this.tags.add(tag);
  }

  /**
   * Set the tag data using a list of strings. Automatically makes all tags lowercase.
   *
   * @param tags the tag data
   */
  public void setTagData(String[] tags) {
    this.tags.clear();
    for (String s : tags) {
      addTag(s.toLowerCase());
    }
  }

  public ArrayList<String> getTags() {
    return tags;
  }

  /**
   * Does the ParseData have tags or not.
   *
   * @return if the tag list is empty return false
   */
  public boolean hasTags() {
    return !tags.isEmpty();
  }

  /**
   * Does the ParseData contain the given tag
   *
   * @param tag the tag
   * @return if the string object has the tag
   */
  public boolean hasTag(String tag) {
    return tags.contains(tag.toLowerCase());
  }


  /**
   * Is the ParseData an object
   *
   * @return if the ParseData is a object return true
   */
  public abstract boolean isObj();

  /**
   * Is the ParseData the given type.
   *
   * @param newDataType the ParseData type to check against
   * @return if the parser ParseData type equals the newDataType return true
   */
  public boolean isType(String newDataType) {
    return dataType.toLowerCase().equals(newDataType.toLowerCase());
  }

  /**
   * Parse the ParseData using the string data and string type.
   *
   * @param lookup the parsing lookup table
   * @param game   the game instance
   * @param flags  the parsing flag data
   * @return the parsed object
   */
  public Object parse(Lookup lookup, Game game, PFlags flags) throws Exception {
    return DataLoader.parseParseData(this, lookup, game, flags);
  }

  @Override
  public String toString() {
    String s = (isObj() ? "Obj" : "Cmd") + " : " + dataType;
    if (hasName()) {
      s += ", Name : " + name;
    }
    if (hasData()) {
      s += ", Data : ";
      for (int i = 0; i < data.size(); i++) {
        s += data.get(i);
        if (i < data.size() - 1) {
          s += " | ";
        }
      }
    }
    if (hasTags()) {
      s += ", Tags : ";
      for (int i = 0; i < tags.size(); i++) {
        s += tags.get(i);
        if (i < tags.size() - 1) {
          s += " | ";
        }
      }
    }

    return s;
  }

}
