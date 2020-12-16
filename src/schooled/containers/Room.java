package schooled.containers;

import java.util.ArrayList;
import schooled.Game;
import schooled.datatypes.Tuple;
import schooled.engines.Engine;
import schooled.entities.BasicEntity;
import schooled.entities.Entity;
import schooled.physics.BoundingBox;
import schooled.physics.MultiShape;
import schooled.physics.PolygonShape;
import schooled.physics.Shape;
import schooled.physics.Vector;
import schooled.visuals.sprite.Sprite;

/**
 * A room object that stores list of objects to be rendered and reacted from. The room also stores a
 * background sprite that is displayed when the room is loaded.
 * <p></p>
 * Program written by Joseph Straceski
 * <p>
 * contact: straceski.joseph@gmail.com, https://github.com/Crepox
 */
public class Room extends EntityHolder {

  public static final int default_width = 1600;
  public static final int default_height = 1200;
  private ArrayList<Tuple<Vector, Sprite>> backgrounds = new ArrayList<>(); // the background sprites
  private boolean backgroundsChanged = false;
  private Shape customShape = null;
  private Shape backgroundShape = null;
  private Vector backgroundWindow = null;

  // a list of entities representing the walls of the room
  private ArrayList<Entity> walls = new ArrayList<>();
  private Vector customWindow = null; // a vector representing a custom room size
  private Sprite customMatte = null; // repeatable matte sprite to draw behind the room
  private World world; // the master parent world container
  private Game game; // the game object reference

  /**
   * Create a Room object with a reference to the master game object and a background sprite.
   *
   * @param g          game reference
   * @param background a background sprite
   */
  public Room(Game g, Sprite background) {
    game = g;
    addBackgroundSprite(new Vector(0, 0), background);
  }

  /**
   * Create a Room object with a reference to the master game object.
   *
   * @param g game reference
   */
  public Room(Game g) {
    this(g, null);
  }

  /**
   * Get the master game object reference.
   *
   * @return game object reference
   */
  public Game getGame() {
    return game;
  }

  /**
   * Return a list of the wall entities.
   *
   * @return list of walls
   */
  public ArrayList<Entity> getWalls() {
    return walls;
  }

  /**
   * Add a list of wall entities to the room's wall list.
   *
   * @param s wall list
   */
  public void addWall(ArrayList<Entity> s) {
    s.forEach(this::addWall);
  }

  /**
   * Add a shape to the wall as an entity.
   *
   * @param v position of the shape relative to the room
   * @param s shape object
   */
  public void addWall(Vector v, Shape s) {
    addWall(new Entity(getGame(), v, s, 0.0f));
  }

  /**
   * Add a shape to the wall as an entity.
   *
   * @param s shape object
   */
  public void addWall(Shape s) {
    addWall(new Entity(getGame(), Vector.zero.clone(), s, 0.0f));
  }

  /**
   * Add an entity to the wall.
   *
   * @param s wall entity
   */
  public void addWall(Entity s) {
    walls.add(s);
    s.setName("wall[" + getName() + "]");
  }

  /**
   * Get a list of all all the entities in the room, including the walls.
   *
   * @return list of entities
   */
  public ArrayList<BasicEntity> getAllBasicEntities() {
    ArrayList<BasicEntity> list = new ArrayList<>();
    list.addAll(walls);
    list.addAll(getEntities());
    return list;
  }

  public ArrayList<Entity> getAllEntities() {
    ArrayList<Entity> list = new ArrayList<>();
    list.addAll(walls);
    list.addAll(getEntities());
    return list;
  }

  @Override
  public void updateCycle(float t) {
    for (Entity entity : walls){
      entity.updateCycle(t);
    }

    for (Entity entity : getEntities()){
      entity.updateCycle(t);
    }
  }

//  /**
//   * Get a list of all all the entities in the room, including the walls.
//   *
//   * @return list of entities
//   */
//  public ArrayList<Entity> getRenderEntities(Vector pos, Vector frameSize, float scale) {
//    ArrayList<Entity> list = new ArrayList<>();
//
//    Vector framePos = pos.addi(frameSize.scalei(0.5f));
//    BoundingBox frameBox = new BoundingBox(new PolygonShape(frameSize));
//
//    for (Entity entity : entities) {
//      Vector sprSize = entity.getSprite()
//
//          .scalei(scale);
//      Vector sprPos = data.x.addi(sprSize.scalei(0.5f));
//
//      BoundingBox spriteBox = new BoundingBox(new PolygonShape(sprSize));
//      if (Engine.boundingBoxCollision(sprPos, spriteBox, framePos, frameBox)) {
//        list.add(data);
//      }
//    }
//
//    list.addAll(walls);
//    return list;
//  }


