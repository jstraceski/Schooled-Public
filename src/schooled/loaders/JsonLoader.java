package schooled.loaders;

import controls.Button;
import controls.Button.ButtonType;
import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.Callable;
import java.util.function.BiConsumer;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import schooled.Game;
import schooled.audio.Sound;
import schooled.containers.Room;
import schooled.entities.BasicEntity;
import schooled.entities.BedEntity;
import schooled.entities.ContainerEntity;
import schooled.entities.CrateEntity;
import schooled.entities.Door;
import schooled.entities.EnterableEntity;
import schooled.entities.Entity;
import schooled.entities.EntityArea;
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
import schooled.menu.Dialogue;
import schooled.menu.MenuEntity;
import schooled.menu.Message;
import schooled.menu.Origin;
import schooled.menu.TextBox;
import schooled.menu.TextContext;
import schooled.physics.AnimatedShape;
import schooled.physics.BoundingBox;
import schooled.physics.Circle;
import schooled.physics.MovementType;
import schooled.physics.Mover;
import schooled.physics.MultiShape;
import schooled.physics.PolygonShape;
import schooled.physics.Shape;
import schooled.physics.StateShape;
import schooled.physics.Vector;
import schooled.visuals.filters.AlphaFilter;
import schooled.visuals.filters.GlitchFilter;
import schooled.visuals.sprite.Animation;
import schooled.visuals.sprite.LayeredSprite;
import schooled.visuals.sprite.Sprite;
import schooled.visuals.sprite.StateSprite;

public class JsonLoader {

  //<editor-fold desc="Top Level Routers">
  public static void parseJson(String path, Lookup lup, Game game, PFlags flg) {
    DStore dStore = new DStore(lup, game, flg);
    parseJson(path, dStore);
  }

  public static void parseJson(String path, DStore dStore) {
    try {
      Path nPath = Paths.get(path);
      JSONObject jsonFile = new JSONObject(new String(Files.readAllBytes(nPath)));

      if (!(jsonFile.has("type"))) {
        return;
      }

      dStore.flags.fileName = path;
      dStore.flags.lastObj = jsonFile;

      if (jsonFile.get("type").equals("save_data")) {
        parseSaveFile(jsonFile, dStore);
      } else if (jsonFile.get("type").equals("edata")) {
        parseEDataJson(jsonFile, nPath.getParent().toString(), dStore);
      } else if (jsonFile.get("type").equals("room")) {
        parseRoomJson(jsonFile, nPath.getParent().toString(), dStore);
      }
    } catch (Exception e) {
      System.err.println("Error Loading: " + dStore.flags.fileName);
      System.err.println(dStore.flags.lastObj.toString(2));
      System.err.println(dStore.lookup);
      e.printStackTrace();
    }
  }
  //</editor-fold>

  //<editor-fold desc="Entity/Room File Parsers">
  public static Entity parseEntity(String type, Game game) {
    if (type == null) {
      return new Entity(game, new Vector(0, 0), null, 0);
    }

    if (type.equals("door")) {
      return new Door(game, new Vector(0, 0), null, 0);
    }

    if (type.equals("player")) {
      return new Player(game);
    }

    if (type.equals("bed")) {
      return new BedEntity(new Entity(game));
    }

    if (type.equals("item")) {
      return new ItemEntity(new Entity(game));
    }

    if (type.equals("chair")) {
      return new SeatEntity(new Entity(game));
    }

    if (type.equals("text_box")) {
      return new TextBox(new Entity(game));
    }

    return new Entity(game, new Vector(0, 0), null, 0);
  }

  public static Entity parseEntity(JSONObject prof, Game game) {
    if (prof.has("etype")) {
      return parseEntity(prof.getString("etype"), game);
    }

    return new Entity(game, new Vector(0, 0), null, 0);
  }

  public static Sprite parseSpriteJson(JSONArray jsonArray, Sprite sheet, Vector imageSize,
      Game game, float dscale) {
    if (jsonArray.length() > 1) {
      LayeredSprite layeredSprite = new LayeredSprite();

      for (int sIdx = 0; sIdx < jsonArray.length(); sIdx++) {
        layeredSprite.addSprite(
            parseSpriteJson(jsonArray.getJSONObject(sIdx), sheet, imageSize, game, dscale));
      }

      return layeredSprite;
    } else if (jsonArray.length() == 1) {
      return parseSpriteJson(jsonArray.getJSONObject(0), sheet, imageSize, game, dscale);
    }

    return game.getImage("clear").clone();
  }

  public static Sprite loadSpriteData(JSONObject jsonObject, Sprite sprite, Vector entityLoc,
      float dScale) {

    sprite.setOrigin(Origin.TOP_LEFT);

    JSONObject boundsData;
    if ((boundsData = jsonObject.optJSONObject("layer_bounds")) != null) {
      Vector pos = parseJsonVector(boundsData);
      Vector size = parseJsonSize(boundsData);
      sprite = sprite.getSubSpr(pos.getXi(), pos.getYi(), size.getXi(), size.getYi());
    }

    float f = jsonObject.optFloat("global_layer");
    if (!Float.isNaN(f)) {
      sprite.setGlobalLayer(f);
    } else if (jsonObject.has("global_layer")) {
      sprite.setGlobalLayer(jsonObject.getInt("global_layer"));
    }

    sprite.resetBaseScale(dScale);

    f = jsonObject.optFloat("local_layer");
    if (!Float.isNaN(f)) {
      sprite.setLocalLayer(f);
    } else if (jsonObject.has("local_layer")) {
      sprite.setLocalLayer(jsonObject.getInt("local_layer"));
    }

    if (jsonObject.has("game_layer")) {
      sprite.setGameLayer(jsonObject.getInt("game_layer"));
    }

    JSONObject subData;
    if ((subData = jsonObject.optJSONObject("local_pos")) != null) {
      sprite.setLocalLocation(parseJsonVector(subData).addi(entityLoc));
    }

    if ((subData = jsonObject.optJSONObject("game_pos")) != null) {
      sprite.setGameLocation(parseJsonVector(subData).addi(entityLoc));
    }

    if ((subData = jsonObject.optJSONObject("child_pos")) != null) {
      sprite.setChildPosition(parseJsonVector(subData).addi(entityLoc));
    }

    if (jsonObject.has("child_layer")) {
      sprite.setChildLayer(jsonObject.getFloat("child_layer"));
    }

    String name = jsonObject.optString("layer_name");
    if (name != null) {
      sprite.setName(name);
    }

    return sprite;
  }

  public static Sprite parseSpriteJson(JSONObject jsonObject, Sprite sheet, Vector entityLoc,
      Game game, float dscale) throws JSONException {
    if (jsonObject.has("states")) {
      JSONObject stateObj = jsonObject.getJSONObject("states");

      StateSprite stateSprite = new StateSprite();

      for (Iterator<String> it = stateObj.keys(); it.hasNext(); ) {
        String s = it.next();
        State state = State.get(s);
        Sprite sprite = parseSpriteJson(stateObj.getJSONArray(s), sheet, entityLoc, game, dscale);
        if (sprite.getName().isEmpty()) {
          sprite.setName(s);
        }
        stateSprite.addFrame(sprite, state);
      }

      return stateSprite;
    } else {
      Sprite sprite;

      if (jsonObject.has("delays")) {
        Animation animation = new Animation();

        JSONArray delays = jsonObject.getJSONArray("delays");
        JSONArray images = jsonObject.getJSONArray("image");

        JSONArray game_pos = jsonObject.optJSONArray("game_pos");
        JSONArray local_pos = jsonObject.optJSONArray("local_pos");

        for (int iIdx = 0; iIdx < images.length(); iIdx++) {
          JSONObject iData = images.getJSONObject(iIdx);

          int x = iData.getInt("x");
          int y = iData.getInt("y");
          int w = iData.getInt("w");
          int h = iData.getInt("h");

          Sprite frameSprite = sheet.getSubSpr(x, y, w, h);

          if (game_pos != null) {
            frameSprite.setGameLocation(parseJsonVector(game_pos.getJSONObject(iIdx)));
          }

          if (local_pos != null) {
            frameSprite.setLocalLocation(parseJsonVector(local_pos.getJSONObject(iIdx)));
          }

          animation.addFrame(frameSprite, delays.getFloat(iIdx));
        }

        sprite = animation;
      } else {
        JSONObject iData = jsonObject.getJSONObject("image");

        int x = iData.getInt("x");
        int y = iData.getInt("y");
        int w = iData.getInt("w");
        int h = iData.getInt("h");

        sprite = sheet.getSubSpr(x, y, w, h);
      }

      return loadSpriteData(jsonObject, sprite, entityLoc, dscale);
    }
  }

  public static Shape parseShapeJson(JSONArray jsonArray, Vector shift, float dscale) {
    MultiShape multiShape = new MultiShape();
    for (int sIdx = 0; sIdx < jsonArray.length(); sIdx++) {
      JSONObject shapeData = jsonArray.optJSONObject(sIdx);

      float cx = shapeData.getFloat("cx") * dscale;
      float cy = shapeData.getFloat("cy") * dscale;
      int w = (int) (shapeData.getInt("w") * dscale);
      int h = (int) (shapeData.getInt("h") * dscale);

      multiShape.add(new PolygonShape(w, h), new Vector(cx, cy).addi(shift));
    }

    return multiShape;
  }

