package schooled;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.GLFW_FALSE;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_BACKSPACE;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_DOWN;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_END;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_ENTER;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_F1;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_F10;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_F11;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_F12;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_F2;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_F3;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_F4;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_F5;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_F6;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_F7;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_F8;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_F9;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_HOME;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_INSERT;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_CONTROL;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_SHIFT;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_PAGE_DOWN;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_PAGE_UP;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT_CONTROL;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT_SHIFT;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_SPACE;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_TAB;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_UP;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_V;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_1;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_2;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_3;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_4;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_5;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_6;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_7;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_8;
import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;
import static org.lwjgl.glfw.GLFW.GLFW_RESIZABLE;
import static org.lwjgl.glfw.GLFW.GLFW_SAMPLES;
import static org.lwjgl.glfw.GLFW.GLFW_TRUE;
import static org.lwjgl.glfw.GLFW.GLFW_VISIBLE;
import static org.lwjgl.glfw.GLFW.glfwCreateWindow;
import static org.lwjgl.glfw.GLFW.glfwDefaultWindowHints;
import static org.lwjgl.glfw.GLFW.glfwDestroyWindow;
import static org.lwjgl.glfw.GLFW.glfwGetClipboardString;
import static org.lwjgl.glfw.GLFW.glfwGetCursorPos;
import static org.lwjgl.glfw.GLFW.glfwGetPrimaryMonitor;
import static org.lwjgl.glfw.GLFW.glfwGetVideoMode;
import static org.lwjgl.glfw.GLFW.glfwInit;
import static org.lwjgl.glfw.GLFW.glfwMakeContextCurrent;
import static org.lwjgl.glfw.GLFW.glfwPollEvents;
import static org.lwjgl.glfw.GLFW.glfwSetClipboardString;
import static org.lwjgl.glfw.GLFW.glfwSetErrorCallback;
import static org.lwjgl.glfw.GLFW.glfwSetFramebufferSizeCallback;
import static org.lwjgl.glfw.GLFW.glfwSetKeyCallback;
import static org.lwjgl.glfw.GLFW.glfwSetMouseButtonCallback;
import static org.lwjgl.glfw.GLFW.glfwSetScrollCallback;
import static org.lwjgl.glfw.GLFW.glfwSetWindowPos;
import static org.lwjgl.glfw.GLFW.glfwShowWindow;
import static org.lwjgl.glfw.GLFW.glfwSwapBuffers;
import static org.lwjgl.glfw.GLFW.glfwSwapInterval;
import static org.lwjgl.glfw.GLFW.glfwTerminate;
import static org.lwjgl.glfw.GLFW.glfwWindowHint;
import static org.lwjgl.glfw.GLFW.glfwWindowShouldClose;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_CULL_FACE;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.GL_LIGHTING;
import static org.lwjgl.opengl.GL11.GL_PROJECTION;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glClearColor;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glGenTextures;
import static org.lwjgl.opengl.GL11.glLoadIdentity;
import static org.lwjgl.opengl.GL11.glMatrixMode;
import static org.lwjgl.opengl.GL11.glOrtho;
import static org.lwjgl.opengl.GL11.glViewport;
import static org.lwjgl.system.MemoryUtil.NULL;

import controls.Button;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWCharModsCallback;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWMouseButtonCallback;
import org.lwjgl.glfw.GLFWScrollCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import schooled.containers.EntityHolder;
import schooled.entities.EntityArea;
import schooled.menu.Menu;
import schooled.physics.Circle;
import schooled.physics.Vector;
import schooled.visuals.FBOWrapper;
import schooled.visuals.GLFontContext;
import schooled.visuals.GLGraphicsContext;
import schooled.visuals.sprite.Sprite;

/**
 * Window object. Contains the data that is displayed to the screen.
 * <p></p>
 * Program written by Joseph Straceski
 * <p>
 * contact: straceski.joseph@gmail.com, https://github.com/Crepox
 */
public class Window {

