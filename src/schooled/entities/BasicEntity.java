package schooled.entities;


import java.io.Serializable;
import java.util.ArrayList;
import schooled.Game;
import schooled.containers.EntityHolder;
import schooled.engines.Engine;
import schooled.physics.BoundingBox;
import schooled.physics.Manifold;
import schooled.physics.MassTree;
import schooled.physics.Shape;
import schooled.physics.Vector;
import schooled.visuals.GraphicsContext;

/**
 * A Entity object that stores the base data types required for all entities.
 * <p>
 * An attempt to organize Entity functions.
 * <p></p>
 * Program written by Joseph Straceski
 * <p>
 * contact: straceski.joseph@gmail.com, https://github.com/Crepox
 */
public class BasicEntity implements Serializable {

  //<editor-fold desc="BasicEntity Variables">
  private String customName = null; // general purpose name string (debug and otherwise)

  //<editor-fold desc="Physics Settings">
  private Vector position; // the entities position in the game
  private Shape shape; // the physical boundaries of the entity
  private float mass; // the mass of the entity

  private float invMass; // variable for storing inverse mass calculations
  private Vector velocity = new Vector(0, 0); // velocity of the entity
  private Vector force = new Vector(0, 0); // force vector being applied to the entity

  private float staticFriction = 1.0f;
  private float elasticConstant = 0.0f;

  private boolean hasPhysics = true;
  private boolean collides = true;
  private boolean cancelCollision = false;
  private boolean interactAll = false;
  //</editor-fold>

  //<editor-fold desc="Physics Registers">
  private float fInvMass;

  private Vector shift = new Vector(0, 0); // shift vector being applied to the entity
  public Vector push = new Vector(0, 0); // shift vector being applied to the entity

  public ArrayList<Vector> inNormal = new ArrayList<>();
  public ArrayList<Vector> outNormal = new ArrayList<>();
  public ArrayList<Float> weights = new ArrayList<>();
  public ArrayList<Vector> mShift = new ArrayList<>();
  public ArrayList<Vector> pNormal = new ArrayList<>();
  public ArrayList<Boolean> active = new ArrayList<>();
  public ArrayList<MassTree> lookup = new ArrayList<>();
  public Vector lastInNormal = null;
  public ArrayList<Vector> zeros = new ArrayList<>();

  public int hitIndex = 0;
  public Vector collector = new Vector(0, 0);
  //</editor-fold>

  //</editor-fold>

  //<editor-fold desc="Debug">
  /**
   * Get the name of the entity.
   * <p>
   * If the entity has a custom name use that, if it doesn't, use the classname.
   *
   * @return name string
   */
  public String getName() {
    if (customName == null) {
      return getClass().getSimpleName();
    } else {
      return customName;
    }
  }

  /**
   * Give the entity a custom name.
   *
   * @param s custom name
   */
  public void setName(String s) {
    customName = s;
  }

  /**
   * This returns the entity name if it exists, otherwise return the java id.
   *
   * @return string name
   */
  @Override
  public String toString() {
    if (customName != null && !customName.isEmpty()) {
      return this.getClass().getSimpleName() + "[" + customName + "]";
    }
    return this.getClass().getSimpleName();
  }
  //</editor-fold>

  //<editor-fold desc="Constructors">
  /**
   * The most basic constructor for a basic entity.
   *
   * Sets the position to (0,0), mass to 0 and shape to null.
   *
   */
  public BasicEntity() {
    this(Vector.zero.clone(), null, 0);
  }

  /**
   * Constructor for a fully-formed basic entity.
   *
   * @param v the position of the entity
   * @param s the shape of the entity
   * @param f the mass of the entity
   */
  public BasicEntity(Vector v, Shape s, float f) {
    setPosition(v);
    setShape(s);
    setMass(f);
  }

  /**
   * Constructor for a fully-formed basic entity.
   *
   * @param v the position of the entity
   * @param s the shape of the entity
   * @param f the mass of the entity
   */
  public BasicEntity(String name, Vector v, Shape s, float f) {
    setName(name);
    setPosition(v);
    setShape(s);
    setMass(f);
  }
  //</editor-fold>

  //<editor-fold desc="Cloning">
  public BasicEntity setClone(BasicEntity entity) {
    entity.setPosition(position == null ? null : position.clone());
    entity.setShape(shape == null ? null : shape.clone());
    entity.setMass(mass);

    entity.setVelocity(velocity.clone());
    entity.setForce(force.clone());
    entity.setShift(shift.clone());

    entity.setCollides(collides);
    entity.setPhysics(hasPhysics);

    entity.setName(customName);

    entity.cancelCollision = cancelCollision;
    return entity;
  }