  public static Shape parseShapeJson(JSONObject jsonObject, Vector shift, float dscale) {

    MultiShape multiShape = new MultiShape();

    int cx = jsonObject.getInt("cx");
    int cy = jsonObject.getInt("cy");
    int w = jsonObject.getInt("w");
    int h = jsonObject.getInt("h");

    multiShape.add(new PolygonShape(w, h), new Vector(cx, cy).addi(shift));

    return multiShape;
  }

  public static Shape parseShapeJson(JSONObject jsonObject, ArrayList<Shape> shapeList) {
    if (jsonObject.has("states")) {
      JSONObject object = jsonObject.getJSONObject("states");

      StateShape stateSprite = new StateShape();

      for (String s : object.keySet()) {
        State state = State.get(s);
        stateSprite.add(parseShapeJson(object.getJSONObject(s), shapeList), state);
      }

      return stateSprite;
    } else {
      if (jsonObject.has("delays")) {
        AnimatedShape animatedShape = new AnimatedShape();

        JSONArray delays = jsonObject.getJSONArray("delays");
        JSONArray sIds = jsonObject.getJSONArray("ids");

        for (int iIdx = 0; iIdx < sIds.length(); iIdx++) {
          animatedShape
              .add(shapeList.get(sIds.getInt(iIdx) - 1), new Vector(0, 0), delays.getFloat(iIdx));
        }

        return animatedShape;
      } else {
        JSONArray sIds = jsonObject.getJSONArray("ids");
        if (sIds.length() < 1) {
          return null;
        }
        return shapeList.get(sIds.getInt(0) - 1);
      }
    }
  }

  public static void parseEDataJson(JSONObject jsonFile, String path, DStore dStore)
      throws IOException {

    Entity entity;

    Sprite sheet = SpriteLoader.parseGLImage(path + "/" + jsonFile.getString("sheet"));
    String lStr = jsonFile.optString("lookup");

    if (lStr != null) {
      lStr = lStr.toLowerCase();
    }

    ArrayList<Shape> shapeList = new ArrayList<>();

    HashMap<String, Sprite> spriteLookup = new HashMap<>();
    HashMap<String, Shape> shapeLookup = new HashMap<>();

    if (dStore.lookup.containsKey(lStr)) {
      entity = (Entity) dStore.lookup.get(lStr);
    } else {
      entity = parseEntity(jsonFile, dStore.g);

      if (lStr == null) {
        lStr = "json_entity";
      }

      dStore.lookup.put(lStr, entity);
    }

    if (lStr != null) {
      entity.setName(lStr);
    }

    float dscale = jsonFile.optFloat("default_scale");
    if (Float.isNaN(dscale)) {
      dscale = 1.0f;
    }

    JSONObject posObj = jsonFile.getJSONObject("pos");
    float x = posObj.getFloat("x");
    float y = posObj.getFloat("y");
    Vector sVector = new Vector(x, y).scalei(-1);

    entity.setSpriteShift(sVector);

    if (jsonFile.has("shape_lookup")) {
      JSONArray jsonArray = jsonFile.getJSONArray("shape_lookup");

      for (int sIdx = 0; sIdx < jsonArray.length(); sIdx++) {
        shapeList.add(parseShapeJson(jsonArray.optJSONArray(sIdx), sVector, dscale));
      }
    }

    if (jsonFile.has("mass")) {
      float f = jsonFile.optFloat("mass");
      if (Float.isNaN(f)) {
        f = jsonFile.optInt("mass");
      }
      entity.setMass(f);
    }

    JSONObject imgObj = jsonFile.getJSONObject("images");

    for (String key : imgObj.keySet()) {
      Object imObj = imgObj.get(key);

      if (imObj instanceof JSONObject) {
        spriteLookup
            .put(key, parseSpriteJson((JSONObject) imObj, sheet, sVector, dStore.g, dscale));
      } else {
        Sprite sprite = parseSpriteJson((JSONArray) imObj, sheet, sVector, dStore.g, dscale);
        if (sprite.getName().isEmpty()) {
          sprite.setName(key);
        }
        spriteLookup.put(key, sprite);
      }
    }

    if (jsonFile.has("areas")) {
      JSONObject areaObj = jsonFile.getJSONObject("areas");

      for (String key : areaObj.keySet()) {
        shapeLookup.put(key, parseShapeJson((JSONObject) areaObj.get(key), shapeList));
      }

      if (shapeLookup.containsKey("#base")) {
        entity.setShape(shapeLookup.get("#base"));
      }
    }

    if (spriteLookup.containsKey("#base")) {
      entity.setSprite(spriteLookup.get("#base"));
    }

    entity.addShape(shapeLookup);
    entity.addLSprite(spriteLookup);

    entity.updateLShapes();
    entity.updateLSprites();
  }

  public static void parseRoomJson(JSONObject jsonFile, String path, DStore dStore)
      throws IOException {

    Room room = new Room(dStore.g);

    Sprite sheet = SpriteLoader.parseGLImage(path + "/" + jsonFile.getString("sheet"));
    String lStr = jsonFile.optString("lookup");

    if (lStr == null) {
      lStr = "json_room";
    }

    lStr = lStr.toLowerCase();

    ArrayList<Shape> shapeList = new ArrayList<>();

    HashMap<String, Sprite> spriteLookup = new HashMap<>();
    HashMap<String, Shape> shapeLookup = new HashMap<>();

    if (jsonFile.has("shape_lookup")) {
      JSONArray jsonArray = jsonFile.getJSONArray("shape_lookup");

      for (int sIdx = 0; sIdx < jsonArray.length(); sIdx++) {
        shapeList.add(parseShapeJson(jsonArray.optJSONArray(sIdx), new Vector(0, 0), 1.0f));
      }
    }

    JSONObject imgObj = jsonFile.getJSONObject("images");

    for (String key : imgObj.keySet()) {
      Object imObj = imgObj.get(key);

      if (imObj instanceof JSONObject) {
        spriteLookup
            .put(key, parseSpriteJson((JSONObject) imObj, sheet, Vector.zero, dStore.g, 1.0f));
      } else {
        Sprite sprite = parseSpriteJson((JSONArray) imObj, sheet, Vector.zero, dStore.g, 1.0f);
        if (sprite.getName().isEmpty()) {
          sprite.setName(key);
        }
        spriteLookup.put(key, sprite);
      }
    }

    if (jsonFile.has("areas")) {
      JSONObject areaObj = jsonFile.getJSONObject("areas");
      for (String key : areaObj.keySet()) {

        Shape shape = parseShapeJson((JSONObject) areaObj.get(key), shapeList);
        assert shape != null;
        BoundingBox bb = shape.getBoundingBox();
        Vector center = bb.getMax().addi(bb.getMin()).scalei(-0.5f);

        MultiShape multiShape = new MultiShape();
        multiShape.add(shape, center);

        EntityArea area = new EntityArea(dStore.g, multiShape);
        area.addPos(center.scalei(-1f));
        area.setPlayerOnly(true);
        room.addEntity(area);
        area.setName(key);
        dStore.lookup.put(key, area);
      }
    }

    if (jsonFile.has("walls")) {
      JSONObject areaObj = jsonFile.getJSONObject("walls");

      for (String key : areaObj.keySet()) {
        shapeLookup.put(key, parseShapeJson((JSONObject) areaObj.get(key), shapeList));
      }

      if (shapeLookup.containsKey("#base")) {
        room.addWall(shapeLookup.get("#base"));
      }
    }

    if (spriteLookup.containsKey("#base")) {
      room.setSprite(spriteLookup.get("#base"));
    }

//    if (jsonFile.has("scale")) {
//      float jsonArray = jsonFile.getFloat("scale");
//    }

    if (jsonFile.has("entities")) {
      JSONArray entities = (JSONArray) jsonFile.get("entities");

      for (int idx = 0; idx < entities.length(); idx++) {
        JSONObject obj = (JSONObject) entities.get(idx);
        float x = obj.getFloat("cx");
        float y = obj.getFloat("cy");
        String id = (String) obj.get("entity");

        if (obj.has("local") && obj.getBoolean("local")) {
          Vector size = parseJsonSize(obj);
          Entity entity = parseEntity(obj.optString("type"), dStore.g);
          entity.setPos(new Vector(x, y));
          entity.setShape(new PolygonShape(size));
          entity.setVisible(false);
          entity.setPhysics(false);
          entity.setName(id);
          room.addEntity(entity);
          dStore.lookup.put(id, entity);
        } else {
          Object seedObjData = dStore.lookup.get(id);
          Entity entity;
          if (obj.has("master")) {
            entity = (Entity) (seedObjData);
          } else {
            entity = ((Entity) (seedObjData)).clone();
          }
          entity.setPosition(new Vector(x, y));
          room.addEntity(entity);

          if (obj.has("lookup")) {
            entity.setName(obj.getString("lookup"));
            dStore.lookup.put(obj.getString("lookup"), entity);
          }
        }
      }
    }

    if (dStore.g != null) {
      dStore.g.getWorld().addRoom(room);
    }

    room.updateBackgroundData();
    room.setName(lStr);

    dStore.lookup.put(lStr, room);
  }

