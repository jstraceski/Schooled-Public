package schooled.entities;

import java.util.ArrayList;
import java.util.HashMap;
import schooled.Game;
import schooled.Window;
import schooled.containers.EntityHolder;
import schooled.containers.Room;
import schooled.event.Event;
import schooled.menu.Dialogue;
import schooled.menu.Origin;
import schooled.physics.BoundingBox;
import schooled.physics.Manifold;
import schooled.physics.Shape;
import schooled.physics.Vector;
import schooled.visuals.GraphicsContext;
import schooled.visuals.filters.GLFilter;
import schooled.visuals.sprite.LayeredSprite;
import schooled.visuals.sprite.Sprite;

/**
 * A class that stores an instance of an entity object.
 * <p>
 * Entities store a position, mass, velocity, shape, and other properties of an entity.
 * <p></p>
 * Program written by Joseph Straceski
 * <p>
 * contact: straceski.joseph@gmail.com, https://github.com/Crepox
 */
public class Entity extends BasicEntity {

  /**
   * Get the game reference that was provided to the entity when it was created.
   *
   * @return the game reference
   */
  public Game getGame() {
    return game;
  }

  /**
   * Set the game reference object.
   *
   * @param game the reference
   */
  public void setGame(Game game) {
    this.game = game;
  }


  private Game game; // a reference to the game instance


  // <editor-fold defaultstate="collapsed" desc="Variables">

  // <editor-fold defaultstate="collapsed" desc="Game/General Variables">

  private State state; // entity's game state
  private State nextState = null;

  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Parent/Container Variables">

  private EntityHolder container = null; // container holding the entity
  private Entity parent; // parent entity
  private ArrayList<Entity> children; // list of entity children

  private Vector childPosition = null; // position offset applied to child entities
  private Vector childOffset = null; // position offset applied to child entities
  private boolean globalPosition = false; // flag that  when set to true, overrides parent offsets

  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Physics Variables">


  private Vector constForce = new Vector(0, 0);
  private Vector constVelocity = new Vector(0, 0); // constant velocity like a varying acceleration
  private HashMap<String, Shape> shapeMap = new HashMap<>(); // shape lookup table

  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Visual Variables">

  private Sprite sprite; // sprite of the entity
  private Sprite nextSprite; // placeholder for the next sprite to display

  // vector offset from the position of the entity to the position of the sprite
  private Vector spriteShift = new Vector(0, 0);

  private boolean visible = true; // is the entity being drawn
  // flag to store if the sprite should transition to the next sprite this update
  private boolean transitionSprite = false;

  private RenderOrder renderOrder = RenderOrder.Normal; // rendering order enum
  private GLFilter filter = null;

  private HashMap<String, Sprite> spriteMap = new HashMap<>(); // sprite lookup table

  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Event/Action Variables">


  private Event updateEvent;
  private Event interactEvent = null; // interaction event
  // flag set when the current entity takes precedence over all other interacting entities
  private boolean interactOverride = false;

  public ArrayList<Entity> interactions; // list of entities interaction with the current one
  // flag set when the current entity has weakly interacted with the world
  //  a weak interaction is one that occurs when not selecting anything obvious
  private boolean weakIncrement = false;
  // flag set when the interactions of the current entity have been overridden by another entity
  private boolean overridden = false;

  // </editor-fold>

  // </editor-fold>

  enum RenderOrder {
    First, Normal, Last
  }

  // <editor-fold defaultstate="collapsed" desc="Constructors">

  /**
   * Most basic entity construction.
   * <p>
   * Uses the basic entity construction setting position (0,0), mass 0, and shape null. All Entities
   * require a link to the game container to exist.
   *
   * @param g game
   */
  public Entity(Game g) {
    super();
    game = g;
    children = new ArrayList<>();
    sprite = new Sprite();
    interactions = new ArrayList<>();
  }

  /**
   * Creates an Entity from a basic entity.
   *
   * @param be basic entity
   */
  public Entity(Entity be) {
    this(be.getGame());
    if (be != this) {
      be.setClone(this);
    }
  }

  /**
   * Creates an Entity from a basic entity.
   *
   * @param be basic entity
   */
  public Entity(BasicEntity be, Game g) {
    this(g, be.getPosition(), be.getShape(), be.getMass());
  }

  /**
   * Creates an entity from some standard entity values.
   *
   * @param g    game
   * @param v    position
   * @param s    shape
   * @param mass mass
   */
  public Entity(Game g, Vector v, Shape s, float mass) {
    this(g);
    setPosition(v);
    setShape(s);
    setMass(mass);
  }

  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Data/Overridden Methods">