  public static GLFontContext DEFAULT_FONT_CONTEXT = null;

  // input buffers
  private boolean[] lastKeyBuf = new boolean[65536];
  private boolean[] keyBuf = new boolean[65536];
  private boolean[] keyBufC = new boolean[65536];
  private boolean[] mouseBuf = new boolean[15];
  private boolean[] mouseBufC = new boolean[65536];
  private boolean[] lastMouseBuf = new boolean[15];
  private boolean[] typedKeyBuf = new boolean[65536];
  private boolean[] typedMouseBuf = new boolean[15];
  private float yScroll = 0;
  private float xScroll = 0;
  private String typeBuffer = ""; // uses key input from opengl to get when multiple keys are pressed
  // this works because the window input loop is much faster than the game loop.

  Graphics2D graphicsBuffer; // what gets drawn to the screen.

  // Game object
  Game game;

  // the width and height of the window
  private int width, height;
  private long window;
  private boolean windowUpdated = false;
  private boolean opengl = true;
  public static Object DEFAULT_GRAPHICS_CONTEXT = null;

  private EntityArea cursor;

  private FBOWrapper fboData = null;
  private FBOWrapper fboData2 = null;

  public void setDefaultGLGC(GLGraphicsContext defaultGraphicsContext) {
    DEFAULT_GRAPHICS_CONTEXT = defaultGraphicsContext;
    defaultGraphicsContext.fboData = fboData;
    defaultGraphicsContext.fboDataChild = fboData2;
  }

  /**
   * Returns if the window is being drawn with opengl or not.
   *
   * @return if the window is opengl return true
   */
  public boolean isGL() {
    return opengl;
  }

  /**
   * Initialize some button constants by name id. These are common keys that are not numbers or
   * letters.
   */
  public void loadButtonConstants() {
    game.setConstant("F1", new Button(GLFW_KEY_F1));
    game.setConstant("F2", new Button(GLFW_KEY_F2));
    game.setConstant("F3", new Button(GLFW_KEY_F3));
    game.setConstant("F4", new Button(GLFW_KEY_F4));
    game.setConstant("F5", new Button(GLFW_KEY_F5));
    game.setConstant("F6", new Button(GLFW_KEY_F6));
    game.setConstant("F7", new Button(GLFW_KEY_F7));
    game.setConstant("F8", new Button(GLFW_KEY_F8));
    game.setConstant("F9", new Button(GLFW_KEY_F9));
    game.setConstant("F10", new Button(GLFW_KEY_F10));
    game.setConstant("F11", new Button(GLFW_KEY_F11));
    game.setConstant("F12", new Button(GLFW_KEY_F12));
    game.setConstant("LEFT", new Button(GLFW_KEY_LEFT));
    game.setConstant("UP", new Button(GLFW_KEY_UP));
    game.setConstant("RIGHT", new Button(GLFW_KEY_RIGHT));
    game.setConstant("DOWN", new Button(GLFW_KEY_DOWN));
    game.setConstant("PAGE_UP", new Button(GLFW_KEY_PAGE_UP));
    game.setConstant("PAGE_DOWN", new Button(GLFW_KEY_PAGE_DOWN));
    game.setConstant("HOME", new Button(GLFW_KEY_HOME));
    game.setConstant("SPACE", new Button(GLFW_KEY_SPACE));
    game.setConstant("END", new Button(GLFW_KEY_END));
    game.setConstant("INSERT", new Button(GLFW_KEY_INSERT));
    game.setConstant("ESCAPE", new Button(GLFW_KEY_ESCAPE));
    game.setConstant("LEFT_SHIFT", new Button(GLFW_KEY_LEFT_SHIFT));
    game.setConstant("RIGHT_SHIFT", new Button(GLFW_KEY_RIGHT_SHIFT));
    game.setConstant("LEFT_CONTROL", new Button(GLFW_KEY_LEFT_CONTROL));
    game.setConstant("RIGHT_CONTROL", new Button(GLFW_KEY_RIGHT_CONTROL));
    game.setConstant("BACKSPACE", new Button(GLFW_KEY_BACKSPACE));
    game.setConstant("ENTER", new Button(GLFW_KEY_ENTER));

    game.setConstant("MOUSE_BUTTON_1", new Button(GLFW_MOUSE_BUTTON_1, Button.ButtonType.Mouse));
    game.setConstant("MOUSE_BUTTON_2", new Button(GLFW_MOUSE_BUTTON_2, Button.ButtonType.Mouse));
    game.setConstant("MOUSE_BUTTON_3", new Button(GLFW_MOUSE_BUTTON_3, Button.ButtonType.Mouse));
    game.setConstant("MOUSE_BUTTON_4", new Button(GLFW_MOUSE_BUTTON_4, Button.ButtonType.Mouse));
    game.setConstant("MOUSE_BUTTON_5", new Button(GLFW_MOUSE_BUTTON_5, Button.ButtonType.Mouse));
    game.setConstant("MOUSE_BUTTON_6", new Button(GLFW_MOUSE_BUTTON_6, Button.ButtonType.Mouse));
    game.setConstant("MOUSE_BUTTON_7", new Button(GLFW_MOUSE_BUTTON_7, Button.ButtonType.Mouse));
    game.setConstant("MOUSE_BUTTON_8", new Button(GLFW_MOUSE_BUTTON_8, Button.ButtonType.Mouse));
  }

