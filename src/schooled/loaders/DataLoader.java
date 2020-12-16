package schooled.loaders;

import controls.Button;
import controls.Button.ButtonType;
import java.awt.Color;
import java.awt.Font;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.json.JSONObject;
import schooled.Game;
import schooled.audio.Sound;
import schooled.containers.Room;
import schooled.engines.Engine;
import schooled.engines.RenderEngine;
import schooled.entities.BedEntity;
import schooled.entities.ContainerEntity;
import schooled.entities.CrateEntity;
import schooled.entities.Door;
import schooled.entities.EnterableEntity;
import schooled.entities.Entity;
import schooled.entities.ItemEntity;
import schooled.entities.NPC;
import schooled.entities.Player;
import schooled.entities.Player.Pose;
import schooled.entities.SeatEntity;
import schooled.entities.State;
import schooled.entities.VariableSeatEntity;
import schooled.entities.Vehicle;
import schooled.entities.YoYoEntity;
import schooled.event.Event;
import schooled.loaders.JsonLoader.DStore;
import schooled.menu.Dialogue;
import schooled.menu.MenuEntity;
import schooled.menu.Message;
import schooled.menu.TextBox;
import schooled.menu.TextContext;
import schooled.physics.Circle;
import schooled.physics.MovementType;
import schooled.physics.Mover;
import schooled.physics.MultiShape;
import schooled.physics.PolygonShape;
import schooled.physics.Shape;
import schooled.physics.Vector;
import schooled.visuals.filters.AlphaFilter;
import schooled.visuals.filters.GLFilter;
import schooled.visuals.GLGraphicsContext;
import schooled.visuals.sprite.Animation;
import schooled.visuals.sprite.LayeredSprite;
import schooled.visuals.sprite.Sprite;

/**
 * A Static class containing data parsing functions.
 * <p>
 * The main purpose of this class is to parse plaintext .sav files. <br> Objects are formatted as
 * object_type:object_name(data1$tag, data2$tag, ...). <br> Commands are formatted as {@literal
 * <command_name> (data1$tag, data2$tag, ...).
 * <p>
 * "data" can be formatted as objects or as a lookup ([object_name]). A lookup attempts to find a
 * previously loaded object with the given name. <br>
 * <br>
 * <p>
 * This will likely change to another format eventually, but it was a fun project. The loading is
 * done mostly modularly so changing the saving format shouldn't be too difficult.
 *
 * @author Joseph Straceski, web: <https://github.com/Crepox>, e-mail: straceski.joseph@gmail.com
 */
public class DataLoader {

  // <editor-fold defaultstate="collapsed" desc="Data Saving And Loading">

  /**
   * A function to save the current game state.
   *
   * @param g the game instance
   */
  public static void saveGame(Game g) {
    //TODO implement
  }

