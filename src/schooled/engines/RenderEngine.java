package schooled.engines;

import static org.lwjgl.opengl.GL11.GL_CURRENT_COLOR;
import static org.lwjgl.opengl.GL11.GL_LINE_LOOP;
import static org.lwjgl.opengl.GL11.GL_NEAREST;
import static org.lwjgl.opengl.GL11.GL_POLYGON;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MAG_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MIN_FILTER;
import static org.lwjgl.opengl.GL11.GL_TRIANGLE_FAN;
import static org.lwjgl.opengl.GL11.GL_TRIANGLE_STRIP;
import static org.lwjgl.opengl.GL11.glBegin;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glEnd;
import static org.lwjgl.opengl.GL11.glGenTextures;
import static org.lwjgl.opengl.GL11.glScissor;
import static org.lwjgl.opengl.GL11.glTexCoord2f;
import static org.lwjgl.opengl.GL11.glTexParameteri;
import static org.lwjgl.opengl.GL11.glVertex2f;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Stack;
import org.lwjgl.opengl.GL11;
import org.lwjgl.stb.STBTTAlignedQuad;
import schooled.Game;
import schooled.Window;
import schooled.containers.Room;
import schooled.datatypes.Tuple;
import schooled.entities.BasicEntity;
import schooled.entities.Entity;
import schooled.entities.LightEntity;
import schooled.entities.Vehicle;
import schooled.menu.Alignment;
import schooled.menu.Menu;
import schooled.menu.MenuEntity;
import schooled.menu.TextArea;
import schooled.menu.TextContext;
import schooled.menu.TextContext.FitType;
import schooled.physics.BoundingBox;
import schooled.physics.Circle;
import schooled.physics.MassTree;
import schooled.physics.MultiShape;
import schooled.physics.PolygonShape;
import schooled.physics.Shape;
import schooled.physics.Vector;
import schooled.visuals.FBOWrapper;
import schooled.visuals.FontContext;
import schooled.visuals.filters.GLFilter;
import schooled.visuals.GLFontContext;
import schooled.visuals.GLGraphicsContext;
import schooled.visuals.JavaGraphicsContext;
import schooled.visuals.TotalLineMetrics;
import schooled.visuals.sprite.ByteBufferImage;
import schooled.visuals.sprite.LayeredSprite;
import schooled.visuals.sprite.Sprite;

/**
 * Rendering engine.
 * <p>
 * Contains drawing and visual methods.
 * <p></p>
 * Program written by Joseph Straceski
 * <p>
 * contact: straceski.joseph@gmail.com, https://github.com/Crepox
 */
public class RenderEngine {

  // <editor-fold defaultstate="collapsed" desc="Main Drawing Methods">

  /**
   * Initialize the graphics data using the graphics context.
   *
   * @param gc graphics context
   */
  public static void init(Object gc) {
    setFont(gc, null);
    setColor(gc, null);
  }

  /**
   * Render the state of the game to the screen.
   * <p>
   * Generates a graphics context from the game state.
   *
   * @param game game instance
   */
  public static void renderGame(Game game) {
    renderGame(game, generateGraphicsContext(game));
  }

  /**
   * Render the state of the game to the screen.
   *
   * @param game     game instance
   * @param gc graphics context
   */
  public static void renderGame(Game game, Object gc) {

    Logger.pushDebugTime("renderInit");
    Room room = game.getWorld().getLoadedRoom(); // get the loaded room of the game instance

    float gScale = game.getGameScreenScale(); // how much is the game window being scaled
    // frame size of the game screen, scaled
    Vector gFrame = new Vector(game.getGameScreenSize()).scalei(gScale);
    // how much is the menu window being scaled
    float mScale = game.getMenuScreenScale();
    // frame size of the menu screen
    Vector mFrame = new Vector(game.getMenuScreenSize()).scalei(mScale);

    // calculate scaled game shift
    Vector gShift = game.getGameShift().scalei(gScale);

    // get the size of the program window frame
    Vector wFrame = new Vector(game.getWindow().getSize());
    // game window shift is the vector shift that will center the game frame in the program window
    //  this is half of the difference between the window frame and the game frame
    // round them to the nearest pixel to prevent rounding errors later
    Vector gwShift = wFrame.subi(gFrame).scalei(0.5f).roundi();
    Vector mwShift = wFrame.subi(mFrame).scalei(0.5f).roundi(); // same thing with the window

    Logger.pushDebugTime("renderInit");

    Logger.pushDebugTime("drawMatte");
    setColor(gc, Color.WHITE); // game drawing methods assume the base color is white

    // draw a background matte image over the whole program window
    drawMatte(gc, room.getMatte(), wFrame, gScale);

    Logger.pushDebugTime("drawMatte");

    // if the graphics are java based or openGL based
    if (gc instanceof JavaGraphicsContext) {
      JavaGraphicsContext jgc = (JavaGraphicsContext) gc; // cast the graphics object
      // start a scissor section for the game render
      jgc.graphics.setClip(gwShift.getXi(), gwShift.getYi(), gFrame.getXi(), gFrame.getYi());
    } else {
      glEnable(GL11.GL_SCISSOR_TEST); // start a scissor section for the game render
      glScissor(gwShift.getXi(), gwShift.getYi(), gFrame.getXi(), gFrame.getYi());
    }

    boolean gFilter = game.getGlobalFilter() != null;
    if (gFilter) {
      pushFilter(gc, game.getGlobalFilter());
    }

    Logger.pushDebugTime("drawRoom");
    // draw the entities in a room, and the room itself
    drawRoom(gc, room, gScale, gShift, gFrame, gwShift);
    Logger.pushDebugTime("drawRoom");
    // end the game screen clip
    if (gc instanceof JavaGraphicsContext) {
      JavaGraphicsContext jgc = (JavaGraphicsContext) gc;
      jgc.graphics.setClip(0, 0, wFrame.getXi(), wFrame.getYi());
    } else {
      glDisable(GL11.GL_SCISSOR_TEST);
    }

    Logger.pushDebugTime("drawHud");

    // calculate render sizes for the text hud
    room.getWorld().getTextHud().updateVisual(gc, mScale);

    // draw the world overlay text
    renderMenuEntity(gc, room.getWorld().getTextHud(), mwShift, mScale);

    for (Entity entity : game.getWorld().getDisplayMenu().getEntities()) {
      renderMenuEntity(gc, ((MenuEntity) entity), mwShift, mScale);
    }

    Logger.pushDebugTime("drawHud");

    if (gFilter) {
      popFilter(gc);
    }

    Logger.pushDebugTime("drawDebug");

    // draw debug information if the game is in debug mode
    if (Logger.debug) {
      setColor(gc, Color.BLACK);
      // draw debug shapes around the loaded entities
      drawDebug(gc, room, gScale, gShift, gwShift);
      // draw debug times
      drawDebugHUD(gc);
    }

    Logger.pushDebugTime("drawDebug");

    // if the graphics are java based dispose the graphics context
    if (gc instanceof JavaGraphicsContext) {
      JavaGraphicsContext jgc = (JavaGraphicsContext) gc;
      jgc.graphics.dispose();
    }

  }

  /**
   * Render the menu to the screen with the game context.
   * <p>
   * Generates a new graphics context.
   *
   * @param menu loaded menu
   * @param game game context
   */
  public static void renderMenu(Menu menu, Game game) {
    renderMenu(menu, game, generateGraphicsContext(game));
  }

  /**
   * Render the menu to the screen with the game and graphics context.
   *
   * @param menu menu to draw
   * @param game game context
   * @param gc   graphics context
   */
  public static void renderMenu(Menu menu, Game game, Object gc) {
    float mScale = game.getMenuScreenScale(); // menu screen scale
    Vector wSize = new Vector(game.getWindow().getSize()); // program window size
    // menu screen size scaled by the menu scale
    Vector mSize = new Vector(game.getMenuScreenSize()).scalei(mScale);
    // calculate the menu window shift
    Vector mwShift = wSize.subi(mSize).scalei(0.5f);

    // If the game is paused and loaded render the game behind the menu
    if (game.isPaused() && game.isGameLoaded()) {
      renderGame(game, gc);

      // draw a grey square over the game screen
      setColor(gc, new Color(0, 0, 0, 100));
      fillRect(gc, 0, 0, wSize.getXi(), wSize.getYi());
    }

    // draw the menu entities from the loaded menu
    for (Entity entity : menu.getEntities()) {
      if (entity instanceof MenuEntity) {
        renderMenuEntity(gc, ((MenuEntity) entity), mwShift, mScale);
      }
    }

    // draw the game debug window
    if (Logger.debug) {
      drawDebugHUD(gc);
    }
  }

  /**
   * Pre-render and render menu.
   *
   * @param menu menu
   */
  public static void totalRenderMenu(Menu menu) {
    menu.preRender(Window.DEFAULT_GRAPHICS_CONTEXT);
    // draw the menu entities from the loaded menu
    for (Entity entity : menu.getEntities()) {
      if (entity instanceof MenuEntity) {
        renderMenuEntity(Window.DEFAULT_GRAPHICS_CONTEXT, ((MenuEntity) entity), Vector.zero, 1.0f);
      }
    }
  }

  /**
   * Generate a graphics context from the game instance.
   * <p>
   * Set default colors and fonts. Java graphics contexts also generate a graphics object.
   *
   * @param game game instance
   * @return graphics context
   */
  public static Object generateGraphicsContext(Game game) {
    if (!game.isGL()) {
      Vector wSize = new Vector(game.getWindow().getSize());
      BufferedImage wImage = new BufferedImage(wSize.getXi(), wSize.getYi(),
          BufferedImage.TYPE_INT_RGB);
      JavaGraphicsContext jgc = new JavaGraphicsContext();
      jgc.defaultColor = Color.black;
      jgc.defaultFontContext = game.getCurrentFont().getJavaFont();
      jgc.graphics = wImage.getGraphics();
      return jgc;
    } else {
      GLFontContext fc = game.getCurrentFont().getOpenGLFont();
      FBOWrapper fbo = game.getWindow().getFBO();
      FBOWrapper fbo2 = game.getWindow().getFBO2();

      return new GLGraphicsContext(fc, fbo, fbo2);
    }
  }

  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Light Rendering">