  /**
   * Create a Window object
   *
   * @param g the game object instance
   */
  public void init(Game g) {
    System.setProperty("sun.java2d.opengl", "true"); // use opengl
    System.setProperty("sun.java2d.pmoffscreen", "false"); // don't capture offscreen mouse inputs
    System.setProperty("java.awt.headless", "false"); // something
    System.setProperty("org.lwjgl.util.NoChecks", "true"); // dont generate lots of errors

    this.game = g;
    // Setup an error callback. The default implementation
    // will print the error message in System.err.
    GLFWErrorCallback.createPrint(System.err).set();

    // Initialize GLFW. Most GLFW functions will not work before doing this.
    if (!glfwInit()) {
      throw new IllegalStateException("Unable to initialize GLFW");
    }

    // Configure the window
    glfwDefaultWindowHints();
    glfwWindowHint(GLFW_SAMPLES, 4);
    glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
    glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);

    GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
    Vector size = new Vector(gd.getDisplayMode().getWidth(),
        gd.getDisplayMode().getHeight()).scalei(0.5f);

    // Create the window
    window = glfwCreateWindow(size.getXi(), size.getYi(), "Schooled", NULL, NULL);
    if (window == NULL) {
      throw new RuntimeException("Failed to create the GLFW window");
    }

    glfwSetFramebufferSizeCallback(window, (window, width, height) -> {
      this.width = width;
      this.height = height;
      this.fboData.setSize(width, height);
      this.fboData2.setSize(width, height);

    });

    // Get the resolution of the primary monitor
    GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
    // Center the window
    glfwSetWindowPos(window, (vidmode.width() - size.getXi()) / 2,
        (vidmode.height() - size.getYi()) / 2);

    // Setup a type buffer callback, this is a special input function that preserves input order
    // between game cycles to accurately input typed keys.
    GLFW.glfwSetCharModsCallback(window, new GLFWCharModsCallback() {
      @Override
      public void invoke(long window, int codepoint, int mods) {
        typeBuffer += (char) codepoint;

      }
    });

    // Setup a key callback. It will be called every time a key is pressed, repeated or released.
    glfwSetKeyCallback(window, new GLFWKeyCallback() {
      @Override
      public void invoke(long window, int key, int scancode, int action, int mods) {
        // TODO Auto-generated method stub
        if (key < 0) {
          return;
        }
        if (key == GLFW_KEY_TAB && action != GLFW.GLFW_RELEASE) {
          typeBuffer += "  ";
        }

        typedKeyBuf[key] = keyBuf[key] && action != GLFW.GLFW_RELEASE;
        keyBuf[key] = action != GLFW.GLFW_RELEASE;
      }
    });