  public static Vector parseJsonSize(JSONObject jsonObject) {
    float a = 0;
    float b = 0;

    if (jsonObject.has("w")) {
      a = jsonObject.getFloat("w");
    }

    if (jsonObject.has("h")) {
      b = jsonObject.getFloat("h");
    }

    return new Vector(a, b);
  }

  public static Vector parseJsonVector(JSONObject jsonObject) {
    float a = 0;
    float b = 0;

    if (jsonObject.has("x")) {
      a = jsonObject.getFloat("x");
    } else if (jsonObject.has("w")) {
      a = jsonObject.getFloat("w");
    }

    if (jsonObject.has("y")) {
      b = jsonObject.getFloat("y");
    } else if (jsonObject.has("h")) {
      b = jsonObject.getFloat("h");
    }

    return new Vector(a, b);
  }
  //</editor-fold>

  //<editor-fold desc="Save Data Parsers">

  /**
   * Container for parsing data.
   */
  static class DStore {

    Lookup lookup;
    Game g;
    PFlags flags;

    public DStore(Lookup lookup, Game game, PFlags flags) {
      this.lookup = lookup;
      this.g = game;
      this.flags = flags;
    }
  }

  //<editor-fold desc="General Parsing and File Hooks">

  /**
   * Parse and execute a JSONObject as a save file.
   *
   * @param obj    JSONObject
   * @param dStore parsing data storage
   * @throws Exception throws processing errors, line object info is updated in dStore
   */
  public static void parseSaveFile(JSONObject obj, DStore dStore) throws Exception {
    for (Object line : obj.getJSONArray("cmds")) {
      if (line instanceof JSONObject) {
        dStore.flags.lastObj = (JSONObject) line;
        parseSaveData((JSONObject) line, dStore);
      }
    }
  }

  /**
   * Route JSONObject data as top level commands.
   * <p>
   * obj are interpreted as an object creation command. obj return a Java Object representation of
   * the described object. func are parsed as function data, func are run and executed when loaded.
   * func do no return Object data, they return null; lookup objects are read as a lookup request,
   * the lookup table is polled and returned. lookup objects return the loaded object referenced by
   * lookup tags.
   *
   * @param obj    JSONObject command
   * @param dStore parsing data storage
   * @return object data or null
   * @throws Exception throws processing errors
   */
  public static Object parseSaveData(JSONObject obj, DStore dStore) throws Exception {
    if (obj == null) {
      return null;
    }

    if (obj.has("obj")) {
      return parseObjData(obj, dStore);
    } else if (obj.has("func")) {
      parseCmdData(obj, dStore);
    } else if (obj.has("lookup")) {
      return dStore.lookup.get(obj.getString("lookup"));
    }

    return null;
  }

  /**
   * Convert JSONObject data into Object representations.
   * <p>
   * Parses JSONArray objects with two numerical values values (i.e. [1.0, 2]) as a Vector Parses
   * Number objects Float/Integer as floats.
   * <p>
   * If the input object is not of any of the aforementioned types, it is simply returned
   * unchanged.
   *
   * @param input  object to convert
   * @param dStore parsing data storage
   * @return converted object
   * @throws Exception throws processing errors
   */
  public static Object exData(Object input, DStore dStore) throws Exception {
    if (input instanceof HashMap) {
      HashMap<String, String> hMap = (HashMap<String, String>) input;
      JSONObject jsonObject = new JSONObject();
      for (String str : hMap.keySet()) {
        jsonObject.put(str, hMap.get(str));
      }
      return parseSaveData(jsonObject, dStore);
    }

    if (input instanceof JSONObject) {
      return parseSaveData((JSONObject) input, dStore);
    }

    if (input instanceof JSONArray) {
      JSONArray array = (JSONArray) input;
      if (array.length() == 2) {
        float x = array.optFloat(0);
        float y = array.optFloat(1);
        if (!Float.isNaN(x) && !Float.isNaN(y)) {
          return new Vector(array.getFloat(0), array.getFloat(1));
        }
      }
    }

    if (input instanceof Number) {
      return ((Number) input).floatValue();
    }

    return input;
  }

  /**
   * Convert the nth data element of a JSONObject into an Object representations.
   * <p>
   * Parses JSONArray objects with two numerical values values (i.e. [1.0, 2]) as a Vector Parses
   * Number objects Float/Integer as floats.
   * <p>
   * If the input object is not of any of the aforementioned types, it is simply returned
   * unchanged.
   *
   * @param input  JSONObject container
   * @param i      nth index
   * @param dStore parsing data storage
   * @return converted object
   * @throws Exception throws processing errors
   */
  public static Object exData(JSONObject input, int i, DStore dStore) throws Exception {
    JSONArray dataList;

    if (input.get("data") instanceof ArrayList) {
      if (i < ((ArrayList) input.get("data")).size()) {
        return exData(((ArrayList) input.get("data")).get(i), dStore);
      }
    }

    if ((dataList = input.getJSONArray("data")) != null) {
      if (i < dataList.length()) {
        return exData(dataList.get(i), dStore);
      }
    }

    return null;
  }
  //</editor-fold>

  //<editor-fold desc="Entity Parsing">
  public static Entity parseEntityData(Entity entity, JSONObject obj, DStore dStore)
      throws Exception {
    for (String key : obj.keySet()) {
      Object input = exData(obj.get(key), dStore);

      if (isKey(key, "visible")) {
        entity.setVisible((Boolean) input);
      }
    }

    return (Entity) parseEntityData((BasicEntity) entity, obj, dStore);
  }

  public static Entity parseEntityData(MenuEntity menuEntity, JSONObject obj, DStore dStore)
      throws Exception {
    for (Object input : obj.getJSONArray("data")) {
      input = exData(input, dStore);

      if (input instanceof String) {
        menuEntity.setText((String) input);
      }
    }

    for (String key : obj.keySet()) {
      Object input = exData(obj.get(key), dStore);

      if (isKey(key, "hCenter")) {
        menuEntity.setHorizontalCenter((Boolean) input);
      } else if (isKey(key, "vCenter")) {
        menuEntity.setVerticalCenter((Boolean) input);
      } else if (isKey(key, "color")) {
        menuEntity.setTextColor(new Color(((Float) input).intValue()));
      }
    }

    return parseEntityData((Entity) menuEntity, obj, dStore);
  }

  public static BasicEntity parseEntityData(BasicEntity entity, JSONObject obj, DStore dStore)
      throws Exception {
    for (Object input : obj.getJSONArray("data")) {
      input = exData(input, dStore);



      if (input instanceof Vector) {
        entity.setPos((Vector) input);
      } else if (input instanceof BasicEntity) {
        ((BasicEntity) input).setClone(entity);
      } else if (input instanceof Shape) {
        entity.setShape((Shape) input);
      } else if (input instanceof Boolean) {
        entity.setPhysics((Boolean) input);
      } else if (input instanceof Float) {
        entity.setMass((Float) input);
      } else if (input instanceof String) {
        entity.setName((String) input);
      }
    }

    for (String key : obj.keySet()) {
      Object input = exData(obj.get(key), dStore);

      if (isKey(key, "physics")) {
        entity.setPhysics((Boolean) input);
      } else if (isKey(key, "shape")) {
        if (input instanceof Sprite) {
          entity.setShape(new PolygonShape(((Sprite) input).getSize()));
        } else if (input instanceof Shape) {
          entity.setShape((Shape) input);
        }
      } else if (isKey(key, "name")) {
        entity.setName((String) input);
      }
    }

    return entity;
  }
  //</editor-fold>

  //<editor-fold desc="Event Parsing">
  interface Func {

    void run() throws Exception;
  }

  public static Event eventFromData(Func runnable, DStore dStore) {
    Event e = new Event((Entity a, Entity b) -> {
      dStore.lookup.put("1", a);
      dStore.lookup.put("2", b);
      runnable.run();
    });
    e.add((Entity a) -> {
      dStore.lookup.put("1", a);
      dStore.lookup.put("2", null);
      runnable.run();
    });
    e.add(() -> {
      dStore.lookup.put("1", null);
      dStore.lookup.put("2", null);
      runnable.run();
    });
    return e;
  }

  public static Event eventFromData(JSONObject obj, DStore dStore) {
    return eventFromData(() -> {
      for (Object so : obj.getJSONArray("data")) {
        exData(so, dStore);
      }
    }, dStore);
  }

