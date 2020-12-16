package schooled.loaders;

/**
 * A storage object for a decoding string encapsulations.
 * <p>
 * ex: in the string "(the quick brown fox (jumps (over) the) lazy dog)" the substring "jumps (over)
 * the" has the following data: <pre>fStart = 22, vEnd = 37, level = 0 </pre>
 * <p>
 * Program written by Joseph Straceski
 * <p>
 * contact: straceski.joseph@gmail.com, https://github.com/Crepox
 */
public class EndData {

  int fStart = -1; // the first delimiter of the string data
  int level = 0;   // the level of the string within other delimiters
  int vEnd = -1;   // the first valid end where the level is 0

  @Override
  public String toString() {
    return String.format("EndData=[s=%d, e=%d, l=%d]",
        fStart, vEnd, level);
  }
}