    // Setup a mouse callback. It will be called every time a mouse button is pressed or released.
    glfwSetMouseButtonCallback(window, new GLFWMouseButtonCallback() {
      @Override
      public void invoke(long window, int button, int action, int mods) {
        typedMouseBuf[button] = mouseBuf[button] && action != GLFW.GLFW_RELEASE;
        mouseBuf[button] = action != GLFW_RELEASE;
      }
    });

    glfwSetScrollCallback(window, new GLFWScrollCallback() {
      @Override
      public void invoke(long window, double xoffset, double yoffset) {
        xScroll += xoffset;
        yScroll += yoffset;
      }
    });

    // Make the OpenGL context current
    glfwMakeContextCurrent(window);

    // Initialize the graphical sub-display of glfw
    GL.createCapabilities();

    // Set the clear color to slight grey
    glClearColor(0.9f, 0.9f, 0.9f, 0.0f);
    // Enable v-sync
    glfwSwapInterval(0);

    fboData = new FBOWrapper();
    fboData2 = new FBOWrapper();
    DEFAULT_FONT_CONTEXT = new GLFontContext("resources/fonts/SGK100.ttf");
    DEFAULT_GRAPHICS_CONTEXT = new GLGraphicsContext(DEFAULT_FONT_CONTEXT, fboData, fboData2);

    cursor = new EntityArea(g, new Circle(1.0f), null);
    cursor.setFunc(cursor::addInteraction);
    cursor.setCollides(false);
    cursor.setInteractAll(true);

