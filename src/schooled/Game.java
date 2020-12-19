package schooled;

import controls.Button;
import controls.Button.ButtonType;
import controls.ButtonData;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.function.BooleanSupplier;
import schooled.containers.Room;
import schooled.containers.World;
import schooled.datatypes.LOG_TYPE;
import schooled.engines.Engine;
import schooled.engines.Logger;
import schooled.engines.RenderEngine;
import schooled.entities.Entity;
import schooled.entities.EntityArea;
import schooled.entities.ItemEntity;
import schooled.entities.Player;
import schooled.event.ConditionalEvent;
import schooled.event.Event;
import schooled.event.TimedEvent;
import schooled.event.events.Move;
import schooled.event.events.Remove;
import schooled.loaders.DataLoader;
import schooled.loaders.SpriteLoader;
import schooled.loaders.Lookup;
import schooled.menu.Menu;
import schooled.menu.MenuEntity;
import schooled.menu.MenuLoader;
import schooled.menu.TextArea;
import schooled.physics.Circle;
import schooled.physics.Vector;
import schooled.visuals.FontContext;
import schooled.visuals.filters.GLFilter;
import schooled.visuals.GLFontContext;
import schooled.visuals.GLFontData;
import schooled.visuals.sprite.Animation;
import schooled.visuals.sprite.Sprite;

/**
 * The game engine container, the brain of the game.
 * <p></p>
 * Program written by Joseph Straceski
 * <p>
 * contact: straceski.joseph@gmail.com, https://github.com/Crepox
 */
public class Game implements Runnable {

  // <editor-fold defaultstate="collapsed" desc="Instance Fields">

  // <editor-fold defaultstate="collapsed" desc="Game Logic Variables">
  Engine e;
  Window w;
  Thread t;
  Player player;
  World world;
  Entity gameEntity;
  EntityArea gameInteractArea;
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Game Loop Variables">
  private boolean gameLoaded;
  private boolean paused;
  private boolean hold;
  private boolean loaded;
  private boolean running;
  private boolean hang;
  private boolean opengl;
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Menu Variables">
  private HashMap<String, Menu> menus;
  private Menu loadedMenu = null;
  private boolean menuLoaded;
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Debug Variables">
  private int debugDecimals = 3;
  public static boolean splitLogLines = false;
  private String debugText = "";
  public static Sprite debugSprite;
  public static boolean DEBUG_LOG = true;
  public static ArrayList<LOG_TYPE> LOG_FILTER = new ArrayList<>();
  Lookup lookup = new Lookup();
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Synced Action Storage Variables">
  ArrayList<ConditionalEvent> condEvents;
  ArrayList<TimedEvent> timedEvents;
  ArrayList<Event> syncedEvents;
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Sprite Scaling and Shifting Variables">
  private float baseScale = 1.0f;
  private static float maxScale = 4.0f;
  private float engineScale = 1.0f;
  private float gameScreenScale = 0.0f;
  private float menuScreenScale = 0.0f;
  Vector engineShift = new Vector(0, 0);
  Vector windowShift;
  Vector gameShift;
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Timing Variables">
  private static final float SECOND = (float) 1e9;
  private int refreshRate = 144;
  private int refreshRateIndex = 0;
  private boolean capFrameRate = true;
  private boolean stepping; // dep_check
  private boolean step;     // dep_check
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Input Variables">
  float currentDelay = 0;
  float keyDelay = 0.03f; // 10ms

  private boolean[] previousKeys;
  private boolean[] previousMouse;
  private boolean[] typedKeys;
  private boolean[] typedMouse;
  private boolean[] keys;
  private boolean[] mouse;

  private HashMap<String, Button> buttonConstants = new HashMap<String, Button>();
  private HashMap<String, Button> buttonControls = new HashMap<String, Button>();
  public Button Up, Down, Left, Right, Sprint, Exit,
      Interact, Action,
      Left_Click, Right_Click,
      Debug_Step, Debug, Debug2,
      Reload_Game, Reload_Resources,
      Backspace, Debug_Switch, Enter, Debug_Step_Toggle;
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Screen Size Variables">
  public static Vector defaultGameSize = new Vector(400, 225);
  public static Vector gameScreenSize = null;
  public static Vector defaultMenuSize = new Vector(800, 450);
  public static Vector menuScreenSize = null;
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Rendering Variables">
  private Object visualContext = null;
  private String defaultFontLabel = "default";
  private FontContext currentFont = null;
  private HashMap<String, FontContext> fonts;
  private HashMap<String, Sprite> sprites;
  private Sprite defaultMatte = null;
  private GLFilter globalFilter = null;
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Game Logic">
  private Event startEvent = null;
  // </editor-fold>

  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Getters and Setters">

  // --------------------------------------------------------------- Not my code
  private long variableYieldTime, lastTime;

  /**
   * Get the default background sprite
   *
   * @return the sprite
   */
  public Sprite getDefaultMatte() {
    if (defaultMatte != null) {
      return defaultMatte;
    }
    return getImage("Default_Matte");
  }

  /**
   * Get the current visual context of the game.
   *
   * @return visual context
   */
  public Object getVisualContext() {
    return visualContext;
  }

  /**
   * Update and set the current visual context of the game.
   * <p>
   * Does not directly effect any visuals.
   */
  public void generateVisualContext() {
    this.visualContext = RenderEngine.generateGraphicsContext(this);
  }

  /**
   * Is the game loaded and ready to run.
   *
   * @return is the game loaded
   */
  public boolean isGameLoaded() {
    return gameLoaded;
  }

  /**
   * Is are the game resources loaded and ready to run.
   *
   * @return is the game loaded
   */
  public boolean isLoaded() {
    return loaded;
  }

  /**
   * Set whether the game is loaded or not
   */
  public void setGameLoaded(boolean gameLoaded) {
    this.gameLoaded = gameLoaded;
  }

  /**
   * Get the base scaling factor for loaded sprites
   *
   * @return a float representing a scalar used in converting the pixels in the loaded sprite to the
   * pixels on screen
   */
  public float getBaseScale() {
    return baseScale;
  }