  /**
   * Clone the given entity's values into the inputs.
   * <p>
   * Effectively makes the input entity a clone of the given entity.
   *
   * @param entity input entity
   * @return clone of the current entity
   */
  public Entity setClone(Entity entity) {
    super.setClone(entity);
    entity.setParent(getParent());

    ArrayList<Entity> cCopy = new ArrayList<>(getChildren());
    cCopy.forEach(entity1 -> entity.addChild(entity1.clone()));

    entity.setRenderOrder(getRenderOrder());

    if (getChildPosition() != null) {
      entity.setChildPosition(getChildPosition().clone());
    }

    if (getChildOffset() != null) {
      entity.setChildOffset(getChildOffset().clone());
    }

    if (getSpriteShift() != null) {
      entity.setSpriteShift(getSpriteShift().clone());
    }

    entity.setContainer(container);


    entity.constForce = constForce.clone();
    entity.constVelocity = constVelocity.clone();
    entity.globalPosition = globalPosition;

    entity.spriteShift = spriteShift.clone();

    entity.filter = filter;
    entity.renderOrder = renderOrder;

    entity.shapeMap = shapeMap;
    entity.spriteMap = spriteMap;

    entity.setState(state);
    if (nextSprite != null) {
      entity.setNextSprite(nextSprite.clone());
    }
    entity.setInteractEvent(interactEvent);
    entity.setInteractOverride(interactOverride);
    entity.setUpdateEvent(updateEvent);

    if (sprite != null) {
      entity.setSprite(sprite.clone());
    }

    entity.setContainer(getContainer());
    entity.setVisible(visible);
    entity.setTransitionSprite(transitionSprite);
    return entity;
  }

  @Override
  public BasicEntity setClone(BasicEntity entity) {
    if (entity instanceof Entity) {
      return setClone(this.getClass().cast(entity));
    } else {
      return super.setClone(entity);
    }
  }

  @Override
  public Entity clone() {
    Entity entity = new Entity(getGame());
    setClone(entity);
    return entity;
  }



  public Vector getLocalPosition() {
    return super.getPosition();
  }

  /**
   * Get the position of the Entity.
   * <p>
   * Used in physics calculations. Takes into consideration parent positions and offsets. Also takes
   * into the global position flag.
   *
   * @return position vector
   */
  @Override
  public Vector getPosition() {
    if (globalPosition) {
      return super.getPosition();
    } else if (hasParent()) {
      Vector position = getParent().getPosition().addi(super.getPosition());
      Vector offset;

      if ((offset = getParent().getChildPosition()) != null) {
        position = position.addi(offset);
      }

      if ((offset = getParent().getChildOffset()) != null) {
        position = position.addi(offset);
      }

      return position;
    } else {
      return super.getPosition();
    }
  }



  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Game Logic Methods">

  public void reset() {

  }

  /**
   * Get the action state of the entity.
   *
   * @return state
   */
  public State getState() {
    if (state == null) {
      return State.DEFAULT;
    }

    return state;
  }

  /**
   * Set the action state of the entity.
   *
   * @param state state
   */
  public void setState(State state) {
    if (this.state == state) {
      return;
    }

    this.state = state;

    Shape shape = getShape();
    Sprite sprite = getSprite();

    if (shape != null) {
      shape.setState(state);
    }

    if (sprite != null) {
      sprite.setState(state);
    }
  }

  /**
   * Set the next action state of the entity.
   *
   * Shape will automatically transition to this state at the end of the current animation/action
   *
   * @param nextState next state
   */
  public void setNextState(State nextState) {
    this.nextState = nextState;
  }

  /**
   * Does the state of the given entity equal the input state.
   *
   * @param s input state
   * @return true if the states are equal, false otherwise
   */
  public boolean stateEquals(State s) {
    return getState().equals(s);
  }

  /**
   * Generate a selection in the container of the current entity.
   */
  public void processSelection() {
    processSelection(getContainer());
  }

  /**
   * Generate a selection in the input entity holder.
   *
   * @param c entity holder
   */
  public void processSelection(EntityHolder c) {
    super.processSelection(c);
    if (getParent() != null) {
      addInteraction(getParent());
    }
  }

  /**
   * Send a message in the given entity's world from the given entity.
   *
   * @param s message in string form
   */
  public void sendMessage(String s) {
    getGame().getWorld().sendMessage(s, this);
  }