  /**
   * A function to load the current game state.
   *
   * @param g the game instance
   */
  public static void loadGame(Game g) {
    //TODO implement
  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Font Loaders">

  /**
   * Construct a HashMap or dictionary of fonts from a folder path in string form.
   *
   * @param folderString the system path of the folder
   * @return the font dictionary
   */
  public static HashMap<String, Font> loadFontList(String folderString) {
    File folder = loadFile(folderString);
    HashMap<String, Font> cache = loadFontList(folder);
    return cache;
  }

  /**
   * Construct a HashMap or dictionary of fonts from a File folder.
   *
   * @param folder the folder that contains the font files
   * @return the font dictionary
   */
  public static HashMap<String, Font> loadFontList(File folder) {
    HashMap<String, Font> cache = new HashMap<>(); // create a map to store the data
    File[] files = folder.listFiles(); // get a list of sub-files
    for (File fontFile : files) { // iterate through the list of files
      if (fontFile.getName().contains(".ttf")) { // is the file a ttf
        Font font = loadFont(fontFile); // generate a font Object from the file
        if (font != null) { // if it worked add it to the cache
          cache.put(fontFile.getName().replace(".ttf", ""), font.deriveFont(16.0f));
        }
      }
    }
    return cache;
  }

  /**
   * Generate a font object from a file string file.
   *
   * @param filePath the ttf file path
   * @return the Font object
   */
  public static Font loadFont(String filePath) {
    return loadFont(loadFile(filePath));
  }

  /**
   * Generate a font object from a ttf file.
   *
   * @param file the ttf file
   * @return the Font object
   */
  public static Font loadFont(File file) {
    Font font;
    try {
      // Load the font file as a ttf
      font = Font.createFont(Font.TRUETYPE_FONT, new FileInputStream(file));
    } catch (Exception ex) { // if the loading fails use the basic serif font
      ex.printStackTrace();
      System.err.println(file.getName() + " not loaded.  Using serif font.");
      font = new Font("serif", Font.PLAIN, 24);
    }
    return font;
  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="File Loaders">

  /**
   * Load a file. First looks for an external version of the resource. If it cant find one use the
   * one inside the class.
   *
   * @param s file string
   * @return the File
   */
  public static File loadFile(String s) {
    try {
      File file = new File(s);

      if (!file.exists()) {
        File sourceDir = new File(DataLoader.class.getResource("/").toURI());
        file = new File(sourceDir, s);
      }

      if (!file.exists()) {
        System.err.println("Error loading file: " + s);
      }

      return file;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Plaintext File Loaders">

  /**
   * Converts a string to a file and then passes it to {@link #loadPlainTextFile(Game, Lookup, PFlags, File)
   * loadPlainTextFile}
   *
   * @param g game instance
   * @param lookup lookup instance
   * @param s file path
   * @return lookup table data
   */
  public static Lookup loadSaveFile(Game g, Lookup lookup, String s) {
    File f = loadFile(s);

    // generate parsing data flag storage
    PFlags parseFlags = new PFlags();
    parseFlags.endData.addEndPair('{', '}');
    parseFlags.endData.addEndPair('(', ')');
    parseFlags.endData.addEndPair('"', '"');
    parseFlags.endData.addEndPair('[', ']');
    parseFlags.endData.addEndPair('<', '>');

    if (s.contains(".sav")) {
      return loadPlainTextFile(g, lookup, parseFlags, f);
    } else {
      JsonLoader.parseJson(s, lookup, g, parseFlags);
    }
    return null;
  }

  public static Lookup loadSaveFile(String s, Game g, Lookup lookup, PFlags pFlags) {
    if (s.contains(".sav")) {
      File f = loadFile(s);
      return loadPlainTextFile(g, lookup, pFlags, f);
    } else {
      JsonLoader.parseJson(s, new DStore(lookup, g, pFlags));
    }
    return null;
  }

  public static Lookup loadSaveFile(String s, DStore dStore) {
    if (s.contains(".sav")) {
      File f = loadFile(s);
      return loadPlainTextFile(dStore.g, dStore.lookup, dStore.flags, f);
    } else {
      JsonLoader.parseJson(s, dStore);
    }
    return null;
  }

  /**
   * Loads data from a formatted .sav file. Reads the file line by line looking for items formatted
   * as an object.
   *
   * @param g      game instance
   * @param lookup loading lookup table
   * @param f      data file
   * @return lookup table data
   * @see DataLoader
   */
  public static Lookup loadPlainTextFile(Game g, Lookup lookup, PFlags parseFlags, File f) {

    try {
      parseFlags.fileName = f.getName();
      Scanner in = new Scanner(new FileInputStream(f)); // create a scanner from a file

      while (in.hasNextLine()) { // while the file has lines
        parseFlags.lineCount++; // increment the line index
        String firstLine = in.nextLine().trim(); // read and trim the line

        // try to load an object from the current line
        ParseData obj = parseText(firstLine, lookup, parseFlags);
        // if the object is formatted as a multi-line object continue loading
        //  the object from the next line before parsing it.
        if (parseFlags.endData.isUnpaired()) {
          continue;
        }

        // if the line made a valid ParseData parse its data.
        if (obj != null) {
          parseParseData(obj, lookup, g, parseFlags);
        }

      }
      in.close();

    } catch (Exception e) {
      System.err.println("Could not load save file.");
      e.printStackTrace();
    }
    return lookup;
  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Command Parsers">

  /**
   * Load a Sprite from a String.
   * <p>
   * Works with String, StringObjects or Sprite, StringObjects (object lookup). If the ParseData is
   * a String look up that string in the games sprite table and return the result.
   *
   * @param s      the filename or identifier
   * @param lookup sprite lookup table
   * @param game   game instance
   * @param flags  parsing flags
   * @return the Sprite with the corresponding id
   * @throws Exception passes parseObjData Exceptions through to caller
   */
  public static Sprite loadSprite(String s, Lookup lookup, Game game,
      PFlags flags) throws Exception {
    ObjData so = strToObjData(s, lookup, game, flags);
    return so.isType("string")
        ? game.getSprite((String) so.getObj())
        : (Sprite) so.getObj();
  }

  /**
   * Parse a general ParseData. If the ParseData is a command it calls {@link #parseCmdData(CmdData,
   * Lookup, Game, PFlags) parseCmdData} and returns null. If the ParseData is a java Object it
   * calls {@link #parseObjData(ObjData, Lookup, Game, PFlags) parseObjData} and returns the
   * object.
   *
   * @param obj    ParseData to parse
   * @param lookup parsing lookup data
   * @param game   game instance
   * @param flags  parsing flags
   * @return the generated object as applicable
   */
  public static Object parseParseData(ParseData obj, Lookup lookup, Game game, PFlags flags) {
    if (obj.isObj()) {
      return parseObjData((ObjData) obj, lookup, game, flags);
    }
    parseCmdData((CmdData) obj, lookup, game, flags);
    return null;
  }


  /**
   * Parse a ParseData as a command. See {@link DataLoader DataLoader} for more parsing information.
   * Uses the command_name to route the data to the correct command interpreter. Then uses the
   * following ParseData data to preform an action. Captures the exception from the raw version of
   * the command.
   *
   * @param cmd    command ParseData
   * @param lookup parsing lookup data
   * @param game   game instance
   * @param flags  parsing flag info
   */
  public static void parseCmdData(CmdData cmd, Lookup lookup, Game game, PFlags flags) {
    try {
      parseCmdDataRaw(cmd, lookup, game, flags);
    } catch (Exception e) {
      logParsingError("command", cmd, flags);
      e.printStackTrace();
    }
  }

  /**
   * Parse a ParseData as a command. See {@link DataLoader DataLoader} for more parsing information.
   * Uses the command_name to route the data to the correct command interpreter. Then uses the
   * following ParseData data to preform an action. Throws an exception if it encounters an error.
   *
   * @param cmd    command ParseData
   * @param lookup parsing lookup data
   * @param game   game instance
   * @param flags  parsing flag info
   * @throws Exception If a command loads an incorrect piece of data i.e. attempts to cast a parsed
   *                   object as an invalid object or encounters a parsing error, pass the Exception
   *                   on to the caller.
   */
  public static void parseCmdDataRaw(CmdData cmd, Lookup lookup, Game game, PFlags flags)
      throws Exception {
    if (cmd.isType("print")) {
      // print the following data to terminal
      for (String string : cmd.getData()) {
        System.out.println(strToObj(string, lookup, game, flags));
      }
    } else if (cmd.isType("addEntity")) {
      game.getWorld().getLoadedRoom().addEntity((Entity) strToObj(cmd.get(0), lookup, game, flags));
    } else if (cmd.isType("playSound")) {
      Sound sound = (Sound) strToObj(cmd.get(0), lookup, game, flags);
      sound.play();
    }  else if (cmd.isType("setplayerroom")) {
      // set the room of the given player

      Room room = (Room) strToObj(cmd.get(0), lookup, game, flags);
      game.getPlayer().setRoom(room);
    } else if (cmd.isType("genwallsfromsprite")) {
      // <>(Room room)
      // <>(Room room, float width, float offset)
      // generate wall shapes for the room with a given offset and horizon offset.
      // defaults to 5, 100.

      Room room = (Room) strToObj(cmd.get(0), lookup, game, flags);
      if (cmd.getSize() > 1) {
        Vector v = (Vector) strToObj(cmd.get(1), lookup, game, flags);
        room.generateRectangleWalls(v.getX(), v.getY());
      } else {
        room.generateRectangleWalls(5, 100f);
      }
    } else if (cmd.isType("setTextColor")) {

      MenuEntity menuEntity = (MenuEntity) strToObj(cmd.get(0), lookup, game, flags);
      menuEntity.setTextColor((Color) strToObj(cmd.get(1), lookup, game, flags));

    } else if (cmd.isType("setText")) {

      MenuEntity menuEntity = (MenuEntity) strToObj(cmd.get(0), lookup, game, flags);
      menuEntity.setText((String) strToObj(cmd.get(1), lookup, game, flags));

    } else if (cmd.isType("setBaseColor")) {

      MenuEntity menuEntity = (MenuEntity) strToObj(cmd.get(0), lookup, game, flags);
      menuEntity.setBackgroundColor((Color) strToObj(cmd.get(1), lookup, game, flags));

    } else if (cmd.isType("setBorder")) {

      MenuEntity menuEntity = (MenuEntity) strToObj(cmd.get(0), lookup, game, flags);

      for (int index = 1; index < cmd.getSize(); index++) {
        ObjData objData = strToObjData(cmd.get(index), lookup, game, flags);

        if (objData.isType("color")) {
          menuEntity.setBorderColor((Color) objData.getObj());
        } else if (objData.isType("integer")) {
          menuEntity.setBorderSize((Integer) objData.getObj());
        } else if (objData.isType("float")) {
          menuEntity.setBorderSize((int) ((float) objData.getObj()));
        }
      }
    } else if (cmd.isType("loadroom")) {
      // Calls setLoadedRoom with the given room

      Room room = (Room) strToObj(cmd.get(0), lookup, game, flags);
      game.getWorld().setLoadedRoom(room);
    } else if (cmd.isType("roomWindow")) {
      // Sets the game window of the room.

      Room room = (Room) strToObj(cmd.get(0), lookup, game, flags);
      Vector size = (Vector) strToObj(cmd.get(1), lookup, game, flags);
      room.setWindowSize(size);
    } else if (cmd.isType("doorexitvector")) {
      // Set the given doors exit vector to the given vector

      Door d = (Door) strToObj(cmd.get(0), lookup, game, flags);
      Vector v = (Vector) strToObj(cmd.get(1), lookup, game, flags);
      d.setExitVector(v);
    } else if (cmd.isType("setplayerpos")) {
      // Set the game instance's player position with the given vector

      Vector v = (Vector) strToObj(cmd.get(0), lookup, game, flags);
      game.getPlayer().setPosition(v);
    } else if (cmd.isType("setPosition")) {
      // Set the given entities position with the given vector

      Entity e = (Entity) strToObj(cmd.get(0), lookup, game, flags);
      Vector v = (Vector) strToObj(cmd.get(1), lookup, game, flags);
      e.setPosition(v);
    } else if (cmd.isType("setVelocity") || cmd.isType("setVel")) {
      // Set the given entities position with the given vector

      Entity e = (Entity) strToObj(cmd.get(0), lookup, game, flags);
      Vector v = (Vector) strToObj(cmd.get(1), lookup, game, flags);
      e.setVelocity(v);
    } else if (cmd.isType("setCVelocity") || cmd.isType("setCVel")) {
      // Set the given entities position with the given vector

      Entity e = (Entity) strToObj(cmd.get(0), lookup, game, flags);
      Vector v = (Vector) strToObj(cmd.get(1), lookup, game, flags);
      e.setConstVelocity(v);
    } else if (cmd.isType("setCForce") || cmd.isType("setCF")) {
      // Set the given entities position with the given vector

      Entity e = (Entity) strToObj(cmd.get(0), lookup, game, flags);
      Vector v = (Vector) strToObj(cmd.get(1), lookup, game, flags);
      e.setConstForce(v);
    } else if (cmd.isType("setAcceleration") || cmd.isType("setAcc")) {
      // Set the given entities position with the given vector

      Entity e = (Entity) strToObj(cmd.get(0), lookup, game, flags);
      Vector v = (Vector) strToObj(cmd.get(1), lookup, game, flags);
      e.addForce(v);
    } else if (cmd.isType("shape")) {
      // <>(Entity/Room, Shape)
      // <>(Entity/Room, Shape, Vector)
      // set the shape of the given entity or room
      // if a vector is added it offsets the shape by that vector
      ObjData e = strToObjData(cmd.get(0), lookup, game, flags);
      ObjData o1 = strToObjData(cmd.get(1), lookup, game, flags);

      Shape s;

      if (o1.isType("entity")) {
        s = ((Entity) o1.getObj()).getShape();
      } else {
        s = ((Shape) o1.getObj());
      }


      if (cmd.getSize() == 3) {
        Vector v = (Vector) strToObj(cmd.get(2), lookup, game, flags);
        MultiShape ms = new MultiShape();

        ms.add(s, v);
        s = ms;
      }

      if (e.isType("entity")) {
        ((Entity) e.getObj()).setShape(s);
      }
//      else {
//        ((Room) e.getObj()).addWall(s);
//      }
    } else if (cmd.isType("setDelay")) {
      // <>(Sprite, float)
      // <>([Sprite1, Sprite2, ...], float)
      // Sets the delay of all the given sprites

      ObjData o = strToObjData(cmd.get(0), lookup, game, flags);
      float del = (float) strToObj(cmd.get(1), lookup, game, flags);
      if (o.isType("array")) {
        ArrayList<String> list = (ArrayList<String>) o.getObj();
        for (String s : list) {
          loadSprite(s, lookup, game, flags).getAnimation().setAllDelay(del);
        }
      } else {
        loadSprite((String) o.getObj(), lookup, game, flags).getAnimation()
            .setAllDelay(del);
      }
    } else if (cmd.isType("spriteShift")) {
      // Set the local shift of the given sprite

      Entity e = (Entity) strToObj(cmd.get(0), lookup, game, flags);
      Vector v = (Vector) strToObj(cmd.get(1), lookup, game, flags);
//      e.setSpriteShift(v);
      e.setSpriteShift(v);
    } else if (cmd.isType("vehicleSpriteData")) {
      // <>(Vehicle, Sprite, Sprite)
      // Sets sprite data of a vehicle
      // first sprite represents the interior sprite the seconds represents the exterior

      Vehicle vehicle = (Vehicle) strToObj(cmd.get(0), lookup, game, flags);
      Sprite in = (Sprite) strToObj(cmd.get(1), lookup, game, flags);
      Sprite out = (Sprite) strToObj(cmd.get(2), lookup, game, flags);

      vehicle.setSpriteData(in, out);
    } else if (cmd.isType("setExitNormals")) {
      // Set the objects exit normals
      ContainerEntity entity = (ContainerEntity) strToObj(cmd.get(0), lookup, game, flags);

      entity.clearExitNormals();
      for (String s : cmd.getData().subList(1, cmd.getSize())) {
        entity.addExitNormal((Vector) strToObj(s, lookup, game, flags));
      }
    } else if (cmd.isType("setPlayer")) {
      // Set the game's player

      game.setPlayer((Player) strToObj(cmd.get(0), lookup, game, flags));
    } else if (cmd.isType("addChild")) {
      // make the second entity a child of the first

      Entity entity = (Entity) strToObj(cmd.get(0), lookup, game, flags);
      Entity entity2 = (Entity) strToObj(cmd.get(1), lookup, game, flags);

      entity.addChild(entity2);

    } else if (cmd.isType("removeChild")) {
      // make the second entity a child of the first

      Entity entity = (Entity) strToObj(cmd.get(0), lookup, game, flags);
      Entity entity2 = (Entity) strToObj(cmd.get(1), lookup, game, flags);

      entity.removeChild(entity2);

      if (cmd.hasTag("room")) {
        entity2.removeFromContainer();
      }
    } else if (cmd.isType("sendDialogue") || cmd.isType("sendDlg")) {
      // <>(Dialogue/String)
      // <>(Dialogue/String, Entity)
      // Send a message from the game world, or from a given sender

      ObjData messageObject = strToObjData(cmd.get(0), lookup, game, flags);
      Dialogue dialogue;

      if (messageObject.isType("dialogue")) {
        dialogue = (Dialogue) messageObject.getObj();
      } else {
        dialogue = new Dialogue((String) messageObject.getObj());
      }

      if (cmd.hasData(1)) {
        dialogue.setAllSpeakers((Entity) strToObj(cmd.get(1), lookup, game, flags));
      }

      dialogue.resetIndex();
      game.getWorld().setDialogue(dialogue);
    } else if (cmd.isType("setBlocking")) {
      // Set a dialogue's blocking state

      Dialogue dialogue = (Dialogue) strToObj(cmd.get(0), lookup, game, flags);
      dialogue.setBlocking((boolean) strToObj(cmd.get(1), lookup, game, flags));
    } else if (cmd.isType("childPosition") || cmd.isType("seat_position")) {
      // Set the child position of the given entity

      ContainerEntity seat = (ContainerEntity) strToObj(cmd.get(0), lookup, game, flags);
      Vector v = (Vector) strToObj(cmd.get(1), lookup, game, flags);
      seat.setChildPosition(v);
    } else if (cmd.isType("childOffset") || cmd.isType("seatOffset")) {
      // Set the child position of the given entity

      ContainerEntity seat = (ContainerEntity) strToObj(cmd.get(0), lookup, game, flags);
      Vector v = (Vector) strToObj(cmd.get(1), lookup, game, flags);
      seat.setChildOffset(v);
    } else if (cmd.isType("setMasterBaseScale")) {
      // set the masterBaseScale of the given sprite

      String img = (String) strToObj(cmd.get(0), lookup, game, flags);
      float scale = toFloat(strToObjData(cmd.get(1), lookup, game, flags));
      game.setMasterBaseScale(img, scale);
    } else if (cmd.isType("resetBaseScale")) {
      // resets the baseScale of the given sprite

      ObjData so = strToObjData(cmd.get(0), lookup, game, flags);
      float scale = toFloat(strToObjData(cmd.get(1), lookup, game, flags));
      so.setObj(game.setBaseScale((Sprite) so.getObj(), scale));
    } else if (cmd.isType("addSprite")) {
      // add a sprite to an entity
      // uses a general cmd parser, to input the entity and Sprite

      parseSpriteCmd(Entity::addSprite, cmd, lookup, game, flags);
    } else if (cmd.isType("entity")) {

      Entity entity = (Entity) strToObj(cmd.get(0), lookup, game, flags);
      entityObj(entity, cmd, lookup, game, flags);
    } else if (cmd.isType("spriteT")) {
      // transition an entity to a sprite

      parseSpriteCmd(Entity::transitionTo, cmd, lookup, game, flags);
    } else if (cmd.isType("s") || cmd.isType("sprite")) {
      // set the current sprite
      parseSpriteCmd(Entity::setSprite, cmd, lookup, game, flags);
    } else if (cmd.isType("lsprite")) {
      // set the current sprite
      String str = (String) strToObj(cmd.get(2), lookup, game, flags);
      parseSpriteCmd(((entity, sprite) -> {
        entity.addLSprite(str, sprite);
        entity.updateLSprites();
      }), cmd, lookup, game, flags);

    } else if (cmd.isType("ulsprite")) {
      // set the current sprite
      Entity e = (Entity) strToObj(cmd.get(0), lookup, game, flags);
      e.updateLSprites();
    } else if (cmd.isType("setName")) {
      // set a given entities name

      Entity e = (Entity) strToObj(cmd.get(0), lookup, game, flags);
      String s = (String) strToObj(cmd.get(1), lookup, game, flags);
      e.setName(s);
    } else if (cmd.isType("localMove")) {
      Entity e = (Entity) strToObj(cmd.get(0), lookup, game, flags);
      Entity e2 = (Entity) strToObj(cmd.get(1), lookup, game, flags);
      Vector location = null;
      Event event = null;
      float delay = 0;
      MovementType type = null;


      for (int index = 2; index < cmd.getSize(); index++ ) {
        ObjData objData = strToObjData(cmd.get(index), lookup, game, flags);

        if (objData.isType("entity")) {
          location = ((Entity) objData.getObj()).getPos();
        }

        if (objData.isType("event")) {
          event = ((Event) objData.getObj());
        }

        if (objData.isType("float")) {
          delay = ((float) objData.getObj());
        }

        if (objData.isType("string")) {
          type = MovementType.valueOf(((String) objData.getObj()).toUpperCase());
        }
      }

      Mover.moveTo(e, location.addi(e.getPos().subi(e2.getPos())), delay, type, event);
    } else if (cmd.isType("setCollision")) {

      Entity e = (Entity) strToObj(cmd.get(0), lookup, game, flags);
      Boolean b = (Boolean) strToObj(cmd.get(1), lookup, game, flags);
      e.setCollides(b);
    } else if (cmd.isType("moveTo")) {
      // set a given entities name

      Entity e = (Entity) strToObj(cmd.get(0), lookup, game, flags);
      Vector location = null;
      Event event = null;
      float delay = 0;
      MovementType type = null;


      for (int index = 1; index < cmd.getSize(); index++ ) {
        ObjData objData = strToObjData(cmd.get(index), lookup, game, flags);

        if (objData.isType("entity")) {
          location = ((Entity) objData.getObj()).getPos();
        }

        if (objData.isType("event")) {
           event = ((Event) objData.getObj());
        }

        if (objData.isType("float")) {
          delay = ((float) objData.getObj());
        }

        if (objData.isType("string")) {
          type = MovementType.valueOf(((String) objData.getObj()).toUpperCase());
        }
      }

      Mover.moveTo(e, location, delay, type, event);
    }  else if (cmd.isType("setPhysics")) {
      // set a given entities name

      Entity e = (Entity) strToObj(cmd.get(0), lookup, game, flags);
      Boolean b = (Boolean) strToObj(cmd.get(1), lookup, game, flags);
      e.setPhysics(b);
    } else if (cmd.isType("addDialogue")) {
      // add dialogue to an npc or character

      NPC e = (NPC) strToObj(cmd.get(0), lookup, game, flags);
      ObjData o = strToObjData(cmd.get(1), lookup, game, flags);
      if (o.isType("string")) {
        e.addDialogue(new Dialogue((String) o.getObj(), e));
      } else {
        e.addDialogue((Dialogue) o.getObj());
      }
    } else if (cmd.isType("shiftPos")) {
      // shift the position of the entity by the vector

      ObjData objData = strToObjData(cmd.get(0), lookup, game, flags);
      Vector v = (Vector) strToObj(cmd.get(1), lookup, game, flags);
      if (objData.isType("multiShape")) {
        ((MultiShape) objData.getObj()).shiftAll(v);
      } else if (objData.isType("polygon")) {
        ((PolygonShape) objData.getObj()).addShift(v);
      } else if (objData.isType("entity")) {
        Entity e = (Entity) objData.getObj();
        e.setPosition(e.getPosition().addi(v));
      }
    } else if (cmd.isType("repeatLast")) {
      // set the npc's dialogue repeat status to repeat the last dialogue

      NPC e = (NPC) strToObj(cmd.get(0), lookup, game, flags);
      e.repeatLast();
    } else if (cmd.isType("repeatAll")) {
      // set the npc's dialogue repeat status to repeat all the dialogue

      NPC e = (NPC) strToObj(cmd.get(0), lookup, game, flags);
      e.repeatAll();
    } else if (cmd.isType("repeatNone")) {
      // set the npc's dialogue repeat status to repeat no dialogue dialogue

      NPC e = (NPC) strToObj(cmd.get(0), lookup, game, flags);
      e.repeatNone();
    } else if (cmd.isType("repeatCustom")) {
      // set the npc's dialogue repeat status to repeat a custom dialogue
      // the custom dialogue is referred to by its index in the npc

      NPC e = (NPC) strToObj(cmd.get(0), lookup, game, flags);
      int i = toInt(strToObjData(cmd.get(0), lookup, game, flags));
      e.repeatCustom(i);
    } else if (cmd.isType("doorConnect")) {
      // connect two given doors to each-other

      Door a = (Door) strToObj(cmd.get(0), lookup, game, flags);
      Door b = (Door) strToObj(cmd.get(1), lookup, game, flags);

      a.setTargetDoor(b);
      b.setTargetDoor(a);
    } else if (cmd.isType("setPickupEvent")) {
      // set the pickup event of the given entity

      ItemEntity i = (ItemEntity) strToObj(cmd.get(0), lookup, game, flags);
      Event e = (Event) strToObj(cmd.get(1), lookup, game, flags);
      i.setPickupEvent(e);
    } else if (cmd.isType("setGlitch")) {
      // set the pickup event of the given entity

      Entity e1 = (Entity) strToObj(cmd.get(0), lookup, game, flags);

      float f1 = (Float) strToObj(cmd.get(1), lookup, game, flags);
      if (cmd.getSize() > 2) {
        float f2 = (Float) strToObj(cmd.get(2), lookup, game, flags);
        float f3 = (Float) strToObj(cmd.get(3), lookup, game, flags);

        e1.setFilter(new AlphaFilter(f1, f2, f3));
      } else {
        if (f1 == 1.0f) {
          e1.setFilter(null);
        } else {
          e1.setFilter(new AlphaFilter(f1));
        }
      }
    } else if (cmd.isType("setAlpha")) {
      // tell the given entity to transition to the next sprite

      Entity e1 = (Entity) strToObj(cmd.get(0), lookup, game, flags);

      float f1 = (Float) strToObj(cmd.get(1), lookup, game, flags);
      if (cmd.getSize() > 2) {
        float f2 = (Float) strToObj(cmd.get(2), lookup, game, flags);
        float f3 = (Float) strToObj(cmd.get(3), lookup, game, flags);

        e1.setFilter(new AlphaFilter(f1, f2, f3));
      } else {
        if (f1 == 1.0f) {
          e1.setFilter(null);
        } else {
          e1.setFilter(new AlphaFilter(f1));
        }
      }

    } else if (cmd.isType("setGameAlpha")) {
      // tell the given entity to transition to the next sprite

      float f1 = (Float) strToObj(cmd.get(0), lookup, game, flags);
      if (cmd.getSize() > 2) {
        float f2 = (Float) strToObj(cmd.get(1), lookup, game, flags);
        float f3 = (Float) strToObj(cmd.get(2), lookup, game, flags);

        game.setGlobalFilter(new AlphaFilter(f1, f2, f3));
      } else {
        if (f1 == 1.0f) {
          game.setGlobalFilter(null);
        } else {
          game.setGlobalFilter(new AlphaFilter(f1));
        }
      }

    } else if (cmd.isType("noMenu")) {
      // tell the given entity to transition to the next sprite

      game.startGame();
    } else if (cmd.isType("hold")) {
      // tell the given entity to transition to the next sprite

      game.setHold((boolean) strToObj(cmd.get(0), lookup, game, flags));
    } else if (cmd.isType("nextImage")) {
      // tell the given entity to transition to the next sprite

      Entity i = (Entity) strToObj(cmd.get(0), lookup, game, flags);
      i.setTransitionSprite(true);
    } else if (cmd.isType("startEvent")) {
      // set the interaction event of the given entity
      // if provided set the interaction override state of the entity

      Event e = (Event) strToObj(cmd.get(0), lookup, game, flags);
      game.setStartEvent(e);
    } else if (cmd.isType("setInteraction") || cmd.isType("setEvent")) {
      // set the interaction event of the given entity
      // if provided set the interaction override state of the entity

      Entity e = (Entity) strToObj(cmd.get(0), lookup, game, flags);
      Event event = (Event) strToObj(cmd.get(1), lookup, game, flags);

      e.setInteractEvent(event);
      if (cmd.hasData(2)) {
        e.setInteractOverride((boolean) strToObj(cmd.get(2), lookup, game, flags));
      }
    } else if (cmd.isType("setDefaultDelay")) {
      // set the default delay of the animation/sprite

      Animation s = (Animation) strToObj(cmd.get(0), lookup, game, flags);
      s.setDefaultDelay((float) strToObj(cmd.get(1), lookup, game, flags));
    } else if (cmd.isType("loadFile")) {
      // load the given save file

      String s = (String) strToObj(cmd.get(0), lookup, game, flags);
      loadSaveFile(s, game, lookup, flags);
    } else if (cmd.isType("setChild")) {
      // set the second entity given as the child of the first

      Entity a = (Entity) strToObj(cmd.get(0), lookup, game, flags);
      Entity b = (Entity) strToObj(cmd.get(1), lookup, game, flags);
      a.addChild(b);
    } else if (cmd.isType("normalize")) {
      // normalize a vector

      Vector vector = (Vector) strToObj(cmd.get(0), lookup, game, flags);
      vector.normalize();
    } else if (cmd.isType("scale")) {
      // scale a given vector

      Vector vector = (Vector) strToObj(cmd.get(0), lookup, game, flags);
      float scale = toFloat(strToObjData(cmd.get(1), lookup, game, flags));
      vector.scale(scale);
    } else if (cmd.isType("setVector")) {
      // scale a given vector

      Vector vector = (Vector) strToObj(cmd.get(0), lookup, game, flags);
      Vector value = (Vector) strToObj(cmd.get(1), lookup, game, flags);
      vector.set(value);
    } else if (cmd.isType("setExitEvent")) {
      // scale a given vector

      EnterableEntity entity = (EnterableEntity) strToObj(cmd.get(0), lookup, game, flags);
      Event event = (Event) strToObj(cmd.get(1), lookup, game, flags);
      entity.setExitEvent(event);
    } else if (cmd.isType("setEnterEvent")) {
      // scale a given vector

      EnterableEntity entity = (EnterableEntity) strToObj(cmd.get(0), lookup, game, flags);
      Event event = (Event) strToObj(cmd.get(1), lookup, game, flags);
      entity.setEnterEvent(event);
    } else if (cmd.isType("flipPolygonHorizontal")) {
      // flip a given polygon horizontally

      PolygonShape p = (PolygonShape) strToObj(cmd.get(0), lookup, game, flags);
      p.flipHorizontal();
    } else if (cmd.isType("setState")) {
      // set the sate of the given entity

      Entity e = (Entity) strToObj(cmd.get(0), lookup, game, flags);
      ObjData s = strToObjData(cmd.get(1), lookup, game, flags);
      if (s.isType("string")) {
        e.setState(State.valueOf(((String) s.getObj()).toUpperCase()));
      } else {
        e.setState((State) s.getObj());
      }

      if (!cmd.get(2).isEmpty()) {
        s = strToObjData(cmd.get(2), lookup, game, flags);
        if (s.isType("string")) {
          e.setNextState(State.valueOf(((String) s.getObj()).toUpperCase()));
        } else {
          e.setNextState((State) s.getObj());
        }
      }

    } else if (cmd.isType("setCustomPose")) {
      // set the sate of the given entity

      SeatEntity e = (SeatEntity) strToObj(cmd.get(0), lookup, game, flags);
      ObjData s = strToObjData(cmd.get(1), lookup, game, flags);
      if (s.isType("string")) {
        e.setCustomPose(Pose.valueOf(((String) s.getObj()).toUpperCase()));
      } else {
        e.setCustomPose((Pose) s.getObj());
      }
      // <editor-fold defaultstate="collapsed" desc="Control Commands">
    } else if (cmd.isType("setVehicleShapes")) {
    // set the sate of the given entity

      Vehicle vehicle = (Vehicle) strToObj(cmd.get(0), lookup, game, flags);

      for (String string : cmd.getData().subList(1, cmd.getSize())) {
        ObjData objData = strToObjData(string, lookup, game, flags);

        Shape shape = null;

        if (objData.isType("String")) {
          String sName = (String) objData.getObj();

          if (vehicle.hasShape(sName)) {
            shape = vehicle.getShape(sName);
          }
        } else {
          shape = (Shape) objData.getObj();
        }

        if (shape != null) {
          if (objData.hasTag("inside") || objData.hasTag("i")) {
            vehicle.setInteriorShape(shape);
          } else if (objData.hasTag("transition") || objData.hasTag("t")) {
            vehicle.setTransitionShape(shape);
          } else if (objData.hasTag("area") || objData.hasTag("a")) {
            vehicle.setInteriorArea(shape);
          }
        }
      }

    // <editor-fold defaultstate="collapsed" desc="Control Commands">
    } else if (cmd.isType("loadButtonConstants")) {
      // load the button constants from the window

      game.getWindow().loadButtonConstants();
    } else if (cmd.isType("keyConstants")) {
      // set the key constants of the game
      // works with array inputs too

      parseButtonCmd(cmd, ButtonType.Keyboard, game::setConstant, lookup, game, flags);
    } else if (cmd.isType("mouseConstants")) {
      // set the mouse constants of the game
      // works with array inputs too

      parseButtonCmd(cmd, ButtonType.Mouse, game::setConstant, lookup, game, flags);
    } else if (cmd.isType("gameKeySettings")) {
      // set the game key control of the game
      // works with array inputs too

      parseButtonCmd(cmd, ButtonType.Keyboard, game::setControl, lookup, game, flags);
    } else if (cmd.isType("load")) {
      // set the game key control of the game
      // works with array inputs too
      String pstr = (String) strToObj(cmd.get(0), lookup, game, flags);
      File path = new File(pstr);

      if (path.isDirectory()) {
        for (String file_str : path.list()) {
          if (file_str.contains(".json")) {
            JsonLoader.parseJson(pstr + "/" + file_str, lookup, game, flags);
          }
        }
      } else {
        JsonLoader.parseJson(pstr, lookup, game, flags);
      }
    } else if (cmd.isType("addRoom")) {
      // set the game key control of the game
      // works with array inputs too
      Room loadedRoom = game.getWorld().getLoadedRoom();
      Room room = (Room) strToObj(cmd.get(0), lookup, game, flags);
      Vector pos = (Vector) strToObj(cmd.get(1), lookup, game, flags);

      boolean cloneEntities = !cmd.hasTag("noclone");

      if (cmd.getSize() == 3) {
        Vector offset = (Vector) strToObj(cmd.get(2), lookup, game, flags);
        loadedRoom.addRoom(room, pos, offset, cloneEntities);
      } else if (cmd.hasTag("absolute")) {
        loadedRoom.addRoomPosition(room, pos, cloneEntities);
      } else {
        loadedRoom.addRoom(room, pos, cloneEntities);
      }
    } else {
      throw new Exception("Command: <" + cmd.getDataType() + "> not supported.");
    }
    // </editor-fold>
  }

  /**
   * Repeat a command on an array of inputs.
   *
   * @param cmdString command string of the cmd originally called
   * @param data      array object data
   * @param lookup    parsing lookup data
   * @param game      game instance
   * @param flags     parsing flag data
   * @throws Exception passes any parsing errors to the caller
   */
  public static void parseCmdArray(String cmdString, ObjData data, Lookup lookup,
      Game game, PFlags flags) throws Exception {
    for (String keyString : data.getData()) {
      ObjData objData = strToObjData(keyString, lookup, game, flags);
      CmdData cmdData = new CmdData(cmdString, objData.getData());
      parseCmdData(cmdData, lookup, game, flags);
    }
  }

  /**
   * General button command parser.
   * <p>
   * Takes in a CmdData, decodes the input and applies it to a function that takes in a button. Uses
   * the button type to dictate the type of button input.
   *
   * @param cmd    original CmdData data
   * @param type   type of button to decode
   * @param f      function to apply to the buttons
   * @param lookup parsing lookup data
   * @param game   game instance
   * @param flags  parsing flag data
   * @throws Exception passes any parsing errors to the caller
   */
  public static void parseButtonCmd(CmdData cmd, ButtonType type, BiConsumer<String, Button> f,
      Lookup lookup, Game game, PFlags flags) throws Exception {
    String label = null;

    for (String str : cmd.getData()) {
      ObjData obj = strToObjData(str, lookup, game, flags);
      if (label != null) {
        Button button = parseButton(type, obj, lookup, game, flags);
        f.accept(label, button);
        label = null;
      } else if (obj.isType("array")) {

        ObjData buttonTag = strToObjData(obj.get(0), lookup, game, flags);
        ObjData buttonData = strToObjData(obj.get(1), lookup, game, flags);
        Button button = parseButton(type, buttonData, lookup, game, flags);
        f.accept((String) buttonTag.getObj(), button);
      } else if (obj.isType("string")) {
        label = (String) obj.getObj();
      }
    }
  }

  /**
   * General sprite command parser. <br>
   * <p>
   * Takes in a CmdData, decodes the input data. If the first input is an entity, it applies the
   * given data to the provided func. If the first input is a Room, it sets the sprite of the room.
   * <p>
   * Forms:
   * <p>
   * -(Entity, Sprite, *Vector/Float, *Vector/Float, *Vector$pos)
   * <p>
   * The starred inputs are optional. Any floats are interpreted as a global layer. Any untagged
   * vectors are interpreted as shift vector. If a vector is tagged as a "position" or "pos" it is
   * interpreted as a custom position.
   * <p>
   * -(Room, Sprite)
   * <p>
   * Set the sprite of the room to the input sprite
   *
   * @param func   function to apply to entity and sprite
   * @param cmd    original CmdData object
   * @param lookup parsing lookup data
   * @param game   game instance
   * @param flags  parsing flag data
   * @throws Exception passes any parsing errors to the caller
   */
  public static void parseSpriteCmd(BiConsumer<Entity, Sprite> func, CmdData cmd,
      Lookup lookup, Game game, PFlags flags) throws Exception {
    ObjData entityRef = strToObjData(cmd.get(0), lookup, game, flags);
    ObjData spriteObj = strToObjData(cmd.get(1), lookup, game, flags);

    Sprite sprite;

    if (entityRef.isType("entity")) {
      Entity e = (Entity) entityRef.getObj();

      if (spriteObj.isType("entity")) {
        Entity e2 = ((Entity) spriteObj.getObj());
        sprite = (e2).getSprite();
        e.addLSprite(e2.getLSpriteMap());
        e.updateLSprites();
      } else {
        sprite = loadSprite(cmd.get(1), lookup, game, flags);
      }

      for (int index = 2; index < cmd.getSize(); index++) {
        ObjData ref2 = strToObjData(cmd.get(index), lookup, game, flags);

        if (ref2.isType("vector")) {
          if (ref2.hasTag("pos") || ref2.hasTag("position") || ref2.hasTag("g") || ref2
              .hasTag("game")) {
            sprite.setGameLocation((Vector) ref2.getObj());
          } else if (ref2.hasTag("l") || ref2.hasTag("local")) {
            sprite.setGameLocation((Vector) ref2.getObj());
          } else {
            e.setSpriteShift((Vector) ref2.getObj());
          }
        } else if (!ref2.isType("string")) {
          if (ref2.hasTag("l") || ref2.hasTag("local")) {
            sprite.setLocalLayer(toFloat(ref2));
          } else if (ref2.hasTag("g") || ref2.hasTag("game")) {
            sprite.setGameLayer(toInt(ref2));
          } else {
            sprite.setGlobalLayer(toInt(ref2));
          }
        }
      }
      func.accept(e, sprite);
    } else if (entityRef.isType("room")) {

      if (spriteObj.isType("entity")) {
        Entity e2 = ((Entity) spriteObj.getObj());
        sprite = (e2).getSprite();
      } else {
        sprite = loadSprite(cmd.get(1), lookup, game, flags);
      }

      ((Room) entityRef.getObj()).setSprite(sprite);
    }
  }

  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Object Parsers">

  /**
   * Create a ObjData object.
   * <p>
   * Load it with data parsed from an input string.
   *
   * @param s      input string
   * @param lookup parsing lookup data
   * @param game   game instance
   * @param flags  parsing flags
   * @return A loaded ObjData object
   */
  public static ObjData strToObjData(String s, Lookup lookup, Game game, PFlags flags) {
    ParseData so = parseText(s, lookup, flags);
    if (so == null) {
      logParsingError("string", s, flags);
    } else if (!so.isObj()) {
      so = parseText("cmd:(" + s + ")", lookup, flags);
    }

    ObjData objData = (ObjData) so;
    parseObjData(objData, lookup, game, flags);
    return objData;
  }

  /**
   * Apply a function to a string if it is a valid string.
   * <p>
   * Ease of use function. Takes in a ObjData object and if its type is a string it will apply a
   * function to convert it to an different Object.
   *
   * @param objData input data
   * @return A loaded java Object
   */
  public static Object stringRouter(Function<String, Object> function, ObjData objData) {
    if (objData.isType("string")) {
      return function.apply((String) objData.getObj());
    } else {
      return objData.getObj();
    }
  }

  /**
   * Create a java object from an input string.
   *
   * @param s      input string
   * @param lookup parsing lookup data
   * @param game   game instance
   * @param flags  parsing flags
   * @return a java object
   */
  public static Object strToObj(String s, Lookup lookup, Game game, PFlags flags) {
    return strToObjData(s, lookup, game, flags).getObj();
  }

  /**
   * Create a java object from an ObjData input.
   * <p>
   * If the ObjData has a name add it to the lookup table under the given name.
   *
   * @param obj    input ObjData
   * @param lookup parsing lookup data
   * @param game   game instance
   * @param flags  parsing flags
   * @return A loaded java object
   */
  public static Object parseObjData(ObjData obj, Lookup lookup, Game game, PFlags flags) {
    try {
      Object o = parseObjDataRaw(obj, lookup, game, flags);
      obj.setObj(o);

      if (obj.hasName() && lookup != null) {
        lookup.put(obj.getName(), obj);
      }

      if (game != null && game.getWorld() != null && game.getWorld().getLoadedRoom() != null &&
          obj.isType("entity") && !obj.hasTag("noload") && !flags.noload) {
        if (!game.getWorld().getLoadedRoom().containsEntity((Entity) o)) {
          game.getWorld().getLoadedRoom().addEntity((Entity) o);
        }
      }

      return o;
    } catch (Exception e) {
      logParsingError("object", obj, flags);
      e.printStackTrace();
      return null;
    }
  }



  /**
   * Create a java object from an ObjData input.
   * <p>
   * See {@link DataLoader DataLoader} for more parsing information.
   * <p>
   * Uses the object_type to route the data to the correct object interpreter. Then uses the data in
   * the ObjData to create a java Object.
   *
   * @param obj    input ObjData
   * @param lookup parsing lookup data
   * @param game   game instance
   * @param flags  parsing flags
   * @return A loaded java object
   * @throws Exception pas ses any parsing errors to the caller
   */
  public static Object parseObjDataRaw(ObjData obj, Lookup lookup, Game game, PFlags flags)
      throws Exception {
    if (obj.getObj() != null) {
      return obj.getObj();
    }

    if (obj.isType("lookup")) {
      obj.addTag("noload");
      return lookup.get(obj.get(0));
    }

    // create a room with no added data
    if (obj.isType("room")) {
      Room room = new Room(game);
      room.setName(obj.getName());
      game.getWorld().addRoom(room);
      return room;
    }

    if (obj.isType("sound")) {
      return new Sound((String) strToObj(obj.get(0), lookup, game, flags));
    }

    // create an entity with the entity parser and add it to the currently loaded room
    if (obj.isType("entity")) {
      return entityObj(new Entity(game), obj, lookup, game, flags);
    }

    // create an npc from the entity parser and add it to the currently loaded room
    if (obj.isType("npc")) {
      NPC npc = new NPC(new Entity(game));
      entityObj(npc, obj, lookup, game, flags);
      return npc;
    }

    if (obj.isType("textBox")) {
      TextBox menuEntity = new TextBox(game);
      entityObj(menuEntity, obj, lookup, game, flags);

      for (String objString : obj.getData()) {
        ObjData objData = strToObjData(objString, lookup, game, flags);
        if (objData.isType("string")) {
          menuEntity.setText((String) objData.getObj());
        }

        if (objData.getObj() instanceof TextBox) {
          ((TextBox) objData.getObj()).setClone(menuEntity);
        }

        if (objData.isType("boolean")) {
          if (objData.hasTag("hCenter")) {
            menuEntity.setHorizontalCenter((boolean) objData.getObj());
          }

          if (objData.hasTag("vCenter")) {
            menuEntity.setVerticalCenter((boolean) objData.getObj());
          }
        }
      }

      game.getWorld().addDisplayText(menuEntity);

      obj.addTag("noload");
      return menuEntity;
    }

    if (obj.isType("mEntity")) {
      MenuEntity menuEntity = new MenuEntity(game);
      entityObj(menuEntity, obj, lookup, game, flags);

      for (String objString : obj.getData()) {
        ObjData objData = strToObjData(objString, lookup, game, flags);
        if (objData.isType("string")) {
          menuEntity.setText((String) objData.getObj());
        }

        if (objData.isType("boolean")) {
          if (objData.hasTag("hCenter")) {
            menuEntity.setHorizontalCenter((boolean) objData.getObj());
          }

          if (objData.hasTag("vCenter")) {
            menuEntity.setVerticalCenter((boolean) objData.getObj());
          }
        }
      }

      game.getWorld().addDisplayText(menuEntity);

      obj.addTag("noload");
      return menuEntity;
    }

    if (obj.isType("color")) {
      Color color = Color.black;
      float[] farr = new float[4];
      farr[3] = 1.0f;
      int fidx = 0;
      for (String data : obj.getData()) {
        ObjData objData = strToObjData(data, lookup, game, flags);

        if (objData.isType("float")) {
          farr[fidx] = (float) objData.getObj();
          fidx++;

          if (fidx > 3) {
            color = new Color(farr[0], farr[1], farr[2], farr[3]);
          } else if (fidx > 2) {
            color = new Color(farr[0], farr[1], farr[2]);
          }
        }
        if (objData.isType("string")) {
          color = Color.getColor((String) objData.getObj());
        }
      }

      return color;
    }

    // create an vehicle from the entity parser and add it to the currently loaded room
    if (obj.isType("vehicle")) {
      Vehicle vehicle = new Vehicle(new Entity(game));
      entityObj(vehicle, obj, lookup, game, flags);
      return vehicle;
    }

    // create a sprite object by looking up a String in the games sprite table
    // input: String, (Vector|Integer$(|local))
    // Vector sets the custom position of the sprite
    // Integer sets global layer of the sprite
    // Integer$local sets the local layer of the sprite
    if (obj.isType("sprite") || obj.isType("s")) {
      obj.setDataType("sprite");
      ObjData objData = strToObjData(obj.get(0), lookup, game, flags);

      Sprite sprite = null;

      if (objData.getObj() instanceof Entity) {
        sprite = ((Entity) objData.getObj()).getSprite();
      } if (objData.isType("string")) {
        String name = (String) strToObj(obj.get(0), lookup, game, flags);
        sprite = game.getSprite(name);
        sprite.setName(name);
      }

      if (sprite == null) {
        throw new Exception("Sprite Not Found.");
      }

      for (int index = 1; index < obj.getSize(); index++) {
        ObjData ref2 = strToObjData(obj.get(index), lookup, game, flags);

        if (ref2.isType("vector")) {
          sprite.setLocalLocation((Vector) ref2.getObj());
        } else {
          if (ref2.hasTag("game") || ref2.hasTag("g")) {
            sprite.setGameLayer(toInt(ref2));
          } else if (ref2.hasTag("local") || ref2.hasTag("l")) {
            sprite.setLocalLayer(toFloat(ref2));
          } else {
            sprite.setGlobalLayer(toInt(ref2));
          }
        }
      }

      // if the sprite has the clone tag clone the sprite from the table
      return obj.hasTag("$clone") ? sprite.clone() : sprite;
    }

    // Reference a state object
    // find the state representation of the input string or the current state of the given entity
    if (obj.isType("clone")) {
      ObjData objData = strToObjData(obj.get(0), lookup, game, flags);

//      if (objData.isType('multiShape'))
//
//      return ;
    }

    // Reference a state object
    // find the state representation of the input string or the current state of the given entity
    if (obj.isType("state")) {
      ObjData objData = strToObjData(obj.get(0), lookup, game, flags);
      if (objData.isType("entity")) {
        return ((Entity) objData.getObj()).getState();
      }

      return State.get((String) objData.getObj());
    }

    // lookup an animation sprite from the master sprite table.
    // if the cloning tag is set clone the animation from the master sprite
    // if the noloop tag is set set the animation to not loop
    if (obj.isType("animation") || obj.isType("a")) {
      obj.setDataType("animation");
      String name = (String) strToObj(obj.get(0), lookup, game, flags);
      Animation sprite = game.getAnimation(name);
//      sprite.setName(name);

      sprite = obj.hasTag("clone") ? sprite.clone() : sprite;
      sprite.setLoop(!obj.hasTag("noloop"));

      // handles states in state or string form
      if (obj.hasData(1)) {
        ObjData stateObj = strToObjData(obj.get(1), lookup, game, flags);
        State state = (State) stringRouter(State::get, stateObj);
        sprite.setState(state);
      }

      return sprite;
    }

    // clone a given entity and return it and add it to the loaded room (DEP)
    if (obj.isType("entityClone")) {
      Entity e = (Entity) strToObj(obj.get(0), lookup, game, flags);
      e = e.clone();
      return e;
    }

//    // clone the given shape (DEP)
    if (obj.isType("shapeClone")) {
      Shape s = (Shape) strToObj(obj.get(0), lookup, game, flags);
      s = s.clone();
      return s;
    }

    // create an item entity uses the entity parser
    if (obj.isType("item")) {
      ItemEntity itemEntity = new ItemEntity(new Entity(game));
      entityObj(itemEntity, obj, lookup, game, flags);
      return itemEntity;
    }

    // obj:(object, object, event, event)
    // create an if equal event if the first equals the second perform the first action
    //  and if the items are not equal preform the second
    if (obj.isType("ifcmd")) {
      ObjData o = strToObjData(obj.get(0), lookup, game, flags);

      ObjData op1;
      ObjData op2;

      ArrayList<String> cList;
      if (o.getObj() instanceof ArrayList && o.hasData()) {
        cList = o.getData();
        op1 = strToObjData(obj.get(1), lookup, game, flags);
        op2 = obj.hasData(2) ? strToObjData(obj.get(2), lookup, game, flags) : null;
      } else {
        cList = new ArrayList<>(Arrays.asList(obj.get(0), obj.get(1)));
        op1 = strToObjData(obj.get(2), lookup, game, flags);
        op2 = obj.hasData(3) ? strToObjData(obj.get(3), lookup, game, flags) : null;
      }


      Event event = new Event((a, b) -> {
        try {

          boolean flag = true;
          for (int i = 0; i < cList.size() - 1; i = i + 2) {
            if (!flag) {
              break;
            }

            ObjData o1 = strToObjData(cList.get(i), lookup, game, flags);
            ObjData o2 = strToObjData(cList.get(i + 1), lookup, game, flags);
            flag = o1.getObj().equals(o2.getObj());
          }

          if (flag ^ obj.hasTag("not")) {
            ((Event) op1.getObj()).act(a, b);
          } else if (op2 != null) {
            ((Event) op2.getObj()).act(a, b);
          }
        } catch (Exception e1) {
          e1.printStackTrace();
        }
      });
      event.add((a) -> {
        try {

          boolean flag = true;
          for (int i = 0; i < cList.size() - 1; i = i + 2) {
            if (!flag) {
              break;
            }

            ObjData o1 = strToObjData(cList.get(i), lookup, game, flags);
            ObjData o2 = strToObjData(cList.get(i + 1), lookup, game, flags);
            flag = o1.getObj().equals(o2.getObj());
          }

          if (flag ^ obj.hasTag("not")) {
            ((Event) op1.getObj()).act(a);
          } else if (op2 != null) {
            ((Event) op2.getObj()).act(a);
          }
        } catch (Exception e1) {
          e1.printStackTrace();
        }
      });
      event.add(() -> {
        try {

          boolean flag = true;
          for (int i = 0; i < cList.size() - 1; i = i + 2) {
            if (!flag) {
              break;
            }

            ObjData a = strToObjData(cList.get(i), lookup, game, flags);
            ObjData b = strToObjData(cList.get(i + 1), lookup, game, flags);
            flag = a.getObj().equals(b.getObj());
          }

          if (flag ^ obj.hasTag("not")) {
            ((Event) op1.getObj()).act();
          } else if (op2 != null) {
            ((Event) op2.getObj()).act();
          }
        } catch (Exception e1) {
          e1.printStackTrace();
        }
      });
      return event;
    }

    // preform a parsed functions as an event
    if (obj.isType("cmd")) {
      ArrayList<ParseData> lso = new ArrayList<>();

      for (String s : obj.getData()) {
        ParseData parseData = parseText(s, lookup, flags);
        lso.add(parseData);
      }

      Event e = new Event((Entity a, Entity b) -> {
        lookup.put("1", new ObjData("entity", a));
        lookup.put("2", new ObjData("entity", b));
        for (ParseData so : lso) {
          parseParseData(so, lookup, game, flags);
        }
      });
      e.add((Entity a) -> {
        lookup.put("1", new ObjData("entity", a));
        lookup.put("2", null);
        for (ParseData so : lso) {
          parseParseData(so, lookup, game, flags);
        }
      });
      e.add(() -> {
        lookup.put("1", null);
        lookup.put("2", null);
        for (ParseData so : lso) {
          parseParseData(so, lookup, game, flags);
        }
      });

      return e;
    }

    // Create an event object event
    if (obj.isType("event")) {
      return EventLoader.parseEvent(obj, lookup, game, flags);
    }

    // get the direction a given Player is facing
    if (obj.isType("facing")) {
      Player entity = (Player) strToObj(obj.get(0), lookup, game, flags);
      return entity.getFacing();
    }

    // create a message object
    // takes the form of a combination of a String, TextContext, Event, or {Message, Message}.
    // (String|TextContext) sets the text of the message
    // (String|TextContext)$add adds text to the message
    // Event sets the display event of the Message
    // Event$(interact|after) sets the interact event of the Message
    // A message array is interpreted as choices for the Message
    if (obj.isType("msg") || obj.isType("message")) {
      obj.setDataType("message");
      Message message = new Message();
      for (String s : obj.getData()) {
        ObjData o2 = strToObjData(s, lookup, game, flags);
        if (o2.isType("string")) {
          if (o2.hasTag("add")) {
            message.addText((String) o2.getObj());
          } else {
            message.setText((String) o2.getObj());
          }
        } else if (o2.isType("array")) {
          for (String choiceStr : o2.getData()) {
            message.addChoice((Message) strToObj(choiceStr, lookup, game, flags));
          }
        } else if (o2.isType("message")) {
          message.addChoice((Message) o2.getObj());
        } else if (o2.isType("event")) {
          if (o2.hasTag("interact") | o2.hasTag("after")) {
            message.setInteractEvent((Event) o2.getObj());
          } else {
            message.setDisplayEvent((Event) o2.getObj());
          }
        } else {
          if (o2.hasTag("add")) {
            message.addText((TextContext) o2.getObj());
          } else {
            message.setText((TextContext) o2.getObj());
          }
        }
      }
      return message;
    }

    // Create a dialogue object
    // Input is any number of (String|Message)
    if (obj.isType("dlg") || obj.isType("dialogue")) {
      obj.setDataType("dialogue");
      Dialogue dialogue = new Dialogue();
      for (String s : obj.getData()) {
        ObjData o2 = strToObjData(s, lookup, game, flags);
        if (o2.isType("string")) {
          String text = (String) strToObj(o2.get(0), lookup, game, flags);
          dialogue.add(text);
        } else {
          dialogue.add((Message) o2.getObj());
        }
      }
      return dialogue;
    }

    // Create a combination shape
    // input: {Vector, Shape}
    // the vector is the relative position to the multi-shape
    if (obj.isType("multishape")) {
      MultiShape shape = new MultiShape();
      for (String s : obj.getData()) {
        ArrayList<?> a = (ArrayList<?>) strToObj(s, lookup, game, flags);
        shape.add((Shape) a.get(1), (Vector) a.get(0));
      }
      return shape;
    }

    // Creates a layered sprite out of other sprites
    if (obj.isType("layeredSprite")) {
      LayeredSprite sprite = new LayeredSprite();
      for (String s : obj.getData()) {
        ObjData sl = strToObjData(s, lookup, game, flags);
        if (sl.isType("vector")) {
          sprite.setLocalLocation((Vector) sl.getObj());
        } else {
          sprite.addSprite((Sprite) stringRouter(game::getSprite, sl));
        }
      }
      return sprite;
    }

    // Creates an empty BedEntity and adds it to the world
    if (obj.isType("bed")) {
      BedEntity bedEntity = new BedEntity(new Entity(game));
      entityObj(bedEntity, obj, lookup, game, flags);
      return bedEntity;
    }

    if (obj.isType("varSeat")) {
      VariableSeatEntity seatEntity = new VariableSeatEntity(new Entity(game));
      entityObj(seatEntity, obj, lookup, game, flags);

      return seatEntity;
    }

    // Creates an empty SeatEntity and adds it to the world
    if (obj.isType("seat")) {
      SeatEntity seatEntity = new SeatEntity(new Entity(game));
      entityObj(seatEntity, obj, lookup, game, flags);

      return seatEntity;
    }

    if (obj.isType("yoyo")) {
      YoYoEntity seatEntity = new YoYoEntity(game);
      entityObj(seatEntity, obj, lookup, game, flags);

      return seatEntity;
    }

    // Creates an empty SeatEntity and adds it to the world
    if (obj.isType("crate")) {
      CrateEntity crate = new CrateEntity(new Entity(game));
      entityObj(crate, obj, lookup, game, flags);
      if (obj.hasTag("breakable")) {
        crate.setBreakable(true);
      }
      return crate;
    }

    // Creates an empty Door Entity
    if (obj.isType("door")) {
      Door door = new Door(new Entity(game));
      entityObj(door, obj, lookup, game, flags);
      return door;
    }

    // Creates a vector with the form (Float, Float)
    // The first Float is X the second is Y
    if (obj.isType("vector") || obj.isType("v")) {
      obj.setDataType("vector");

      Entity e1 = null, e2 = null;

      for (String s : obj.getData()) {
        ObjData o2 = strToObjData(s, lookup, game, flags);
        if (o2.isType("entity") && e1 == null) {
          e1 = (Entity) o2.getObj();
        } else if (o2.isType("entity")) {
          e2 = (Entity) o2.getObj();
        }
      }

      if (e1 != null && e2 != null) {
        return e2.getPos().subi(e1.getPos());
      }

      return new Vector(Float.parseFloat(obj.get(0)), Float.parseFloat(obj.get(1)));
    }

    // For any array the object loader parses all elements one encapsulation down
    // Then it returns the elements as a list of Objects
    if (obj.isType("array")) {
      ArrayList<Object> list = new ArrayList<>();
      for (String s : obj.getData()) {
        list.add(strToObj(s, lookup, game, flags));
      }
      return list;
    }

    // Creates a string out of the basic representation
    // Removes all escaped characters
    if (obj.isType("string")) {
      String string = obj.get(0);
      string = string.substring(1, string.length() - 1);

      Pattern strRep = Pattern.compile("[^\\\\](\\\\t)");
      Matcher matcher = strRep.matcher(string);
      string = matcher.replaceAll((MatchResult mr) -> mr.group().substring(0, 1) + "\t");

      strRep = Pattern.compile("(?:[^\\\\])(\\\\n)");

      matcher = strRep.matcher(string);
      string = matcher.replaceAll((MatchResult mr) -> mr.group().substring(0, 1) + "\n");

      strRep = Pattern.compile("(\\\\\\\\)");
      matcher = strRep.matcher(string);
      string = matcher.replaceAll("\\\\");
      return string;
    }

    // Creates a Float object using Float::parseFloat
    if (obj.isType("float")) {
      return Float.parseFloat(obj.get(0));
    }

    // Creates a Integer object using Integer::parseInt
    if (obj.isType("integer")) {
      return Integer.parseInt(obj.get(0));
    }

    // Translates the string representation of a boolean to the boolean data-type
    if (obj.isType("boolean") || obj.isType("bool")) {
      return obj.get(0).equalsIgnoreCase("true");
    }

    // Creates a new Player Entity and sets the games player as it
    if (obj.isType("player")) {
      Player player = new Player(game);
      game.setPlayer(player);
      return player;
    }

    // Creates a polygon from 3 or more vectors or from 1, 2, or 4 Float
    // See PolygonShape for more details on the 1,2,4 Float breakdown
    // The Vectors are parsed as points on the PolygonShape
    if (obj.isType("polygon")) {
      PolygonShape poly = null;
      ObjData sObj = strToObjData(obj.get(0), lookup, game, flags);
      if (sObj.isType("vector")) {
        poly = new PolygonShape();
        for (String s : obj.getData()) {
          poly.addVertex((Vector) strToObj(s, lookup, game, flags));
        }
      }

      if (obj.getSize() == 1) {
        poly = new PolygonShape(toFloat(sObj));
      } else if (obj.getSize() == 2) {
        float d2 = toFloat(strToObjData(obj.get(1), lookup, game, flags));
        poly = new PolygonShape(toFloat(sObj), d2);
      } else if (obj.getSize() == 4) {
        float d2 = toFloat(strToObjData(obj.get(1), lookup, game, flags));
        float d3 = toFloat(strToObjData(obj.get(2), lookup, game, flags));
        float d4 = toFloat(strToObjData(obj.get(3), lookup, game, flags));
        poly = new PolygonShape(toFloat(sObj), d2, d3, d4);
      }

      return poly;
    }

    // Creates a Circle shape from a Float given as a radius
    if (obj.isType("circle")) {
      return new Circle(toFloat(strToObjData(obj.get(0), lookup, game, flags)));
    }

    // If nothing is parsed then the program as encountered an error
    throw new Exception("ObjData <" + obj + "> is not a valid object");
  }

  /**
   * Parse a Float or Integer ObjData and convert it to a float.
   *
   * @param object input ObjData
   * @return float representation
   * @throws Exception if the object can not be converted to a float throw an exception
   */
  public static float toFloat(ObjData object) throws Exception {
    if (object.isType("float") || object.isType("integer")) {
      return ((Number) object.getObj()).floatValue();
    } else {
      throw new Exception("Cannot cast object: <" + object + "> to a float.");
    }
  }

  /**
   * Parse a Float or Integer ObjData and convert it to a int.
   *
   * @param object input ObjData
   * @return int representation
   * @throws Exception if the object can not be converted to a int throw an exception
   */
  public static int toInt(ObjData object) throws Exception {
    if (object.isType("float") || object.isType("integer")) {
      return ((Number) object.getObj()).intValue();
    } else {
      throw new Exception("Cannot cast object: <" + object + "> to a float.");
    }
  }

  /**
   * Parses a button from a string object
   *
   * @param type   type of button
   * @param o      string object
   * @param lookup parsing lookup table
   * @param game   game instance
   * @param flags  parsing flags
   * @return A Button object
   * @throws Exception If any parsing errors occur, this function passes through that error
   */
  public static Button parseButton(ButtonType type, ObjData o, Lookup lookup, Game game,
      PFlags flags) throws Exception {
    if (o == null) {
      throw new Exception("[ERROR] parseButton cannot be passed a null ParseData.");
    }

    Button b = null; // A button object
    if (o.isType("array")) { // if the string object data is an array
      for (String str : o.getData()) { // get each string from the array and parse it as Text
        ObjData objData = strToObjData(str, lookup, game, flags);
        if (b == null) { // create a button and add any additional buttons to it
          b = parseButton(type, objData, lookup, game, flags);
        } else {
          b.addButton(parseButton(type, objData, lookup, game, flags));
        }
      }
    } else if (o.isType("string")) { // if the ParseData is a string parse it as one
      String s = (String) parseObjData(o, lookup, game, flags);
      b = game.getConstant(s); // attempt to get the Button as a constant from the game
      if (b == null && s.length() == 1) { // If the string is one long
        // convert it to a char and set it to the button value by its ascii value
        b = new Button((s).toUpperCase().charAt(0), type);
      }
    } else {
      // otherwise try to convert it to an int, if it isn't an integer throw an error
      b = new Button(toInt(o), type);
    }
    return b;
  }

  /**
   * Parse a parseData object as an entity.
   * <p>
   * Entity objects can be any combination of Shape, Sprite, Vector, Boolean, or Float. Shapes set
   * the shape of the entity. Sprite$shape sets the shape of the entity based on the size of the
   * sprite. Sprite sets the sprite of the entity. Vector sets the position of the entity. Boolean
   * sets the physics of the entity. Float/Integer set the mass of the entity.
   *
   * @param entity Entity template, the entity to set the data on
   * @param object ParseObject data
   * @param lookup parsing lookup table
   * @param game   game instance
   * @param flags  parsing flags
   * @return a generated entity
   * @throws Exception if any parsing errors occur they are passed on to the caller
   */
  public static Entity entityObj(Entity entity, ParseData object, Lookup lookup, Game game,
      PFlags flags) throws Exception {


    for (String data : object.getData()) {
      ObjData objData = strToObjData(data, lookup, game, flags);
      if (objData.isType("entity")) {
        ((Entity) objData.getObj()).setClone(entity);
      } else if (objData.isType("shape")) {
        entity.setShape((Shape) objData.getObj());
      } else if (objData.isType("sprite")) {
        if (objData.hasTag("shape")) {
          Sprite sprite = (Sprite) objData.getObj();
          Vector vector = sprite.getSize();
          entity.setShape(new PolygonShape(vector));
        } else {
          entity.setSprite((Sprite) objData.getObj());
        }
      } else if (objData.isType("vector")) {
        entity.setPosition((Vector) objData.getObj());
      } else if (objData.isType("boolean")) {
        if (objData.hasTag("visible")) {
          entity.setVisible((Boolean) objData.getObj());
        } else {
          entity.setPhysics((Boolean) objData.getObj());
        }
      } else if (objData.isType("number")) {
        entity.setMass(toFloat(objData));
      }
    }

    if (object.hasName()) {
      entity.setName(object.getName());
    }
    return entity;
  }

  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="String Manipulation Functions">

  // Searching file patterns, uses regex to find data elements
  static private Pattern floatPattern = Pattern.compile(
      "(-?\\d+\\.\\d+)(\\$[\\dA-Za-z_\\-]+)*");
  static private Pattern integerPattern = Pattern.compile(
      "(-?\\d+)(\\$[\\dA-Za-z_\\-]+)*");
  static private Pattern booleanPattern = Pattern.compile(
      "(true|false)(\\$[\\dA-Za-z_\\-]+)*", Pattern.CASE_INSENSITIVE);

  /**
   * Converts a string to a ParseData Object and populates the data fields to be converted into a
   * java Object.
   * <p>
   * Data can have several input forms. <br> # commented lines. <br> [lookup_name] - data enclosed
   * in brackets is interpreted as a lookup, and the lookup_name is used to look up the
   * corresponding ObjData's name in the lookup table. <br> object_type:object_name(object_data)
   * ObjData take this form and set the type, name, and data of the ObjData. <br>
   * &lt;command_name&gt;(command_data) CmdData take this form and set the name and data of the
   * CmdData. <br> {}/() lone brackets are interpreted as array ObjData and are comma separated.
   * <br> 1.3 lone numbers are interpreted as either Float or Integer ObjData. <br> true/false
   * plaintext are interpreted as boolean ObjData. <br> " " strings are interpreted as anything
   * encapsulated by double quotes or that doesn't fit into any other category. <br>
   * <p>
   * Supports multi-line string loading if pertinent.
   *
   * @param s      input string
   * @param lookup object lookup table
   * @param flags  parsing flags
   * @return the ParseData representation of the string
   */
  public static ParseData parseText(String s, Lookup lookup, PFlags flags) {
    s = s.trim(); // remove excess whitespace
    EndData lastEndData = null;
    PairedData endData = flags.endData;
    ParseData sl = null; // create a null ParseData object to return at the end of the function
    Set<String> endKeys = flags.endData.getEndKeys();

    // lines beginning with # are ignored
    if (s.startsWith("#")) {
      return null;
    }

    // check if the string matches any of the raw data patterns
    Matcher floatMatcher = floatPattern.matcher(s);
    Matcher integerMatcher = integerPattern.matcher(s);
    Matcher booleanMatcher = booleanPattern.matcher(s);

    // find the first colon index not within the current strings encapsulations

    // was the last parsed line a multiline/ unpaired data structure
    boolean lastMultiline = endData.isUnpaired();
    // parse the current line for end-line data
    // the char input is parsed for and the first non-encapsulated one's index is set to firstCharIndex
    parseLineData(s, ':', endData, lastMultiline);
    // Check if the current line is unpaired
    boolean currentMultiline = endData.isUnpaired();

    // if the last line was multiline capture the parser
    if (lastMultiline) {
      // if the current line ends it
      // set the current ParseData and parse the extra data
      if (!currentMultiline) {
        sl = flags.multiLineObj;
        // add the rest of the current line to the string buffer
        flags.string += s.substring(0, endData.lastValidEnd);
        sl.addData(divideDataText(flags.string, endKeys)); // divide the string input
      } else {
        // if the line doesn't end the multiline string add to the string buffer
        flags.string += s.trim();
        return null; // end the line parsing
      }
    } else if (s.startsWith(
        "{")) { // if the string starts with "{" its an array and attempt to divide it up
      lastEndData = endData.getEndData("{}"); // set the current end data to the "{}" bracket
      sl = new ObjData("array", "", divideDataText(s, lastEndData, endKeys));
    } else if (s.startsWith(
        "(")) { // if the string starts with "(" its an array and attempt to divide it up
      lastEndData = endData.getEndData("()"); // set the current end data to the "()" bracket
      sl = new ObjData("array", "", divideDataText(s, lastEndData, endKeys));
    } else if (s.startsWith("\"")) { // if the string starts with '"' its a String
      lastEndData = endData.getEndData("\"\""); // get the PairedData of "\"\""
      sl = new ObjData("string", "", null); // create an empty String ObjData
      sl.addData(s.substring(0, lastEndData.vEnd + 1)); // set the data of the current ObjData
    } else if (s.startsWith("[")) { // if the string starts with "[" it is a lookup
      lastEndData = endData.getEndData("[]"); // set the current end data to the "[]" bracket
      sl = new ObjData("lookup", "", null);
      sl.addData(extractEndData(s, lastEndData)); // extract string inside "[]" and lookup the ObjData
      //sl.addTag("noload"); // make sure lookups don't load objects into rooms
    } else if (s.startsWith("<")) { // if the string starts with "<" its a CmdObj
      String command = extractEndData(s, endData.getEndData("<>")); // get the string between "<>"
      // because of how the parser works the "<>" command id cannot be split between lines
      lastEndData = endData.getEndData("()"); // set the current PairedData to "()"
      // divide and add data to the command
      sl = new CmdData(command, "", divideDataText(s, lastEndData, endKeys));
    } else if (endData.firstCharIndex
        > 0) { // if the firstCharIndex is set there is an un-captured ":"
      // this entails an ObjData definition
      lastEndData = endData.getEndData("()"); // set the last PairedData to "()"
      String type = s
          .substring(0, endData.firstCharIndex); // set the object type to the string before ":"
      String name = "";
      // if there is a valid string between ":" and the first "(" or the end of the current line
      // set the name to it
      if (lastEndData.fStart > endData.firstCharIndex + 1) {
        name = s.substring(endData.firstCharIndex + 1, lastEndData.fStart);
      } else if (lastEndData.fStart == -1 && endData.firstCharIndex + 1 < s.length()) {
        name = s.substring(endData.firstCharIndex + 1);
      }

      sl = new ObjData(type, name,
          divideDataText(s, lastEndData, endKeys)); // divide input data, create ObjData
    } else { // if the data doesn't start with an identifier then the string is a data literal
      if (floatMatcher.matches()) { // if the string matches the float regex
        endData.lastValidEnd = floatMatcher.end(1);
        // set the lastValidEnd to the end of the first regex, this allows tag parsing to function
        sl = new ObjData("float", "", null); // create the Float Object template
        sl.addData(floatMatcher.group(1)); // add the float data to the Object's data
      } else if (integerMatcher
          .matches()) { // if the string matches the Integer regex, identical to float parser
        endData.lastValidEnd = integerMatcher.end(1);
        sl = new ObjData("integer", "", null);
        sl.addData(integerMatcher.group(1));
      } else if (booleanMatcher.matches()) {
        // if the string matches the Boolean regex, identical to float parser
        endData.lastValidEnd = booleanMatcher.end(1);
        sl = new ObjData("boolean", "", null);
        sl.addData(booleanMatcher.group(1));
      } else if (!s.isEmpty()) { // if the string matches noting interpret it as a String literal
        sl = new ObjData("string", "", null);
        sl.addData("\"" + s + "\"");
      }
    }

    if (!lastMultiline && currentMultiline) {
      // if the line is a multiline and the last was not, start tracking the multiline data
      flags.multiLineObj = sl;
      flags.string = extractEndData(s, lastEndData);
      return null; // unnecessary but more robust
    } else { // else, the line contains the end of a ParseData
      // find the first "$" index, and if its valid add the surrounding string data as flags
      if ((endData.lastValidEnd = s.indexOf('$', endData.lastValidEnd)) >= 0) {
        sl.setTagData(s.substring(endData.lastValidEnd + 1).split("[$]+"));
      }
      flags.endData.clear(); // clear the parsing data
    }

    return sl;
  }

  /**
   * Remove "\" escape Characters from a string.
   *
   * @param s input string
   * @return the modified string
   */
  public static String removeBackslash(String s) {
    StringBuilder out = new StringBuilder();
    for (int index = 0; index < s.length(); index++) {
      char c = s.charAt(index);
      if (c == '\\') {
        index++;
        c = s.charAt(index);
      }
      out.append(c);
    }
    return out.toString();
  }

  /**
   * Using end data and an input string generate a substring.
   *
   * @param s    input string
   * @param data end data
   * @return substring
   */
  public static String extractEndData(String s, EndData data) {
    if (s == null || data == null) {
      return "";
    }

    boolean startValid = data.fStart + 1 < s.length() && data.fStart > -1;

    if (startValid && data.vEnd < s.length() && data.vEnd > -1) {
      return s.substring(data.fStart + 1, data.vEnd);
    } else if (startValid) {
      return s.substring(data.fStart + 1);
    }
    return "";
  }

  /**
   * Find the first non-encapsulated index of the Character c in the String s.
   *
   * @param s        input String
   * @param c        character to find
   * @param endPairs end pair information
   * @return the first index of the Character c, if no index is found return -1
   */
  public static int findFirstIndex(String s, char c, Set<String> endPairs) {
    return parseLineData(s, c, endPairs, null, false, true).firstCharIndex;
  }

  /**
   * Parse the current line for PairedData.
   * <p>
   * Calls parseLineData with the pairedData key set and the Character short circuit set to false.
   *
   * @param s          input String
   * @param c          Character to find
   * @param pairedData input data.
   * @param endOnFind  end the search when the String is paired up.
   * @return the modified pairedData Object
   */
  public static PairedData parseLineData(String s, Character c, PairedData pairedData,
      boolean endOnFind) {
    return parseLineData(s, c, pairedData.getEndKeys(), pairedData, endOnFind, false);
  }

  /**
   * Parse the current line for PairedData.
   * <p>
   * Takes in a string s and iterates over each Character. If the pairedData object is null then an
   * empty one is created with the endPairs Set. The function then uses the pairedData to calculate
   * the encapsulations within the string. This allows unpaired data to be skipped for Character
   * searching. If the Character input is not null it attempts to capture the first non enclosed
   * index of the Character. Also, each Character in the String s is parsed for level data using the
   * endPairs String data. If checkUnpaired is set to true, the parser ends when the pairedData
   * object is in a paired state. If checkChar is set to true, the parser ends when a valid
   * Character is equal to the input Character c. The data is stored in the pairedData object. The
   * pairedData object is also returned.
   *
   * @param s             input String
   * @param c             Character to find
   * @param endPairs      end-pair Set
   * @param pairedData    input data.
   * @param checkUnpaired end the search when the String is paired up
   * @param checkChar     end the search when the String is paired up.
   * @return the modified pairedData Object
   */
  public static PairedData parseLineData(String s, Character c, Set<String> endPairs,
      PairedData pairedData, boolean checkUnpaired, boolean checkChar) {
    pairedData = pairedData == null ? new PairedData(endPairs) : pairedData;

    for (int index = 0; index < s.length(); index++) {
      char selectedChar = s.charAt(index);

      if (selectedChar == '\\') {
        index++;
        continue;
      }

      if (c != null && c == selectedChar && !pairedData.isUnpaired()) {
        pairedData.captureChar(index);
        if (checkChar) {
          return pairedData;
        }
      }

      for (String pairString : endPairs) {
        char a = pairString.charAt(0);
        char b = pairString.charAt(1);

        if (a == b && selectedChar == a) {
          pairedData.toggleEndLevel(pairString);
          pairedData.captureStart(pairString, index);
          pairedData.captureEnd(pairString, index);
        } else if (selectedChar == a) {
          pairedData.addEndLevel(pairString, 1);
          pairedData.captureStart(pairString, index);
        } else if (selectedChar == b) {
          pairedData.addEndLevel(pairString, -1);
          pairedData.captureEnd(pairString, index);
        }

        if (checkUnpaired && !pairedData.isUnpaired()) {
          return pairedData;
        }
      }
    }

    return pairedData;
  }

  /**
   * Divide a data string separated by ',' into a list of data Strings.
   * <p>
   * Extracts the substring data from a valid EndData eData object.
   *
   * @param str   input data string
   * @param eData the data ends
   * @param ends  encapsulating string data
   * @return the list of Strings, returns an empty list if the eData, or str is not correctly
   * formatted
   */
  public static ArrayList<String> divideDataText(String str, EndData eData, Set<String> ends) {
    if (eData.vEnd > -1) {
      return divideDataText(extractEndData(str, eData), ends);
    } else {
      return new ArrayList<>();
    }
  }

  /**
   * Divide a data string separated by ',' into a list of data Strings.
   *
   * @param str  input data string
   * @param ends encapsulating string data
   * @return the list of Strings, returns an empty list if the str is not correctly formatted
   */
  public static ArrayList<String> divideDataText(String str, Set<String> ends) {
    ArrayList<String> data = new ArrayList<String>(); // create an empty list
    str = str.trim(); // trim the string to avoid adding empty String data to the list
    while (!str.isEmpty()) { // check if the str is empty
      int index = findFirstIndex(str, ',', ends); // find the first index of ","

      if (index == -1) { // if its not found add the current string to the data list
        data.add(str.trim());
        return data; // return the data because the dividing is finished
      } else {
        data.add(str.substring(0, index).trim()); // add the divided data
      }

      if (index + 1 >= str.length()) { // if the index is at the end of the string break
        break;
      }

      str = str.substring(index + 1); // cut the parsed data from the string
    }

    return data; // return the data
  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Debug Functions">

  /**
   * Log a parsing error to the terminal.
   *
   * @param id    type of parsing that failed
   * @param o     object it failed on
   * @param flags current PFlags state
   */
  public static void logParsingError(String id, Object o, PFlags flags) {
    System.err.println("An error occurred while parsing " + id + ": <" + o.toString() + ">"
        + "\nline: " + (flags.lineCount) + ", file: " + flags.fileName);
  }

  // </editor-fold>
}
