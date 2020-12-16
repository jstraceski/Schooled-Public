package schooled.loaders;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;

/**
 * Object for storing multiple different EndData Objects indexed by starting and ending characters.
 * <p>
 * Stores data for character indexes and overall index values. Valid Starts and Ends are instances
 * of the values found when the EndData for the given Character is 0.
 * <p></p>
 * Program written by Joseph Straceski
 * <p>
 * contact: straceski.joseph@gmail.com, https://github.com/Crepox
 */
public class PairedData {

  private HashMap<String, EndData> endPairs = new HashMap<>(); // list of each type of EndData
  int lastValidEnd = -1;      // first Valid end found in the String
  int firstCharIndex = -1;    // first Valid or non-encapsulated index of a given char
  int firstParsedStart = -1;  // first Valid start found in the String

  /**
   * Empty constructor for PairedData.
   * Does nothing.
   */
  public PairedData() {
  }

  /**
   * Set up a PairedData object with endPairs data already set.
   *
   * @param endPairs pre-generated endPairs data
   */
  public PairedData(HashMap<String, EndData> endPairs) {
    this.endPairs = endPairs;
  }

  /**
   * Create a new PairedData object with a set of end Strings.
   *
   * @param endPairStrings set of end Strings
   */
  public PairedData(Set<String> endPairStrings) {
    for (String endPair : endPairStrings) {
      endPairs.put(endPair, new EndData());
    }
  }

  /**
   * Get the Set of EndData keys.
   *
   * @return endPairs key Set
   */
  public Set<String> getEndKeys() {
    return endPairs.keySet();
  }

  /**
   * Add EndData to the list using the given starting and ending characters.
   *
   * @param start stating char
   * @param end   ending char
   */
  public void addEndPair(char start, char end) {
    String key = String.valueOf(start) + end;
    endPairs.put(key, new EndData());
  }

  /**
   * Add a level value to the EndData set to the given key. If the key is not in the endPairs list
   * create a new entry.
   *
   * @param key   EndData key
   * @param level value to add
   */
  public void addEndLevel(String key, int level) {
    if (!endPairs.containsKey(key)) {
      setEndLevel(key, level);
    } else {
      setEndLevel(key, endPairs.get(key).level + level);
    }
  }

  /**
   * Toggle the level of the EndData. If the EndData doesn't exist, set the level to 1.
   *
   * @param key EndData key
   */
  public void toggleEndLevel(String key) {
    if (endPairs.containsKey(key)) {
      setEndLevel(key, endPairs.get(key).level == 0 ? 1 : 0);
    } else {
      setEndLevel(key, 1);
    }
  }

  /**
   * Set the level EndData with the given key to the level data provided.
   *
   * @param key   EndData key
   * @param level level value
   */
  public void setEndLevel(String key, int level) {
    EndData endData = endPairs.get(key);
    endData.level = level;
    endPairs.put(key, endData);
  }

  /**
   * Get the EndData Object with the current key.
   *
   * @param key key string
   * @return the data set to the given key if it exists
   */
  public EndData getEndData(String key) {
    return endPairs.get(key);
  }

  /**
   * Capture the first Character index provided.
   *
   * @param index index of the Character
   */
  public void captureChar(int index) {
    if (firstCharIndex == -1) {
      firstCharIndex = index;
    }
  }

  /**
   * Capture the first start index provided for the given key. Also capture the first overall start
   * index. No need to check if the level is 0 because if fStart or firstParsedStart isn't set then
   * the level has to be 0.
   *
   * @param key   EndData key
   * @param index key index
   */
  public void captureStart(String key, int index) {
    EndData endData = endPairs.get(key);
    if (endData.fStart == -1) {
      endData.fStart = index;
    }
    if (firstParsedStart == -1) {
      firstParsedStart = index;
    }
    endPairs.put(key, endData);
  }

  /**
   * Capture the first valid end index provided for the given key. Also capture the first valid
   * overall end index. An index if valid if the level is 0.
   *
   * @param key   EndData key
   * @param index key index
   */
  public void captureEnd(String key, int index) {
    EndData endData = endPairs.get(key);
    if (endData.level == 0) {
      if (endData.vEnd == -1) {
        endData.vEnd = index;
      }
      lastValidEnd = index;
    }

    endPairs.put(key, endData);
  }

  /**
   * Are any of the EndData Objects not level 0. This entails that one of the EndData Object pairs
   * has not been satisfied.
   *
   * @return true if one of the EndData Objects does not have a 0 level, false otherwise
   */
  public boolean isUnpaired() {
    for (EndData endData : endPairs.values()) {
      if (endData.level != 0) {
        return true;
      }
    }
    return false;
  }

  /**
   * Clear all of the data to the default state. Preserve the EndData keys but not the values.
   */
  public void clear() {
    for (String key : endPairs.keySet()) {
      endPairs.put(key, new EndData());
    }
    lastValidEnd = -1;
    firstParsedStart = -1;
    firstCharIndex = -1;
  }

  @Override
  public String toString() {
    return "PFlags=[" + Arrays.toString(endPairs.keySet().toArray())
        + ", " + Arrays.toString(endPairs.values().toArray()) + "]";
  }
}