  /**
   * Set the base scaling factor
   */
  public void setBaseScale(float baseScale) {
    this.baseScale = baseScale;
  }

  /**
   * Returns true if the game instance is using opengl
   *
   * @return result
   */
  public boolean isGL() {
    return w.isGL();
  }

  /**
   * The engine scale is the scaling factor to used to convert the relative size and location of
   * sprites in the game engine to their intended on-screen location.
   * <p>
   * See the scaling algorithm for a more in depth explanation.
   *
   * @return the engineScale
   */
  public float getEngineScale() {
    return engineScale;
  }

  /**
   * Set the engine scale
   *
   * @param engineScale the engineScale to set
   */
  public void setEngineScale(float engineScale) {
    this.engineScale = engineScale;
  }

  /**
   * The engine shift is the shifting factor to used to convert the relative size and location of
   * sprites in the game engine to their intended on-screen location.
   * <p>
   * See the scaling algorithm for a more in depth explanation.
   *
   * @return the engineShift
   */
  public Vector getEngineShift() {
    return engineShift;
  }

  /**
   * Set the engine shift
   *
   * @param engineShift the engineShift to set
   */
  public void setEngineShift(Vector engineShift) {
    this.engineShift = engineShift;
  }

  /**
   * Set up a keyboard constant i.e. (F12, left_arrow, #, etc.) to a button object
   *
   * @param s the button's string
   * @param b the button
   */
  public void setConstant(String s, Button b) {
    buttonConstants.put(s, b);
  }

  /**
   * Get a button keyboard constant from its label
   *
   * @param s the buttons label
   * @return the button object
   */
  public Button getConstant(String s) {
    return buttonConstants.get(s.toUpperCase());
  }

  /**
   * Check if the given key label s has a related keyboard button
   *
   * @param s the label
   * @return the button
   */
  public boolean isConstant(String s) {
    return buttonConstants.containsKey(s.toUpperCase());
  }

  /**
   * Set up a button name i.e. (sprint, left, right, interact) with a button object
   *
   * @param s the button's name
   * @param b the button
   */
  public void setControl(String s, Button b) {
    buttonControls.put(s.toUpperCase(), b);
  }

  /**
   * Get a button from its name
   *
   * @param s the buttons name
   * @return the button
   */
  public Button getControl(String s) {
    return buttonControls.get(s.toUpperCase());
  }

  /**
   * Get the current debug text
   * <p>
   * The debug text is a feed of information in the top left hand corner of the screen that is
   * displayed in real time.
   *
   * @return the debug text
   */
  public String getDebugText() {
    return debugText;
  }

  /**
   * Set the current debug text
   */
  public void setDebugText(String debugText) {
    this.debugText = debugText;
  }

  /**
   * Get the default game screen size in a Vector.
   * <p>
   * The game screen size refers to the size of the display window, minus the border.
   *
   * @return default size
   */
  public Vector getDefaultGameSize() {
    return defaultGameSize;
  }

  /**
   * Get the default menu screen size in a Vector.
   * <p>
   * The menu screen size refers to the size of the display window, minus the border.
   *
   * @return default size
   */
  public Vector getDefaultMenuSize() {
    return defaultMenuSize;
  }

  /**
   * Get the base screen size in a Vector
   * <p>
   * Currently identical to getDefaultScreenSize();
   *
   * @return the current screen's size
   */
  public Vector getBaseScreenSize() {
    return defaultGameSize;
  }

  /**
   * Get the games current screen scale, this is the scale that is applied to the inner game screen
   * to scale it to the size of the current window.
   *
   * @return the current screen scale
   */
  public float getCurrentScreenScale() {
    if (isPaused() || !isGameLoaded()) {
      return getMenuScreenScale();
    }
    return getGameScreenScale();
  }

  /**
   * Get the game screen's current size This is the size of the screen of the game inside the window
   * not the window itself.
   *
   * @return the size in vector form
   */
  public Vector getCurrentScreenSize() {
    if (isPaused() || !isGameLoaded()) {
      return getMenuScreenSize();
    }
    return getGameScreenSize();
  }

  /**
   * The scale of the game screen inside the window pane
   *
   * @return the size in vector form
   */
  public float getGameScreenScale() {
    return gameScreenScale;
  }

  /**
   * set the current game screen scale
   *
   * @param currentScale the scalar
   */
  public void setGameScreenScale(float currentScale) {
    this.gameScreenScale = currentScale;
  }

  /**
   * The scale of the menu screen inside the window pane
   *
   * @return the size in vector form
   */
  public float getMenuScreenScale() {
    return menuScreenScale;
  }

  /**
   * get the menun screen scale
   *
   * @param currentScale the scalar
   */
  public void setMenuScreenScale(float currentScale) {
    this.menuScreenScale = currentScale;
  }

  /**
   * set the current game screen size
   * <p>
   * this is used to calculate the scale and placement of game objects
   *
   * @param v the size of the game screen
   */
  public void setGameSize(Vector v) {
    gameScreenSize = v;
  }

  /**
   * set the current menu screen size
   * <p>
   * this is used to calculate the scale and placement of menu objects
   *
   * @param v the size of the menu screen
   */
  public void setMenuSize(Vector v) {
    menuScreenSize = v;
  }

  /**
   * Get the current game screen size
   *
   * @return the size of the current game screen in vector form
   */
  public Vector getGameScreenSize() {
    if (gameScreenSize != null) {
      return gameScreenSize;
    }
    return getDefaultGameSize();
  }

  /**
   * Get the current menu screen size
   *
   * @return the menu size in vector form
   */
  public Vector getMenuScreenSize() {
    if (menuScreenSize != null) {
      return menuScreenSize;
    }
    return getDefaultMenuSize();
  }

  /**
   * reset the screen size to null this makes the game screen size use the default factor
   */
  public void resetSize() {
    gameScreenSize = null;
  }

  /**
   * Get the current loaded menu get the top loaded menu (the currently displayed one)
   *
   * @return the menu object
   */
  public Menu getLoadedMenu() {
    return loadedMenu;
  }