  /**
   * Deprecated light render method.
   *
   * @param game game instance
   * @param g    graphics
   * @param l    light entity
   * @param i    light level
   */
  public static void renderLight(Game game, Graphics2D g, LightEntity l, int i) {
    g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP));
    Vector pos = l.getPosition();
    int length = (int) (l.getLightLevel() + 0.5);

    switch (i) {
      case (0): {
        g.setColor(new Color(1.0f, 1.0f, 0.3f, 0.1f));
        fillCircle(g, length + (length / 2) + (length / 3) + (length / 4), null, pos, 1.0f);
      }
      case (1): {
        g.setColor(new Color(1.0f, 1.0f, 0.5f, 0.3f));
        fillCircle(g, length + (length / 2) + (length / 3), null, pos, 1.0f);
      }
      case (2): {
        g.setColor(new Color(1.0f, 1.0f, 0.6f, 0.5f));
        fillCircle(g, length + (length / 2), null, pos, 1.0f);
      }
      case (3): {
        g.setColor(new Color(1.0f, 1.0f, 0.8f, 1.0f));
        fillCircle(g, length, null, pos, 1.0f);
      }
    }

    g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Menu Rendering">

  public static void renderMenuEntity(MenuEntity entity) {
    renderMenuEntity(Window.DEFAULT_GRAPHICS_CONTEXT, entity, new Vector(0, 0), 1.0f);
  }

  /**
   * Render a menu entity to the screen.
   * <p>
   * Sets the window shift to the zero vector. Scale the menu entity size and position by the input
   * scale as well.
   *
   * @param gc     graphics context
   * @param entity menu entity
   * @param scale  menu scale
   */
  public static void renderMenuEntity(Object gc, MenuEntity entity, float scale) {
    renderMenuEntity(gc, entity, new Vector(0, 0), scale);
  }

  /**
   * Render a menu entity to the screen.
   * <p>
   * Scale the menu entity size and position by the window scale. Then shift the menu objects by the
   * window shift.
   *
   * @param gc     graphics context
   * @param entity menu entity
   * @param wShift window shift
   * @param scale  window scale
   */
  public static void renderMenuEntity(Object gc, MenuEntity entity, Vector wShift, float scale) {
    if (!entity.isVisible()) {
      return;
    }

    Color previous = getColor(gc); // store the previous color

    boolean hasFilter = entity.hasFilter();
    if (hasFilter) {
      pushFilter(gc, entity.getFilter());
    }

    if (entity.hasSpriteData()) {
      setColor(gc, Color.WHITE);
      drawEntitySprite(gc, entity, entity.getSprite(), wShift, scale);
    }

    // if the menu entity has a border trace the border shape
    if (entity.hasBorder()) {
      setColor(gc, entity.getBorderColor());
      traceShape(gc, entity.getBorderShape(), entity.getPos(), wShift, scale);
    }

    // if the menu entity has a background color, draw it
    if (entity.hasBackgroundColor()) {
      setColor(gc, entity.getBackgroundColor());
      fillShape(gc, entity.getShape(), entity.getPos(), wShift, scale);
    }

    setColor(gc, entity.getDefaultFontColor());

    renderText(gc, entity, wShift, scale); // render the text in the menu entity
    entity.renderHook(gc, wShift, scale);

    if (hasFilter) {
      popFilter(gc);
    }

    setColor(gc, previous);
  }

  /**
   * Render the text inside a menu entity to the screen.
   *
   * @param gc     graphics context
   * @param entity menu entity
   * @param shift  screen shift
   * @param scale  screen scale
   */
  public static void renderText(Object gc, MenuEntity entity, Vector shift, float scale) {
    // store the previous font and color
    Color previousColor = null;
    FontContext previousFont = null;

    // get the modified (scaled and shifted) version of the position and size
    Vector size = entity.getSize().scalei(scale);
    Vector pos = entity.getPos().scalei(scale).addi(shift);

    float lineShift = 0; // current string shift of the line

    // get some corner markers for the menu area
    Vector topLeft = pos.subi(size.scalei(0.5f));
    Vector topRight = topLeft.addi(new Vector(size.getX(), 0));

    // get the top padding, this is the distance between the top of the menu entity and the top of
    //  the first line
    float newHeight = entity.getRenderTopPadding();

    // loop through the render list and draw the text to the screen
    for (TextContext textContext : entity.getRenderTextList()) {
      // get the string representation of the text
      String text = textContext.getText();
      float lineWidth = textContext.getLineWidth(); // get the total width of the line of text

      // set the font and color of the text if it has one
      if (textContext.hasFont()) {
        previousFont = getFont(gc);
        setFont(gc, textContext.getFont());
      }

      // set the style parameters of the font if the current font differs from the style settings
      if (textContext.isBold() && !getFont(gc).isBold()) {
        previousFont = previousFont != null ? previousFont : getFont(gc);
        setFont(gc, getFont(gc).deriveBold());
      }

      if (textContext.getCurrentSize() != getFont(gc).getFontSize()) {
        previousFont = previousFont != null ? previousFont : getFont(gc);
        setFont(gc, getFont(gc).deriveFontSize(textContext.getCurrentSize()));
      }

      if (textContext.hasColor()) {
        previousColor = getColor(gc);
        setColor(gc, textContext.getColor());
      }

      // storage for string position
      // string y position is calculated as the top edge of the menu entity plus the previous
      //  string height, the ascent of the string is then subtracted to get the bottom edge
      //  of the font
      Vector stringPos = new Vector(0, topRight.getY() + newHeight - textContext.getLineAscent());

      if (entity.getAlignment() == Alignment.RIGHT) {
        // x position for right alignment is calculated as the right side minus the padding and the
        //  line width, the string shift is then added to get the current text position
        stringPos.setX(topRight.getX() - entity.getRenderRightPadding() - lineWidth + lineShift);
      } else if (entity.getAlignment() == Alignment.LEFT) {
        // x position for left alignment is the left edge plus the padding and previous string width
        stringPos.setX(topLeft.getX() + entity.getRenderLeftPadding() + lineShift);
      } else if (entity.getAlignment() == Alignment.CENTER) {
        // if the text is centered the left padding is calculated as the value needed to shift the
        //  line (referenced by the line width) from the left edge of the entity, so that it is
        //  centered in the menu entity, the previous shift is then added
        stringPos.setX(topLeft.getX() + ((size.getX() - lineWidth) / 2.0f) + lineShift);
      }

      // draw the text at the calculated position
      drawText(gc, stringPos, Vector.zero, 1.0f, 1.0f, text);

      if (!textContext.hasNewLine()) {
        // if the text doesn't have a newline add the width and advance to the current line shift
        lineShift += textContext.getWidth() + textContext.getAdvance();
      } else {
        // if the test is the end of a line add the line height to the current height
        newHeight += textContext.getLineHeight();
        lineShift = 0; // set the line shift to 0 to reset the line marker
      }

      // if the font or color changed this drawing step, set it to the previous font or color
      if (previousColor != null) {
        setColor(gc, previousColor);
        previousColor = null;
      }

      if (previousFont != null) {
        setFont(gc, previousFont);
        previousFont = null;
      }
    }
  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Tracing">

  /**
   * Trace the shape of an entity to the screen.
   *
   * @param gc     graphics context
   * @param entity entity data
   * @param shift  window shift
   * @param scale  window scale
   */
  public static void traceEntity(Object gc, Entity entity, Vector shift, float scale) {
    traceShape(gc, entity.getShape(), entity.getPosition(), shift, scale);
  }

  /**
   * Trace the shape of an entity to the screen.
   *
   * @param entity entity data
   */
  public static void traceEntity(BasicEntity entity) {
    traceShape(Window.DEFAULT_GRAPHICS_CONTEXT, entity.getShape(), entity.getPosition(), new Vector(0, 0), 1.0f);
    drawText(entity.getPosition(), entity.getName());
  }

  public static void traceShape(Shape s, Vector position) {
    traceShape(Window.DEFAULT_GRAPHICS_CONTEXT, s, position, new Vector(0, 0), 1);
  }

  public static void traceEntities(ArrayList<BasicEntity> entities) {
    entities.forEach(entity -> traceShape(entity.getShape(), entity.getPos()));
    entities.forEach(entity -> {
      drawText(entity.getPosition(), entity.getName());
    });
  }

  /**
   * Trace a shape at a position on the screen.
   *
   * @param gc       graphics context
   * @param s        shape to draw
   * @param position position to draw the shape
   * @param shift    window shift
   * @param scale    window scale
   */
  public static void traceShape(Object gc, Shape s, Vector position, Vector shift, float scale) {

    if (shift == null) {
      shift = new Vector(0, 0); // if shit is equal to null set it to zero to make math work
    }

    if (s instanceof Circle) {
      // if the shape is a circle trace it
      traceCircle(gc, (Circle) s, position, shift, scale);
    }

    if (s instanceof MultiShape) {
      // if the shape is a multi-shape trace all the shapes in the multi-shape
      MultiShape ms = (MultiShape) s;
      for (int i = 0; i < ms.size(); i++) {
        // trace the multi-shape shifted the indexed shift from the original input
        traceShape(gc, ms.getShape(i), ms.getShift(i).addi(position), shift, scale);
      }
    }

    if (s instanceof PolygonShape) {
      // if the shape is a polygon trace the polygon
      tracePolygon(gc, ((PolygonShape) s), position, shift, scale);
    }

    if (s instanceof BoundingBox) {
      // if the shape is a polygon trace the polygon
      BoundingBox boundingBox = (BoundingBox) s;

      tracePolygon(gc, new PolygonShape(boundingBox), position, shift, scale);
    }
  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Filling">

  /**
   * Fill a shape at a position on the screen.
   *
   * @param gc       graphics context
   * @param s        shape to fill
   * @param position position to draw the shape
   * @param shift    window shift
   * @param scale    window scale
   */
  public static void fillShape(Object gc, Shape s, Vector position, Vector shift, float scale) {
    if (shift == null) {
      shift = new Vector(0, 0); // if shit is equal to null set it to zero to make math work
    }

    if (s instanceof Circle) {
      // if the shape is a circle fill it
      fillCircle(gc, (Circle) s, position, shift, scale);
    }

    if (s instanceof MultiShape) {
      // if the shape is a multi-shape fill all the shapes in the multi-shape
      MultiShape ms = (MultiShape) s;
      for (int i = 0; i < ms.size(); i++) {
        // fill the multi-shape shifted the indexed shift from the original input
        fillShape(gc, ms.getShape(i), ms.getShift(i).addi(position), shift, scale);
      }
    }

    if (s instanceof PolygonShape) {
      // if the shape is a polygon fill it
      fillPolygon(gc, ((PolygonShape) s), position, shift, scale);
    }
  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Text Drawing Methods">

  public static void drawText(Vector v, String s, float f) {
    drawText(Window.DEFAULT_GRAPHICS_CONTEXT, v, Vector.zero, 1, f, s);
  }

  public static void drawText(Vector v, String s) {
    drawText(Window.DEFAULT_GRAPHICS_CONTEXT, v, Vector.zero, 1, 1, s);
  }

  /**
   * Draw text to the screen.
   * <p>
   * Using the graphics context get the current font and use it to draw a string to the screen.
   *
   * @param gc     graphics context
   * @param v      position of the text
   * @param shift  screen shift
   * @param scale  screen scale
   * @param fScale font scale
   * @param s      string to draw
   */
  public static void drawText(Object gc, Vector v, Vector shift, float scale, float fScale,
      String s) {
    if (gc instanceof JavaGraphicsContext) {
      JavaGraphicsContext jgc = (JavaGraphicsContext) gc;
      float size = jgc.graphics.getFont().getSize();
      jgc.graphics.setFont(jgc.graphics.getFont().deriveFont(size * fScale));
      jgc.graphics.drawString(s, v.getXi(), v.getYi());
    }
    if (gc instanceof GLGraphicsContext) {
      GLGraphicsContext glg = (GLGraphicsContext) gc;
      // use the current font if it exists, if it doesn't, use the default font
      if (glg.currentFontContext == null) {
        glg.defaultFontContext.renderText(glg, s, v, shift, scale, fScale);
      } else {
        glg.currentFontContext.renderText(glg, s, v, shift, scale, fScale);
      }
    }
  }

  /**
   * Set the font of the graphics context.
   * <p>
   * Handles the difference between java and openGl fonts.
   *
   * @param gc graphics context
   * @param c  font context
   */
  public static void setFont(Object gc, FontContext c) {
    if (gc instanceof JavaGraphicsContext) {
      JavaGraphicsContext jgc = (JavaGraphicsContext) gc;
      if (c != null) {
        jgc.graphics.setFont(c.getJavaFont());
      } else {
        jgc.graphics.setFont(jgc.defaultFontContext);
      }
    }

    if (gc instanceof GLGraphicsContext) {
      GLGraphicsContext glg = (GLGraphicsContext) gc;
      GLFontContext fc;
      if (c != null && !(fc = c.getOpenGLFont()).equals(glg.currentFontContext)) {
        glg.currentFontContext = fc;
      } else if (!glg.defaultFontContext.equals(glg.currentFontContext)) {
        glg.currentFontContext = glg.defaultFontContext;
      }
    }
  }


  /**
   * Get the default font of the graphics context.
   *
   * @param gc graphics context
   * @return default font
   */
  public static FontContext getDefaultFont(Object gc) {
    if (gc instanceof JavaGraphicsContext) {
      JavaGraphicsContext jgc = (JavaGraphicsContext) gc;
      return new FontContext(jgc.defaultFontContext);
    }
    if (gc instanceof GLGraphicsContext) {
      GLGraphicsContext glg = (GLGraphicsContext) gc;
      return new FontContext(glg.defaultFontContext);
    }
    return null;
  }

  /**
   * Calculate the size of a string of text.
   * <p>
   * Sets the total line metrics of the input text context as well as retuning the data.
   *
   * @param gc   graphics context
   * @param tc   text context
   * @return line metrics
   */
  public static TotalLineMetrics calculateTextSize(Object gc, TextContext tc) {
    FontContext fc = tc.getFont(); // get the font of the text context
    TotalLineMetrics tlm; // line metrics
    tlm = calculateTextSize(gc, fc, tc.getFit(), tc.getText(), tc.getCurrentSize(), tc.getCurrentScale());
    tc.setRenderValues(tlm); // set the render values using the line metrics object
    return tlm;
  }

  /**
   * Calculate the metrics of text in a given font.
   *
   * @param gc          graphics context
   * @param fc          font of the text
   * @param type        fit type of the text
   * @param text        text
   * @param customSize  custom font size
   * @param customScale custom font scale
   * @return line metrics for the input text
   */
  public static TotalLineMetrics calculateTextSize(Object gc, FontContext fc, FitType type,
      String text, float customSize, float customScale) {
    float ascent, advance, height, width; // metric variables

    if (gc instanceof GLGraphicsContext || fc.isOpenGL()) {
      // font is openGl based
      GLFontContext glf;
      if (fc == null || fc.getOpenGLFont() == null) {
        glf = ((GLGraphicsContext) gc).defaultFontContext;
      } else {
        glf = fc.getOpenGLFont();
      }

      // calculate the line metrics using the openGl font context
      TotalLineMetrics metrics = glf.calculateMetrics(text, customSize, customScale);
      // transcribe values here
      ascent = metrics.ascent;
      width = metrics.width;
      advance = metrics.advance;

      switch (type) {
        case tight:
          // if the fit is tight use the direct values from the text
          height = metrics.descent - metrics.ascent;
          break;
        case base:
          // if the fit is base, use the base character string to calculate the standard character
          //  height
          TotalLineMetrics metrics2 = glf.calculateMetrics(TextContext.baseChars, customSize, customScale);
//          Game.log(text, "base", -metrics2.ascent);
          height = -metrics2.ascent;
          ascent = metrics2.ascent;
          break;
        default:
          // if the fit is all then the full height of the base characters is used
          TotalLineMetrics metrics3 = glf.calculateMetrics(TextContext.baseChars, customSize, customScale);
          height = metrics3.descent - metrics3.ascent;
          ascent = metrics3.ascent;
          break;
      }

      // create a lime metrics object to transfer line data
      TotalLineMetrics totalLineMetrics = new TotalLineMetrics();
      totalLineMetrics.advance = advance;
      totalLineMetrics.ascent = ascent;
      totalLineMetrics.width = width;
      totalLineMetrics.height = height;
      totalLineMetrics.descent = metrics.descent;

      return totalLineMetrics;
    } else {
      Font font = fc.getJavaFont(); // get the java font context

      if (gc == null) {
        gc = new JavaGraphicsContext();
      }

      JavaGraphicsContext jgc = (JavaGraphicsContext) gc; // cast the java context

      // java fonts do not support fit type
      FontRenderContext frc = ((Graphics2D) jgc.graphics).getFontRenderContext();

      Rectangle2D fontMetrics = font.getStringBounds(text, frc);
      LineMetrics lineMetrics = font.getLineMetrics(text, frc);
      height = lineMetrics.getHeight();
      ascent = lineMetrics.getAscent();
      width = (float) fontMetrics.getWidth();

      TotalLineMetrics totalLineMetrics = new TotalLineMetrics();
      totalLineMetrics.advance = 0;
      totalLineMetrics.ascent = ascent;
      totalLineMetrics.width = width;
      totalLineMetrics.height = height;

      return totalLineMetrics;
    }
  }

  /**
   * Get the font of the graphics context.
   * <p>
   * Gets the current font context, if one doesn't exist use the default font context.
   *
   * @param gc graphics context
   * @return font
   */
  public static FontContext getFont(Object gc) {
    if (gc instanceof JavaGraphicsContext) {
      JavaGraphicsContext jgc = (JavaGraphicsContext) gc;
      Font font;
      if ((font = jgc.graphics.getFont()) != null) {
        return new FontContext(font);
      } else {
        return new FontContext(jgc.defaultFontContext);
      }
    }
    if (gc instanceof GLGraphicsContext) {
      GLGraphicsContext glg = (GLGraphicsContext) gc;
      if (glg.currentFontContext != null) {
        return new FontContext(glg.currentFontContext);
      } else {
        return new FontContext(glg.defaultFontContext);
      }
    }
    return null;
  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Image Drawing Methods">

  public static void drawSprite(Object gc, Sprite s, Vector pos) {
    drawSprite(gc, s, pos, Vector.zero, 0);
  }

  /**
   * Draw a sprite to the screen.
   * pos is the top left corner
   *
   * @param gc  graphics context
   * @param s   sprite
   * @param pos position
   */
  public static void drawSprite(Object gc, Sprite s, Vector pos, Vector center, float rotation) {
    if (gc instanceof JavaGraphicsContext) {
      // if java graphics is selected draw the buffered image to the screen at the given position
      JavaGraphicsContext jgc = (JavaGraphicsContext) gc;
      jgc.graphics.drawImage(s.getBufferedImage(), pos.getXi(), pos.getYi(), null);
    } else {
      GLGraphicsContext glgc = (GLGraphicsContext) gc;

      if(glgc.hasFilter() && glgc.filter.isActive()) {
        glgc.filter.start(glgc);
      }

      ByteBufferImage bbi = s.getByteBufferImage();

      // calculate the other corner of the sprite
      Vector pos2 = pos.addi(s.getSize());

      // internal dimensions
      Vector ins = bbi.getInternalSize();
      Vector inp = bbi.getInternalPosition();

      // the loading size, the original width of the sprite in pixels
      Vector size = bbi.getOriginalSize();

      // use the original scale to scale the internal dimensions to texture coordinates
      Vector texPos2 = inp.addi(ins).divi(size);
      Vector texPos = inp.divi(size);

      // snap positions to the pixel grid to give crisp lines
      if (s.round) {
        pos.round();
        pos2.round();
      }

      // set up the drawing of a texture buffer to screen
      glEnable(GL_TEXTURE_2D);
      glBindTexture(GL_TEXTURE_2D, bbi.getTexID()); // bind the original texture
      // texture scaling uses nearest to make scaling look nice
      glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
      glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

      if(glgc.hasFilter() && glgc.filter.isActive()) {
        glgc.filter.glRenderQuad(glgc, Vector.zero, 1.0f,  pos, pos2, texPos, texPos2, rotation);
      } else {
        glRenderQuad(glgc, Vector.zero, 1.0f,  pos, pos2, texPos, texPos2, center, rotation);
      }

      glDisable(GL_TEXTURE_2D);

      if(glgc.hasFilter() && glgc.filter.isActive()) {
        glgc.filter.end(glgc);
      }
    }
  }

  /**
   * Draw a matte sprite.
   * <p>
   * A matte sprite is a sprite that fills an entire area. In this case the function repeats the
   * sprite to fill the entire frame size.
   *
   * @param gc     graphics context
   * @param sprite sprite to matte
   * @param frame  frame size
   * @param gScale game scale
   */
  public static void drawMatte(Object gc, Sprite sprite, Vector frame, float gScale) {
    if (sprite != null) {
      sprite = sprite.getScaled(gScale); // get scaled instance

      // find the amount of images that will fit in the frame
      Vector div = frame.divi(sprite.getSize());
      // stack the images in a grid to fit the screen
      sprite = sprite.clone().multiplyImage(div);
      // center the matte sprite in the frame window
      Vector pos = frame.subi(sprite.getSize()).scalei(0.5f);
      // draw the sprite
      drawSprite(gc, sprite, pos);
    }
  }

  /**
   * Draw a a rooms background sprites.
   * <p>
   * Uses the game shift, scale, and frame to cut out a section of the larger background sprite.
   * Automatically stops at edges. TODO: possibly implement screen momentum.
   *
   * @param gc     graphics context
   * @param room   Room object
   * @param gShift game shift, or game location
   * @param gFrame game frame, or game screen size
   * @param gScale game scale, or engine to game position scale
   * @param wShift window shift, or shift of the game window in the game screen
   */
  public static void drawBackground(Object gc, Room room,
      float gScale, Vector gShift, Vector gFrame, Vector wShift) {

    ArrayList<Tuple<Vector, Sprite>> bList = room.getRenderSprites(gShift, gFrame, gScale);

    for (Tuple<Vector, Sprite> dataPair : bList){
      Sprite sclSpr = dataPair.b.getScaled(gScale);
      Vector sclSize = sclSpr.getSize();
      Vector sclPos = dataPair.a.scalei(gScale);

      gShift = gShift.roundi();
      Vector tPoint = Vector.max(gShift.subi(sclPos), new Vector(0, 0));
      Vector bPoint = Vector.min(sclPos.addi(sclSize), gShift.addi(gFrame));
      Sprite outSpr = sclSpr.getSubSpr(tPoint.getX(), tPoint.getY(), bPoint.getX(), bPoint.getY());

      Vector sPoint = Vector.max(sclPos.subi(gShift), new Vector(0, 0));
      drawSprite(gc, outSpr, sPoint.addi(wShift));
    }

//    sprite = sprite.getScaled(gScale); // get the correct image size & scale
//
//    // get the background subsprite
//    sprite = sprite.getSubsprite(gShift.getX(), gShift.getY(), gFrame.getX(), gFrame.getY());
//
//    // draw the sprite
//    drawSprite(gc, sprite, wShift);
  }
//
//  public static Vector getEntitySpriteShift(Entity entity) {
//    // add the sprite shift to the entity position
//    return entity.getPosition().addi(entity.getSpriteShift());
//  }
//
//  public static Vector getSpriteRenderShift(Entity entity, Sprite sprite, Vector pos, float scale) {
//
//    // add sprite rendering shifts
//    return pos.addi(getSpriteRenderShift(sprite, scale));
//  }

  public static Vector getSpriteRenderShift(Sprite sprite, float scale) {
    if (sprite == null || !sprite.hasData()) {
      return new Vector(0, 0);
    }

    Vector cPos = null;
    if (sprite.hasInternalShift()){
      cPos = sprite.getInternalOffset();
    }

    // calculate the origin of the sprite
    return Vector.originShift(sprite.getSize().scalei(-0.5f), sprite.getSize(), sprite.getOrigin(), cPos, scale);
  }

//  /**
//   * Draw the sprite of an entity.
//   *
//   * @param gc     graphics context
//   * @param data   concatenated sprite drawing data
//   * @param shift  total shift value
//   * @param gScale game scale
//   */
//  public static void drawEntitySprite(Object gc, SpriteData data, Vector shift, float gScale) {
//    Vector pos = data.spriteLoc.scalei(gScale).addi(shift); // apply the window scale and shift
//
//    if (data.entity.hasFilter() && data.entity.getFilter().isActive()) {
//      pushFilter(gc, data.entity.getFilter());
//    }
//
//    // get the scaled version of the given sprite and draw the sprite at the calculated position
//    drawSprite(gc, data.sprite.getScaled(gScale), pos, Vector.zero, data.sprite.getRotation());
//
//    if (data.entity.hasFilter() && data.entity.getFilter().isActive()) {
//      popFilter(gc);
//    }
//  }

  public static void drawEntity(Entity entity, float scale) {
    drawEntitySprite(Window.DEFAULT_GRAPHICS_CONTEXT, entity, entity.getSprite(), Vector.zero, scale);
  }

  /**
   * Draw the sprite of an entity.
   *
   * @param gc     graphics context
   * @param entity entity of the sprite
   * @param sprite sprite to draw
   * @param shift  total shift value
   * @param gScale game scale
   */
  public static void drawEntitySprite(Object gc, Entity entity, Sprite sprite, Vector shift, float gScale) {

    if (sprite.isLayered()) {
      for(Sprite lspr : ((LayeredSprite) sprite).getSprites()) {
        drawEntitySprite(gc, entity, lspr, shift, gScale);
      }
      return;
    }

    if (entity.hasFilter()) {
      pushFilter(gc, entity.getFilter());
    }

    Sprite newSprite = sprite.getScaled(gScale);

    // if the sprite can't be drawn get the error sprite from the game
    if (newSprite == null || !newSprite.hasData()) {
      newSprite = Game.debugSprite.clone();
    }

    Vector center = entity.getPos().addi(getSpriteRenderShift(sprite, 1.0f));
    center.scale(gScale);
    center.add(shift);

    Vector pos = center.addScaledi(entity.getSpriteShift(), gScale);

    // get the scaled version of the given sprite and draw the sprite at the calculated position
    drawSprite(gc, newSprite, pos, center, sprite.getRotation());

    if (entity.hasFilter()) {
      popFilter(gc);
    }
  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Base Drawing Methods">

  public static Vector fixRender(Vector v, float scale, Vector shift) {
    return v.scalei(scale).addi(shift).roundi();
  }

  public static void setColor(Color c) {
    setColor(Window.DEFAULT_GRAPHICS_CONTEXT, c);
  }

  /**
   * Set the drawing color of the graphics context.
   *
   * @param gc graphics context
   * @param c  color
   */
  public static void setColor(Object gc, Color c) {
    if (gc instanceof JavaGraphicsContext) {
      JavaGraphicsContext jgc = (JavaGraphicsContext) gc;
      if (c != null) {
        jgc.graphics.setColor(c);
      } else {
        jgc.graphics.setColor(jgc.defaultColor);
      }
    } else {
      GLGraphicsContext ggc = (GLGraphicsContext) gc;
      if (gc == null) {
        float[] s = c.getComponents(null);
        GL11.glColor4f(s[0], s[1], s[2], s[3]);
      } else if (c != null) {
        if (!c.equals(ggc.currentColor) || c.getAlpha() != ggc.currentColor.getAlpha()) {
          ggc.currentColor = c;
          float[] s = c.getComponents(null);
          GL11.glColor4f(s[0], s[1], s[2], s[3]);
        }
      } else if (!ggc.defaultColor.equals(ggc.currentColor)) {
        float[] s = ggc.defaultColor.getComponents(null);
        ggc.currentColor = ggc.defaultColor;
        GL11.glColor4f(s[0], s[1], s[2], s[3]);
      }
    }
  }

  /**
   * Get the drawing color of the graphics context.
   *
   * @param gc graphics context
   * @return color
   */
  public static Color getColor(Object gc) {
    if (gc instanceof JavaGraphicsContext) {
      JavaGraphicsContext jgc = (JavaGraphicsContext) gc;
      return jgc.graphics.getColor();
    } else {
      float[] previous = new float[4];
      GL11.glGetFloatv(GL_CURRENT_COLOR, previous);
      return new Color(previous[0], previous[1], previous[2], previous[3]);
    }
  }


  /**
   * Get the drawing color of the graphics context.
   *
   * @param gc graphics context
   * @return color
   */
  public static void pushColor(Object gc, Color color) {
    GLGraphicsContext glgc = (GLGraphicsContext) gc;
    setColor(gc, color);
    glgc.colors.push(color);
  }

  /**
   * Get the drawing color of the graphics context.
   *
   * @param gc graphics context
   * @return color
   */
  public static Color popColor(Object gc) {
    GLGraphicsContext glgc = (GLGraphicsContext) gc;
    Color color;

    if (glgc.colors.isEmpty()) {
      color = null;
    } else {
      color = glgc.colors.pop();
    }

    setColor(gc, color);

    return color;
  }

  /**
   * Set the drawing color of the graphics context.
   *
   * @param gc graphics context
   * @param filter filter
   */
  public static void pushFilter(Object gc, GLFilter filter) {
    if (filter != null) {
      if (gc instanceof GLGraphicsContext) {
        GLGraphicsContext ggc = (GLGraphicsContext) gc;

        if (ggc.filter != null) {
          ggc.filters.add(ggc.filter);
          ggc.filter = filter;
        } else {
          ggc.filter = filter;
        }
      }
    }
  }

  /**
   * Set the drawing color of the graphics context.
   *
   * @param gc graphics context
   * @return  filter
   */
  public static GLFilter popFilter(Object gc) {
    if (gc instanceof GLGraphicsContext) {
      GLGraphicsContext ggc = (GLGraphicsContext) gc;

      if (!ggc.filters.isEmpty()) {
        ggc.filter = ggc.filters.pop();
      } else {
        ggc.filter = null;
      }

      return ggc.filter;
    }
    return null;
  }

  public static void drawLine(Vector a, Vector b) {
    drawLine(Window.DEFAULT_GRAPHICS_CONTEXT, a, b);
  }


  /**
   * Draw a line to the screen.
   *
   * @param gc graphics context
   * @param a  first point
   * @param b  second point
   */
  public static void drawLine(Object gc, Vector a, Vector b) {
    if (gc instanceof JavaGraphicsContext) {
      JavaGraphicsContext jgc = (JavaGraphicsContext) gc;
      jgc.graphics.drawLine(a.getXi(), a.getYi(), b.getXi(), b.getYi());
    } else {
//      GLGraphicsContext glgc = (GLGraphicsContext) gc;
//      if (glgc.hasFilter() && glgc.filter.isActive()) {
//        glgc.fboData.start(glgc, glgc.filter);
//      }

      glBegin(GL11.GL_LINES);
      GL11.glVertex3f(a.getXi(), a.getYi(), 0);
      GL11.glVertex3f(b.getXi(), b.getYi(), 0);
      glEnd();

//      if (glgc.hasFilter() && glgc.filter.isActive()) {
//        glgc.fboData.draw(glgc, glgc.filter, new BoundingBox(a, b), Vector.zero, 1.0f, Vector.zero);
//      }
//
//      if (glgc.hasFilter() && glgc.filter.isActive()) {
//        glgc.fboData.end(glgc, glgc.filter);
//      }
    }
  }

  public static void drawArrow(Vector a, Vector b) {
    drawArrow(Window.DEFAULT_GRAPHICS_CONTEXT, a, b, 15.0f);
  }

  public static void drawArrow(Vector a, Vector b, float edge) {
    drawArrow(Window.DEFAULT_GRAPHICS_CONTEXT, a, b, edge);
  }

  public static void drawArrow(Object gc, Vector a, Vector b, float edge) {
    drawLine(gc, a, b);
    Vector dir = a.subi(b).normalizei();
    Vector up = dir.perpi();
    Vector down = up.negatei();

    up = dir.addi(up).scalei(0.5f);
    down = dir.addi(down).scalei(0.5f);

    drawLine(gc, b, b.addScaledi(up, edge));
    drawLine(gc, b, b.addScaledi(down, edge));
  }


  /**
   * Generate a scaled and shifted polygon.
   * <p>
   * Preforms a position shift a scale and then another shift.
   *
   * @param p     polygon
   * @param pos   position
   * @param shift shift
   * @param scale scale
   * @return
   */
  public static Polygon generatePolygon(PolygonShape p, Vector pos, Vector shift, float scale) {
    Polygon poly = new Polygon();
    for (int i = 0; i < p.getVertices().size(); i++) {
      Vector v1 = p.getVertices().get(i);
      v1 = fixRender(v1.addi(pos), scale, shift);
      poly.addPoint(v1.getXi(), v1.getYi());
    }
    return poly;
  }

  /**
   * Draw a polygon shape to the screen.
   *
   * @param gc    graphics context
   * @param p     polygon
   * @param pos   position of the polygon
   * @param shift shift
   * @param scale scale
   */
  public static void drawPolygon(Object gc, PolygonShape p, Vector pos, Vector shift, float scale) {

    if (!p.isValid()) {
      return;
    }

    // if the polygon has a trace shape render the polygon as border
    if (p.hasTraceShape()) {
      PolygonShape ts = p.getTraceShape();
      Vector a, b = null;
      Vector c, d = null;
      // loop through corresponding vertices and fill in the border as separate polygons
      for (int i = 0; i < ts.getVertices().size() + 1; i++) {
        int index = i % ts.getVertices().size(); // loop around to the first index
        a = ts.getVertices().get(index);
        c = p.getVertices().get(index);

        if (b != null && d != null) {
          PolygonShape polygon = new PolygonShape();
          polygon.addVertex(b.getX(), b.getY());
          polygon.addVertex(a.getX(), a.getY());
          polygon.addVertex(c.getX(), c.getY());
          polygon.addVertex(d.getX(), d.getY());

          fillPolygon(gc, polygon, pos, shift, scale);
        }

        b = a;
        d = c;
      }
    } else if (gc instanceof GLGraphicsContext) {
      GLGraphicsContext glgc = (GLGraphicsContext) gc;
      if (glgc.hasFilter() && glgc.filter.isActive()) {
        glgc.fboData.start(glgc, glgc.filter);
      }

      glBegin(GL_LINE_LOOP);
      // loop through the indexes and render the vertexes
      for (int i = 0; i < p.getVertices().size() + 1; i++) {
        int index = i % p.getVertices().size(); // loop around to the first index
        Vector v1 = p.getVertices().get(index);
        v1 = fixRender(v1.addi(pos), scale, shift);
        glVertex2f(v1.getX(), v1.getY());
      }
      glEnd();

      if (glgc.hasFilter() && glgc.filter.isActive()) {
        glgc.fboData.draw(glgc, glgc.filter, new BoundingBox(p), pos, scale, shift);
      }

      if (glgc.hasFilter() && glgc.filter.isActive()) {
        glgc.fboData.end(glgc, glgc.filter);
      }
    } else {
      // shift and draw the polygon
      JavaGraphicsContext jgc = (JavaGraphicsContext) gc;
      jgc.graphics.drawPolygon(generatePolygon(p, pos, shift, scale));
    }
  }

  /**
   * Trace a polygon on the screen.
   *
   * @param gc    graphics context
   * @param p     polygon shape
   * @param pos   polygon position
   * @param shift window shift
   * @param scale window scale
   */
  public static void tracePolygon(Object gc, PolygonShape p, Vector pos, Vector shift, float scale) {
    drawPolygon(gc, p, pos, shift, scale);
  }

  /**
   * Fill a polygon shape on the screen.
   *
   * @param gc    graphics context
   * @param p     polygon shape
   * @param pos   polygon position
   * @param shift widow shift
   * @param scale window scale
   */
  public static void fillPolygon(Object gc, PolygonShape p, Vector pos, Vector shift, float scale) {

    if (!p.isValid()) {
      return;
    }

    // if the polygon has a trace shape render the polygon as border
    if (p.hasTraceShape()) {
      PolygonShape ts = p.getTraceShape();
      Vector a, b = null;
      Vector c, d = null;
      // loop through corresponding vertices and fill in the border as separate polygons
      for (int i = 0; i < ts.getVertices().size() + 1; i++) {
        int index = i % ts.getVertices().size(); // loop around to the first index
        a = ts.getVertices().get(index);
        c = p.getVertices().get(index);

        if (b != null && d != null) {
          PolygonShape polygon = new PolygonShape();
          polygon.addVertex(b.getX(), b.getY());
          polygon.addVertex(a.getX(), a.getY());
          polygon.addVertex(c.getX(), c.getY());
          polygon.addVertex(d.getX(), d.getY());

          fillPolygon(gc, polygon, pos, shift, scale);
        }

        b = a;
        d = c;
      }
    }

    if (gc instanceof GLGraphicsContext) {
      GLGraphicsContext glgc = (GLGraphicsContext) gc;
      if (glgc.hasFilter() && glgc.filter.isActive()) {
        glgc.fboData.start(glgc, glgc.filter);
      }

      glBegin(GL_POLYGON);
      // loop through the indexes and render the vertexes
      for (int i = 0; i < p.getVertices().size() + 1; i++) {
        int index = i % p.getVertices().size(); // loop around to the first index
        Vector v1 = p.getVertices().get(index);
        v1 = fixRender(v1.addi(pos), scale, shift);
        glVertex2f(v1.getX(), v1.getY());
      }
      glEnd();

      if (glgc.hasFilter() && glgc.filter.isActive()) {
        glgc.fboData.draw(glgc, glgc.filter, new BoundingBox(p), pos, scale, shift);
      }

      if (glgc.hasFilter() && glgc.filter.isActive()) {
        glgc.fboData.end(glgc, glgc.filter);
      }
    } else {
      // shift and draw the polygon
      JavaGraphicsContext jgc = (JavaGraphicsContext) gc;
      jgc.graphics.drawPolygon(generatePolygon(p, pos, shift, scale));
    }
  }


  public static void fillRect(Object gc, Vector topLeft, Vector bottomRight) {
    fillRect(gc, topLeft.getXi(), topLeft.getYi(), bottomRight.getXi(), bottomRight.getYi());
  }

  /**
   * Fill a rectangle on the screen.
   *
   * @param gc     graphics context
   * @param x      top left x coordinate
   * @param y      top left y coordinate
   * @param x2      top left x coordinate
   * @param y2      top left y coordinate
   */
  public static void fillRect(Object gc, int x, int y, int x2, int y2) {
    if (gc instanceof JavaGraphicsContext) {
      ((JavaGraphicsContext) gc).graphics.fillRect(x, y, x2 - x, y2 - y);
    } else {
      GLGraphicsContext glgc = (GLGraphicsContext) gc;
//      if (glgc.hasFilter() && glgc.filter.isActive()) {
//        glgc.fboData.start(glgc, glgc.filter);
//      }

      glBegin(GL11.GL_QUADS);
      {
        glVertex2f(x, y);
        glVertex2f(x, y2);
        glVertex2f(x2, y2);
        glVertex2f(x2, y);
      }
      glEnd();

//      if (glgc.hasFilter() && glgc.filter.isActive()) {
//        glgc.fboData.draw(glgc, glgc.filter, new BoundingBox(x, y, x + width, y + height),
//            Vector.zero, 1.0f, Vector.zero);
//      }
//
//      if (glgc.hasFilter() && glgc.filter.isActive()) {
//        glgc.fboData.end(glgc, glgc.filter);
//      }
    }
  }

  public static void glRenderQuad(GLGraphicsContext glgc, Vector pos, float scale,
      Vector topLeft, Vector bottomRight, Vector topLeftT, Vector bottomRightT, float rotation) {
      glRenderQuad(glgc, pos, scale, topLeft, bottomRight, topLeftT, bottomRightT, Vector.zero, rotation);
  }
  /**
   * Render a Textured Quad
   * @param glgc
   * @param pos
   * @param scale
   * @param topLeft
   * @param bottomRight
   * @param topLeftT
   * @param bottomRightT
   */
  public static void glRenderQuad(GLGraphicsContext glgc, Vector pos, float scale,
    Vector topLeft, Vector bottomRight, Vector topLeftT, Vector bottomRightT, Vector center, float rotation) {

    if (rotation != 0) {

      Vector topRight = new Vector(bottomRight.getX(), topLeft.getY());
      Vector bottomLeft = new Vector(topLeft.getX(), bottomRight.getY());

//      center = topLeft.addi(bottomRight).scalei(0.5f);

      topLeft = topLeft.subi(center).roti(rotation).addi(center);
      bottomLeft = bottomLeft.subi(center).roti(rotation).addi(center);
      bottomRight = bottomRight.subi(center).roti(rotation).addi(center);
      topRight = topRight.subi(center).roti(rotation).addi(center);

      glBegin(GL_TRIANGLE_STRIP);
      {
        glTexCoord2f(topLeftT.getX(), topLeftT.getY());
        glVertex2f(topLeft.getX(), topLeft.getY());

        glTexCoord2f(topLeftT.getX(), bottomRightT.getY());
        glVertex2f(bottomLeft.getX(), bottomLeft.getY());

        glTexCoord2f(bottomRightT.getX(), topLeftT.getY());
        glVertex2f(topRight.getX(), topRight.getY());

        glTexCoord2f(bottomRightT.getX(), bottomRightT.getY());
        glVertex2f(bottomRight.getX(), bottomRight.getY());
      }
      glEnd();

    } else {

      glBegin(GL_TRIANGLE_STRIP);
      {
        glTexCoord2f(topLeftT.getX(), topLeftT.getY());
        glVertex2f(topLeft.getX(), topLeft.getY());

        glTexCoord2f(topLeftT.getX(), bottomRightT.getY());
        glVertex2f(topLeft.getX(), bottomRight.getY());

        glTexCoord2f(bottomRightT.getX(), topLeftT.getY());
        glVertex2f(bottomRight.getX(), topLeft.getY());

        glTexCoord2f(bottomRightT.getX(), bottomRightT.getY());
        glVertex2f(bottomRight.getX(), bottomRight.getY());
      }
      glEnd();
    }
  }

  /**
   * Render a Textured Quad
   * @param glg
   * @param pos
   * @param scale
   * @param q
   */
  public static void glRenderQuad(GLGraphicsContext glg, Vector pos, float scale, STBTTAlignedQuad q) {
    Vector topLeft = new Vector(q.x0(), q.y0());
    Vector bottomRight = new Vector(q.x1(), q.y1());

    Vector topLeftT = new Vector(q.s0(), q.t0());
    Vector bottomRightT = new Vector(q.s1(), q.t1());

    glRenderQuad(glg, pos, scale, topLeft, bottomRight, topLeftT, bottomRightT, 0);
  }


  /**
   * Fill a circle on the screen.
   *
   * @param gc    graphics context
   * @param c     circle to fill
   * @param pos   position of the circle
   * @param shift window shift
   * @param scale window scale
   */
  public static void fillCircle(Object gc, Circle c, Vector pos, Vector shift, float scale) {
    fillCircle(gc, c.getRadius(), pos, shift, scale);
  }

  /**
   * Fill a circle on the screen.
   *
   * @param gc     graphics context
   * @param radius radius of the circle
   * @param pos    position of the circle
   * @param shift  window shift
   * @param scale  window scale
   */
  public static void fillCircle(Object gc, float radius, Vector pos, Vector shift, float scale) {
    radius *= scale;
    // modify the render position with the drawing data
    pos = fixRender(pos, scale, shift);
    if (gc instanceof GLGraphicsContext) {
      GLGraphicsContext glgc = (GLGraphicsContext) gc;
      if (glgc.hasFilter() && glgc.filter.isActive()) {
        glgc.fboData.start(glgc, glgc.filter);
      }

      // draw circles as a triangle fan with 360 segments
      glBegin(GL_TRIANGLE_FAN);
      for (float i = 0; i < 360; i += 0.1f) {
        float rad = i * (float) (Math.PI / 180.0f);
        glVertex2f(pos.getX() + (float) Math.sin(rad) * radius,
            pos.getY() + (float) Math.cos(rad) * radius);
      }
      glEnd();

      if (glgc.hasFilter() && glgc.filter.isActive()) {
        glgc.fboData.draw(glgc, glgc.filter, new BoundingBox(radius, radius, -radius, -radius), pos, scale, shift);
      }

      if (glgc.hasFilter() && glgc.filter.isActive()) {
        glgc.fboData.end(glgc, glgc.filter);
      }

    } else {
      JavaGraphicsContext jgc = (JavaGraphicsContext) gc;

      // clamp the radius to an integer and then fill a java oval
      int r = (int) Math.floor(radius + 0.5);
      jgc.graphics.fillOval(pos.getXi() - r, pos.getYi() - r, r * 2, r * 2);
    }
  }

  /**
   * Trace a circle on the screen.
   *
   * @param gc    graphics context
   * @param c     circle to fill
   * @param pos   position of the circle
   * @param shift window shift
   * @param scale window scale
   */
  public static void traceCircle(Object gc, Circle c, Vector pos, Vector shift, float scale) {
    traceCircle(gc, c.getRadius(), pos, shift, scale);

  }

  /**
   * Trace a circle on the screen.
   *
   * @param gc     graphics context
   * @param radius radius of the circle
   * @param pos    position of the circle
   * @param shift  window shift
   * @param scale  window scale
   */
  public static void traceCircle(Object gc, float radius, Vector pos, Vector shift, float scale) {
    radius *= scale;
    pos = fixRender(pos, scale, shift);
    if (gc instanceof GLGraphicsContext) {
      GLGraphicsContext glgc = (GLGraphicsContext) gc;
      if (glgc.hasFilter() && glgc.filter.isActive()) {
        glgc.fboData.start(glgc, glgc.filter);
      }


      int count = 360;
      int div = 30;

      // draw a circle as a polygon with 360 identical segments
      glBegin(GL_LINE_LOOP);
      for (int i = 0; i < count / div; i++) {
        float rad = i * div * (float) (Math.PI / 180.0f);
        glVertex2f((float) Math.sin(rad) * radius + pos.getX(),
            (float) Math.cos(rad) * radius + pos.getY());
      }
      glEnd();


      if (glgc.hasFilter() && glgc.filter.isActive()) {
        glgc.fboData.draw(glgc, glgc.filter, new BoundingBox(radius, radius, -radius, -radius), pos, scale, shift);
      }

      if (glgc.hasFilter() && glgc.filter.isActive()) {
        glgc.fboData.end(glgc, glgc.filter);
      }

    } else {
      JavaGraphicsContext jgc = (JavaGraphicsContext) gc;
      int r = (int) Math.floor(radius + 0.5);
      jgc.graphics.drawOval(pos.getXi() - r, pos.getYi() - r, r * 2, r * 2);
    }
  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Shift/Scaling Calculators">

  /**
   * Calculate the appropriate shift value for the game.
   * <p>
   * Takes into consideration player position, screen size, and room size. TODO: add velocity
   *
   * @param game game instance
   * @return game shift
   */
  public static Vector calculateGameShift(Game game) {
    if (game == null || game.getWorld() == null) {
      return new Vector(0, 0);
    }

    Room room = game.getWorld().getLoadedRoom();

    Vector min = new Vector(0, 0);
    Vector max = game.getDefaultGameSize();

    if (room.hasShape()) {
      BoundingBox box = new BoundingBox(room.getShape());
      min = box.getMin();
      max = box.getMax();
    }

    Vector v2 = calculateShiftMinMax(min, max, game.getGameScreenSize(), game.getPlayer().getPosition());
    return v2.addScaledi(game.getGameScreenSize(), -0.5f);
  }

  /**
   * Calculate the game scale.
   * <p>
   * The scale is the largest whole number that will scale the game window to be smaller the window
   * frame.
   *
   * @param g game instance
   * @return game scale
   */
  public static float calculateGameScale(Game g) {
    Vector frame = new Vector(g.getWindow().getSize());
    return fixDimension(frame, g.getGameScreenSize(), 1.0f);
  }

  /**
   * Calculate the menu scale.
   * <p>
   * The scale is the largest whole number that will scale the menu window to be smaller the window
   * frame.
   *
   * @param g game instance
   * @return game scale
   */
  public static float calculateMenuScale(Game g) {
    Vector frame = new Vector(g.getWindow().getSize());
    return fixDimension(frame, g.getMenuScreenSize(), 0.5f);
  }

  /**
   * Calculate the window shift.
   * <p>
   * The window shift is the vector needed to center the current screen in the center of the program
   * window.
   *
   * @param g game instance
   * @return game scale
   */
  public static Vector calculateWindowShift(Game g) {
    Vector scaledSize = g.getCurrentScreenSize().scalei(g.getCurrentScreenScale());
    Vector frame = new Vector(g.getWindow().getSize());
    return frame.subi(scaledSize).scalei(0.5f);
  }

  /**
   * Calculate the screen shift for the in box inside the outer box.
   * <p>
   * Acts like a rubber band attached from the input position to the center of the inner box.
   *
   * @param min  minimum corner of the window
   * @param max  maximum corner of the window
   * @param in  inner window
   * @param ip  inner position
   * @return shift
   */
  public static Vector calculateShiftMinMax(Vector min, Vector max, Vector in, Vector ip) {
    Vector shift = ip.clone();

    if (max.getX() < ip.getX() + (in.getX() / 2)) {
      // if the outer x is larger then we just set the shift to center the inner window in the outer
      shift.setX(max.getX() - (in.getX() / 2));
    } else if (min.getX() > ip.getX() - (in.getX() / 2)) {
      // if the shift will place the inner window outside the outer then set the shift to the
      //  size of the window
      shift.setX(min.getX() + (in.getX() / 2));
    }

    // repeat the same as above with the y values
    if (max.getY() < ip.getY() + (in.getY() / 2)) {
      shift.setY(max.getY() - (in.getY() / 2));
    } else if (min.getY() > ip.getY() - (in.getY() / 2)) {
      shift.setY(min.getY() + (in.getY() / 2));
    }

    return shift;
  }

  /**
   * Calculate the inner window scale.
   * <p>
   * Calculate the largest whole number scalar that, when the inner window is scaled by the value,
   * the scaled inner window is inside the outer.
   *
   * @param outer outer window
   * @param inner inner window
   * @return scalar
   */
  public static float fixDimension(Vector outer, Vector inner, float p) {
    float scale;

    if (outer.getY() == 0 || inner.getX() == 0 || outer.getX() == 0 || inner.getY() == 0) {
      scale = p;
    } else {

      float x = outer.getX() / inner.getX();
      float y = outer.getY() / inner.getY();

      scale = Math.min(p * ((int) (x / p)), p * ((int) (y / p)));
    }

    return Math.max(scale, p);
  }

  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Debug Drawing Methods">

  /**
   * Draw debug related data to the screen.
   *
   * @param gc     graphics context
   * @param r      room data to display
   * @param gScale game scale
   * @param gShift game shift
   * @param wShift window shift
   */
  public static void drawDebug(Object gc, Room r, float gScale, Vector gShift, Vector wShift) {
    drawDebug(gc, r.getAllEntities(), gScale, gShift, wShift);
  }

  public static void drawDebug(Object gc, ArrayList<Entity> entities,  float gScale, Vector gShift, Vector wShift) {

    // go through all the entities in the room and trace their shapes.
    for (Entity e : entities) {
      if (e.getShape() != null && e.getShape().getDebugColor() != null) {
        setColor(gc, e.getShape().getDebugColor());
      }

      setColor(gc, Color.black);

      traceShape(gc, e.getShape(), e.getPos(), wShift.subi(gShift), gScale);

      if (e instanceof Vehicle) {
        setColor(gc, Color.red);
        Vehicle v = ((Vehicle) e);
        traceShape(gc, v.iShape, e.getPos(), wShift.subi(gShift), gScale);
      }

      setColor(gc, Color.green);

      traceShape(gc, e.getBoundingBox(), e.getPos(), wShift.subi(gShift), gScale);

      drawText(gc, e.getPos(), wShift.subi(gShift), gScale, 0.5f, e.getName());

      setColor(gc, Color.black);

//      for (Shape shape : e.getShapeMap().values()) {
//        if (shape.getDebugColor() != null) {
//          setColor(gc, shape.getDebugColor());
//        }
//        traceShape(gc, shape, e.getPos(), wShift.subi(gShift), gScale);
//        setColor(gc, Color.black);
//      }

      setColor(gc, Color.red);
      fillCircle(gc, 1, e.getPosition(), wShift.subi(gShift), gScale);
      setColor(gc, Color.black);

      if (e.hasSprite()) {
        // draw custom sprite positions for non layered and layered sprites
        drawLocations(gc, e.getSprite(), e.getPos(),
            new Vector(0, 0), new Vector(0, 0), gScale, gShift, wShift);
      }
    }
  }

  /**
   * Draw Sprite Locations and offsets
   * @param gc
   * @param pSpr
   * @param ePos
   * @param lAcc
   * @param gLoc
   * @param gScale
   * @param gShift
   * @param wShift
   */
  public static void drawLocations(Object gc, Sprite pSpr, Vector ePos, Vector lAcc, Vector gLoc,
      float gScale, Vector gShift, Vector wShift) {
    if (pSpr instanceof LayeredSprite) {
      for (Sprite cSpr : ((LayeredSprite) pSpr).getSprites()) {
        drawLocations(gc, cSpr, ePos,
            lAcc.addi(cSpr.getChildPosition()),
            gLoc.addi(cSpr.getGameLocation()),
            gScale, gShift, wShift);
      }
    } else {
      if (pSpr.hasChildPosition()) {
        Color before = getColor(gc);
        setColor(Color.magenta);
        fillCircle(gc, 1, ePos.addi(lAcc).addi(pSpr.getChildPosition()), wShift.subi(gShift), gScale);
        setColor(before);
      }

      if (pSpr.hasGameLocation()) {
        Color before = getColor(gc);
        setColor(Color.cyan);
        fillCircle(gc, 1, ePos.addi(gLoc).addi(pSpr.getGameLocation()), wShift.subi(gShift), gScale);
        setColor(before);
      }
    }
  }

  /**
   * Draw debug text data to the screen.
   *
   * @param gc graphics context
   */
  public static void drawDebugHUD(Object gc) {
    // place the text in the top left corner
    Logger.loggerMenu.updateVisual(gc);
    // draw the text to the screen

    Logger.pushDebugTime("drawDebugMenu");
    renderMenuEntity(gc, Logger.loggerMenu, 1.0f);
    Logger.pushDebugTime("drawDebugMenu");
  }

  /**
   * Draw a collision tree.
   *
   * @param massTree collision tree
   */
  public static void drawTree(MassTree massTree) {
    if (massTree != null && massTree.entity != null) {
      RenderEngine.drawArrow(massTree.entity.getPos(),
          massTree.entity.getPos().addScaledi(massTree.outNormal, 10));
      if (massTree.hitChildren != null) {
        for (MassTree child : massTree.hitChildren) {

          RenderEngine.drawArrow(massTree.entity.getPos(), child.entity.getPos());
          drawTree(child);
        }
      }
    }
  }



  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Sprite Drawing Methods">

  /**
   * Add image data to the layer lists.
   *
   * @param layers layer lists
   * @param data   image data
   */
  public static void putLayer(HashMap<Float, ArrayList<SpriteData>> layers, SpriteData data) {

    if (!layers.containsKey(data.sprite.getGlobalLayer())) {
      // if the layer list doesn't contain the sprites layer, create the layer and add data to it
      ArrayList<SpriteData> arr = new ArrayList<>();
      arr.add(data);
      layers.put(data.sprite.getGlobalLayer(), arr);
    } else {
      // add the sprite data to the correct layer list
      layers.get(data.sprite.getGlobalLayer()).add(data);
    }
  }

  /**
   * Remove sprites from a sprite list that lie entirely outside of the draw frame.
   *
   * @param dList   sprite data list
   * @param gScale  game scale
   * @param gShift  game shift
   * @param gFrame  game frame size
   */
  public static ArrayList<SpriteData> removeOffscreen(ArrayList<SpriteData> dList,
      float gScale, Vector gShift, Vector gFrame) {

    BoundingBox frameBox = new BoundingBox(gFrame); // generate a frame bounding box
    ArrayList<SpriteData> result = new ArrayList<>(); // create the list to return

    for (SpriteData spriteData : dList) { // loop through all the sprites in the list
      // scale the position of the sprite to follow the game scale
      Vector spritePos = spriteData.spriteLoc.scalei(gScale);

      // scale the sprites size relative to the game size and generate a bounding box
      BoundingBox spriteBox = new BoundingBox(spriteData.sprite.getSize().scalei(gScale));

      // find if the sprite is within the frame bounding box
      // the frame is positioned at the game shift
      if (Engine.boundingBoxCollision(gShift, frameBox, spritePos, spriteBox)) {
        result.add(spriteData);
      }
    }

    return result;
  }

  /**
   * Extract sprite rendering data from a list of entities.
   * <p>
   * Extracts all multi layered images and various image modifications present in an entity and
   * generates a flattened list of to pass to a rendering method.
   *
   * @param eList entity list
   * @return a list of sprite data including global location, parent entity, and, layer location
   *  For a full list look at the SpriteData data-types.
   */
  public static ArrayList<SpriteData> getSpriteData( ArrayList<Entity> eList) {
    ArrayList<SpriteData> dataList = new ArrayList<>(); // output list
    // stack for layered sprites
    Stack<Tuple<LayeredSprite, Iterator<Sprite>>> layeredStack = new Stack<>();

    Vector gameAcc, childAcc; // layer location accumulator
    Vector eShift; // entity sprite shift
    Sprite sprite, parent;

    // iterate through the entity list
    for (Entity e : eList) {
      eShift = e.getPosition().addi(e.getSpriteShift());
      LinkedList<Entity> parentLL = new LinkedList<>();
      Entity parentEntity = e;

      parentLL.addFirst(parentEntity);
      while (parentEntity.hasParent()) {
        parentEntity = parentEntity.getParent();
        parentLL.addFirst(parentEntity);
      }

      if (e.isVisible()) {
        sprite = e.getSprite();
        gameAcc = new Vector(0, 0);
        parent = null;

        // if the sprite can't be drawn get the error sprite from the game
        if (sprite == null || !sprite.hasData()) {
          sprite = Game.debugSprite.clone();
        }

        // loop through multi layered sprites and any branches they might have
        while (sprite != null) {

          // if the sprite is a layered sprite
          if (sprite.isLayered()) {
            LayeredSprite layeredSprite = (LayeredSprite) sprite;
            // get an iterator for the sprites in the layered sprite
            Iterator<Sprite> sprIterator = layeredSprite.getSprites().iterator();

            // If the iterator has a sprite available get the next sprite
            sprite = sprIterator.hasNext() ? sprIterator.next() : null;

            // Add the iterator with the sprite to the stack of sprites
            layeredStack.add(new Tuple<>(layeredSprite, sprIterator));

            // Add the layered sprites location to the layer location of the child sprites
            gameAcc.add(layeredSprite.getGameLocation());

            // set the parent placeholder to the current sprite
            parent = layeredSprite;
          } else {
            // create a sprite render instance
            SpriteData spriteData = new SpriteData();

            // set the data of the render instance
            spriteData.entity = e;
            spriteData.spriteLoc = eShift.addi(getSpriteRenderShift(sprite, 1.0f));
            spriteData.parentLL = parentLL;
            spriteData.sprite = sprite;
            spriteData.parent = parent;

            // add the render instance to the return list
            dataList.add(spriteData);

            // set the sprite placeholder to null to signal finding the next sprite
            sprite = null;
          }

          // if there is no sprite selected and the layer stack has entries pop the last layered
          // sprite with more children to loop through
          while (sprite == null && !layeredStack.isEmpty()) {
            Tuple<LayeredSprite, Iterator<Sprite>> data = layeredStack.pop();

            // if the layered sprite has a next sprite to look at, get it and push the current
            // layered sprite back on to the stack
            // set the parent to the layered sprite to cover when the last layered sprite has
            // finished and the one below it on the stack has been popped
            if (data.b.hasNext()) {
              sprite = data.b.next();
              layeredStack.push(data);
              parent = data.a;
            } else {
              // if the layered sprite has no more children undo the layer location shift
              gameAcc.sub(data.a.getLocalLocation());
            }
          }
        }
      }
    }

    return dataList;
  }

  public static int zFilt(int i, Object a, Object b) {
    return i != 0 ? i : Float.compare(a.hashCode(), b.hashCode());
  }

  public static int clHelper(SpriteData a, SpriteData b) {

    if (b.entity == a.entity || (b.parent != null && a.parent == b.parent)) {

//      Game.log("[A1]:\n\t" + a.sprite.getLocalLayer() + " "  + a + "\n\t" + b.sprite.getLocalLayer() + " " + b);
      if (a.sprite.getLocalLayer() == b.sprite.getLocalLayer()) {
        if (a.sprite != b.sprite) {
          Game.log("[ZF_S]:\n\t" + a + "\n\t" + b);
        }
        return zFilt(0, a.sprite, b.sprite);
      }
//      Game.log("[A2]");
      return Float.compare(a.sprite.getLocalLayer(), b.sprite.getLocalLayer());
    } else {

      boolean commonParent = (a.parentLL.peek() == b.parentLL.peek() && b.parentLL.peek() != null);

//      Game.log("[B1]:\n\t" + a + "\n\t" + b);
      if (commonParent && (a.sprite.getChildLayer() != 0 || b.sprite.getChildLayer() != 0)) {
        return Float.compare(a.sprite.getChildLayer(), b.sprite.getChildLayer());
      }

      LinkedList<Entity> aPLL = new LinkedList<>(a.parentLL);
      LinkedList<Entity> bPLL = new LinkedList<>(b.parentLL);

      Vector agPos = Vector.zero;
      Vector bgPos = Vector.zero;

      while (!aPLL.isEmpty() && !bPLL.isEmpty() && commonParent) {
        aPLL.poll();
        bPLL.poll();
        commonParent = (aPLL.peek() == bPLL.peek());
      }

      boolean ABase = true;
      boolean BBase = true;

      Vector offset;

      while (!aPLL.isEmpty()) {
        ABase = false;
        Entity entity = aPLL.pop();

        agPos = agPos.addi(entity.getLocalPosition());

        if ((offset = entity.getChildPosition()) != null) {
          agPos = agPos.addi(offset);
        }

        if ((offset = entity.getChildOffset()) != null) {
          agPos = agPos.addi(offset);
        }
      }

      while (!bPLL.isEmpty()) {
        BBase = false;
        Entity entity = bPLL.pop();

        bgPos = bgPos.addi(entity.getLocalPosition());

        if ((offset = entity.getChildPosition()) != null) {
          bgPos = bgPos.addi(offset);
        }

        if ((offset = entity.getChildOffset()) != null) {
          bgPos = bgPos.addi(offset);
        }
      }

//      Game.log("[B2A]:\n\t" + ABase + "\n\t" + BBase);

      agPos = agPos.addi(a.sprite.getGameLocation());
      bgPos = bgPos.addi(b.sprite.getGameLocation());

//      Game.log("[B2B]:\n\t" + agPos + "\n\t" + bgPos);
//      Game.log("[B3]:\n\t" + (cAShift.getY() + a.sprite.getChildPosition().getY()) + "\n\t" + (cBShift.getY() + b.sprite.getChildPosition().getY()));

      if ((ABase || BBase)) {
//        Game.log("[B3]:\n\t" + a.sprite.getChildPosition() + "\n\t" + b.sprite.getChildPosition());

        if (ABase ^ BBase) {
//          Game.log("[B4]:\n\t" + a.sprite.getChildLayer() + "\n\t" + b.sprite.getChildLayer());
          int comp = Float.compare(a.sprite.getChildLayer(), b.sprite.getChildLayer());
          if (comp != 0) {
            return comp;
          }
        }

        if (ABase) {
          agPos = agPos.addi(a.sprite.getChildPosition());
        } else {
          bgPos = bgPos.addi(b.sprite.getChildPosition());
        }

        int comp = Float.compare(agPos.getY(), bgPos.getY());

        if (comp == 0) {
          if (BBase) {
            return 1;
          } else {
            return -1;
          }
        }

        return zFilt(comp, a.entity, b.entity);
      }

      return zFilt(Float.compare(agPos.getY(), bgPos.getY()), a.entity, b.entity);
    }
  }

  public static int compareLayers(SpriteData a, SpriteData b) {

//    Game.log("=====", a, b);
    int out = clHelper(a, b);
//    Game.log("-----", out);
    return  out;

  }

  /**
   * Draw all of the entities in a room to the screen.
   * <p>
   * Separates all of the entity sprites into layer lists and then uses sorting algorithms to fix
   * the drawing order.
   *
   * @param gc     graphics context
   * @param r      room
   * @param gScale game scale
   * @param gShift game shift
   * @param gFrame game window size
   * @param gwShift window shift
   */
  public static void drawRoom(Object gc, Room r, float gScale, Vector gShift, Vector gFrame, Vector gwShift) {

    // if the room has a background sprite draw it
    if (r.hasSprite()) {
      drawBackground(gc, r, gScale, gShift, gFrame, gwShift);
    }

    drawEntities(gc, r.getEntities(), gScale, gShift, gFrame, gwShift);
  }

  public static  HashMap<Float, ArrayList<SpriteData>> LastLayers = null;

  public static void drawEntities(Object gc, ArrayList<Entity> entities, float gScale, Vector gShift, Vector gFrame, Vector wShift) {
    HashMap<Float, ArrayList<SpriteData>> layers = new HashMap<>(); // create layer lists

    ArrayList<SpriteData> dataList = getSpriteData(entities);
    dataList = removeOffscreen(dataList, gScale, gShift, gFrame);
    dataList.forEach((spriteData -> putLayer(layers, spriteData)));

    // sort the layers
    ArrayList<Float> arr = new ArrayList<>(layers.keySet());
    if (!arr.isEmpty()) {
      quickSort(0, arr.size() - 1, arr, Float::compare);
    }

    Vector v = wShift.subi(gShift);

    // sort each layer and draw it
    for (float layer : arr) {
      ArrayList<SpriteData> data = layers.get(layer); // extract layer data
      // sort the layer by local layer and then by y position

      if (LastLayers != null && LastLayers.containsKey(layer)) {
        ArrayList<SpriteData> nList = new ArrayList<>(data.size());
        for (SpriteData id : data) {
          if (id.sprite.id > nList.size()) {
            nList.add(id);
          } else {
            nList.add(id.sprite.id, id);
          }
        }

        data = nList;
      }

      data = quickSort(0, data.size() - 1, data, RenderEngine::compareLayers);
//      Game.log(data);

      for (int i = 0; i < data.size(); i++) {
        SpriteData id = data.get(i);
        id.sprite.id = i;
        id.entity.renderHook(gc, v, gScale);
        drawEntitySprite(gc, id.entity, id.sprite, v, gScale);
      }
    }

    LastLayers = layers;
  }

  /**
   * Preform a quick-sort on a set of data.
   *
   * @param l lowest index to sort
   * @param h highest index to sort
   * @param arr array of values to sort
   * @param comp comparison function
   * @param <T> data-type to sort
   * @return sorted list
   */
  private static <T> ArrayList<T> quickSort(int l, int h, ArrayList<T> arr, Comparator<T> comp) {
    int i = l;
    int j = h;

    int mid = l + (h - l) / 2;

    T pivot = arr.get(mid);

    while (i <= j) {
      if (i != mid) {
        while (comp.compare(arr.get(i), pivot) < 0) {
          i++;
          if (i == mid) {
            break;
          }
        }
      }


      if (j != mid) {
        while (comp.compare(arr.get(j), pivot) > 0) {
          j--;
          if (j == mid) {
            break;
          }
        }
      }

      if (i <= j) {
        if (i != j) {
          T temp = arr.get(i);
          arr.set(i, arr.get(j));
          arr.set(j, temp);
        }
        i++;
        j--;
      }
    }
    if (l < j) {
      quickSort(l, j, arr, comp);
    }
    if (i < h) {
      quickSort(i, h, arr, comp);
    }

    return arr;
  }

  /**
   * Struct to pass sprite and entity data together.
   */
  static class SpriteData {

    Entity entity;
    Sprite sprite;
    LinkedList<Entity> parentLL;
    Vector spriteLoc;
    Sprite parent;

    @Override
    public String toString() {
      String out = "SD[e:" + entity + ", s:" + sprite.name;

//      if (parent != null) {
//        out += ", p:" + parent;
//      }

      out += "]";

      return out;
    }
  }

  // </editor-fold>
}