  /**
   * Set the custom window size of the room
   *
   * @param windowSize custom window size
   */
  public void setWindowSize(Vector windowSize) {
    customWindow = windowSize;
  }

  /**
   * Load the size of the game screen based on the current room.
   */
  public void loadRoomWindow() {
    if (customWindow != null) {
      getGame().setGameSize(customWindow);
    } else if (backgroundWindow != null) {
      Vector window = Vector.min(backgroundWindow, getGame().getDefaultGameSize());
      getGame().setGameSize(window);
    } else {
      getGame().resetSize();
    }
  }

  /**
   * Get the world object reference.
   *
   * @return a reference to the world object.
   */
  public World getWorld() {
    return world;
  }

  /**
   * Set the world reference object.
   *
   * @param world world
   */
  public void setWorld(World world) {
    this.world = world;
  }

  /**
   * Get the Room's sprite.
   *
   * @return room sprite
   */
  public Sprite getSprite() {
    return backgrounds.get(0).b;
  }

  /**
   * Get the Room's background sprites to draw within the render window.
   *
   * @return room sprite
   */
  public ArrayList<Tuple<Vector, Sprite>> getRenderSprites(Vector pos, Vector frameSize, float scale) {
    ArrayList<Tuple<Vector, Sprite>> newList = new ArrayList<>();

    Vector framePos = pos.addi(frameSize.scalei(0.5f));
    BoundingBox frameBox = new BoundingBox(new PolygonShape(frameSize));

    for (Tuple<Vector, Sprite> data : backgrounds) {
      Vector sprSize = data.b.getSize().scalei(scale);
//      Game.log(data.y);
      Vector sprPos = data.a.scalei(scale).addi(sprSize.scalei(0.5f));

      BoundingBox spriteBox = new BoundingBox(new PolygonShape(sprSize));
//      Game.log(sprPos);
//      Game.log(pos);
//      Game.log(spriteBox);
//      Game.log(Engine.boundingBoxCollision(sprPos, spriteBox, framePos, frameBox));
      if (Engine.boundingBoxCollision(sprPos, spriteBox, framePos, frameBox)) {
        newList.add(data);
      }
    }

    return newList;
  }

  /**
   * Add a sprite to the Room's background sprites.
   *
   * @return room sprite
   */
  public void addBackgroundSprite(Vector pos, Sprite sprite) {
    if (sprite != null) {
      backgroundsChanged = true;
      backgrounds.add(new Tuple<>(pos, sprite));
    }
  }

  /**
   * Set the room's sprite object.
   *
   * @param sprite room sprite
   */
  public void setSprite(Sprite sprite) {
    backgrounds.clear();
    if (sprite != null) {
      addBackgroundSprite(new Vector(0, 0), sprite);
    }
  }

  /**
   * Check if the room has a sprite.
   *
   * @return if room has a sprite return true
   */
  public boolean hasSprite() {
    return !backgrounds.isEmpty();
  }

  /**
   * Update background driven data
   *
   */
  public void updateBackgroundData() {
    backgroundsChanged = false;

    Vector min = new Vector(Float.MAX_VALUE, Float.MAX_VALUE);
    Vector max = new Vector(-Float.MAX_VALUE, -Float.MAX_VALUE);

    for (Tuple<Vector, Sprite> tuple : backgrounds) {
      Vector pos = tuple.a;
      Sprite sprite = tuple.b;

      min = Vector.min(pos, min);
      max = Vector.max(pos.addi(sprite.getSize()), max);
    }

    backgroundWindow = max.subi(min);
    backgroundShape = new PolygonShape(max.getX(), max.getY(), min.getX(), min.getY());
  }

  /**
   * Does the room have a shape.
   *
   * @return true if the room has a shape false otherwise
   */
  public boolean hasShape() {
    return getShape() != null;
  }

  /**
   * Set the the shape of the room.
   *
   * @param shape the shape
   */
  public void setShape(Shape shape) {
    customShape = shape;
  }