  /**
   * Method called when dialogue involving the sender has finished.
   *
   * @param m dialogue
   */
  public void dialogueFinish(Dialogue m) {
  }

  /**
   * Do the update cycle using a time difference.
   *
   * @param t time difference
   */
  public void updateCycle(float t) {
    if (hasFilter()) {
      getFilter().updateFilter(t);
    }

    if (isFinishedState()) {
      if (hasTransitionSprite() && hasNextSprite()) {
        setSprite(nextSprite);
        nextSprite = null;
        transitionSprite = false;
      }

      if (nextState != null) {
        setState(nextState);
        nextState = null;
      }
    }

    updateSprite(t);
    updateShape(t);

    updateCycle();
  }

  /**
   * Do the update cycle for things that don't use a time difference.
   */
  public void updateCycle() {
    if (updateEvent != null) {
      updateEvent.act();
    }
  }

  public void preRender(){
    preRender(Window.DEFAULT_GRAPHICS_CONTEXT);
  }

  public void preRender(Object graphicsContext) { }


  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Container Methods">

  /**
   * Get the container of the entity if it has one.
   *
   * @return container
   */
  public boolean hasContainer() {
    return container != null;
  }

  /**
   * Get the container of the entity if it has one.
   *
   * @return container
   */
  public EntityHolder getContainer() {
    return container;
  }

  /**
   * Set the container of the entity.
   *
   * @param newContainer new container
   */
  public void setContainer(EntityHolder newContainer) {
    EntityHolder entityHolder = container;
    container = newContainer;

    if (entityHolder != null && entityHolder.hasEntity(this)) {
      entityHolder.removeEntity(this);
    }

    if (newContainer != null && !newContainer.hasEntity(this)) {
      newContainer.addEntity(this);
    }
  }

  /**
   * Remove the given entity from its container.
   * <p>
   * Handles null exceptions.
   */
  public void removeFromContainer() {
    EntityHolder entityHolder = container;
    container = null;

    if (entityHolder != null && entityHolder.hasEntity(this)) {
      entityHolder.removeEntity(this);
    }
  }

  /**
   * Get the container of the given entity in room form.
   *
   * @return room holding the entity
   */
  public Room getRoom() {
    EntityHolder entityHolder = getContainer();
    if (entityHolder instanceof Room) {
      return (Room) entityHolder;
    }

    return null;
  }

  /**
   * Moves an entity from one room to another.
   * <p>
   * Handles the removing of the given entity from its current room and the adding of the given
   * entity to the input room.
   *
   * @param r input room
   */
  public void setRoom(Room r) {
    setContainer(r);
  }

  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Parent/Child Methods">

  /**
   * Get the parent of the given entity.
   *
   * @return parent
   */
  public Entity getParent() {
    return parent;
  }

  /**
   * If the entity has a parent.
   *
   * @return true if the entity has a parent, false otherwise
   */
  public boolean hasParent() {
    return getParent() != null;
  }

  /**
   * If the input entity is the parent of the given entity.
   *
   * @param e input entity
   * @return true if the entity is the parent, false otherwise
   */
  @Override
  public boolean isParent(BasicEntity e) {
    if (parent != null) {
      return parent == e || parent.isParent(e);
    }
    return false;
  }


  /**
   * Set the parent.
   * <p>
   * Removes the entity from the child list of the old parent. Handles the null case as removing the
   * Entity from the current container.
   *
   * @param n_parent new parent
   */
  public void setParent(Entity n_parent) {
    if (parent != n_parent) {
      if (parent != null) {
        parent.children.remove(n_parent);
      }

      parent = n_parent;

      if (n_parent != null) {
        n_parent.children.add(this);

        if (n_parent.hasContainer()) {
          n_parent.getContainer().addEntity(this);
        }

        n_parent.childAdded(this);
      }
      parentSet(n_parent);
    }
  }

  /**
   * Add a child to the given entity.
   * <p>
   * Sets the parent of the child entity to the given entity.
   *
   * @param e child
   */
  public void addChild(Entity e) {
    if (!children.contains(e) && e != null) {
      if (e.parent != null && parent != null) {
        parent.children.remove(e);
      }

      e.parent = this;

      children.add(e);

      if (hasContainer()) {
        getContainer().addEntity(e);
      }

      e.parentSet(this);
      childAdded(e);
    }
  }

