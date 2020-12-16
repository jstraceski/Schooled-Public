package schooled.visuals;

import java.nio.ByteBuffer;
import org.lwjgl.stb.STBTTBakedChar;

/**
 * OpenGL font data context.
 * <p>
 * Stores data used in text drawing. Has some redundant storage for future reference. Stores
 * character positioning and size data, the character texture id, the character sheet in a byte
 * buffer and the default font size.
 * <p></p>
 * Program written by Joseph Straceski
 * <p>
 * contact: straceski.joseph@gmail.com, https://github.com/Crepox
 */
public class GLFontData {

  public STBTTBakedChar.Buffer cdata; // font character data
  public int texID; // texture id of the baked character sheet
  public ByteBuffer ttf_data; // reference to the raw data used to make the character sheet
  public float defaultSize; // point size of the font when loaded

  public GLFontData(STBTTBakedChar.Buffer cdata, int texID, ByteBuffer ttf_data,
      float defaultSize) {
    this.cdata = cdata;
    this.texID = texID;
    this.ttf_data = ttf_data;
    this.defaultSize = defaultSize;
  }
}
