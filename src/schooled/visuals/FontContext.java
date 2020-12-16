package schooled.visuals;

import java.awt.Font;

/**
 * Wrapper class to generalize openGL fonts and java fonts.
 * <p></p>
 * Program written by Joseph Straceski
 * <p>
 * contact: straceski.joseph@gmail.com, https://github.com/Crepox
 */
public class FontContext {

  private Font f = null; // java font data
  private GLFontContext glf = null; // openGL font data
  private FontType fontType = FontType.NULL; // enum storing font types

  /**
   * Create an empty font context.
   */
  private FontContext() {
  }

  /**
   * Create a new Font Context that is a clone of the input.
   *
   * @param f input font context
   */
  public FontContext(FontContext f) {
    FontContext newContext = f.clone();
    this.f = newContext.f;
    this.glf = newContext.glf;
    this.fontType = newContext.fontType;
  }

  /**
   * Create a font context from a java font.
   *
   * @param f java font
   */
  public FontContext(Font f) {
    setFont(f.deriveFont(f.getTransform()));
  }

  /**
   * Create a font context from an openGL font
   *
   * @param f openGL font
   */
  public FontContext(GLFontContext f) {
    setFont(f.clone());
  }


  public FontType getFontType() {
    return fontType;
  }

  /**
   * Clone the various font types.
   *
   * @return cloned instance
   */
  @Override
  public FontContext clone() {
    FontContext newFontContext = new FontContext();
    if (f != null) {
      newFontContext.f = f.deriveFont(f.getTransform());
    }
    if (glf != null) {
      newFontContext.glf = glf.clone();
    }

    newFontContext.fontType = fontType;
    return newFontContext;
  }

  /**
   * Is the font data openGL based.
   *
   * @return true if the context has openGL font data, false otherwise
   */
  public boolean isOpenGL() {
    return fontType == FontType.OPENGL;
  }

  /**
   * Is the font data java based.
   *
   * @return true if the context has java font data, false otherwise
   */
  public boolean isJava() {
    return fontType == FontType.JAVA2D;
  }

  /**
   * Set the font data to a java font.
   *
   * @param f java font
   */
  public void setFont(Font f) {
    this.f = f;
    if (f != null) {
      fontType = FontType.JAVA2D;
    }
  }

  /**
   * Set the font data to an openGL font.
   *
   * @param glf openGL font
   */
  public void setFont(GLFontContext glf) {
    this.glf = glf;
    if (glf != null) {
      fontType = FontType.OPENGL;
    }
  }

  /**
   * Get the java font data.
   *
   * @return java font data
   */
  public Font getJavaFont() {
    return f;
  }

  /**
   * Get the openGL font data.
   *
   * @return openGL font data
   */
  public GLFontContext getOpenGLFont() {
    return glf;
  }

  /**
   * Get the size of the font.
   *
   * Handles the underlying font type.
   *
   * @return font size
   */
  public float getFontSize() {
    if (fontType == FontType.JAVA2D) {
      return f.getSize();
    }
    if (fontType == FontType.OPENGL) {
      return glf.getFontSize();
    }
    return 0;
  }

  /**
   * Derive a version of the given font with the input font size.
   *
   * Handles the underlying font type.
   *
   * @param s font size
   * @return derived font with a new size
   */
  public FontContext deriveFontSize(float s) {
    if (fontType == FontType.JAVA2D) {
      return new FontContext(f.deriveFont(s));
    } else if (fontType == FontType.OPENGL) {
      return new FontContext(glf.deriveFontSize(s));
    }
    return null;
  }

  /**
   * Derive a bold version of the given font.
   *
   * Handles the underlying font type.
   *
   * @return derived bold font
   */
  public FontContext deriveBold() {
    if (fontType == FontType.JAVA2D) {
      return new FontContext(f.deriveFont(1));
    } else if (fontType == FontType.OPENGL) {
      return new FontContext(glf.getBoldFont());
    }
    return null;
  }

  /**
   * Derive an unmodified version of the given font.
   *
   * Handles the underlying font type.
   *
   * @return derived plain font
   */
  public FontContext derivePlain() {
    if (fontType == FontType.JAVA2D) {
      return new FontContext(f.deriveFont(0));
    } else if (fontType == FontType.OPENGL) {
      return new FontContext(glf.getPlainFont());
    }
    return null;
  }


  /**
   * Is the font bold.
   *
   * @return  true if the font is bold, false otherwise.
   */
  public boolean isBold() {
    if (fontType == FontType.JAVA2D) {
      return f.isBold();
    } else if (fontType == FontType.OPENGL) {
      return glf.isBoldFont();
    }
    return false;
  }

  /**
   * Is the font italic.
   *
   * TODO: Implement
   *
   * @return  true if the font is italic, false otherwise.
   */
  public boolean isItalic() {
    return false;
  }


  /**
   * font type enumeration.
   */
  public enum FontType {
    NULL,
    JAVA2D,
    OPENGL
  }
}