  /**
   * Clone a basic entity including all contained objects.
   *
   * @return a clone of the basic entity
   */
  public BasicEntity clone() {
    BasicEntity basicEntity = new BasicEntity();
    return setClone(basicEntity);
  }
  //</editor-fold>

  //<editor-fold desc="Base Physics">
  /**
   * Get the position of the entity.
   *
   * @return position
   */
  public Vector getPosition() {
    return position;
  }

  /**
   * Get the position of the entity.
   *
   * (Shorthand)
   *
   * @return position
   */
  public Vector getPos() {
    return getPosition();
  }

  /**
   * Set the position of the entity.
   *
   * @param pos entity position
   */
  public void setPosition(Vector pos) {
    if (pos == null) {
      this.position = new Vector(0, 0);
    } else {
      this.position = pos;
    }
  }

  /**
   * Set the position of the entity.
   *
   * @param pos entity position
   */
  public void setPos(Vector pos) {
    this.setPosition(pos);
  }

  /**
   * Add to the position of the entity.
   *
   * @param pos entity position offset
   */
  public void addPosition(Vector pos) {
    if (pos != null) {
      this.position.add(pos);
    }
  }

  /**
   * Add to the position of the entity.
   *
   * @param pos entity position offset
   */
  public void addPos(Vector pos) {
    this.addPosition(pos);
  }


  public Shape getShape(BasicEntity entity) {
    return getShape();
  }

  /**
   * Get the shape of the entity.
   *
   * @return shape of the entity
   */
  public Shape getShape() {
    return shape;
  }

  /**
   * Sets the shape of the entity.
   * <p>
   * Pre-calculates a bounding box from the shape as well.
   *
   * @param shape shape of the entity
   */
  public void setShape(Shape shape) {
    this.shape = shape;
  }


  /**
   * Get the stored bounding box.
   *
   * @return bounding box
   */
  public BoundingBox getBoundingBox() {
    Shape shape = getShape();

    if (shape != null) {
      return shape.getBoundingBox();
    }

    return null;
  }

  /**
   * Get the stored bounding box.
   *
   * @return bounding box
   */
  public BoundingBox getBB() {
    return getBoundingBox();
  }

  /**
   * Get the stored bounding box.
   *
   * @return bounding box
   */
  public BoundingBox getBoundingBox(BasicEntity entity) {
    Shape shape = getShape(entity);

    if (shape != null) {
      return shape.getBoundingBox();
    }

    return null;
  }


  /**
   * Get the mass of the entity.
   *
   * @return entity's mass
   */
  public float getMass() {
    return mass;
  }

  /**
   * Sets the mass of the Entity.
   * <p>
   * Used in physics calculations. Pre-calculates the inverse mass too.
   *
   * @param mass mass in kg
   */
  public void setMass(float mass) {
    if (mass < 0)  {
      setPhysics(false);
    } else {
      setPhysics(true);
    }
    this.mass = mass;
    invMass = mass <= 0.0f ? 0.0f : 1 / mass;
  }

  /**
   * Get the stored inverse mass of the entity.
   * <p>
   * If the mess is 0 the inverse mass is also 0.
   *
   * @return inverse mass
   */
  public float getInvMass() {
    return invMass;
  }


  /**
   * Gets the velocity of the entity.
   *
   * @return the velocity
   */
  public Vector getVelocity() {
    return getLocalVelocity();
  }

  /**
   * Sets the velocity of the entity.
   *
   * @param velocity velocity
   */
  public void setVelocity(Vector velocity) {
    this.velocity = velocity;
  }

  /**
   * Sets add to the velocity of the entity.
   *
   * @param velocity velocity
   */
  public void addVelocity(Vector velocity) {
    this.velocity.add(velocity);
  }

  public void scaleVelocity(float scale) {
    velocity.scale(scale);
  }

  /**
   * Get the velocity of the entity.
   *
   * @return get the stored velocity
   */
  public Vector getLocalVelocity() {
    return velocity;
  }

  public float getAirDrag() {
    return Engine.AIR_DRAG;
  }


  public float getStaticFriction() {
    return staticFriction;
  }

  public void setStaticFriction(float staticFriction) {
    this.staticFriction = staticFriction;
  }

  public float getElasticConstant() {
    return elasticConstant;
  }

  //</editor-fold>