  /**
   * Remove an input child entity from the given entity.
   * <p>
   * Sets the parent of the child entity to null.
   *
   * @param e child
   */
  public void removeChild(Entity e) {
    if (children.remove(e) && e != null) {
      children.remove(e);
      e.parent = null;

//      if (hasContainer()) {
//        getContainer().removeEntity(e);
//      }

      e.parentSet(null);
      childRemoved(e);
    }
  }

  /**
   * Remove all children.
   */
  public void removeChildren() {
    ArrayList<Entity> removeList = (ArrayList<Entity>) children.clone();
    for (Entity entity : removeList) {
      removeChild(entity);
    }
  }

  public void parentSet(Entity entity) {

  }

  public void childAdded(Entity entity) {

  }

  public void childRemoved(Entity entity) {

  }

  /**
   * Is the input entity a direct child of the given entity.
   *
   * @param e entity
   * @return true if the input entity is a child of the given entity. false otherwise
   */
  public boolean isChild(Entity e) {
    return children.contains(e);
  }

  public ArrayList<Entity> getChildren() {
    return children;
  }

  public boolean hasChildren() {
    return !children.isEmpty();
  }

  public ArrayList<Entity> getAllChildren() {
    return getAllChildren(new ArrayList<>());
  }

  public ArrayList<Entity> getAllChildren(ArrayList<Entity> list) {
    for (Entity entity : getChildren()) {
      if (!list.contains(entity)) {
        list.add(entity);
      }

      if (entity.hasChildren()) {
        entity.getAllChildren(list);
      }
    }
    return list;
  }

  /**
   * Reset the children list to the input list.
   *
   * @param list input list
   */
  public void setChildren(ArrayList<Entity> list) {
    children.clear();
    for (Entity e : children) {
      this.addChild(e);
    }
  }

  public Vector getChildPosition() {
    return childPosition;
  }

  public void setChildPosition(Vector childPosition) {
    this.childPosition = childPosition;
  }

  public Vector getChildOffset() {
    return childOffset;
  }

  public void setChildOffset(Vector childOffset) {
    this.childOffset = childOffset;
  }

  public boolean hasGlobalPosition() {
    return globalPosition;
  }

  public void setGlobalPosition(boolean globalPosition) {
    this.globalPosition = globalPosition;
  }

  /**
   * Update the positions of all the children of the given entity.
   */
  public void updateChildrenPositions() {
  }

  public boolean blockInput(Entity player, Vector vector){
    return false;
  }

  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Physics Methods">

  /**
   * Get the force being applied to the entity.
   *
   * @return force
   */
  public Vector getForce() {
    return super.getForce().addi(constForce);
  }

  /**
   * Set the force being applied to the entity.
   *
   * @param nForce input force
   */
  public void setConstForce(Vector nForce) {
    this.constForce = nForce;
  }

  /**
   * Get the constant velocity of the entity.
   *
   * @return get the stored velocity
   */
  public Vector getConstVelocity() {
    return constVelocity;
  }

  /**
   * Sets the constant velocity of the entity.
   *
   * @param constVelocity velocity
   */
  public void setConstVelocity(Vector constVelocity) {
    this.constVelocity = constVelocity;
  }

  /**
   * Get the velocity of the entity.
   *
   * @return get the stored velocity
   */
  public Vector getVelocity() {
    Vector out = getLocalVelocity();

    if (hasParent()) {
      out.add(getParent().getVelocity());
    }

    return out;
  }

  /**
   * Get the velocity of the entity.
   *
   * @return get the stored velocity
   */
  public Vector getLocalVelocity() {
    return super.getLocalVelocity().addi(constVelocity);
  }


  /**
   * Get the stored bounding box.
   *
   * @return bounding box
   */
  public BoundingBox getBoundingBox(BasicEntity entity) {
    if (entity.isParent(this)) {
      return null;
    } else {
      return getBoundingBox();
    }
  }

  public Shape getShape(Entity entity) {
    if (entity.isParent(this)) {
      return null;
    } else {
      return super.getShape(entity);
    }
  }

  public void addPosition(Vector pos) {
    super.addPosition(pos);
  }

  public boolean hasShape(String str) {
    return shapeMap.containsKey(str);
  }

  public void addShape(String str, Shape shp) {
    shapeMap.put(str, shp);
  }

  public void addShape(HashMap<String, Shape> newShapes) {
    shapeMap.putAll(newShapes);
  }

  public Shape getShape(String str) {
    return shapeMap.get(str);
  }
  public HashMap<String, Shape> getShapeMap() {
    return new HashMap<>(shapeMap);
  }