  public static Event parseEventData(JSONObject obj, DStore dStore) throws Exception {
    final Game game = dStore.g;
    float delay = -1;
    ArrayList<Event> actions = new ArrayList<>();

    for (Object input : obj.getJSONArray("data")) {
      if (input instanceof JSONObject && ((JSONObject) input).has("func")) {
        final JSONObject fInput = (JSONObject) input;
        if (fInput.has("time")) {
          final float fTime = fInput.getFloat("time");
          actions.add(eventFromData(() -> game.addTimedEvent(fTime, new Event(() -> exData(fInput, dStore))), dStore));
        } else {
          actions.add(eventFromData(() -> exData(fInput, dStore), dStore));
        }
      } else {
        input = exData(input, dStore);
        if (input instanceof Float) {
          delay = (float) input;
        } else if (input instanceof Event) {
          actions.add((Event) input);
        }
      }
    }

    for (String key : obj.keySet()) {
      Object input = exData(obj.get(key), dStore);

      if (isKey(key, "time")) {
        delay = (Float) input;
      }
    }

    if (delay > 0) {
      final float fTime = delay;

      Event e2 = new Event(
          () -> game.addTimedEvent(fTime, new Event(() -> actions.forEach((Event::act)))));
      e2.add((Entity a) -> game
          .addTimedEvent(fTime, new Event(() -> actions.forEach((event -> event.act(a))))));
      e2.add((Entity a, Entity b) -> game
          .addTimedEvent(fTime, new Event(() -> actions.forEach((event -> event.act(a, b))))));

      return e2;
    }

    Event e = new Event((Entity a, Entity b) -> actions.forEach((event -> event.act(a, b))));
    e.add((Entity a) -> actions.forEach((event -> event.act(a))));
    e.add(() -> actions.forEach((Event::act)));

    return e;
  }
  //</editor-fold>

  //<editor-fold desc="Sprite/Button Parsing">
  public static Sprite parseSpriteData(JSONObject obj, DStore dStore) throws Exception {
    return parseSpriteData(obj, 0, null, dStore);
  }

  public static Sprite parseSpriteData(JSONObject obj, int index, Object consumer, DStore dStore) throws Exception {
    Sprite sprite = null;

    JSONArray jsonArray = obj.getJSONArray("data");
    for (int i = index; i < jsonArray.length(); i++) {
      Object input = exData(jsonArray.get(i), dStore);

      if (input instanceof Sprite) {
        sprite = ((Sprite) input).clone();
      } else if (input instanceof Entity) {
        if (consumer instanceof Entity) {
          ((Entity) consumer).addLSprite(((Entity) input).getLSpriteMap());
          ((Entity) consumer).updateLSprites();
        }
        sprite = ((Entity) input).getSprite();
      } else if (input instanceof String) {
        sprite = dStore.g.getSprite((String) input);
      }
    }

    if (sprite == null) {
      throw new Exception("Sprite Not Found.");
    }

    for (Object input : obj.getJSONArray("data")) {
      input = exData(input, dStore);
      if (input instanceof Float) {
        sprite.setGlobalLayer((int) (float) input);
      } else if (input instanceof Vector) {
        if (consumer instanceof Entity) {
          ((Entity) consumer).setSpriteShift((Vector) input);
        } else {
          sprite.setLocalLocation((Vector) input);
        }
      } else if (input instanceof State) {
        sprite.setState((State) input);
      }
    }

    for (String key : obj.keySet()) {
      Object input = exData(obj.get(key), dStore);

      if (isKey(key, "local")) {
        sprite.setLocalLayer((int) input);
      } else if (isKey(key, "game")) {
        sprite.setGameLayer((int) input);
      } else if (isKey(key, "global")) {
        sprite.setGlobalLayer((int) input);
      } else if (isKey(key, "lookup")) {
        sprite.setName((String) input);
      } else if (isKey(key, "position")) {
        sprite.setGameLocation((Vector) input);
      }
    }

    // if the sprite has the clone tag clone the sprite from the table
    return obj.optBoolean("clone", false) ? sprite.clone() : sprite;
  }

  public static Button parseButton(ButtonType type, Object o, DStore dStore) throws Exception {
    if (o == null) {
      throw new Exception("[ERROR] parseButton cannot be passed a null ParseData.");
    }

    Button b = null; // A button object

    if (o instanceof JSONArray) { // if the string object data is an array
      for (Object sub_obj : (JSONArray) o) { // get each string from the array and parse it as Text
        sub_obj = exData(sub_obj, dStore);

        if (b == null) { // create a button and add any additional buttons to it
          b = parseButton(type, sub_obj, dStore);
        } else {
          b.addButton(parseButton(type, sub_obj, dStore));
        }
      }
    } else if (o instanceof String) { // if the ParseData is a string parse it as one
      String s = (String) o;
      b = dStore.g.getConstant(s); // attempt to get the Button as a constant from the game
      if (b == null && s.length() == 1) { // If the string is one long
        // convert it to a char and set it to the button value by its ascii value
        b = new Button((s).toUpperCase().charAt(0), type);
      }
    } else if (o instanceof Number) {
      // otherwise try to convert it to an int, if it isn't an integer throw an error
      b = new Button(((Number) o).intValue(), type);
    }
    return b;
  }

  public static void parseButtonCmd(JSONObject cmd, ButtonType type, BiConsumer<String, Button> f,
      DStore dStore) throws Exception {
    String label = null;

    for (Object o : cmd.getJSONArray("data")) {
      Object input = exData(o, dStore);

      if (label != null) {
        f.accept(label, parseButton(type, input, dStore));
        label = null;
      }
      if (input instanceof JSONArray) {
        JSONArray arr = (JSONArray) input;
        String string = (String) exData(arr.get(0), dStore);
        f.accept(string, parseButton(type, exData(arr.get(1), dStore), dStore));
      } else if (input instanceof String) {
        label = (String) input;
      }
    }
  }
  //</editor-fold>

  //<editor-fold desc="Key Routing">

  /**
   * Compares key values.
   *
   * @param a key 1
   * @param b key 2
   * @return if key 1 equals key 2 return true, otherwise false
   */
  public static boolean isKey(Object a, Object b) {
    if (a instanceof String) {
      a = ((String) a).toLowerCase();
    }

    if (b instanceof String) {
      b = ((String) b).toLowerCase();
    }

    return a.equals(b);
  }

  /**
   * Parse a JSONObject as command data.
   *
   * @param cmd    JSONObject command
   * @param dStore parsing data storage
   * @throws Exception throws processing errors
   */
  public static void parseCmdData(JSONObject cmd, DStore dStore) throws Exception {
    parseCmdData(cmd.getString("func"), cmd, dStore);
  }

