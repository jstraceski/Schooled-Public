package schooled.visuals;

import static org.lwjgl.opengl.GL11.GL_ALPHA;
import static org.lwjgl.opengl.GL11.GL_BLEND;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_NEAREST;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_QUADS;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MAG_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MIN_FILTER;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE;
import static org.lwjgl.opengl.GL11.glBegin;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glBlendFunc;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glEnd;
import static org.lwjgl.opengl.GL11.glGenTextures;
import static org.lwjgl.opengl.GL11.glScalef;
import static org.lwjgl.opengl.GL11.glTexCoord2f;
import static org.lwjgl.opengl.GL11.glTexImage2D;
import static org.lwjgl.opengl.GL11.glTexParameteri;
import static org.lwjgl.opengl.GL11.glTranslatef;
import static org.lwjgl.opengl.GL11.glVertex2f;
import static org.lwjgl.stb.STBTruetype.stbtt_BakeFontBitmap;
import static org.lwjgl.stb.STBTruetype.stbtt_GetBakedQuad;
import static org.lwjgl.system.MemoryStack.stackPush;

import java.awt.Color;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.stb.STBTTAlignedQuad;
import org.lwjgl.stb.STBTTBakedChar;
import org.lwjgl.system.MemoryStack;
import schooled.Game;
import schooled.engines.Logger;
import schooled.engines.RenderEngine;
import schooled.menu.IOUtil;
import schooled.physics.BoundingBox;
import schooled.physics.Vector;

/**
 * OpenGL based font.
 * <p>
 * Stores drawing methods and data for rendering text. Has storage for bold, italic, and plain
 * versions of the font.
 * <p></p>
 * Program written by Joseph Straceski
 * <p>
 * contact: straceski.joseph@gmail.com, https://github.com/Crepox
 */
public class GLFontContext {

  private static int BM_W = 512 * 8; // default bitmap width
  private static int BM_H = 512 * 8; // default bitmap height
  private static float LOADING_SIZE = 32f; // default loading size

  private static STBTTAlignedQuad q; // text data
  private static FloatBuffer x; // x position buffer
  private static FloatBuffer y; // y position buffer
  private float size = 32f; // size buffer
  private GLFontData plainFont; // plain font data
  private GLFontData boldFont; // bold font data
  private GLFontData italicFont; // italic font data
  private GLFontData currentFont; // current loaded font data

