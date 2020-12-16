package schooled.visuals;

import java.awt.font.LineMetrics;

/**
 * Size and positioning data for a line of text.
 * <p>
 * Stripped down implementation of a LineMetrics data-type with the inclusion of a width value.
 * <p></p>
 * Program written by Joseph Straceski
 * <p>
 * contact: straceski.joseph@gmail.com, https://github.com/Crepox
 */
public class TotalLineMetrics extends LineMetrics {

  public float ascent;
  public float descent;
  public float advance;
  public float width;

  // ease of use register to store the combination of ascent and descent
  //  or possibly just the ascent depending on the situation
  public float height;

  @Override
  public String toString() {
    return "GLFontMetrics[ascent=" + ascent + ", descent=" + descent
        + ", advance=" + advance + ", width=" + width + "]";
  }


  @Override
  public int getNumChars() {
    return 0;
  }

  @Override
  public float getAscent() {
    return ascent;
  }

  @Override
  public float getDescent() {
    return descent;
  }

  @Override
  public float getLeading() {
    return advance;
  }

  @Override
  public float getHeight() {
    return height;
  }

  @Override
  public int getBaselineIndex() {
    return 0;
  }

  @Override
  public float[] getBaselineOffsets() {
    return new float[0];
  }

  @Override
  public float getStrikethroughOffset() {
    return 0;
  }

  @Override
  public float getStrikethroughThickness() {
    return 0;
  }

  @Override
  public float getUnderlineOffset() {
    return 0;
  }

  @Override
  public float getUnderlineThickness() {
    return 0;
  }
}