  /**
   * get the shift of the game screen in the window pane
   *
   * @return the shift in vector form
   */
  public Vector getGameShift() {
    return gameShift;
  }

  /**
   * Set the shift of the game screen inside the window
   *
   * @param gameShift the game shift in vector form
   */
  public void setGameShift(Vector gameShift) {
    this.gameShift = gameShift;
  }

  /**
   * Returns whether of not the game loop is currently running
   *
   * @return the state
   */
  public boolean isRunning() {
    return running;
  }

  /**
   * Set the state of the game If set to false, the game will quit
   */
  public void setRunning(boolean running) {
    this.running = running;
  }

  /**
   * Is the game paused returns true if the game is in a menu that pauses the game can still receive
   * input if paused
   *
   * @return the state
   */
  public boolean isPaused() {
    return paused;
  }

  /**
   * Set the game to a paused state
   */
  public void setPaused(boolean paused) {
    this.paused = paused;
  }

  /**
   * Get the current player object that holds the context for the game
   *
   * @return the Player object
   */
  public Player getPlayer() {
    return player;
  }

  /**
   * Store a Player instance as the focus of the game
   *
   * @param e the player entity
   */
  public void setPlayer(Player e) {
    this.player = e;
  }

  /**
   * Get the current world container
   *
   * @return the current world
   */
  public World getWorld() {
    return world;
  }

  /**
   * Get itself as an object. (almost useless)
   *
   * @return the game
   */
  public Game getGame() {
    return this;
  }

  /**
   * Get a sprite object from a string
   *
   * @param string the sprites label
   * @return the sprite object
   */
  public Sprite getMasterSprite(String string) {
    return sprites.get(string);
  }

  /**
   * Get a sprite object from a string
   * <p>
   * calls master sprite and then clones the sprite to prevent corruption of data
   *
   * @param string the sprites label
   * @return the sprite object
   */
  public Sprite getSprite(String string) {
    return getMasterSprite(string).clone();
  }

  /**
   * Get an animation object from the lookup table of loaded animations
   *
   * @param string the label
   * @return the animation sprite object
   */
  public Animation getAnimation(String string) {
    Sprite so = sprites.get(string);
    if (so != null && so.isAnimated()) {
      return (Animation) so;
    }
    return null;
  }

  /**
   * Get the current window pane object
   *
   * @return the window
   */
  public Window getWindow() {
    return w;
  }

  /**
   * Get a FontContext object from the loaded lookup table
   *
   * @param s the label
   * @return the FontContext object
   */
  public FontContext getMasterFont(String s) {
    return fonts.get(s);
  }

  /**
   * Get a copy of the font context from the lookup table
   *
   * @param s the label
   * @return the FontContext object
   */
  public FontContext getFont(String s) {
    return getMasterFont(s).clone();
  }

  /**
   * Get the current font object If no font is set, use the default font label to get a current
   * font
   *
   * @return the font object
   */
  public FontContext getCurrentFont() {
    if (currentFont == null) {
      currentFont = getFont(defaultFontLabel);
    }
    return currentFont;
  }

  /**
   * Set the current font object
   *
   * @param font the font object
   */
  public void setCurrentFont(FontContext font) {
    this.currentFont = font;
  }

  /**
   * Get the game entity The game entity is a placeholder entity used to preform actions from a
   * omnipotent context
   *
   * @return the entity
   */
  public Entity getGameEntity() {
    return gameEntity;
  }

  /**
   * Specifically try to get an sprite sprite if it is loaded if there is no sprite with the
   * corresponding label, return null
   *
   * @param s the label
   * @return the Image
   */
  public Sprite getImage(String s) {
    Sprite out = sprites.get(s);
    if (!out.isAnimated()) {
      return out;
    }
    return null;
  }

  // -----------------------------------------------------------------

  /**
   * Set the master scaling value. Set the base scale factor that is applied to the sprite in the
   * lookup table.
   *
   * @param label        the sprites label
   * @param newBaseScale the base scale factor
   */
  public void setMasterBaseScale(String label, float newBaseScale) {
    setBaseScale(getImage(label), newBaseScale);
  }

  /**
   * Sets the default scaling factor for the sprite. (this is the scale factor that is applied
   * before any game scaling occurs)
   *
   * @param sprite       the referenced sprite
   * @param newBaseScale the new scale factor
   * @return the referenced sprite
   */
  public static Sprite setBaseScale(Sprite sprite, float newBaseScale) {

    sprite.resetBaseScale(newBaseScale);
    for (int i = 1; i < (maxScale / 0.5f) - 2; i++) {
      sprite.generateScale(1.0f + (i * 0.5f));
    }
    return sprite;
  }

  /**
   * Update the synchronous event list. First execute the conditional event then execute the timed
   * event.
   *
   * @param dt the time difference
   */
  public void updateSyncedEvents(float dt) {
    for (int i = 0; i < condEvents.size(); i++) {
      if (condEvents.get(i).test()) {
        if (condEvents.get(i).getEvent().isRepeat()) {
          condEvents.remove(i);
          i--;
        }
      }
    }

    for (int i = 0; i < timedEvents.size(); i++) {
      if (timedEvents.get(i).test(dt)) {
        if (!timedEvents.get(i).getEvent().isRepeat()) {
          timedEvents.remove(i);
          i--;
        }
      }
    }

    updateEvents();
  }

  /**
   * Add a synchronous conditional event from a function that returns a boolean.
   *
   * @param bs A boolean supplier (any function that returns a boolean)
   * @param e  the event (action) to preform when the conditional statement is true
   */
  public void addConditionalEvent(BooleanSupplier bs, Event e) {
    condEvents.add(new ConditionalEvent(bs, e));
  }

  public Event getStartEvent() {
    return startEvent;
  }

  public void setStartEvent(Event startEvent) {
    this.startEvent = startEvent;
  }

  // -----------------------------------------------------------------

  /**
   * Add a synchronous conditional event.
   *
   * @param ce the conditional event
   */
  public void addConditionalEvent(ConditionalEvent ce) {
    condEvents.add(ce);
  }