  public void updateLShapes() {
  }

  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Visual/Render Methods">

  /**
   * Get the sprite of the entity.
   * <p>
   * Called when rendering the entity.
   *
   * @return sprite
   */
  public Sprite getSprite() {
    return sprite;
  }
//
//  /**
//   * Set the sprite of the entity using an entity.
//   *
//   * @param entity entity data to use
//   */
//  public void setSprite(Entity entity) {
//    setSprite(entity.getSprite());
//  }

  /**
   * Set the sprite of the entity.
   *
   * @param sprite sprite to set
   */
  public void setSprite(Sprite sprite) {
    if (sprite != null) {
      sprite.reset();
      sprite.setState(state);
    }

    this.sprite = sprite;
  }


  public void setShape(Shape shape) {
    if (shape != null) {
      shape.reset();
      shape.setState(state);
    }

    super.setShape(shape);
  }

  /**
   * Add a sprite to the existing sprite of the given entity.
   * <p>
   * Automatically converts the current sprite to a layered sprite and adds the input sprite. If the
   * entity does not have a sprite, create a new layered sprite containing the input sprite.
   *
   * @param so additional sprite
   */
  public void addSprite(Sprite so) {
    if (!hasSprite()) {
      setSprite(new LayeredSprite(null));
    } else if (!getSprite().isLayered()) {
      setSprite(new LayeredSprite(getSprite()));
    }
    ((LayeredSprite) getSprite()).addSprite(so);
  }

  /**
   * Has a valid sprite with valid data.
   *
   * @return true if the sprite is there and valid, false otherwise
   */
  public boolean hasSprite() {
    return sprite != null;
  }

  public boolean hasSpriteData() {
    return hasSprite() && sprite.hasData();
  }

  public void addLSprite(String lookup, Sprite so) {
    spriteMap.put(lookup, so);
  }

  public void addLSprite(HashMap<String, Sprite> newSprites) {
    spriteMap.putAll(newSprites);
  }

  public boolean hasLSprite(String lookup) {
    return spriteMap.containsKey(lookup);
  }

  public Sprite getLSprite(String lookup) {
    return spriteMap.get(lookup);
  }

  public void updateLSprites() {
  }

  public HashMap<String, Sprite> getLSpriteMap() {
    return spriteMap;
  }

  public boolean isFinishedState() {
    Sprite so = getSprite(); // get current sprite
    return so == null || so.atFinishPoint();
  }

  /**
   * Update the current sprite with a time difference.
   * <p>
   * Automatically transitions to the next sprite of the entity if it has one. Then, if the next
   * sprite isn't valid, we check if the current sprite has a linked sprite. If the current sprite
   * automatically transitions to it's linked sprite or the transition flag is set, it transitions
   * to the linked sprite.
   */
  public void updateSprite(float time) {
    Sprite sprite = getSprite();
    if (sprite != null) {
      sprite.update(time); // update the sprite with the current time difference.
    }
  }

  public void updateShape(float time) {
    Shape shape = getShape();
    if (shape != null) {
      shape.update(time);
    }
  }


  /**
   * Get the next sprite to load.
   *
   * @return next sprite
   */
  public Sprite getNextSprite() {
    return nextSprite;
  }

  /**
   * Set the next sprite to load.
   *
   * @param nextSprite next sprite
   */
  public void setNextSprite(Sprite nextSprite) {
    this.nextSprite = nextSprite;
  }

  /**
   * Does the entity have a next sprite and does it have data.
   *
   * @return true if the next sprite is valid, false otherwise
   */
  public boolean hasNextSprite() {
    return getNextSprite() != null && getNextSprite().hasData();
  }


  /**
   * Should the entity transition to the next sprite.
   *
   * @return true if the sprite should transition, false otherwise
   */
  public boolean hasTransitionSprite() {
    return transitionSprite;
  }

  /**
   * Set if the entity should transition to the next sprite.
   *
   * @param transitionSprite transition state
   */
  public void setTransitionSprite(boolean transitionSprite) {
    this.transitionSprite = transitionSprite;
  }

  /**
   * Naturally transition to the input sprite.
   *
   * Sets the input sprite to the next sprite.
   *
   * @param next next sprite
   */
  public void transitionTo(Sprite next) {
    setNextSprite(next);
    transitionSprite = true;
  }

  public Vector getSpriteShift() {
    return spriteShift;
  }

  public void setSpriteShift(Vector imageShift) {
    this.spriteShift = imageShift;
  }