  /**
   * Parse a JSONObject as command data.
   *
   * @param key    lookup key of the command
   * @param cmd    JSONObject command
   * @param dStore parsing data storage
   * @throws Exception throws processing errors
   */
  public static Object parseCmdData(Object key, JSONObject cmd, DStore dStore) throws Exception {
    if (isKey(key, "log") || isKey(key, "print")) {
      Game.log(exData(cmd, 0, dStore));
      return null;
    }

    //<editor-fold desc="Menu Entity">
    if (isKey(key, "setTextColor")) {
      ((MenuEntity) exData(cmd, 0, dStore)).setTextColor((Color) exData(cmd, 0, dStore));
      return null;
    }

    if (isKey(key, "setText")) {
      ((MenuEntity) exData(cmd, 0, dStore)).setText((String) exData(cmd, 1, dStore));
      return null;
    }

    if (isKey(key, "setBaseColor")) {
      ((MenuEntity) exData(cmd, 0, dStore)).setBackgroundColor((Color) exData(cmd, 1, dStore));
      return null;
    }

    if (isKey(key, "setBorder")) {

      MenuEntity menuEntity = (MenuEntity) exData(cmd, 0, dStore);

      for (Object so : cmd.getJSONArray("data")) {
        so = exData(so, dStore);

        if (so instanceof Color) {
          menuEntity.setBorderColor((Color) so);
          return null;
        }

        if (so instanceof Float) {
          menuEntity.setBorderSize((int) so);
        }
      }

      return null;
    }
    //</editor-fold>

    //<editor-fold desc="Room">
    if (isKey(key, "roomWindow")) {
      // Sets the game window of the room.
      ((Room) exData(cmd, 0, dStore)).setWindowSize((Vector) exData(cmd, 1, dStore));
      return null;
    }

    if (isKey(key, "genWallsFromSprite")) {
      // <>(Room room)
      // <>(Room room, float width, float offset)
      // generate wall shapes for the room with a given offset and horizon offset.
      // defaults to 5, 100.

      Room room = (Room) exData(cmd, 0, dStore);
      Vector size = (Vector) exData(cmd, 1, dStore);

      if (size != null) {
        room.generateRectangleWalls(size.getX(), size.getY());
      } else {
        room.generateRectangleWalls(5, 100f);
      }

      return null;
    }

    if (isKey(key, "addRoom")) {
      Room loadedRoom = dStore.g.getWorld().getLoadedRoom();
      Room room = (Room) exData(cmd, 0, dStore);
      Vector pos = (Vector) exData(cmd, 1, dStore);
      Vector offset = null;

      boolean absolute = false, cloneEntities = true;

      JSONArray arr = cmd.getJSONArray("data");
      for (int index = 2; index < arr.length(); index++) {
        Object input = exData(arr.get(index), dStore);
        if (input instanceof Boolean) {
          cloneEntities = (Boolean) input;
        } else if (input instanceof String) {
          absolute = input.equals("absolute");
        } else if (input instanceof Vector) {
          offset = (Vector) input;
        }
      }

      for (String sub_key : cmd.keySet()) {
        Object input = exData(cmd.get(sub_key), dStore);
        if (isKey(sub_key, "absolute")) {
          absolute = (Boolean) input;
        } else if (isKey(sub_key, "noclone")) {
          cloneEntities = !((Boolean) input);
        }
      }

      if (offset != null) {
        loadedRoom.addRoom(room, pos, offset, cloneEntities);
      } else if (absolute) {
        loadedRoom.addRoomPosition(room, pos, cloneEntities);
      } else {
        loadedRoom.addRoom(room, pos, cloneEntities);
      }
      return null;
    }
    //</editor-fold>

    //<editor-fold desc="Dialogue">
    if (isKey(key, "sendDialogue") || isKey(key, "sendDlg")) {
      // <>(Dialogue/String)
      // <>(Dialogue/String, Entity)
      // Send a message from the game world, or from a given sender

      Object messageObject = exData(cmd, 0, dStore);
      Dialogue dialogue = null;

      if (messageObject instanceof Dialogue) {
        dialogue = (Dialogue) messageObject;
      } else if (messageObject instanceof String) {
        dialogue = new Dialogue((String) messageObject);
      }

      Object entity = exData(cmd, 1, dStore);

      if (entity instanceof Entity) {
        dialogue.setAllSpeakers((Entity) entity);
      }

      dialogue.resetIndex();
      dStore.g.getWorld().setDialogue(dialogue);
      return null;
    }

    if (isKey(key, "setBlocking")) {
      // Set a dialogue's blocking state
      ((Dialogue) exData(cmd, 0, dStore)).setBlocking((boolean) exData(cmd, 1, dStore));
      return null;
    }
    //</editor-fold>

    //<editor-fold desc="Sprite">
    if (isKey(key, "setMasterBaseScale")) {
      // set the masterBaseScale of the given sprite
      dStore.g.setMasterBaseScale((String) exData(cmd, 0, dStore), (float) exData(cmd, 1, dStore));
      return null;
    }

    if (isKey(key, "resetBaseScale")) {
      // resets the baseScale of the given sprite
      dStore.g.setBaseScale((Sprite) exData(cmd, 0, dStore), (float) exData(cmd, 1, dStore));
      return null;
    }

    if (isKey(key, "setDelay")) {
      // <>(Sprite, float)
      // <>([Sprite1, Sprite2, ...], float)
      // Sets the delay of all the given sprites

      Object sprite = exData(cmd, 0, dStore);
      float delay = (float) exData(cmd, 0, dStore);

      if (sprite instanceof Sprite) {
        ((Animation) sprite).setAllDelay(delay);
      } else if (sprite instanceof JSONArray) {
        for (Object input : (JSONArray) sprite) {
          ((Animation) input).setAllDelay(delay);
        }
      }
      return null;
    }

    if (isKey(key, "setDefaultDelay")) {
      // set the default delay of the animation/sprite
      ((Animation) exData(cmd, 0, dStore)).setDefaultDelay((Float) exData(cmd, 1, dStore));
      return null;
    }
    //</editor-fold>

    //<editor-fold desc="NPC">
    if (isKey(key, "repeatLast")) {
      // set the npc's dialogue repeat status to repeat the last dialogue
      ((NPC) exData(cmd, 0, dStore)).repeatLast();
      return null;
    }

    if (isKey(key, "repeatAll")) {
      // set the npc's dialogue repeat status to repeat all the dialogue
      ((NPC) exData(cmd, 0, dStore)).repeatAll();
      return null;
    }

    if (isKey(key, "repeatNone")) {
      // set the npc's dialogue repeat status to repeat no dialogue dialogue
      ((NPC) exData(cmd, 0, dStore)).repeatNone();
      return null;
    }

    if (isKey(key, "repeatCustom")) {
      // set the npc's dialogue repeat status to repeat a custom dialogue
      // the custom dialogue is referred to by its index in the npc
      ((NPC) exData(cmd, 0, dStore)).repeatCustom((int) exData(cmd, 0, dStore));
      return null;
    }

    if (isKey(key, "addDialogue")) {
      // add dialogue to an npc or character
      NPC e = (NPC) exData(cmd, 0, dStore);
      Object input = exData(cmd, 1, dStore);
      if (input instanceof String) {
        e.addDialogue(new Dialogue((String) input, e));
      } else if (input instanceof Dialogue) {
        e.addDialogue((Dialogue) input);
      }
      return null;
    }
    //</editor-fold>

    //<editor-fold desc="Custom Entity">
    if (isKey(key, "setPickupEvent")) {
      // set the pickup event of the given entity
      ((ItemEntity) exData(cmd, 0, dStore)).setPickupEvent((Event) exData(cmd, 1, dStore));
      return null;
    }

    if (isKey(key, "doorConnect")) {
      Door a = (Door) exData(cmd, 0, dStore);
      Door b = (Door) exData(cmd, 1, dStore);

      a.setTargetDoor(b);
      b.setTargetDoor(a);
      return null;
    }

    if (isKey(key, "doorExitVector")) {
      // Set the given doors exit vector to the given vector
      ((Door) exData(cmd, 0, dStore)).setExitVector((Vector) exData(cmd, 1, dStore));
      return null;
    }

    if (isKey(key, "vehicleSpriteData")) {
      // <>(Vehicle, Sprite, Sprite)
      // Sets sprite data of a vehicle
      // first sprite represents the interior sprite the seconds represents the exterior

      Vehicle vehicle = (Vehicle) exData(cmd, 0, dStore);
      vehicle.setSpriteData((Sprite) exData(cmd, 1, dStore), (Sprite) exData(cmd, 2, dStore));
      return null;
    }

    if (isKey(key, "setVehicleShapes")) {
      // set the sate of the given entity

      Vehicle vehicle = (Vehicle) exData(cmd, 0, dStore);
      JSONArray arr = cmd.getJSONArray("data");
      Shape inside = null, transition = null, area = null;

      for (int index = 1; index < arr.length(); index++) {
        Object input = exData(arr.get(index), dStore);
        Shape shape = null;
        if (input instanceof Shape) {
          shape = (Shape) input;
        } else if (input instanceof String) {
          shape = vehicle.getShape((String) input);
        }

        if (inside == null) {
          inside = shape;
        } else if (transition == null) {
          transition = shape;
        } else if (area == null) {
          area = shape;
        }
      }

      for (String sub_key : cmd.keySet()) {
        Object input = exData(cmd.get(sub_key), dStore);
        if (isKey(sub_key, "inside") || isKey(sub_key, "i")) {
          inside = (Shape) input;
        } else if (isKey(sub_key, "transition") || isKey(sub_key, "t")) {
          transition = (Shape) input;
        } else if (isKey(sub_key, "area") || isKey(sub_key, "a")) {
          area = (Shape) input;
        }
      }

      vehicle.setInteriorShape(inside);
      vehicle.setTransitionShape(transition);
      vehicle.setInteriorArea(area);
      return null;
    }
    //</editor-fold>

    //<editor-fold desc="Visual Modifiers">
    if (isKey(key, "setGlitch")) {
      // set the pickup event of the given entity

      Entity e1 = (Entity) exData(cmd, 0, dStore);
      Float f1 = (Float) exData(cmd, 1, dStore);

      if (f1 == null) {
        e1.setFilter(new GlitchFilter());
      } else {
        e1.setFilter(new GlitchFilter(f1));
      }
      return null;
    }

    if (isKey(key, "setAlpha")) {
      // tell the given entity to transition to the next sprite
      // set the pickup event of the given entity

      Entity e1 = (Entity) exData(cmd, 0, dStore);
      Float f1 = (Float) exData(cmd, 1, dStore);
      Float f2 = (Float) exData(cmd, 2, dStore);
      Float f3 = (Float) exData(cmd, 3, dStore);

      if (f2 != null && f3 != null) {
        e1.setFilter(new AlphaFilter(f1, f2, f3));
      } else {
        if (f1 == 1.0f) {
          e1.setFilter(null);
        } else {
          e1.setFilter(new AlphaFilter(f1));
        }
      }

      return null;
    }

    if (isKey(key, "setGameAlpha")) {
      // tell the given entity to transition to the next sprite

      Float f1 = (Float) exData(cmd, 0, dStore);
      Float f2 = (Float) exData(cmd, 1, dStore);
      Float f3 = (Float) exData(cmd, 2, dStore);

      if (f2 != null && f3 != null) {
        dStore.g.setGlobalFilter(new AlphaFilter(f1, f2, f3));
      } else {
        if (f1 == 1.0f) {
          dStore.g.setGlobalFilter(null);
        } else {
          dStore.g.setGlobalFilter(new AlphaFilter(f1));
        }
      }
      return null;
    }
    //</editor-fold>

    //<editor-fold desc="Game Modifiers">
    if (isKey(key, "hold")) {
      dStore.g.setHold((boolean) exData(cmd, 0, dStore));
      return null;
    }

    if (isKey(key, "startEvent")) {
      // set the interaction event of the given entity
      // if provided set the interaction override state of the entity
      dStore.g.setStartEvent((Event) exData(cmd, 0, dStore));
      return null;
    }

    if (isKey(key, "noMenu")) {
      // tell the given entity to transition to the next sprite
      if ((boolean) exData(cmd, 0, dStore)) {
        dStore.g.startGame();
      }
      return null;
    }

    if (isKey(key, "loadRoom")) {
      // Calls setLoadedRoom with the given room
      dStore.g.getWorld().setLoadedRoom((Room) exData(cmd, 0, dStore));
      return null;
    }

    if (isKey(key, "setPlayerPos")) {
      // Set the game instance's player position with the given vector
      dStore.g.getPlayer().setPosition((Vector) exData(cmd, 0, dStore));
      return null;
    }

    if (isKey(key, "setPlayerRoom")) {
      // set the room of the given player
      dStore.g.getPlayer().setRoom((Room) exData(cmd, 0, dStore));
      return null;
    }

    if (isKey(key, "setPlayer")) {
      // Set the game's player
      dStore.g.setPlayer((Player) exData(cmd, 0, dStore));
      return null;
    }

    if (isKey(key, "addEntity")) {
      dStore.g.getWorld().getLoadedRoom().addEntity((Entity) exData(cmd, 0, dStore));
      return null;
    }
    //</editor-fold>

    //<editor-fold desc="General Entity">
    if (isKey(key, "addSprite")) {
      // add a sprite to an entity
      // uses a general cmd parser, to input the entity and Sprite
      ((Entity) exData(cmd, 0, dStore)).addSprite((Sprite) exData(cmd, 1, dStore));
      return null;
    }

    if (isKey(key, "spriteT")) {
      // transition an entity to a sprite
      ((Entity) exData(cmd, 0, dStore)).transitionTo((Sprite) exData(cmd, 1, dStore));
      return null;
    }

    if (isKey(key, "spriteShift")) {
      // Set the local shift of the given sprite
      ((Entity) exData(cmd, 0, dStore)).setSpriteShift((Vector) exData(cmd, 1, dStore));
      return null;
    }
//
//    if  (isKey(key, "setChild")) {
//      // set the second entity given as the child of the first
//      ((Entity) exData(cmd, 0, dStore)).removeChildren();
//      ((Entity) exData(cmd, 0, dStore)).addChild((Entity) exData(cmd, 1, dStore));
//      return null;
//    }

    if (isKey(key, "addChild")) {
      // set the second entity given as the child of the first
      ((Entity) exData(cmd, 0, dStore)).addChild((Entity) exData(cmd, 1, dStore));
      return null;
    }

    if (isKey(key, "removeChild")) {
      // make the second entity a child of the first
      Entity e2 = (Entity) exData(cmd, 1, dStore);
      ((Entity) exData(cmd, 0, dStore)).removeChild(e2);
      if (cmd.optBoolean("room", false)) {
        e2.removeFromContainer();
      }
      return null;
    }

    if (isKey(key, "setInteraction") || isKey(key, "setEvent")) {
      // set the interaction event of the given entity
      // if provided set the interaction override state of the entity

      Entity e = (Entity) exData(cmd, 0, dStore);
      e.setInteractEvent((Event) exData(cmd, 1, dStore));

      Boolean bool = (Boolean) exData(cmd, 2, dStore);
      if (bool != null) {
        e.setInteractOverride(bool);
      }
      return null;
    }

    if (isKey(key, "nextImage")) {
      // tell the given entity to transition to the next sprite
      ((Entity) exData(cmd, 0, dStore)).setTransitionSprite(true);
      return null;
    }

    if (isKey(key, "setPhysics")) {
      // set a given entities name
      ((Entity) exData(cmd, 0, dStore)).setPhysics((Boolean) exData(cmd, 1, dStore));
      return null;
    }

    if (isKey(key, "setCollision")) {
      ((Entity) exData(cmd, 0, dStore)).setCollides((Boolean) exData(cmd, 1, dStore));
      return null;
    }

    if (isKey(key, "lsprite")) {
      // set the current sprite

      Entity entity = (Entity) exData(cmd, 0, dStore);
      Object arr = exData(cmd, 1, dStore);

      if (arr instanceof JSONArray) {
        for (Object input : (JSONArray) arr) {
          String lookup = null;
          Sprite sprite = null;

          for (Object sub_input : ((ArrayList<?>) input)) {
            if (sub_input instanceof String) {
              lookup = (String) sub_input;
            } else if (sub_input instanceof Sprite) {
              sprite = (Sprite) sub_input;
            }
          }

          entity.addLSprite(lookup, sprite);
        }
      } else if (arr instanceof String) {
        entity.addLSprite((String) arr, (Sprite) exData(cmd, 2, dStore));
      }

      entity.updateLSprites();
      return null;
    }

    if (isKey(key, "ulsprite")) {
      // set the current sprite
      ((Entity) exData(cmd, 0, dStore)).updateLSprites();
      return null;
    }

    if (isKey(key, "setName")) {
      // set a given entities name
      ((Entity) exData(cmd, 0, dStore)).setName((String) exData(cmd, 1, dStore));
      return null;
    }

    if (isKey(key, "setState")) {
      // set the sate of the given entity
      Entity e = (Entity) exData(cmd, 0, dStore);
      Object input = exData(cmd, 1, dStore);
      Object input2 = exData(cmd, 2, dStore);

      if (input instanceof String) {
        e.setState(State.valueOf(((String) input).toUpperCase()));
      } else if (input instanceof State) {
        e.setState((State) input);
      }

      if (input2 != null) {
        if (input2 instanceof String) {
          e.setNextState(State.valueOf(((String) input2).toUpperCase()));
        } else if (input2 instanceof State) {
          e.setNextState((State) input2);
        }
      }

      return null;
    }

    if (isKey(key, "setPosition")) {
      // Set the given entities position with the given vector
      ((Entity) exData(cmd, 0, dStore)).setPosition((Vector) exData(cmd, 1, dStore));
      return null;
    }

    if (isKey(key, "setVelocity") || isKey(key, "setVel")) {
      // Set the given entities position with the given vector
      ((Entity) exData(cmd, 0, dStore)).setVelocity((Vector) exData(cmd, 1, dStore));
      return null;
    }

    if (isKey(key, "setCVelocity") || isKey(key, "setCVel")) {
      // Set the given entities position with the given vector
      ((Entity) exData(cmd, 0, dStore)).setConstVelocity((Vector) exData(cmd, 1, dStore));
      return null;
    }

    if (isKey(key, "setCForce") || isKey(key, "setCF")) {
      // Set the given entities position with the given vector
      ((Entity) exData(cmd, 0, dStore)).setConstForce((Vector) exData(cmd, 1, dStore));
      return null;
    }

    if (isKey(key, "setAcceleration") || isKey(key, "setAcc")) {
      // Set the given entities position with the given vector
      ((Entity) exData(cmd, 0, dStore)).addForce((Vector) exData(cmd, 1, dStore));
      return null;
    }

    if (isKey(key, "localMove")) {
      Entity e = (Entity) exData(cmd, 0, dStore);
      Entity e2 = (Entity) exData(cmd, 1, dStore);

      Vector location = null;
      Event event = null;
      float delay = 0;
      MovementType type = null;

      JSONArray jsonArray = cmd.getJSONArray("data");

      for (int index = 2; index < jsonArray.length(); index++) {
        Object input = exData(jsonArray.get(index), dStore);

        if (input instanceof Entity) {
          location = ((Entity) input).getPos();
        } else if (input instanceof Event) {
          event = ((Event) input);
        } else if (input instanceof Float) {
          delay = ((float) input);
        } else if (input instanceof String) {
          type = MovementType.valueOf(((String) input).toUpperCase());
        }
      }

      if (location == null || e == null || e2 == null) {
        throw new RuntimeException("Malformed localMove {location:" + location + ", e:" + e + ", e2:" + e2 + "}" );
      }

      Mover.moveTo(e, location.addi(e.getPos().subi(e2.getPos())), delay, type, event);
      return null;
    }

    if (isKey(key, "moveTo")) {
      // set a given entities name

      Entity e = (Entity) exData(cmd, 0, dStore);

      Vector location = null;
      Event event = null;
      float delay = 0;
      MovementType type = null;

      JSONArray jsonArray = cmd.getJSONArray("data");

      for (int index = 1; index < jsonArray.length(); index++) {
        Object input = exData(jsonArray.get(index), dStore);

        if (input instanceof Entity) {
          location = ((Entity) input).getPos();
        } else if (input instanceof Event) {
          event = ((Event) input);
        } else if (input instanceof Float) {
          delay = ((float) input);
        } else if (input instanceof String) {
          type = MovementType.valueOf(((String) input).toUpperCase());
        }
      }

      Mover.moveTo(e, location, delay, type, event);
      return null;
    }
    //</editor-fold>

    //<editor-fold desc="Vector">
    if (isKey(key, "normalize")) {
      // normalize a vector
      ((Vector) exData(cmd, 0, dStore)).normalize();
      return null;
    }

    if (isKey(key, "scale")) {
      // scale a given vector
      ((Vector) exData(cmd, 0, dStore)).scale((float) exData(cmd, 1, dStore));
      return null;
    }

    if (isKey(key, "setVector")) {
      // scale a given vector
      ((Vector) exData(cmd, 0, dStore)).set((Vector) exData(cmd, 1, dStore));
      return null;
    }
    //</editor-fold>

    //<editor-fold desc="Container Entities">
    if (isKey(key, "setExitEvent")) {
      // scale a given vector
      ((EnterableEntity) exData(cmd, 0, dStore)).setExitEvent((Event) exData(cmd, 1, dStore));
      return null;
    }

    if (isKey(key, "setEnterEvent")) {
      // scale a given vector
      ((EnterableEntity) exData(cmd, 0, dStore)).setEnterEvent((Event) exData(cmd, 1, dStore));
      return null;
    }

    if (isKey(key, "setCustomPose")) {
      // set the sate of the given entity

      SeatEntity e = (SeatEntity) exData(cmd, 0, dStore);
      Object input = exData(cmd, 1, dStore);

      if (input instanceof String) {
        e.setCustomPose(Pose.valueOf(((String) input).toUpperCase()));
      } else if (input instanceof Pose) {
        e.setCustomPose((Pose) input);
      }
      return null;
    }

    if (isKey(key, "childPosition") || isKey(key, "seat_position")) {
      // Set the child position of the given entity
      ((ContainerEntity) exData(cmd, 0, dStore)).setChildPosition((Vector) exData(cmd, 1, dStore));
      return null;
    }

    if (isKey(key, "childOffset") || isKey(key, "seatOffset")) {
      // Set the child position of the given entity
      ((ContainerEntity) exData(cmd, 0, dStore)).setChildOffset((Vector) exData(cmd, 1, dStore));
      return null;
    }

    if (isKey(key, "setExitNormals")) {
      // Set the objects exit normals
      ContainerEntity entity = (ContainerEntity) exData(cmd, 0, dStore);

      entity.clearExitNormals();
      JSONArray arr = cmd.getJSONArray("data");
      for (int index = 1; index < arr.length(); index++) {
        entity.addExitNormal((Vector) exData(arr.get(index), dStore));
      }
      return null;
    }
    //</editor-fold>

    //<editor-fold desc="Shape">
    if (isKey(key, "flipPolygonHorizontal")) {
      // flip a given polygon horizontally
      ((PolygonShape) exData(cmd, 0, dStore)).flipHorizontal();
      return null;
    }
    //</editor-fold>

    //<editor-fold desc="Audio">
    if (isKey(key, "playSound")) {
      ((Sound) exData(cmd, 0, dStore)).play();
      return null;
    }
    //</editor-fold>

    //<editor-fold desc="General Purpose & High Level">
    if (isKey(key, "setSprite") || isKey(key, "sprite")) {
      // set the current sprite
      Object i1 = exData(cmd, 0, dStore);
      Sprite sprite = parseSpriteData(cmd, 1, i1, dStore);

      if (!cmd.optBoolean("set_lookup", false)) {
        if (i1 instanceof Entity) {
          ((Entity) i1).setSprite(sprite);
        } else if (i1 instanceof Room) {
          ((Room) i1).setSprite(sprite);
        }
      }
      return null;
    }

    if (isKey(key, "setShape")) {
      // <>(Entity/Room, Shape)
      // <>(Entity/Room, Shape, Vector)
      // set the shape of the given entity or room
      Object collector = exData(cmd, 0, dStore);
      Object shape = exData(cmd, 1, dStore);

      Shape s;

      if (shape instanceof Entity) {
        s = ((Entity) shape).getShape();
        if (collector instanceof Entity) {
          ((Entity) collector).addShape(((Entity) shape).getShapeMap());
        }
      } else {
        s = ((Shape) shape);
      }

      Object shift = exData(cmd, 2, dStore);
      if (shift != null) {
        MultiShape ms = new MultiShape();
        ms.add(s, (Vector) shift);
        s = ms;
      }

      if (collector instanceof Entity) {
        ((Entity) collector).setShape(s);
      } else if (collector instanceof Room) {
        ((Room) collector).setShape(s);
      }
      return null;
    }

    if (isKey(key, "shiftPos")) {
      Object input = exData(cmd, 0, dStore);
      Vector vector = (Vector) exData(cmd, 1, dStore);
      if (input instanceof MultiShape) {
        ((MultiShape) input).shiftAll(vector);
      } else if (input instanceof PolygonShape) {
        ((PolygonShape) input).addShift(vector);
      } else if (input instanceof Entity) {
        ((Entity) input).setPosition(((Entity) input).getPosition().addi(vector));
      }
      return null;
    }

    // VVV HIGH LEVEL VVV

    if (isKey(key, "load")) {
      String prev = dStore.flags.fileName;
      String pstr = (String) exData(cmd, 0, dStore);
      File path = new File(pstr);

      if (path.isDirectory()) {
        for (String file_str : path.list()) {
          if (file_str.contains(".json")) {
            JsonLoader.parseJson(pstr + "/" + file_str, dStore);
          }
        }
      } else {
        JsonLoader.parseJson(pstr, dStore);
      }
      dStore.flags.fileName = prev;
      return null;
    }

    if (isKey(key, "loadFile")) {
      // load the given save file
      String prev = dStore.flags.fileName;
      DataLoader.loadSaveFile((String) exData(cmd, 0, dStore), dStore);
      dStore.flags.fileName = prev;
      return null;
    }
    //</editor-fold>

    //<editor-fold desc="Input">
    if (isKey(key, "loadButtonConstants")) {
      // load the button constants from the window
      dStore.g.getWindow().loadButtonConstants();
      return null;
    }

    if (isKey(key, "keyConstants")) {
      // set the key constants of the game
      // works with array inputs too
      parseButtonCmd(cmd, ButtonType.Keyboard, dStore.g::setConstant, dStore);
      return null;
    }

    if (isKey(key, "mouseConstants")) {
      // set the mouse constants of the game
      // works with array inputs too
      parseButtonCmd(cmd, ButtonType.Mouse, dStore.g::setConstant, dStore);
      return null;
    }

    if (isKey(key, "gameKeySettings")) {
      // set the game key control of the game
      // works with array inputs too
      parseButtonCmd(cmd, ButtonType.Keyboard, dStore.g::setControl, dStore);
      return null;
    }
    //</editor-fold>

    throw new Exception("Command: <" + key + "> isn't supported.");
  }