  /**
   * Add a timed synchronous event that is activated after t seconds.
   *
   * @param t  the amount of seconds until the event is preformed
   * @param te the event
   */
  public void addTimedEvent(float t, Event te) {
    timedEvents.add(new TimedEvent(t, te));
  }

  /**
   * Add a timed synchronous event.
   *
   * @param te the timed event
   */
  public void addTimedEvent(TimedEvent te) {
    timedEvents.add(te);
  }

  // -----------------------------------------------------------------

  /**
   * Using a HashMap with string and sprite pair, update the master sprite lookup table. If any of
   * the names are already in the master sprite list, remove them.
   *
   * @param spriteMap the list of sprite-name combinations.
   */
  public void resetSprites(HashMap<String, Sprite> spriteMap) {
    for (String s : spriteMap.keySet()) {
      if (spriteMap.get(s) != null && s != null) {
        resetSprite(s, spriteMap.get(s));
      }
    }
  }

  /**
   * Resets the reference to s in the master lookup table with the new sprite object.
   *
   * @param s  the sprite's reference name
   * @param so the sprite object
   */
  public void resetSprite(String s, Sprite so) {
    resetMasterSprite(s, so, baseScale);
  }

  // -----------------------------------------------------------------------------------------------

  /**
   * Resets the reference to s in the master lookup table with the new sprite object and set the
   * base scale of the sprite to newScale.
   *
   * @param string   the sprite's reference name
   * @param so       the sprite object
   * @param newScale the new base scale
   */
  public void resetMasterSprite(String string, Sprite so, float newScale) {
    if (so == null) {
      return;
    }

    sprites.remove(string);
    setBaseScale(so, newScale);
    so.setName(string);

    sprites.put(string, so);
  }