  public RenderOrder getRenderOrder() {
    return renderOrder;
  }

  public void setRenderOrder(RenderOrder ro) {
    renderOrder = ro;
  }


  /**
   * Is the current entity visible.
   *
   * Signals the Rendering engine to draw the entity.
   *
   * @return true if its visible, false otherwise
   */
  public boolean isVisible() {
    return visible;
  }

  /**
   * Sets if the entity is visible.
   *
   * @param visible visibility
   */

  public void setVisible(boolean visible) {
    this.visible = visible;
  }


  public boolean hasFilter() {
    return filter != null;
  }

  public GLFilter getFilter() {
    return filter;
  }

  public void addFilter(GLFilter nFilt) {
    if (filter == null) {
      filter = nFilt;
    } else {
      filter.addChildFilter(nFilt);
    }
  }

  public void setFilter(GLFilter filter) {
    this.filter = filter;
  }

  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Interaction Methods">

  /**
   * Get the interaction event.
   *
   * The interaction event is triggered when interacting with the given entity.
   *
   * @return interaction event
   */
  public Event getInteractEvent() {
    return interactEvent;
  }

  /**
   * Set the interaction event.
   *
   * The interaction event is triggered when interacting with the given entity.
   *
   * @param interactEvent interaction event
   */
  public void setInteractEvent(Event interactEvent) {
    setInteractEvent(interactEvent, interactOverride);
  }

  /**
   * Set the interaction event and the override status of the given entity.
   *
   * The interaction event is triggered when interacting with the given entity.
   * If the override flag is set, when entities interact with this entity it overrides all other
   *  interactions that entity has gotten or will get.
   *
   * @param interactEvent interaction event
   * @param override override flag
   */
  public void setInteractEvent(Event interactEvent, boolean override) {
    this.interactEvent = interactEvent;
    this.interactOverride = override;
  }

  /**
   * Set the current entity override flag.
   *
   * Sets if the entity overrides other interactions.
   *
   * @return b interaction override flag
   */
  public void setInteractOverride(boolean b) {
    interactOverride = b;
  }

  public Event getUpdateEvent() {
    return updateEvent;
  }

  public void setUpdateEvent(Event updateEvent) {
    this.updateEvent = updateEvent;
  }

  /**
   * Does the current entity override interactions.
   *
   * @return true if the entity overrides other interactions, false otherwise
   */
  public boolean doesOverrideInteract() {
    return interactOverride;
  }


  /**
   * This method is called when a player interacts with the Entity If the method overwrites the
   * interact action it returns true
   */
  public boolean interact(Entity e) {
    if (interactEvent != null) {
      interactEvent.act(e, this);
      return true;
    }
    return false;
  }

  /**
   * Does the given entity have any loaded interactions.
   *
   * @return true if the entity has interactions, false otherwise
   */
  public boolean hasInteractions() {
    return !interactions.isEmpty();
  }

  /**
   * Add an interaction from another entity.
   *
   * Adds the input entity to the interaction list if interactions have not been overridden.
   *
   * @param e other entity
   */
  public boolean addInteraction(Entity e, ArrayList<Manifold> m) {
    this.addInteraction(e);
    return true;
  }

  /**
   * Add an interaction from another entity.
   *
   * Adds the input entity to the interaction list if interactions have not been overridden.
   *
   * @param e other entity
   */
  public void addInteraction(Entity e) {
    if (!overridden) { // if interactions have been overridden stop adding interactions
      if (e.doesOverrideInteract()) { // does the input entity override interactions
        interactions.clear(); // clear the interaction list
        overridden = true; // set the overridden flag
      }
      if (!interactions.contains(e)) {
        interactions.add(e); // add the entity to the interaction list
      }
    }
  }

  /**
   * Clear the interaction list.
   */
  public void clearInteractions() {
    interactions.clear();
  }

  /**
   * Apply all the interactions in the
   */
  public void applyInteractions() {
    weakIncrement = true;
    for (Entity e : interactions) {
      if (e.interact(this)) {
        weakIncrement = false;
      }
    }
    overridden = false;
  }

  public void processInteractions() {
    applyInteractions();
    clearInteractions();
  }

  /**
   * Get the weak increment flag.
   *
   * The weak increment flag is used to pass an increment to a world or container when nothing
   *  specific was selected.
   *
   * @return true if the entity preformed a weak increment, false otherwise
   */
  public boolean weakIncrement() {
    return weakIncrement;
  }

  // </editor-fold>

}