  /**
   * Get the size of the current room.
   *
   * @return room's size
   */
  public Shape getShape() {
    if (customShape == null) {
      if (backgroundsChanged) {
        updateBackgroundData();
      }
      return backgroundShape;
    } else {
      return customShape;
    }
  }

  /**
   * Does the room contain the given entity.
   *
   * @param e entity
   * @return true if it contains the entity false otherwise
   */
  public boolean containsEntity(Entity e) {
    return entities.contains(e);
  }

  /**
   * Generates walls using a width and a sort of horizon (aka screenShiftDown)
   *
   * @param wallSize       width of the generated walls
   * @param screenShiftDown position of the middle fold of the wall generation
   */
  public void generateRectangleWalls(float wallSize, float screenShiftDown) {
    BoundingBox bb = new BoundingBox(getShape());

    float sideHeight = bb.yMax - (bb.yMin + screenShiftDown);
    float edgeWidth = bb.xMax - bb.xMin;

    float sideLeftX = bb.xMin + (wallSize / 2);
    float sideRightX = bb.xMax - (wallSize / 2);
    float sideY = bb.yMin + (bb.yMin + screenShiftDown + sideHeight / 2);

    float edgeTopY = bb.yMin + screenShiftDown + (wallSize / 2);
    float edgeBottomY = bb.yMax - (wallSize / 2);
    float edgeX = bb.xMin + edgeWidth / 2;

    MultiShape ms = new MultiShape();
    ms.add(new PolygonShape(wallSize, sideHeight), new Vector(sideLeftX, sideY));
    ms.add(new PolygonShape(wallSize, sideHeight), new Vector(sideRightX, sideY));

    ms.add(new PolygonShape(edgeWidth, wallSize), new Vector(edgeX, edgeTopY));
    ms.add(new PolygonShape(edgeWidth, wallSize), new Vector(edgeX, edgeBottomY));
    addWall(ms);
  }

  /**
   * Removes an entity from the room.
   *
   * @param e entity reference
   */
  @Override
  public void removeEntity(Entity e) {
    entities.remove(e);
    e.setContainer(null);
  }

  /**
   * Returns the matte sprite for the Room object.
   *
   * @return matte sprite
   */
  public Sprite getMatte() {
    if (customMatte == null) {
      return getGame().getDefaultMatte();
    }
    return customMatte;
  }

  /**
   * Returns an array list of entities in the room.
   *
   * @return list of entities in the room
   */
  @Override
  public ArrayList<Entity> getEntities() {
    ArrayList<Entity> list = new ArrayList<>();

    for (Entity entity : entities) {
      if (!list.contains(entity)) {
        list.add(entity);
      }

      for (Entity child: entity.getAllChildren()) {
        if (!list.contains(child)) {
          list.add(child);
        }
      }
    }
    return list;
  }

  /**
   * Sets an array list of entities in the room.
   *
   * @param entities list of entities
   */
  public void setEntities(ArrayList<Entity> entities) {
    this.entities = entities;
  }

  public void addRoomPosition(Room room, Vector pos, boolean cloneEntities) {
    ArrayList<Entity> list = new ArrayList<>(room.getEntities());
    for (Entity entity : list) {
      if (cloneEntities) {
        entity = entity.clone();
      }

      addEntity(entity);
      entity.addPosition(pos);
    }

    for (Entity entity : room.getWalls()) {
      Entity ce = entity.clone();
      ce.addPosition(pos);
      addWall(ce);
    }

    for (Tuple<Vector, Sprite> data : room.backgrounds) {
      addBackgroundSprite(data.a.addi(pos), data.b.clone());
    }
  }

  public void addRoom(Room room, Vector vector, Vector shift, boolean cloneEntities) {
    BoundingBox bb1 = new BoundingBox(getShape());
    BoundingBox bb2 = new BoundingBox(room.getShape());
    Vector pos = vector.scalei(Math.max(bb1.getMax().dot(vector), 0));
    pos.addScaled(vector, Math.max(bb1.getMin().subi(bb2.getMax()).dot(vector), 0));

    addRoomPosition(room, pos.addi(shift), cloneEntities);
  }

  public void addRoom(Room room, Vector vector, boolean cloneEntities) {
    addRoom(room, vector, new Vector(0, 0), cloneEntities);
  }

  public String toString() {
    if (getName() != null) {
      return "Room[name=\"" + getName() + "\"]";
    }
    return toString();
  }
}
