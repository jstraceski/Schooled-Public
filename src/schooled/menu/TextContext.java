package schooled.menu;

import java.awt.Color;
import java.awt.Font;
import schooled.Game;
import schooled.Window;
import schooled.visuals.FontContext;
import schooled.visuals.GLFontContext;
import schooled.visuals.TotalLineMetrics;

/**
 * Text context class to store text with formatting data.
 * <p></p>
 * Program written by Joseph Straceski
 * <p>
 * contact: straceski.joseph@gmail.com, https://github.com/Crepox
 */
public class TextContext {

  public final static byte BOLD = 0x1; // 00000001
  public final static byte ITALICS = 0x2; // 00000010
  public final static byte NEWLINE = 0x4; // 00000100


  private String text; // string representing the text to draw
  private byte style; // style of the text
  private Color color; // color of the text
  private FontContext font; // fancy special custom font
  private FitType fit = FitType.base; // fit type for size calculations

  private float currentSize = 32f; // size tracker
  private float currentScale = 1.0f; // scale tracker
  private float rWidth; // render width
  private float rHeight; // render height
  private float rAdvance; // render advance
  private float rAscent; // render ascent

  private float lineWidth = 0, lineHeight = 0; // larger context size values
  private float lineAscent = 0; // larger context size values

  // list of basic characters to calculate average font size
  // remove " ' * ` because they are special characters that are high up

  public static String baseChars = " !#%&()+,-./0123456789:;<=>?@ABCDEFGHI"
      + "JKLMNOPQRSTUVWXYZ[\\]^_abcdefghijklmnopqrstuvwxyz{}~";

  /**
   * Empty constructor.
   */
  public TextContext() {
    text = "";
  }

  /**
   * Create a text context with a string of text.
   *
   * @param s string of text
   */
  public TextContext(String s) {
    this();
    text = s;
  }

  /**
   * Create a text context with some text and a font context.
   *
   * @param s text
   * @param f font context
   */
  public TextContext(String s, FontContext f) {
    this(s);
    setFont(f);
  }

  /**
   * Create a text context with a java font.
   *
   * @param s text
   * @param f java font
   */
  public TextContext(String s, Font f) {
    this(s, new FontContext(f));
  }

  /**
   * Create a text context with an openGL font.
   *
   * @param s text
   * @param f openGL font
   */
  public TextContext(String s, GLFontContext f) {
    this(s, new FontContext(f));
  }

  /**
   * Create a text context with the game instance.
   * <p>
   * The game instance can set default font information.
   *
   * @param g game instance
   * @param s text
   */
  public TextContext(Game g, String s) {
    if (g == null) {
      text = s;
      setFont(new FontContext(Window.DEFAULT_FONT_CONTEXT));
    } else {
      text = s;
      setFont(g.getCurrentFont());
    }
  }

  @Override
  public TextContext clone() {
    FontContext fc = font == null ? null : font.clone();
    TextContext te = new TextContext(text, fc);
    te.fit = fit;
    te.color = color;
    te.style = style;
    te.rAscent = rAscent;
    te.rAdvance = rAdvance;
    te.lineWidth = lineWidth;
    te.lineAscent = lineAscent;
    te.lineHeight = lineHeight;
    te.rWidth = rWidth;
    te.rHeight = rHeight;
    te.currentSize = currentSize;
    return te;
  }

  @Override
  public String toString() {
    return "TextContext[text=" + text + "]";
  }

  /**
   * Get text of the context.
   *
   * @return text
   */
  public String getText() {
    return text;
  }

  /**
   * Set text of the context.
   *
   * @param s text
   */
  public void setText(String s) {
    text = s;
  }

  /**
   * Add text to the text of the context.
   *
   * @param s text to add
   */
  public void addText(String s) {
    text += s;
  }

  public Color getColor() {
    return color;
  }

  public void setColor(Color color) {
    this.color = color;
  }

  /**
   * Does the text have a color.
   *
   * @return true if the text has a color, false otherwise
   */
  public boolean hasColor() {
    return color != null;
  }

  /**
   * Is the end of the text a newline.
   *
   * @return true if the text ends in a newline, false otherwise
   */
  public boolean hasNewLine() {
    return (style & NEWLINE) > 0;
  }

  public boolean hasNL() {
    return hasNewLine();
  }

  /**
   * Set if the text of the context ends in a newline.
   *
   * @return true if the text ends in a newline, false otherwise
   */
  public void setNewLine(boolean bool) {
    if (hasNewLine() != bool) {
      style ^= NEWLINE;
    }
  }

  /**
   * Is the text bold.
   * @return true if the text is bold, false otherwise
   */
  public boolean isBold() {
    return (style & BOLD) > 0;
  }