  //<editor-fold desc="Physics Register Manipulation">
  /**
   * Get the force being applied to the entity.
   *
   * @return force
   */
  public Vector getForce() {
    return this.force;
  }

  /**
   * Set the force being applied to the entity.
   *
   * @param nForce input force
   */
  public void setForce(Vector nForce) {
    this.force = nForce;
  }

  /**
   * Add a force to the stored force.
   *
   * @param nForce force added
   */
  public void addForce(Vector nForce) {
    this.force.add(nForce);
  }


  /**
   * Get the stored shifting value.
   * <p>
   * Used in physics engine to solve shift calculations.
   *
   * @return shift value
   */
  public Vector getShift() {
    return shift;
  }

  /**
   * Set the shifting value.
   * <p>
   * Used in physics engine to solve shift calculations.
   *
   * @param shift shift value
   */
  public void addShift(Vector shift) {
    this.shift.add(shift);
  }

  /**
   * Set the shifting value.
   * <p>
   * Used in physics engine to solve shift calculations.
   *
   * @param shift shift value
   */
  public void setShift(Vector shift) {
    this.shift = shift;
  }


  public void setFInvMass(float f) {
    fInvMass = f;
  }

  public float getFInvMass() {
    return invMass;
  }
  //</editor-fold>

  //<editor-fold desc="Game/Collision Logic">


  public boolean isInteractAll() {
    return interactAll;
  }

  public void setInteractAll(boolean interactAll) {
    this.interactAll = interactAll;
  }

  /**
   * Does the entity processes collisions with other entities.
   *
   * @return true if the entity can collide with other entities, false otherwise
   */
  public boolean isCollides() {
    return collides;
  }

  /**
   * Set the collision processing state of the entity.
   * <p>
   * When passed through a physics engine this flag is used to let the engine know if the entity is
   * calculates collisions with other entities.
   *
   * @param collides physics processing state
   */
  public void setCollides(boolean collides) {
    this.collides = collides;
  }

  /**
   * Check the cancel collision flag and reset it.
   *
   * @return previous state of the cancel collision flag
   */
  public boolean checkCollisionCancel() {
    boolean temp = cancelCollision;
    cancelCollision = false;
    return temp;
  }

  /**
   * Set the collision cancellation flag to true.
   */
  public void cancelCollision() {
    this.cancelCollision = true;
  }

  /**
   * Set the collision cancellation flag to the input value.
   * <p>
   * If the cancel collision flag is set to true, the physics engine will retroactively cancel a
   * collision. This can be used in the collision function to cancel a collision after it has been
   * triggered.
   *
   * @param cancelCollision collision flag
   */
  public void cancelCollision(boolean cancelCollision) {
    this.cancelCollision = cancelCollision;
  }

  /**
   * This method is called when another entity collides with this Entity
   *
   * @param e other entity
   * @param m collision manifold
   */
  public boolean collision(BasicEntity e, ArrayList<Manifold> m) {
    return this.collision(e);
  }

  /**
   * This method is called when another entity collides with this Entity
   *
   * @param e other entity
   */
  public boolean collision(BasicEntity e) {
    return true;
  }

  /**
   * Generate a selection in the input entity holder.
   *
   * @param c entity holder
   */
  public void processSelection(EntityHolder c) {
  }

  /**
   * Does the entity process physics interactions.
   *
   * @return true if the entity processes physics, false otherwise
   */
  public boolean hasPhysics() {
    return hasPhysics;
  }

  /**
   * Set the physics processing state of the entity.
   * <p>
   * When passed through a physics engine this flag is used to let the engine know if the entity is
   * used in physics processing.
   *
   * @param hasPhysics physics processing state
   */
  public void setPhysics(boolean hasPhysics) {
    this.hasPhysics = hasPhysics;
  }

  public boolean isParent(BasicEntity entity) {
    return false;
  }
  //</editor-fold>

  //<editor-fold desc="Cycle Update Methods">
  /**
   * Do the update cycle using a time difference.
   *
   * @param t time difference
   */
  public void updateCycle(float t) {

  }

  /**
   * Update the position of the given entity.
   * <p>
   * Using the velocity of the entity and a time delta, update the position of the entity.
   *
   * @param t time delta
   */
  public void moveCycle(float t) {
    addPosition(getLocalVelocity().scalei(t * 60));
  }
  //</editor-fold>


  //<editor-fold desc="Rendering Methods">
  public void renderHook(Object gc, Vector gShift, float gScale) { }
  //</editor-fold>
}