  /**
   * Parse a JSONObject as object data.
   * <p>
   * Automatically adds entities to the loaded room unless {load: false} is present. If the object
   * has the lookup tag set, {lookup: "lookup_tag"}, the Object representation of the JSONObject is
   * placed in the lookup table under the value of the tag ("lookup_tag").
   *
   * @param obj    JSONObject object
   * @param dStore parsing data storage
   * @throws Exception throws processing errors
   */
  public static Object parseObjData(JSONObject obj, DStore dStore) throws Exception {
    Object output = parseObjData(obj.getString("obj"), obj, dStore);

    if (output instanceof Entity) {
      if (dStore.g != null && dStore.g.getWorld() != null && dStore.g.getWorld().getLoadedRoom() != null) {
        if (output instanceof MenuEntity && obj.optBoolean("load_menu", true)) {
          dStore.g.getWorld().addDisplayText((MenuEntity) output);
        } else if (obj.optBoolean("load", true)) {
          dStore.g.getWorld().getLoadedRoom().addEntity((Entity) output);
        }
      }
    }

    if (obj.has("lookup")) {
      dStore.lookup.put(obj.getString("lookup"), output);
    }

    return output;
  }

  /**
   * Parse a JSONObject as object data.
   * <p>
   * Automatically adds entities to the loaded room unless {load: false} is present. If the object
   * has the lookup tag set, {lookup: "lookup_tag"}, the Object representation of the JSONObject is
   * placed in the lookup table under the value of the tag ("lookup_tag").
   *
   * @param key    lookup key of the object
   * @param obj    JSONObject object
   * @param dStore parsing data storage
   * @throws Exception throws processing errors
   */
  public static Object parseObjData(Object key, JSONObject obj, DStore dStore) throws Exception {
    //<editor-fold desc="shapes parsers">
    if (isKey(key, "circle")) {
      return new Circle((float) exData(obj, 0, dStore));
    }

    if (isKey(key, "polygon")) {
      ArrayList<Vector> verticies = new ArrayList<>();
      float x = -1;
      float y = -1;

      for (Object input : obj.getJSONArray("data")) {
        input = exData(input, dStore);

        if (input instanceof Vector) {
          verticies.add((Vector) input);
        } else if (input instanceof Float) {
          if (x == -1) {
            x = (float) input;
          } else {
            y = (float) input;
          }
        }
      }

      if (verticies.isEmpty()) {
        return new PolygonShape(x, y);
      } else {
        return new PolygonShape(verticies);
      }
    }

    if (isKey(key, "multishape")) {
      MultiShape shape = new MultiShape();
      Shape sub_shape = null;
      Vector location = null;

      for (Object input : obj.getJSONArray("data")) {
        input = exData(input, dStore);

        if (input instanceof JSONArray) {
          for (Object sub_input : (JSONArray) input) {
            sub_input = exData(sub_input, dStore);
            if (sub_input instanceof Vector && location != null) {
              sub_shape = new PolygonShape((Vector) sub_input);
            } else if (sub_input instanceof Shape) {
              sub_shape = (Shape) sub_input;
            } else if (sub_input instanceof Vector) {
              location = (Vector) sub_input;
            }
          }

          if (sub_shape != null && location != null) {
            shape.add(sub_shape, location);
            sub_shape = null;
            location = null;
          }
        }
      }

      return shape;
    }
    //</editor-fold>

    //<editor-fold desc="sprite parsers">
    if (isKey(key, "sprite") || isKey(key, "s")) {
      return parseSpriteData(obj, dStore);
    }

    if (isKey(key, "layeredsprite")) {
      LayeredSprite sprite = new LayeredSprite();

      for (Object input : obj.getJSONArray("data")) {
        input = exData(input, dStore);
        if (input instanceof Vector) {
          sprite.setLocalLocation((Vector) input);
        } else if (input instanceof Sprite) {
          sprite.addSprite((Sprite) input);
        } else if (input instanceof String) {
          sprite.addSprite(dStore.g.getSprite((String) input));
        }
      }

      return sprite;
    }

    if (isKey(key, "animation")) {
      Sprite sprite = parseSpriteData(obj, dStore);
      assert sprite instanceof Animation;

      Animation animation = (Animation) sprite;
      animation.setLoop(obj.optBoolean("loop", true));
      return animation;
    }
    //</editor-fold>

    //<editor-fold desc="basic data types">
    if (isKey(key, "clone")) {
      Object o = exData(obj, 0, dStore);

      if (o instanceof Entity) {
        return ((Entity) o).clone();
      } else if (o instanceof Shape) {
        return ((Shape) o).clone();
      } else if (o instanceof Vector) {
        return ((Vector) o).clone();
      } else if (o instanceof Number) {
        return ((Number) o);
      }

      return o;
    }

    if (isKey(key, "vector") || isKey(key, "v")) {
      Entity e1 = null;
      Float a = null;

      for (Object input : obj.getJSONArray("data")) {
        input = exData(input, dStore);

        if (input instanceof Entity) {
          if (e1 == null) {
            e1 = (Entity) input;
          } else {
            return ((Entity) input).getPos().subi(e1.getPos());
          }
        } else if (input instanceof Float) {
          if (a == null) {
            a = (Float) input;
          } else {
            return new Vector(a, (Float) input);
          }
        }
      }
    }

    if (isKey(key, "bool") || isKey(key, "boolean")) {
      return exData(obj, 0, dStore);
    }

    if (isKey(key, "state")) {
      Object input = exData(obj, 0, dStore);
      if (input instanceof Entity) {
        return ((Entity) input).getState();
      }

      assert input != null;

      return State.get((String) input);
    }

    if (isKey(key, "sound")) {
      return new Sound((String) exData(obj, 0, dStore));
    }

    if (isKey(key, "color")) {
      Color color = Color.black;
      float[] farr = new float[4];
      farr[3] = 1.0f;
      int cIdx = 0;

      for (Object input : obj.getJSONArray("data")) {
        input = exData(input, dStore);

        if (input instanceof Float) {
          farr[cIdx] = (float) input;
          cIdx++;

          if (cIdx > 3) {
            color = new Color(farr[0], farr[1], farr[2], farr[3]);
          } else if (cIdx > 2) {
            color = new Color(farr[0], farr[1], farr[2]);
          }
        } else if (input instanceof String) {
          color = Color.getColor((String) input);
        }
      }

      return color;
    }

    if (isKey(key, "room")) {
      Room room = new Room(dStore.g);

      for (String sub_key : obj.keySet()) {
        Object input = exData(obj.get(sub_key), dStore);

        if (isKey(key, "name")) {
          room.setName((String) input);
        }
      }

      dStore.g.getWorld().addRoom(room);
      return room;
    }

    if (isKey(key, "facing")) {
      return ((Player) exData(obj, dStore)).getFacing();
    }
    //</editor-fold>

    //<editor-fold desc="entities">
    if (isKey(key, "entity")) {
      return parseEntityData(new Entity(dStore.g), obj, dStore);
    }

    if (isKey(key, "npc")) {
      return parseEntityData(new NPC(dStore.g), obj, dStore);
    }

    if (key.equals("basic_entity")) {
      return parseEntityData(new BasicEntity(), obj, dStore);
    }

    if (isKey(key, "vehicle")) {
      return parseEntityData(new Vehicle(dStore.g), obj, dStore);
    }

    if (isKey(key, "item")) {
      return parseEntityData(new ItemEntity(dStore.g), obj, dStore);
    }

    if (isKey(key, "bed")) {
      return parseEntityData(new BedEntity(dStore.g), obj, dStore);
    }

    if (isKey(key, "varseat")) {
      return parseEntityData(new VariableSeatEntity(dStore.g), obj, dStore);
    }

    if (isKey(key, "seat")) {
      return parseEntityData(new SeatEntity(dStore.g), obj, dStore);
    }

    if (isKey(key, "yoyo")) {
      return parseEntityData(new YoYoEntity(dStore.g), obj, dStore);
    }

    if (isKey(key, "crate")) {
      return parseEntityData(new CrateEntity(dStore.g), obj, dStore);
    }

    if (isKey(key, "door")) {
      return parseEntityData(new Door(dStore.g), obj, dStore);
    }

    if (isKey(key, "player")) {
      return parseEntityData(new Player(dStore.g), obj, dStore);
    }

    //</editor-fold>

    //<editor-fold desc="menu entities">
    if (key.equals("text_box")) {
      return parseEntityData(new TextBox(dStore.g), obj, dStore);
    }

    if (key.equals("menu_entity")) {
      Entity out = parseEntityData(new MenuEntity(dStore.g), obj, dStore);

      return out;
    }
    //</editor-fold>

    //<editor-fold desc="command objects">
    if (isKey(key, "if") || isKey(key, "ifcmd")) {
      ArrayList<Object> comp_list;
      boolean not = obj.optBoolean("not", false);
      JSONArray arr = obj.getJSONArray("data");
      Event op1 = null, op2 = null;

      if (arr.get(0) instanceof JSONArray) {
        comp_list = new ArrayList<>(((JSONArray) arr.get(0)).toList());
        op1 = (Event) exData(arr.get(1), dStore);
        op2 = arr.length() > 2 ? (Event) exData(arr.get(2), dStore) : null;
      } else {
        comp_list = new ArrayList<>(Arrays.asList(arr.get(0), arr.get(1)));
        op1 = (Event) exData(arr.get(2), dStore);
        op2 = arr.length() > 3 ? (Event) exData(arr.get(3), dStore) : null;
      }


      final Event pass = op1, fail = op2;
      assert pass != null;

      Callable<Boolean> test = () -> {
        boolean flag = true;

        for (int i = 0; i < comp_list.size() - 1; i = i + 2) {
          if (!flag) {
            break;
          }

          Object a = exData(comp_list.get(i), dStore);
          Object b = exData(comp_list.get(i + 1), dStore);
          flag = a.equals(b);
        }

        return flag ^ not;
      };

      Event event = new Event((a, b) -> {
        if (test.call()) {
          pass.act(a, b);
        } else if (fail != null) {
          fail.act(a, b);
        }
      });
      event.add((a) -> {
        if (test.call()) {
          pass.act(a);
        } else if (fail != null) {
          fail.act(a);
        }
      });
      event.add(() -> {
        if (test.call()) {
          pass.act();
        } else if (fail != null) {
          fail.act();
        }
      });
      return event;
    }

    if (isKey(key, "cmd")) {
      return eventFromData(obj, dStore);
    }

    if (isKey(key, "event")) {
      return parseEventData(obj, dStore);
    }
    //</editor-fold>

    //<editor-fold desc="messages">
    if (isKey(key, "msg")) {
      Message message = new Message();
      for (Object input : obj.getJSONArray("data")) {
        input = exData(input, dStore);
        if (input instanceof String) {
          message.addText((String) input);
        } else if (input instanceof TextContext) {
          message.addText((TextContext) input);
        } else if (input instanceof JSONArray) {
          for (Object input2 : ((JSONArray) input)) {
            message.addChoice((Message) exData(input2, dStore));
          }
        } else if (input instanceof Message) {
          message.addChoice((Message) input);
        }
      }

      for (String sub_key : obj.keySet()) {
        Object input = exData(obj.get(sub_key), dStore);
        if (isKey(sub_key, "interact") || isKey(sub_key, "after")) {
          message.setInteractEvent((Event) input);
        } else if (isKey(sub_key, "display")) {
          message.setDisplayEvent((Event) input);
        }
      }

      return message;
    }

    if (isKey(key, "dlg") || isKey(key, "dialogue")) {
      Dialogue dialogue = new Dialogue();
      for (Object input : obj.getJSONArray("data")) {
        input = exData(input, dStore);

        if (input instanceof String) {
          dialogue.add((String) input);
        } else if (input instanceof Message) {
          dialogue.add((Message) input);
        }
      }
      return dialogue;
    }
    //</editor-fold>

    throw new Exception("[UNSUPPORTED] Object: (" + key + ") isn't supported.");
  }
  //</editor-fold>

  //</editor-fold>
}













