  /**
   * Set the bold state of the text.
   *
   * @param bool bold state
   */
  public void setBold(boolean bool) {
    if (bool != isBold()) {
      style ^= BOLD;
      if (font != null) {
        if (bool) {
          font = font.deriveBold();
        } else {
          font = font.derivePlain();
        }
      }
    }
  }

  /**
   * Is the text italicized.
   *
   * TODO: implement italics
   *
   * @return true if the text is in italics, false otherwise
   */
  public boolean isItalic() {
    return (style & ITALICS) > 0;
  }

  /**
   * Get the style byte of the object.
   *
   * @return style
   */
  public byte getStyle() {
    return style;
  }

  /**
   * Set the style byte of the object.
   *
   * Automatically handles style configurations.
   *
   * @param newStyle style byte
   */
  public void setStyle(byte newStyle) {
    style = newStyle;
    setBold((newStyle & BOLD) == BOLD);
  }

  public FontContext getFont() {
    return font;
  }

  public void setFont(FontContext f) {
    font = f;
  }

  /**
   * Is the font of the text set.
   *
   * @return true if the text has a font, false otherwise.
   */
  public boolean hasFont() {
    return font != null;
  }

  /**
   * Get the text size fit type.
   * @return fit type
   */
  public FitType getFit() {
    return fit;
  }

  /**
   * Set the text size fit type.
   * @param currentFit fit type
   */
  public void setFit(FitType currentFit) {
    this.fit = currentFit;
  }

  /**
   * Get the font size.
   *
   * Handles if the text context has a font.
   *
   * @return font size
   */
  public float getCurrentSize() {
    return currentSize;
  }

  /**
   * Set the point size of the font.
   *
   * Handles if the text has a custom font.
   *
   * @param i font point size
   */
  public void setFontSize(float i) {
    currentSize = i;
    currentScale = 1.0f;
  }

  /**
   * Scale the point size of the font.
   *
   * Handles if the text has a custom font.
   *
   * @param scale font point scale
   */
  public void scaleFontSize(float scale) {
    currentScale = scale;
    currentSize *= scale;
  }

  /**
   * Get the current font scale.
   *
   * If the text has a custom font the scale is always 1.
   *
   * @return the font scale
   */
  public float getCurrentScale() {
    if (getFont() != null) {
      return 1;
    } else {
      return currentScale;
    }
  }

  /**
   * Get the render width.
   *
   * Has to be generated and set externally.
   *
   * @return render width
   */
  public float getWidth() {
    return rWidth;
  }

  /**
   * Get the render height.
   *
   * Has to be generated and set externally.
   *
   * @return render height
   */
  public float getHeight() {
    return rHeight;
  }

  /**
   * Get the render advance.
   *
   * Has to be generated and set externally.
   *
   * @return render advance
   */
  public float getAdvance() {
    return rAdvance;
  }

  /**
   * Get the render ascent.
   *
   * Has to be generated and set externally.
   *
   * @return render ascent
   */
  public float getAscent() {
    return rAscent;
  }

  /**
   * Set the render values using a line metric data type.
   *
   * @param totalLineMetrics line metrics
   */
  public void setRenderValues(TotalLineMetrics totalLineMetrics) {
    this.rWidth = totalLineMetrics.width;
    this.rHeight = totalLineMetrics.height;
    this.rAdvance = totalLineMetrics.advance;
    this.rAscent = totalLineMetrics.ascent;
  }

  /**
   * Set the render values using float values.
   *
   * @param rWidth render width
   * @param rHeight render height
   * @param rAdvance render advance
   * @param rAscent render ascent
   */
  public void setRenderValues(float rWidth, float rHeight, float rAdvance, float rAscent) {
    this.rWidth = rWidth;
    this.rHeight = rHeight;
    this.rAdvance = rAdvance;
    this.rAscent = rAscent;
  }

  /**
   * Get the line width register.
   *
   * Stores the total width of the line this text is a part of.
   *
   * @return line width
   */
  public float getLineWidth() {
    return lineWidth;
  }

  /**
   * Set the line width register.
   *
   * Stores the total width of the line this text is a part of.
   *
   * @param lineWidth line width
   */
  public void setLineWidth(float lineWidth) {
    this.lineWidth = lineWidth;
  }

  /**
   * Get the line height register.
   *
   * Stores the total height of the line this text is a part of.
   *
   * @return line width
   */
  public float getLineHeight() {
    return lineHeight;
  }

  /**
   * Set the line width register.
   *
   * Stores the total height of the line this text is a part of.
   *
   * @param lineHeight line height
   */
  public void setLineHeight(float lineHeight) {
    this.lineHeight = lineHeight;
  }

  public float getLineAscent() {
    return lineAscent;
  }

  public void setLineAscent(float lineAscent) {
    this.lineAscent = lineAscent;
  }

  /**
   * Fit type of size calculation.
   */
  public enum FitType {
    tight, base, all
  }
}