  /**
   * Get a menu object from the menu lookup table.
   *
   * @param s the name of the menu
   * @return the menu object
   */
  public Menu getMenuFromList(String s) {
    return menus.get(s);
  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Misc Methods">

  /**
   * Add a menu object to the lookup table.
   *
   * @param s    the menu name
   * @param menu the menu object
   */
  public void addMenu(String s, Menu menu) {
    menus.put(s, menu);
  }

  /**
   * Get the shift of the window inside the screen.
   *
   * @return the shift in vector form
   */
  public Vector getWindowShift() {
    return windowShift;
  }

  /**
   * Set the window shift inside the screen.
   *
   * @param windowShift the window shift
   */
  public void setWindowShift(Vector windowShift) {
    this.windowShift = windowShift;
  }

  /**
   * Add an event that is synced with the game update cycle.
   *
   * @param event the event to preform
   */
  public void addSyncedEvent(Event event) {
    syncedEvents.add(event);
  }

  /**
   * Update synced game event
   */
  public void updateEvents() {
    for (Event e : syncedEvents) {
      e.act();
    }

    syncedEvents.clear();
  }

  /**
   * Load the given menu object.
   */
  public void loadMenu(Menu menu) {
    loadMenu(menu, true);
  }

  /**
   * Load the given menu object and set the current menu to the previous one.
   *
   * @param newLoadedMenu the menu object
   * @param previous      if true, link the current menu to the new menu
   */
  public void loadMenu(Menu newLoadedMenu, boolean previous) {
    if (previous) {
      newLoadedMenu.setPreviousMenu(loadedMenu);
    }

    loadedMenu = newLoadedMenu;
    menuLoaded = true;
  }

  /**
   * Start the game.
   */
  public void startGame() {
    if (startEvent != null) {
      startEvent.act();
    }

    gameLoaded = true;
    paused = false;
  }

  /**
   * Pause the game.
   */
  public void pause() {
    Menu pauseMenu = getMenuFromList("pause");
    loadMenu(pauseMenu);
    paused = true;
  }

  public void setHold(boolean hold) {
    this.hold = hold;
  }

  /**
   * Load the main menu and set the state of the game to unloaded.
   */
  public void loadMainMenu() {
    Menu mainMenu = getMenuFromList("main");
    loadMenu(mainMenu);
  }

  /**
   * Un-pause the game.
   */
  public void unPause() {
    paused = false;
  }
  
  /**
   * Update the time on all of the loaded entities sprites.
   *
   * @param time the change in time
   */
  public void updateGameEntity(float time) {
    gameEntity.updateCycle(time);
    gameInteractArea.updateCycle(time);
  }

  /**
   * Update the time on all of the loaded entities sprites.
   *
   * @param time the change in time
   */
  public void flushGameEntity(float time) {
    gameEntity.clearInteractions();
    gameInteractArea.clearInteractions();
  }

  public GLFilter getGlobalFilter() {
    return globalFilter;
  }

  public void setGlobalFilter(GLFilter globalFilter) {
    this.globalFilter = globalFilter;
  }

  /**
   * Reset the game.
   */
  public void reset() {
    hang = true;
    loadGame();
    hang = false;
  }

  /**
   * Reload the game.
   */
  public void reload() {
    loadGame();
  }

  /**
   * Update the game inputs based on elapsed time.
   *
   * @param t the elapsed time
   */
  public void updateInputs(float t) {
    currentDelay += t;
    if (currentDelay >= keyDelay) {
      currentDelay = 0;
      w.refreshInputs();
      getGeneralInput();
    }
  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Debug Methods">

  public static String toStr(ArrayList<Object> arrayList) {
    StringBuilder collect = null;

    for (Object object : arrayList) {
      if (collect == null) {
        collect = new StringBuilder(toStr(object));
      } else {
        if (Game.splitLogLines) {
          collect.append(",\n").append(toStr(object));
        } else {
          collect.append(", ").append(toStr(object));
        }
      }
    }

    return "{" + collect + "}";
  }

  public static String toStr(HashMap<Object, Object> map) {
    StringBuilder collect = null;

    for (Object key : map.keySet()) {
      String keyStr = toStr(key);
      String objStr = toStr(map.get(key));

      if (collect == null) {
        collect = new StringBuilder(keyStr + ": " + objStr);
      } else {
        if (Game.splitLogLines) {
          collect.append(",\n").append(keyStr).append(": ").append(objStr);
        } else {
          collect.append(", ").append(keyStr).append(": ").append(objStr);
        }

      }
    }

    return "{" + collect + "}";
  }


  public static String toStr(Object... list) {
    StringBuilder string = new StringBuilder();
    for (Object s : list) {
      String representation = "";

      if (s instanceof HashMap) {
        representation = toStr((HashMap) s);
      } else if (s instanceof ArrayList) {
        representation = toStr((ArrayList) s);
      } else if (s != null) {
        representation = s.toString();
      }

      if (string.length() == 0) {
        string = new StringBuilder(s != null ? representation : "null");
      } else {
        if (Game.splitLogLines) {
          string.append(",\n").append(s != null ? representation : "null");
        } else {
          string.append(", ").append(s != null ? representation : "null");
        }
      }
    }
    return string.toString();
  }

  public static void log(Object... list) {
    if (DEBUG_LOG) {
      System.out.println(toStr(list));
    }
  }

  public static void log(LOG_TYPE type, Object... list) {
    if (LOG_FILTER.contains(type)) {
      log(list);
    }
  }

  /**
   * Get the general paused and un-paused Input
   */
  public void getGeneralInput() {
    if (!paused && gameLoaded) {
      getGameInput();
    } else {
      getPausedInput();
    }

    getDefaultInput();
  }

  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Initilization Methods">

  /**
   * Initialize the game.
   *
   * @param window the games window object
   * @param engine the games engine object
   */
  public void init(Window window, Engine engine) {
    w = window; // set the window object
    w.init(this); // initialize the window with an instance of the current game

    e = engine; // set the engine object

    opengl = true;
    hang = false;
    running = true;
    loaded = false;
    stepping = false;
    step = false;

    createTables();
    loadRequiredResources();
    loadDebugResources();

    // w.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    // w.setLocationRelativeTo(null);
    // w.setVisible(true);

    // Load the game data asynchronously
    t = new Thread(() -> {
      initializeScreenValues();
      generateVisualContext(); // create a visual context for objects that rely on direct visuals
      System.out.println("Loaded Screen Values.");
      loadGame();
      loadGameButtons();
      System.out.println("Loaded Game Values.");
      loaded = true;
    });

    t.start();
  }

  /**
   * Start the game.
   */
  public void start() {

    run();
  }

  /**
   * Load GameButtons.
   */
  public void loadGameButtons() {
    Up = getButton("Up");
    Down = getButton("Down");
    Left = getButton("Left");
    Right = getButton("Right");
    Sprint = getButton("Sprint");
    Exit = getButton("Exit");
    Interact = getButton("Interact");
    Action = getButton("Action");
    Left_Click = getButton("Left_Click");
    Right_Click = getButton("Right_Click");
    Debug_Step = getButton("Debug_Step");
    Debug = getButton("Debug");
    Debug2 = getButton("Debug2");
    Reload_Game = getButton("Reload_Game");
    Reload_Resources = getButton("Reload_Resources");
    Backspace = getButton("Backspace");
    Debug_Switch = getButton("Debug_Switch");
    Enter = getButton("Enter");
    Debug_Step_Toggle = getButton("Debug_Step_Toggle");
  }

  /**
   * Create the visual lookup tables.
   */
  public void createVisualTables() {
    sprites = new HashMap<String, Sprite>();
    fonts = new HashMap<String, FontContext>();
  }

  /**
   * Create the game object lookup tables.
   */
  public void createGameTables() {
    menus = new HashMap<String, Menu>();
  }

  /**
   * Create the lookup tables.
   */
  public void createTables() {
    createVisualTables();
    createGameTables();
  }

  /**
   * Load the font data for the game.
   */
  public void loadFonts() {
    GLFontContext g = GLFontContext.createFontFromPath("resources/fonts/SGK100.ttf");
    GLFontData g2 = GLFontContext.createFontDataFromPath("resources/fonts/SGK100_bold.ttf");
    g.setBoldFont(g2);
    fonts.put("default", new FontContext(g));
  }

  /**
   * Load the resources required to start the game.
   */
  public void loadRequiredResources() {
    loadFonts();
    SpriteLoader.loadSprites(this, "resources/images");
  }

  /**
   * Load the debug resources required to start the game.
   */
  public void loadDebugResources() {
    debugSprite = getSprite("ERROR");
  }

  /**
   * Calculate and set up the screen values.
   */
  public void initializeScreenValues() {
    setGameShift(RenderEngine.calculateGameShift(this));
    setGameScreenScale(RenderEngine.calculateGameScale(this));
    setMenuScreenScale(RenderEngine.calculateMenuScale(this));
    setWindowShift(RenderEngine.calculateWindowShift(this));
  }

  /**
   * Reload the world data.
   */
  public void reloadWorld() {
    loaded = false; // set the state of the game to not loaded (basically a game mutex)

    String room = getPlayer().getRoom().getName(); // get the name (id) of the current room
    loadWorld(); // loadRoomSize the data in the new world
    Room newRoom = getWorld().getLookupRoom(room); // find the room with the id of the old room
    getWorld().loadEntityRoom(newRoom, getPlayer()); // loadRoomSize the new room with the player

    log("Reloaded World."); // debug info
    loaded = true; // return game mutex
  }

  /**
   * Load a new world and populate it with the data file.
   */
  public void loadWorld() {
    world = new World(this);
    lookup = new Lookup();
    DataLoader.loadSaveFile(this, lookup, "resources/saves/Data.json");
  }

  /**
   * Reset the visual tables and reload the visual information.
   */
  public void reloadVisuals() {
    loaded = false;
    createVisualTables();
    loadRequiredResources();

    log("Reloaded Visuals.");
    loaded = true;
  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Main Run Cycle">

  /**
   * Load the game data.
   */
  public void loadGame() {
    keys = new boolean[256 * 4]; // array that can hold the id's for an extended keyboard layout
    mouse = new boolean[5]; // include most mouse buttons
    previousKeys = new boolean[256 * 4]; // same as above
    previousMouse = new boolean[5];

    windowShift = new Vector(0, 0); // set up the window and game shift to 0, 0
    gameShift = new Vector(0, 0);

    paused = false; // set the default state of the game to not loaded and not paused
    menuLoaded = false;
    gameLoaded = false;

    syncedEvents = new ArrayList<>(); // initialize the event and cycle lists
    condEvents = new ArrayList<>();
    timedEvents = new ArrayList<>();

    gameEntity = new Entity(this, null, new Circle(1.0f), -1); // create the "game" entity
    gameEntity.setName("Mr. Game");
    gameEntity.setInteractAll(true);

    gameInteractArea = new EntityArea(this, new Circle(1.0f), gameEntity::addInteraction);
    gameInteractArea.setCollides(false);
    gameInteractArea.setInteractAll(true);
    gameEntity.addChild(gameInteractArea);
    loadWorld(); // load the world

    MenuLoader.initMainMenuTest(this); // TODO move menu loading to a .sav file
    MenuLoader.initPauseMenuTest(this);
    loadMainMenu();
  }

  /**
   * The games main run loop
   */
  @Override
  public void run() {
    long beforeTime, afterTime, timeDiff = 0;
    long delay = (long) Math.floor(SECOND / refreshRate); // the delay between game ticks

    beforeTime = System.nanoTime(); // record the time before the tick

    while (running) {
      if (hang) // if the hang flag is on do nothing in the program
      {
        continue;
      }

      if (getWindow().shouldWindowClose()) // if the window should close, close it
      {
        break;
      }

      Logger.pushDebugTime("total", System.nanoTime());
      float timeRef = Math.min(timeDiff / SECOND, 0.01f); // get the time reference
      if (opengl) // if we are using opengl initialize the draw window
      {
        getWindow().initWindow();
      }

      if (loaded) { // if the game is loaded, update the inputs and continue running the game
        Logger.pushDebugTime("input");
        updateInputs(timeRef);
        Logger.pushDebugTime("input");

        updateGameEntity(timeRef);
        // update the synchronized event lists
        updateSyncedEvents(timeRef);

        if (gameLoaded && !paused) { // if the game is loaded and not paused run the game functions
          Logger.pushDebugTime("total_game");
          updateAndDrawGame(timeRef);
          Logger.pushDebugTime("total_game");

        } else if (menuLoaded) {
          Logger.pushDebugTime("total_menu");
          updateAndDrawMenu(timeRef); // update menu logic and render the menu visual
          Logger.pushDebugTime("total_menu");
        }

        flushGameEntity(timeRef);

        step = false;
      }

      Logger.pushDebugTime("screen_update");
      w.update(); // update the screen / window
      Logger.pushDebugTime("screen_update");

      Logger.pushDebugTime("total");

      if (Logger.debugInput || Logger.debug) {
        Logger.calculateDebugTimesList(); // calculate the debug times
      }

      if (capFrameRate) { // limit the frame rate or not
        afterTime = System.nanoTime();
        timeDiff = (afterTime - beforeTime);
        beforeTime = System.nanoTime();
        sync(delay);
      }
    }
    System.exit(0);
  }

  /**
   * delay the given thready by the sleepTime.
   *
   * @param sleepTime the time in seconds
   */
  private void sync(long sleepTime) {
    if (sleepTime <= 0) {
      return;
    }
    // yieldTime + remainder micro & nano seconds if smaller than sleepTime
    long yieldTime = Math.min(sleepTime, variableYieldTime + sleepTime % (1000 * 1000));
    long overSleep = 0; // time the sync goes over by

    try {
      while (true) {
        long t = System.nanoTime() - lastTime;

        if (t < sleepTime - yieldTime) {
          Thread.sleep(1);
        } else if (t < sleepTime) {
          // burn the last few CPU cycles to ensure accuracy
          Thread.yield();
        } else {
          overSleep = t - sleepTime;
          break; // exit while loop
        }
      }
    } catch (InterruptedException e) {
      e.printStackTrace();
    } finally {
      lastTime = System.nanoTime() - Math.min(overSleep, sleepTime);

      // auto tune the time sync should yield
      if (overSleep > variableYieldTime) {
        // increase by 200 microseconds (1/5 a ms)
        variableYieldTime = Math.min(variableYieldTime + 200 * 1000, sleepTime);
      } else if (overSleep < variableYieldTime - 200 * 1000) {
        // decrease by 2 microseconds
        variableYieldTime = Math.max(variableYieldTime - 2 * 1000, 0);
      }
    }
  }

  // ---------------------------------------------------------------

  /**
   * Process all of the game logic inputs and interactions, then draw the game.
   *
   * @param timeRef the previous time step
   */
  public void updateAndDrawGame(float timeRef) {
    // this lock below only blocks the execution if stepping is on and step is off.
    // This allows the physical, and game based calculations to be stepped
    // 	through like a slide show.
    if (!stepping || step) {
      Logger.pushDebugTime("pre_update");
      world.updateCycle(timeRef); // update the world and its entities by the time difference
      Logger.pushDebugTime("pre_update");

      Logger.pushDebugTime("physics");
      // process physical interactions using the time difference and the loaded Entities
      Engine.process(world.getLoadedRoom().getAllBasicEntities(), timeRef);
      Logger.pushDebugTime("physics");

      Logger.pushDebugTime("post_update");
      world.postEngineCheck(player); // check for physics based interactions
      Logger.pushDebugTime("post_update");
    }

    Logger.pushDebugTime("screen_values");
    initializeScreenValues(); // re-initialize the screen shifts and scales before rendering
    generateVisualContext(); // create a visual context for objects that rely on direct visuals
    Logger.pushDebugTime("screen_values");

    if (globalFilter != null) {
      globalFilter.updateFilter(timeRef);
    }

    Logger.pushDebugTime("render_game");
    world.preRender(getVisualContext());
    RenderEngine.renderGame(this, getVisualContext()); // render the game screen
    Logger.pushDebugTime("render_game");

  }

  /**
   * Process all of the menu logic inputs and interactions, then draw the menu.
   *
   * @param t the previous time step
   */
  public void updateAndDrawMenu(float t) {
    if (loadedMenu != null) {

      Logger.pushDebugTime("update_loaded_menus");
      loadedMenu.updateCycle(t);
      Logger.pushDebugTime("update_loaded_menus");

      Logger.pushDebugTime("process_menu");
      e.processMenu(loadedMenu, getGameEntity(), t);
      Logger.pushDebugTime("process_menu");

      Logger.pushDebugTime("menu_interactions");
      gameEntity.applyInteractions();
      Logger.pushDebugTime("menu_interactions");

      Logger.pushDebugTime("screen_values");
      initializeScreenValues();
      generateVisualContext();
      Logger.pushDebugTime("screen_values");

      Logger.pushDebugTime("render_menu");
      loadedMenu.preRender(getVisualContext());
      RenderEngine.renderMenu(loadedMenu, this, getVisualContext());
      Logger.pushDebugTime("render_menu");
    }

  }
//  /**
//   * Process menu entity sizes and scale based on a graphics context.
//   *
//   * @param entity menu entity
//   */
//  public void preCalculateRenderSize(MenuEntity entity) {
//    entity.updateVisual(getVisualContext(), getMenuScreenScale());
//  }
//
//  /**
//   * Process menu sizes and scale based on a graphics context.
//   *
//   * @param menu menu
//   */
//  public void preCalculateRenderSize(Menu menu) {
//    ArrayList<Entity> entities = menu.getEntities();
//    for (Entity entity : entities) {
//      if (entity instanceof MenuEntity) {
//        preCalculateRenderSize((MenuEntity) entity);
//      }
//    }
//  }

  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Input Handlers">

  /**
   * Get the button Object from a id string
   *
   * @param s the id string
   * @return the button object
   */
  public Button getButton(String s) {
    Button b = getControl(s);
    if (b == null) {
      b = getConstant(s);
    }
    return b;
  }

  /**
   * Check the master input lists to see if the button provided is currently down.
   *
   * @param button the button
   * @return if the button is down return true
   */
  public boolean checkButton(Button button) {
    return checkButton(button, ButtonInputTypes.DOWN);
  }

  /**
   * Check a master input set to see if the button is down or not
   *
   * @param button the button to check
   * @param type   the type of input to check for
   * @return if the button has been activated in the specified input state, return true
   */
  public boolean checkButton(Button button, ButtonInputTypes type) {
    ArrayList<ButtonData> buttons = button.getButtons();

    for (ButtonData b : buttons) {
      if (b.type == ButtonType.Keyboard) {
        switch (type) {
          case HIT:
            return w.keyHit(b.button);
          case RELEASED:
            return w.keyReleased(b.button);
          case TYPED:
            return w.keyTyped(b.button);
          case DOWN:
            return w.keyDown(b.button);
        }
      } else if (b.type == ButtonType.Mouse) {
        switch (type) {
          case HIT:
            return w.mouseHit(b.button);
          case RELEASED:
            return w.mouseReleased(b.button);
          case TYPED:
            return w.mouseTyped(b.button);
          case DOWN:
            return w.mouseDown(b.button);
        }
      }
    }
    return false;
  }

  /**
   * Returns true if the button b is down.
   *
   * @param b button object
   * @return if the button is currently down return true
   */
  public boolean buttonDown(Button b) {
    if (b == null) {
      return false;
    }

    return checkButton(b);
  }

  /**
   * Returns true if the button was released in the current input tick.
   *
   * @param b button object
   * @return if the button was released return true
   */
  public boolean buttonReleased(Button b) {
    if (b == null) {
      return false;
    }

    return checkButton(b, ButtonInputTypes.RELEASED);
  }

  /**
   * Returns true if the button was typed in the current input frame.
   *
   * @param b the button to check
   * @return if the button was typed return true
   */
  public boolean buttonTyped(Button b) {
    if (b == null) {
      return false;
    }
    return checkButton(b, ButtonInputTypes.TYPED);
  }

  /**
   * Returns true if the button was pressed in the current input frame and not pressed the last
   * one.
   *
   * @param b the button to check
   * @return if the button was pressed return true
   */
  public boolean buttonPressed(Button b) {
    if (b == null) {
      return false;
    }

    return checkButton(b, ButtonInputTypes.HIT);
  }

  /**
   * Check if a specific button KeyEvent has been pressed.
   *
   * @param i the KeyEvent number
   * @return return true if the key was pressed
   */
  public boolean keyPressed(int i) {
    return (keys[i] && !previousKeys[i]);
  }

  // <editor-fold defaultstate="collapsed" desc="Low Level Key Functions">

  /**
   * Check if a specific button KeyEvent has been released.
   *
   * @param i the KeyEvent number
   * @return return true if the key was pressed
   */
  public boolean keyReleased(int i) {
    return (!keys[i] && previousKeys[i]);
  }

  /**
   * Check if a specific mouse MouseEvent has been pressed.
   *
   * @param i the MouseEvent number
   * @return return true if the key was pressed
   */
  public boolean mousePressed(int i) {
    return (mouse[i] && !previousMouse[i]);
  }

  /**
   * Check if a specific mouse MouseEvent has been released.
   *
   * @param i the MouseEvent number
   * @return return true if the key was pressed
   */
  public boolean mouseReleased(int i) {
    return (!mouse[i] && previousMouse[i]);
  }

//  /**
//   * Convert a game vector to an engine vector. Scales a game vector by the engine scale and shifts
//   * the vector by the engine shift.
//   *
//   * @param v the vector input
//   * @return the modified vector
//   */
//  public Vector gameVectorToEngineVector(Vector v) {
//    return v.addi(engineShift).scalei(engineScale);
//  }

  // </editor-fold>

  ArrayList<Vector> inputBuffer = new ArrayList<>();

  /**
   * Poll game inputs and preform game logic based on them.
   */
  public void getGameInput() {
    // set the sprint state based on the sprint input
    player.setSprint(buttonDown(Sprint));
    // If the player currently has a message with a choice don't move with the directional input
    if (!(world.hasValidDialogue() && world.getDialogue().getSelected().hasChoices())) {
      Vector move = new Vector(0, 0);

      ArrayList<Button> inputList = new ArrayList<>(Arrays.asList(Up, Down, Left, Right));
      ArrayList<Vector> vList = new ArrayList<>(Arrays.asList(Vector.up, Vector.down,
          Vector.left, Vector.right));

      boolean sInput = false;

      for (int i = 0; i < inputList.size(); i++) {
        Button button = inputList.get(i);

        if (buttonPressed(button)) {
          inputBuffer.add(vList.get(i));
        }


        if (!hold) {
          if (buttonDown(button)) {
            move.add(vList.get(i));
          }
        }

        if (buttonReleased(button)) {
          inputBuffer.remove(vList.get(i));
        }
      }

      if (!hold && inputBuffer.size() > 0) {
        // logic to make the player face the direction that was most recently pressed
        player.setFacingVector(inputBuffer.get(inputBuffer.size() - 1));
      }

      for (int i = 0; i < inputList.size(); i++) {
        if (buttonPressed(inputList.get(i))) {
          if (!hold) {
            sInput = !player.hasParent() || !player.getParent().blockInput(player, vList.get(i));
          }
        }
      }

      if (sInput) {
        move = Vector.zero.clone();
      }

      // move the player by the input
      player.setMoveDirection(move);
    }

    // left click actions
    if (buttonPressed(Left_Click) && getWindow().isMouseInside()) {

      Animation so = (Animation) getSprite("cloud_puff_puff");
      so.setLoop(false);

      ItemEntity item = new ItemEntity(this, calculateMousePos(), new Circle(4.5f), 5);

      item.setRemoveOnPickup(false);
      item.setPickupEvent(new Event((a, b) -> {
        world.sendMessage("BOOM!", getGameEntity());
        Vector v = a.getPosition().subi(b.getPosition());
        a.setVelocity(v.normalizei().scalei(10));

        Entity puff = new Entity(this, b.getPosition(), null, 0);

        b.getContainer().addEntity(puff);
        puff.setSprite(so.clone());
        b.removeFromContainer();
        this.addConditionalEvent(() -> puff.getSprite().isFinished(), new Remove(puff));

      }));
      world.getLoadedRoom().addEntity(item);
    }
//
//    if (buttonPressed()) {
//      Game.log("A");
//    }

    if (buttonPressed(Action)) {
      player.useItem(0);
    }

    if (buttonPressed(Right_Click)) {
      if (getWindow().isMouseInside()) {
        world.addSyncedEvent(new Move(getPlayer(), calculateMousePos()));
      }
    }

    if (buttonPressed(Debug_Step)) {
      step = true;
    }

    if (buttonPressed(Debug_Step_Toggle)) {
      stepping = !stepping;
    }

    if (buttonPressed(Debug)) {
      Logger.debugInput = true;
    }

    if (buttonPressed(Debug2)) {
      Game.log("-MARKER-");
    }

    if (buttonPressed(Reload_Game)) {
      reloadWorld();
    }

    if (buttonPressed(Reload_Resources)) {
      reloadVisuals();
    }

    if (world.hasValidDialogue() && world.getDialogue().getSelected().hasChoices()) {
      if ((buttonPressed(Up) || keyPressed(KeyEvent.VK_T))) {
        world.getDialogue().getSelected().incIndex();
        world.updateRenderText();
      }
      if ((buttonPressed(Down) || keyPressed(KeyEvent.VK_G))) {
        world.getDialogue().getSelected().decIndex();
        world.updateRenderText();
      }
    }

    if (buttonPressed(Interact)) {
      world.processInteraction(player);
      if (!world.isBlocking()) {
        player.processSelection();
      }
    }

    if (buttonDown(Sprint) && buttonPressed(Exit)) {
      this.reset();
      return;
    }

    if (buttonPressed(Exit)) {
      if (!hold) {
        pause();
      }
    }
  }

  /**
   * Calculate the position of the mouse within the game window or the overall window depending on
   * what is loaded.
   *
   * @return The mouse position
   */
  public Vector calculateMousePos() {
    Vector v2 = getWindow().getMousePos().clone().subi(getWindowShift());
    v2.scale(1.0f / getCurrentScreenScale());
    if (gameLoaded && !paused) {
      v2.add(getGameShift());
    }
    return v2;
  }

  /**
   * Get the input of the game while paused and do paused logic
   */
  public void getPausedInput() {
    if (buttonPressed(Debug2)) {
      System.out.println("MARKER------------------MARKER");
    }

    if (getWindow().isMouseInside()) {
      gameEntity.setPosition(calculateMousePos());
    }

    if (buttonReleased(Left_Click)) {
      getLoadedMenu().setSelectedArea(null);
      gameInteractArea.collideThisTick();
    }

    if (getLoadedMenu().getSelectedArea() != null) {
      MenuEntity me = getLoadedMenu().getSelectedArea();
      if (me instanceof TextArea) {
        if (buttonTyped(Enter)) {
          ((TextArea) me).enter();
        }
        if (buttonTyped(Backspace)) {
          ((TextArea) me).backspace();
        }
        if (!w.getTypeBuffer().equals("")) {
          ((TextArea) me).addString(w.getTypeBuffer());
        }
      }
    }

    if (buttonPressed(Exit)) {
      if (gameLoaded) {
        unPause();
      } else {
        loadMainMenu();
      }
    }

    if (buttonPressed(Debug_Step)) {
      step = true;
    }

    if (buttonPressed(Debug_Step_Toggle)) {
      stepping = !stepping;
    }
  }

  /**
   * Get general input of the game window
   */
  public void getDefaultInput() {
    if (buttonPressed(Debug_Switch)) {
      Logger.debugInput = !Logger.debugInput;
    }
  }

  /**
   * The types of button inputs. CURRENT 	- The current button states PREVIOUS 	- The last frames
   * button states TYPED 		- The typed button states
   */
  enum ButtonInputTypes {
    HIT, TYPED, RELEASED, DOWN
  }

  // </editor-fold>
}