  /**
   * Default constructor.
   * <p>
   * Sets up size registers to store and transfer openGL drawing data.
   */
  public GLFontContext() {
    try {
      if (x == null || y == null || q == null) {
        MemoryStack stack = stackPush();
        x = stack.mallocFloat(1);
        y = stack.mallocFloat(1);
        q = STBTTAlignedQuad.mallocStack(stack);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Default constructor.
   * <p>
   * Sets up size registers to store and transfer openGL drawing data.
   */
  public GLFontContext(String string) {
    this();
    setPlainFont(createFontDataFromPath(string));
  }

  /**
   * Clear the drawing registers.
   */
  public static void clear() {
    x.put(0, 0);
    y.put(0, 0);
  }

  /**
   * Create a GLFontContext from a file path.
   *
   * @param path font path
   * @return font data
   */
  public static GLFontContext createFontFromPath(String path) {
    GLFontContext f = new GLFontContext();
    f.setPlainFont(createFontDataFromPath(path));
    return f;
  }

  /**
   * Create font data from a path.
   *
   * @param path font data path
   * @return font data
   */
  public static GLFontData createFontDataFromPath(String path) {
    return createFontDataFromPath(path, LOADING_SIZE);
  }

  /**
   * Create openGL font data from a path.
   * <p>
   * Allows you to set the point size of the font as well.
   *
   * @param path        font path
   * @param loadingSize font point size
   * @return font data
   */
  private static GLFontData createFontDataFromPath(String path, float loadingSize) {
    try {
      return generateFontTextures(IOUtil.ioResourceToByteBuffer(path, 160 * 1024), loadingSize);
    } catch (Exception e) {
      System.err.println(path + " not found.");
      e.printStackTrace();
    }
    return null;
  }

  /**
   * Generate font textures from a true type font data and a point size.
   *
   * @param ttf_data true type font data
   * @param size     point size
   * @return font data with texture data set
   */
  private static GLFontData generateFontTextures(ByteBuffer ttf_data, float size) {
    int texID = glGenTextures();
    glEnable(GL_BLEND);
    glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

    STBTTBakedChar.Buffer cdata = STBTTBakedChar.malloc(96 * 4);
    ByteBuffer bitmap = BufferUtils.createByteBuffer(BM_W * BM_H);
    stbtt_BakeFontBitmap(ttf_data, size, bitmap, BM_W, BM_H, 0, cdata);

    glBindTexture(GL_TEXTURE_2D, texID);
    glTexImage2D(GL_TEXTURE_2D, 0, GL_ALPHA, BM_W, BM_H, 0, GL_ALPHA, GL_UNSIGNED_BYTE, bitmap);

    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
    if (size % 16 != 0 && size % 12 != 0) {
      System.err.println("Likely unsupported size : " + size);
    }

    return new GLFontData(cdata, texID, ttf_data, size);
  }

  /**
   * Set the point size used to load the font texture data.
   *
   * @param loadingSize font point size
   */
  public void setLoadingSize(float loadingSize) {
    GLFontContext.LOADING_SIZE = loadingSize;
  }

  /**
   * Get the bitmap width used when loading true type font texture data.
   *
   * @return bitmap width
   */
  public int getBitmapWidth() {
    return BM_W;
  }

  /**
   * Set the bitmap width used when loading true type font texture data.
   *
   * @param width bitmap width
   */
  public void setBitmapWidth(int width) {
    GLFontContext.BM_W = width;
  }

  /**
   * Get the bitmap height used when loading true type font texture data.
   *
   * @return bitmap height
   */
  public int getBitmapHeight() {
    return BM_H;
  }

  /**
   * Set the bitmap height used when loading true type font texture data.
   *
   * @param height bitmap height
   */
  public void setBitmapHeight(int height) {
    GLFontContext.BM_H = height;
  }

  @Override
  public GLFontContext clone() {
    GLFontContext newFC = new GLFontContext();

    newFC.plainFont = plainFont;
    newFC.boldFont = boldFont;
    newFC.italicFont = italicFont;
    newFC.currentFont = currentFont;
    newFC.size = size;

    return newFC;
  }

  /**
   * Render the text at the input position.
   *
   * @param text text to render
   * @param pos  position
   */
  public void renderText(GLGraphicsContext glg, String text, Vector pos) {
    renderText(glg, text, pos, Vector.zero, 1, 1);
  }

  /**
   * Render text at the input position.
   * <p>
   * Using the font data of the given font context, draw the input text to the screen. Uses openGL
   * methods to render the character data as a set of textured quads.
   *
   * @param text   text to render
   * @param pos    position to render the text
   * @param shift  visual shift to apply to the text position
   * @param scale  visual scale to apply to the text position
   * @param fScale font scale to apply to the text
   */
  public void renderText(GLGraphicsContext glg, String text, Vector pos, Vector shift, float scale, float fScale) {
    GLFontData fontData = getCurrentFontData(); // get the current font data
    float fontScale = fScale * size / fontData.defaultSize; // get the font size scale

    Vector nPos = pos.scalei(scale).addi(shift); // apply game shifts and scales
    nPos.round(); // round the position to the nearest pixel

    if (glg.hasFilter()) {
      glg.fboData.start(glg, glg.filter);
    }

    glTranslatef(nPos.getX(), nPos.getY(), 0); // shift the drawing window to the text position
    glScalef(fontScale, fontScale, 1); // scale the drawing window to the text scale

    glBindTexture(GL_TEXTURE_2D, fontData.texID); // bind the font character sheet texture
    glEnable(GL_TEXTURE_2D); // enable texture drawing

    clear(); // clear the buffer registers

    Vector topLeft = new Vector(Float.MAX_VALUE, Float.MAX_VALUE);
    Vector bottomRight = new Vector(-Float.MAX_VALUE, -Float.MAX_VALUE);

    for (char c : text.toCharArray()) { // run through the characters in the text
      // extract character position data from the font's character data
      stbtt_GetBakedQuad(fontData.cdata, BM_W, BM_H, c, x, y, q, true);
      RenderEngine.glRenderQuad(glg, nPos, fontScale, q);

      topLeft = Vector.min(topLeft, new Vector(q.x0(), q.y0()));
      bottomRight = Vector.max(bottomRight, new Vector(q.x1(), q.y1()));
    }

    // stop drawing the character and reset the drawing window
    glDisable(GL_TEXTURE_2D);

    glScalef(1 / fontScale, 1 / fontScale, 1);
    glTranslatef(-nPos.getX(), -nPos.getY(), 0);

    topLeft = topLeft.scalei(fontScale).addi(nPos);
    bottomRight = bottomRight.scalei(fontScale).addi(nPos);

    if (glg.hasFilter()) {
      glg.fboData.draw(glg, glg.filter, new BoundingBox(topLeft, bottomRight), Vector.zero, scale, shift);
    }

    if (glg.hasFilter()) {
      glg.fboData.end(glg, glg.filter);
    }

  }

  /**
   * Calculate the font metrics of the input text.
   * @param text text to measure
   * @return line metrics
   */
  public TotalLineMetrics calculateMetrics(String text) {
    return calculateMetrics(text, -1, 1.0f);
  }

  /**
   * Calculate the line metrics of the input text.
   *
   * Custom size and custom scale is used to calculate custom size of the input text
   *
   * @param text input text
   * @param customSize custom font point size
   * @param customScale custom font point size scale
   * @return line metrics of the text
   */
  public TotalLineMetrics calculateMetrics(String text, float customSize, float customScale) {
    TotalLineMetrics fm = new TotalLineMetrics(); // the total line metrics
    GLFontData fontData = getCurrentFontData(); // access the current font data

    clear(); // clear out the character buffers

    stbtt_GetBakedQuad(fontData.cdata, BM_W, BM_H, 'a', x, y, q, false);
    fm.advance = x.get(0) - q.x1(); // set the standard advance value

    clear(); // clear again
    // iterate through the character array to find the metrics of the entire text
    for (char c : text.toCharArray()) {
      stbtt_GetBakedQuad(fontData.cdata, BM_W, BM_H, c, x, y, q, true);
      // the ascent is calculated as the smallest y0 value because -y is up
      fm.ascent = Math.min(fm.ascent, q.y0());

      // the descent is calculated in a similar manner
      fm.descent = Math.max(fm.descent, q.y1());

      // if the char array ends in a space set the width to the carriage offset of the space character minus the character advance
      if (c != ' ') {
        fm.width = q.x1();
      } else {
        fm.width = x.get(0) - fm.advance;
      }
    }

    // the size scalar is the custom size or the size divided by the default size.
    float scalar;

    if (customSize > 0.0) {
      scalar = customSize / fontData.defaultSize;
    } else {
      scalar = size / fontData.defaultSize;
      scalar = scalar * customScale;
    }

    // scale the metric values and return the data
    fm.ascent *= scalar;
    fm.advance *= scalar;
    fm.width *= scalar;
    fm.descent *= scalar;

    return fm;
  }

  /**
   * Get the point size of the font.
   *
   * @return font point size
   */
  public float getFontSize() {
    return size;
  }

  /**
   * Get a version of the font context with the input point size.
   *
   * @param fontSize font size
   */
  public GLFontContext deriveFontSize(float fontSize) {
    GLFontContext fontContext = clone();
    fontContext.size = fontSize;
    return fontContext;
  }


  /**
   * Get the plain font data of the font context.
   *
   * @return plain font data
   */
  public GLFontContext getPlainFont() {
    if (plainFont == null) {
      return this;
    } else {
      GLFontContext fontContext = clone();
      fontContext.currentFont = plainFont;
      return fontContext;
    }
  }

  /**
   * Set the plain font data of the font context.
   *
   * @param newFont
   */
  public void setPlainFont(GLFontData newFont) {
    plainFont = newFont;
  }

  /**
   * Get the bold font version of this font context.
   *
   * If there is no bold font set, return the plain font.
   *
   * @return bold font
   */
  public GLFontContext getBoldFont() {
    if (boldFont == null) {
      return this;
    } else {
      GLFontContext fontContext = clone();
      fontContext.currentFont = boldFont;
      return fontContext;
    }
  }

  /**
   * Get if the current font is bold.
   *
   * @return true if the current font is bold, false otherwise
   */
  public boolean isBoldFont() {
    return boldFont == currentFont;
  }

  /**
   * Set the bold font data of the font context.
   *
   * @param newFont the bold font data
   */
  public void setBoldFont(GLFontData newFont) {
    boldFont = newFont;
  }

  /**
   * Get the italic font version of this font context.
   *
   * If there is no italic font set, return the plain font.
   *
   * @return bold font
   */
  public GLFontContext getItalicFont() {
    if (italicFont == null) {
      return this;
    } else {
      GLFontContext fontContext = (GLFontContext) clone();
      fontContext.currentFont = italicFont;
      return fontContext;
    }
  }

  /**
   * Set the italic font data of the font context.
   *
   * @param newFont italic font data
   */
  public void setItalicFont(GLFontData newFont) {
    italicFont = newFont;
  }

  /**
   * Get the current font.
   *
   * If the current font register isn't set then return the plain font.
   *
   * @return current font
   */
  public GLFontData getCurrentFontData() {
    if (currentFont == null) {
      return plainFont;
    }
    return currentFont;
  }

  /**
   * Set the current font data register.
   *
   * @param newFont current font data
   */
  public void setCurrentFont(GLFontData newFont) {
    currentFont = newFont;
  }
}
