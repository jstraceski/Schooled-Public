package schooled.visuals;

import java.awt.Color;
import java.util.Stack;
import schooled.visuals.filters.GLFilter;

/**
 * OpenGL graphics context.
 * <p>
 * Stores data used in rendering openGL text and images.
 * Fonts and Colors.
 *
 * <p></p>
 * Program written by Joseph Straceski
 * <p>
 * contact: straceski.joseph@gmail.com, https://github.com/Crepox
 */
public class GLGraphicsContext {

  public FBOWrapper fboData;
  public FBOWrapper fboDataChild;
  public FBOWrapper menuFbo;

  public GLFilter filter = null;
  public Stack<GLFilter> filters = new Stack<>();
  public Stack<Color> colors = new Stack<>();

  public GLFontContext defaultFontContext;
  public Color defaultColor = new Color(0,0,0);

  public GLFontContext currentFontContext;
  public Color currentColor;

  /**
   * Makes sure that every graphics context has a default font.
   */
  private GLGraphicsContext(){}

  /**
   * Makes sure that every graphics context has a default font.
   */
  public GLGraphicsContext(GLFontContext fontContext){
    defaultFontContext = fontContext;
  }

  /**
   * Makes sure that every graphics context has a default font.
   */
  public GLGraphicsContext(GLFontContext fontContext, GLFilter glFilter){
    defaultFontContext = fontContext;
    filter = glFilter;
  }

  /**
   * Makes sure that every graphics context has a default font.
   */
  public GLGraphicsContext(GLFontContext fontContext, FBOWrapper fboWrapper, FBOWrapper fboWrapper2){
    defaultFontContext = fontContext;
    fboData = fboWrapper;
    fboDataChild = fboWrapper2;
  }

  /**
   * Makes sure that every graphics context has a default font.
   */
  public GLGraphicsContext(GLFontContext fontContext, FBOWrapper fboWrapper, GLFilter glFilter){
    defaultFontContext = fontContext;
    fboData = fboWrapper;
    filter = glFilter;
  }

  public boolean hasFilter() {
    return filter != null && filter.isActive();
  }
}