    // Make the window visible
    glfwShowWindow(window);
  }

  /**
   * Take a screenshot. Saved as a BufferedImage.
   *
   * @return a sprite containing the current sprite buffer
   */
  public Sprite getScreen() {
    Vector v = getSize();

    GL11.glReadBuffer(GL11.GL_FRONT);
    int width = v.getXi();
    int height = v.getYi();
    int bpp = 4; // Assuming a 32-bit display with a byte each for red,
    // green, blue, and alpha.
    ByteBuffer buffer = BufferUtils.createByteBuffer(width * height * bpp);
    GL11.glReadPixels(0, 0, width, height, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);

    BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

    for (int x = 0; x < width; x++) {
      for (int y = 0; y < height; y++) {
        int i = (x + (width * y)) * bpp;
        int r = buffer.get(i) & 0xFF;
        int g = buffer.get(i + 1) & 0xFF;
        int b = buffer.get(i + 2) & 0xFF;
        image.setRGB(x, height - (y + 1), (0xFF << 24) | (r << 16) | (g << 8) | b);
      }
    }

    return new Sprite(image);
  }

  /**
   * Get the size of the screen in Vector format.
   *
   * @return the size of the window
   */
  public Vector getSize() {
    return new Vector(width, height);
  }

  /**
   * Has the window been requested to close.
   *
   * @return if the window has been requested to close return true
   */
  public boolean shouldWindowClose() {
    return glfwWindowShouldClose(window);
  }

  /**
   * Initialize an opengl draw.
   */
  public void initWindow() {
    glDisable(GL_CULL_FACE);
    glDisable(GL_TEXTURE_2D);
    glDisable(GL_LIGHTING);
    glDisable(GL_DEPTH_TEST);

    glViewport(0, 0, width, height);
    glMatrixMode(GL_PROJECTION);
    glLoadIdentity();
    glOrtho(0, width, height, 0, -1, 1);
    glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);


  }

  public void refreshInputs() {
    if ((keyDown(GLFW_KEY_LEFT_CONTROL) || keyDown(GLFW_KEY_RIGHT_CONTROL)) && keyHit(GLFW_KEY_V)) {
      typeBuffer += getClipboard();
    }

    cursor.setPos(getMousePos());

    if (mouseHit(GLFW.GLFW_MOUSE_BUTTON_1)) {
      EntityHolder holder = cursor.getContainer();

      if (holder instanceof Menu) {
        ((Menu) holder).setSelectedArea(null);
      }

      cursor.collideThisTick();
    }

    lastKeyBuf = keyBufC.clone();
    lastMouseBuf = mouseBufC.clone();

    keyBufC = keyBuf.clone();
    mouseBufC = mouseBuf.clone();

  }



  public void setClip(String str) {
    if (str != null) {
      glfwSetClipboardString(window, str);
    }
  }

  public boolean mouseDown(int i) {
    return mouseBufC[i];
  }

  public boolean mouseReleased(int i) {
    return !mouseBufC[i] && lastMouseBuf[i];
  }

  public boolean mouseHit(int i) {
    return mouseBufC[i] && !lastMouseBuf[i] && isMouseInside();
  }

  public boolean mouseTyped(int i) {
    return typedMouseBuf[i] || mouseHit(i);
  }

  public boolean keyDown(int i) {
    return keyBufC[i];
  }

  public boolean keyHit(int i) {
    return keyBufC[i] && !lastKeyBuf[i];
  }

  public boolean keyReleased(int i) {
    return !keyBufC[i] && lastKeyBuf[i];
  }

  public boolean keyTyped(int i) {
    return typedKeyBuf[i] || keyHit(i);
  }

  public float getYScroll() {
    return yScroll;
  }

  public float getXScroll() {
    return xScroll;
  }

  /**
   * Refresh the display.
   */
  public void update() {
    xScroll = 0;
    yScroll = 0;

    clearTypeBuffer();

    glfwSwapBuffers(window);
    glfwPollEvents();
  }

  /**
   * Close the display neatly.
   */
  public void close() {
    glfwFreeCallbacks(window);
    glfwDestroyWindow(window);
    glfwTerminate();
    glfwSetErrorCallback(null).free();
  }

  /**
   * Check if the mouse is inside the window or not.
   *
   * @return if the mouse is inside the window return true
   */
  public boolean isMouseInside() {
    Vector pos = getMousePos();
    Vector size = getSize();
    return pos.getX() < size.getX() && pos.getX() > 0 && pos.getY() < size.getY() && pos.getY() > 0;
  }

  /**
   * Get the global position of the mouse in vector format.
   *
   * @return the location of te mouse
   */
  public Vector getMousePos() {
    DoubleBuffer xBuffer = BufferUtils.createDoubleBuffer(1);
    DoubleBuffer yBuffer = BufferUtils.createDoubleBuffer(1);
    glfwGetCursorPos(window, xBuffer, yBuffer);
    return new Vector(xBuffer.get(0), yBuffer.get(0));
  }

  public String  getClipboard() {
    return glfwGetClipboardString(window);
  }

  public EntityArea getCursor() {
    return cursor;
  }

  public boolean[] getKeyBuf() {
    return keyBuf;
  }

  public boolean[] getMouseBuf() {
    return mouseBuf;
  }

  public boolean[] getTypedKeyBuf() {
    return typedKeyBuf;
  }

  public boolean[] getTypedMouseBuf() {
    return typedMouseBuf;
  }

  public String getTypeBuffer() {
    return typeBuffer;
  }

  public void clearTypeBuffer() {
    typeBuffer = "";
  }

  public void setKeyBuf(boolean[] keyBuf) {
    this.keyBuf = keyBuf;
  }

  public void setMouseBuf(boolean[] mouseBuf) {
    this.mouseBuf = mouseBuf;
  }

  public void setTypedKeyBuf(boolean[] typedKeyBuf) {
    this.typedKeyBuf = typedKeyBuf;
  }

  public void setTypedMouseBuf(boolean[] typedMouseBuf) {
    this.typedMouseBuf = typedMouseBuf;
  }

  public void setTypeBuffer(String typeBuffer) {
    this.typeBuffer = typeBuffer;
  }

  public FBOWrapper getFBO() {
    return fboData;
  }
  public FBOWrapper getFBO2() {
    return fboData;
  }
}
