package schooled.engines;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import schooled.Game;
import schooled.containers.EntityHolder;
import schooled.datatypes.Tuple;
import schooled.entities.BasicEntity;
import schooled.entities.Entity;
import schooled.menu.Menu;
import schooled.physics.BoundingBox;
import schooled.physics.Circle;
import schooled.physics.Manifold;
import schooled.physics.MassTree;
import schooled.physics.MultiShape;
import schooled.physics.PolygonShape;
import schooled.physics.Shape;
import schooled.physics.Vector;

/**
 * Physics engine.
 * <p>
 * Process a list of Entity states and preform reactions.
 * <p></p>
 * Program written by Joseph Straceski
 * <p>
 * contact: straceski.joseph@gmail.com, https://github.com/Crepox
 */
public class Engine {

  // <editor-fold defaultstate="collapsed" desc="Global Physics Variables">

  // Error variables
  static float POS_ERROR_MARGIN = 0.001f; // minimum distance to fix positions in collisions
  static float ERROR_MARGIN = 0.1f; // minimum velocity difference that activates a collision
  static float FIX_SCALAR = 0.98f; // value manifold penetration distance is scaled by
  static float MAX_HITS = 10f; // value manifold penetration distance is scaled by

  //Restrictive variables
  public static float AIR_DRAG = 0.89f; // air drag scalar
  static float STATIC_FRICTION_MAX = 1f;
  static float STATIC_FRICTION_SLOPE = 1.6180339887f; //golden ratio baby
  static float STATIC_FRICTION_VEL_CUTOFF = 2f;

  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Bounding Box">

  /**
   * Do the bounding boxes of entity a and entity b collide.
   *
   * @param a entity a
   * @param b entity b
   * @return if the bounding boxes collide return true otherwise return false
   */
  public static boolean boundingBoxCollision(Entity a, Entity b) {
    return boundingBoxCollision(a.getPosition(), a.getBoundingBox(),
        b.getPosition(), b.getBoundingBox());
  }

  /**
   * Do the bounding boxes of entity a and entity b collide.
   *
   * @param a entity a
   * @param b entity b
   * @return if the bounding boxes collide return true otherwise return false
   */
  public static boolean boundingBoxCollision(Vector posA, BoundingBox a,
      Vector posB, BoundingBox b) {
    if (a == null || b == null) {
      return false;
    }

    // get the maximum and minimum point of the entity a's bounding box

    Logger.pushDebugTime("engine_bbc2");

    float maxXA = posA.getX() + a.xMax;
    float maxYA = posA.getY() + a.yMax;

    float minXA = posA.getX() + a.xMin;
    float minYA = posA.getY() + a.yMin;

    // get the maximum and minimum point of the entity b's bounding box

    float maxXB = posB.getX() + b.xMax;
    float maxYB = posB.getY() + b.yMax;

    float minXB = posB.getX() + b.xMin;
    float minYB = posB.getY() + b.yMin;

    Logger.pushDebugTime("engine_bbc2");
    Logger.pushDebugTime("engine_bbc3");

    boolean flag = maxXA > minXB && maxYA > minYB && maxXB > minXA && maxYB > minYA;
    Logger.pushDebugTime("engine_bbc3");

    // if the max is greater than the min in all cases the bounding boxes collide
    return flag;
  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Main Processing">

  /**
   * Process the physical interactions of an entity list. Find when entities collide, calculate
   * collision manifolds, solve the manifolds generating velocities and position shifts.
   *
   * @param entities list of colliding entities
   * @param t        time difference during the processing tick
   */
  public static void process(ArrayList<BasicEntity> entities, float t) {
    // do entity pre-processing

    Logger.pushDebugTime("engineUpdate");
    for (int a = 0; a < entities.size(); a++) {
      BasicEntity e = entities.get(a);
      float scalar = Math.max(0.0f, 1.0f - (1.0f - e.getAirDrag()) * t * 60);
      // calculate the air drag in context of the time difference
      e.scaleVelocity(scalar); // scale all the velocities by air drag
    }
    Logger.pushDebugTime("engineUpdate");

    Logger.pushDebugTime("engineSearch");
    // get manifolds
    ArrayList<Manifold> manifolds = findManifolds(entities);
    Logger.pushDebugTime("engineSearch");
    Logger.pushDebugTime("engineLookup");
    // create a lookup table for manifolds indexed by entities
    HashMap<BasicEntity, ArrayList<Manifold>> manifoldLookup = generateManifoldLookup(manifolds);
    Logger.pushDebugTime("engineLookup");

    // create forces and positional shifts from the collision manifolds
    parseManifold2(manifolds, manifoldLookup);

    Logger.pushDebugTime("engineApply");
    // apply the calculated forces
    for (int a = 0; a < entities.size(); a++) {
      BasicEntity entity = entities.get(a);
      entity.addVelocity(entity.getForce().scalei(entity.getInvMass())); // apply velocity
      entity.addPosition(entity.getShift());
      entity.addVelocity(entity.push);
      Engine.clearAll(entity);
      entity.setShift(Vector.zero.clone());
      entity.setForce(Vector.zero.clone()); // zero out the force
      entities.get(a).moveCycle(t); // apply the velocities to the position
    }
    Logger.pushDebugTime("engineApply");
  }

  /**
   * Process menu collisions.
   *
   * @param m Menu container
   * @param t time difference
   */
  static public void updateAndProcess(EntityHolder m, float t) {
    m.updateCycle(t);
    process(new ArrayList<>(m.getEntities()), t);
    m.processInteractions();
  }

  /**
   * Process menu collisions.
   *
   * @param m Menu container
   * @param gameEntity (game selector entity)
   * @param t time difference
   */
  static public void processMenu(Menu m, Entity gameEntity, float t) {
    ArrayList<BasicEntity> entities = new ArrayList<>(m.getEntities());
    if (gameEntity != null) {
      entities.add(gameEntity);
      entities.addAll(gameEntity.getChildren());
    }
    process(entities, t);
  }

  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Manifold Processing">

  /**
   * Generate a manifold lookup table. Produces a hashmap indexed by BasicEntitiy, contains all of
   * the collision manifold containing the given BasicEntity.
   *
   * @param manifolds list of collision manifolds
   * @return lookup HashMap
   */
  public static HashMap<BasicEntity, ArrayList<Manifold>> generateManifoldLookup(
      ArrayList<Manifold> manifolds) {

    HashMap<BasicEntity, ArrayList<Manifold>> manifoldLookup = new HashMap<>();

    for (Manifold manifold : manifolds) {
      BasicEntity entity_a = manifold.a;
      BasicEntity entity_b = manifold.b;

      // create lookup tables for the entities (need to make this faster)
      if (!manifoldLookup.containsKey(entity_a)) {
        manifoldLookup.put(entity_a, new ArrayList<>());
      }
      manifoldLookup.get(entity_a).add(manifold);

      if (!manifoldLookup.containsKey(entity_b)) {
        manifoldLookup.put(entity_b, new ArrayList<>());
      }
      manifoldLookup.get(entity_b).add(manifold);
    }

    return manifoldLookup;
  }

  /**
   * Generate a list of collision manifolds from a list of entities.
   * <p>
   * Automatically filters invalid collision manifolds canceled by internal or physical means. Uses
   * {@link #processCollision(BasicEntity, BasicEntity, ArrayList) processCollision} to filter out
   * logical interactions based on entity specific flags.
   *
   * @param entities BasicEntity list
   * @return list of valid collision manifolds
   */
  public static ArrayList<Manifold> findManifolds(ArrayList<BasicEntity> entities) {
    ArrayList<Manifold> manifolds = new ArrayList<>();

    // calculate manifolds from entity positions and shapes
    for (int a = 0; a < entities.size(); a++) {
      BasicEntity entity_a = entities.get(a);

      if (!entity_a.isCollides()) { // does entity a collide if not skip calculations
        // DO NOT PUT CODE HERE UNLESS YOU ARE ABSOLUTELY SURE
        // It probably does not belong here
        continue;
      }

      for (int b = (a + 1); b < entities.size(); b++) {
        BasicEntity entity_b = entities.get(b);

        if (!entity_b.isCollides()) { // does entity b collide if not skip calculations
          // DO NOT PUT CODE HERE UNLESS YOU ARE ABSOLUTELY SURE
          // It probably does not belong here
          continue;
        }


        if (entity_a.getInvMass() == 0 && entity_b.getInvMass() == 0
            && !entity_a.isInteractAll() && !entity_b.isInteractAll()) {
          // DO NOT PUT CODE HERE UNLESS YOU ARE ABSOLUTELY SURE
          // It probably does not belong here
          continue;
        }

        Logger.pushDebugTime("engine_bl");
        Logger.pushDebugTime("engine_bl");

        Logger.pushDebugTime("engine_bbc");



        BoundingBox bba = entity_a.getBoundingBox(entity_b);
        BoundingBox bbb = entity_b.getBoundingBox(entity_a);


        // if the entities bounding boxes' do not collide, skip more heavy calculations
        if (!boundingBoxCollision(entity_a.getPosition(), bba, entity_b.getPosition(), bbb)) {
          // DO NOT PUT CODE HERE UNLESS YOU ARE ABSOLUTELY SURE.
          // It probably does not belong here

          Logger.pushDebugTime("engine_bbc");
          continue;
        }

        Logger.pushDebugTime("engine_bbc");

        Logger.pushDebugTime("engine_shapes_get");

        // could extrapolate parent checking
        Shape shapeA = entity_a.getShape(entity_b);
        Shape shapeB = entity_b.getShape(entity_a);

        Logger.pushDebugTime("engine_shapes_get");

        if (shapeA == null || shapeB == null) {
          continue;
        }

        Logger.pushDebugTime("engine_routing");
        // detect collisions between the two entities
        // Returns any calculated collisions in a list
        ArrayList<Manifold> list = routeEntityShapes(
            entity_a, shapeA, entity_a.getPosition(),
            entity_b, shapeB, entity_b.getPosition());

        Logger.pushDebugTime("engine_routing");

        list.removeIf(Objects::isNull);

        if (list.isEmpty() || processCollision(entity_a, entity_b, list)) {
          continue;
        }

        manifolds.addAll(list); // add manifolds to master list
      }
    }

    return manifolds;
  }

  /**
   * Process a collision between entities. Return false if the collision is valid.
   *
   * @param a         entity a
   * @param b         entity b
   * @param manifolds manifold list
   * @return false if the collision should continue, true otherwise
   */
  public static boolean processCollision(BasicEntity a, BasicEntity b,
      ArrayList<Manifold> manifolds) {
    boolean cancelA = a.collision(b, manifolds);
    boolean cancelB = b.collision(a, manifolds);

    // check if the entities are canceling physical collisions
    if (!cancelA || !cancelB) {
      return true;
    }

    // check if the entities are canceling physical collisions
    if (a.checkCollisionCancel() || b.checkCollisionCancel()) {
      return true;
    }

    // if both entities are physically static cancel the physical processing of the manifold
    if (a.getInvMass() == 0 && b.getInvMass() == 0) {
      return true;
    }

    // check if the entities have physics enabled or not
    return !b.hasPhysics() || !a.hasPhysics();
  }

  /**
   * Generate and apply forces and shifts using collision reaction physics.
   * <br> <br>
   * Uses {@link #collectMass(BasicEntity, HashMap, Vector) collectMass} to generate a collision
   * tree. The function then uses this tree to calculate the total mass of a given entity with all
   * physically connected masses included. This means that all entities that will be affected by the
   * starting entity traveling along a given collision normal are collected and added. This function
   * uses the collective mass to create a basic normal collision and propagates the effect down the
   * collision trees.
   * <p>
   * version: 2
   *
   * @param manifolds list of collision manifolds
   * @param mLookup   manifold lookup table for calculating branching collisions
   */
  public static void parseManifold2(ArrayList<Manifold> manifolds,
      HashMap<BasicEntity, ArrayList<Manifold>> mLookup) {
    HashMap<BasicEntity, ArrayList<MassTree>> master = new HashMap<>();

    float factor = 100f;
//    Game.log("-----");

    Logger.pushDebugTime("engine_inital");
    for (BasicEntity entity : mLookup.keySet()) {
      Vector vel = entity.getVelocity();
      if (entity.getInvMass() != 0 && vel.magSqr() > 0) {
        Vector v = vel.normalizei();

        Engine.clearShifts(mLookup.keySet());
        MassTree bData = Engine.collectMass(master, mLookup, entity, v, false);

        float relMass = -1;
        if (entity.getFInvMass() > 0 && bData.iMass > 0) {
          relMass = bData.iMass / entity.getFInvMass();
        }

        float elasticConstant = entity.getElasticConstant();

        if ((bData.iMass == 0.0f || (relMass > factor)) && bData.rNormal != null && bData.rNormal.dot(vel) < 0) {
          entity.addVelocity(v.scalei((1.0f + elasticConstant) * bData.rNormal.dot(vel)));
        } else if (bData.iMass == 0.0f) {
          entity.setVelocity(vel.scalei(-elasticConstant));
        }
      }
    }
    Logger.pushDebugTime("engine_inital");
//    Game.log(mLookup);

    Logger.pushDebugTime("engine_manifolds");
    for (Manifold m : manifolds) { // loop through the manifolds
      // -----
      // Shared Calculations
      // -----

      BasicEntity A = m.a;
      BasicEntity B = m.b;

      // calculate the relative velocities
      Vector relativeVel = Vector.sub(B.getVelocity(), A.getVelocity());
      // store a normalized version of the relative vector
      Vector relNormal = relativeVel.normalizei();

      // convert the list of normal vectors into a single normal describing the collision
      //  (distributionVector relates the collision normals to a vector that is within the
      //   collision angle created by the m.normals vectors)
      // the resVector is the distribution response vector going in the same direction as the
      //  relative velocity
      Vector resVector = distributionVector(relNormal, m.normals, 1);

      // the contact velocity is the relative velocity of the collision in the direction of the
      //  maximum contact normal (or just contact normal)
      float contactVelocity = Vector.dot(resVector, relativeVel);

      // calculate the total separation vector from the manifold's individual separation vectors
      Vector svnorm = new Vector(0, 0);
      float maxSqrLength = 0;

      // loop through the separation vectors
      for (Vector v : m.vectors) {
        // find the separation vector with the maximum squared length
        maxSqrLength = Math.max(maxSqrLength, v.magSqr());
      }

      if (maxSqrLength > POS_ERROR_MARGIN || contactVelocity > 0) {

        for (Vector v : m.normals) {
          svnorm.add(v);
        }

        // use the size of the separation vector list to calculate the average vector
        svnorm = svnorm.normalizei();


        Logger.pushDebugTime("engine_collectMass");
        Engine.clearShifts(mLookup.keySet());
        MassTree aData = Engine.collectMass(master, mLookup, A, svnorm, false);

        Engine.clearShifts(mLookup.keySet());
        MassTree bData = Engine.collectMass(master, mLookup, B, svnorm.scalei(-1), false);
        Logger.pushDebugTime("engine_collectMass");

        // the total inverse mass of the collision
        float totalInvMass = aData.iMass + bData.iMass;


        float relMass = -1;
        if (aData.iMass > 0 && bData.iMass > 0) {
          relMass = aData.iMass / bData.iMass;
        }

        float elasticConstant = Math.max(A.getElasticConstant(), B.getElasticConstant());

        // the contact scalar is multiplied by a damping value where 1.0 is represented by a
        //  perfectly inelastic collision and 2.0 represents a completely elastic collision
        float contactScalar = Math.abs(-(1.0f + elasticConstant) * contactVelocity);

        // --------------------
        // Shift Calculations
        // --------------------

        // if the maximum squared length is greater than the minimum position margin
        //  calculate the separation vector
        if (maxSqrLength > POS_ERROR_MARGIN) {
          float maxLength = (float) Math.sqrt(maxSqrLength);

          if (aData.iMass > 0 && bData.iMass > 0 && (relMass > 1.0f/factor && relMass < factor)) {
            Engine.shiftEntities(aData, maxLength * aData.iMass / totalInvMass * FIX_SCALAR);
//            Game.log("~~~~~~~~~");
            Engine.shiftEntities(bData, maxLength * bData.iMass / totalInvMass * FIX_SCALAR);
          } else if ((aData.iMass > 0 && relMass == -1f) || (relMass > factor)) {
            Engine.shiftEntities(aData, maxLength * FIX_SCALAR);
          } else if ((bData.iMass > 0 && relMass == -1f) || (relMass > 0 && relMass < 1.0f/factor)) {
            Engine.shiftEntities(bData, maxLength * FIX_SCALAR);
          }
        }

        // --------------------
        // Velocity Calculations
        // --------------------

        if (contactVelocity > 0) {
          if (aData.iMass > 0 && bData.iMass > 0 && (relMass > 1.0f/factor && relMass < factor)) {
            Engine.forceEntities(aData, resVector, contactScalar * aData.iMass / totalInvMass);
            Engine.forceEntities(bData, resVector.scalei(-1), contactScalar * bData.iMass / totalInvMass);
          } else if ((aData.iMass > 0 && relMass == -1f) || (relMass > factor)) {
            Engine.forceEntities(aData, resVector, contactScalar);
          } else if ((bData.iMass > 0 && relMass == -1f) || (relMass > 0 && relMass < 1.0f/factor)) {
            Engine.forceEntities(bData, resVector.scalei(-1), contactScalar);
          }
        }
      }
    }

    Logger.pushDebugTime("engine_manifolds");
  }

  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Collision Hooks">

  /**
   * Do the two Entities collide.
   * <p>
   * Uses each entity to route the other entity's shape.
   *
   * @param a entity a
   * @param b entity b
   * @return true if the entities collide, false otherwise.
   */
  public static boolean collides(Entity a, Entity b) {
    return collides(a.getShape(b), a.getPosition(), b.getShape(a), b.getPosition());
  }

  /**
   * Does the entity, e, collide with the shape, s, at position, v.
   * <p>
   * Uses each entity to route the other entity's shape.
   *
   * @param s shape s
   * @param v position v
   * @param e entity e
   * @return true if there is a collision, false otherwise.
   */
  public static boolean collides(Shape s, Vector v, Entity e) {
    return collides(s, v, e.getShape(), e.getPosition());
  }

  /**
   * Does the shape, s1, at position, v1, collide with the shape, s2, at position, v2.
   * <p>
   * Uses each entity to route the other entity's shape.
   *
   * @param s1 shape s1
   * @param v1 position v1
   * @param s2 shape s2
   * @param v2 position v2
   * @return true if there is a collision, false otherwise.
   */
  public static boolean collides(Shape s1, Vector v1, Shape s2, Vector v2) {
    if (s1 == null || v1 == null || s2 == null || v2 == null) {
      return false;
    }

    ArrayList<Manifold> list = routeEntityShapes(null, s1, v1, null, s2, v2);
    list.removeIf(Objects::isNull);
    return !list.isEmpty();
  }

  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Vector Math">

  /**
   * Find the vector perpendicular to the input v that is pointed in the direction n.
   *
   * @param v input vector
   * @param d pointing vector
   * @return the perpendicular vector
   */
  public static Vector pointedPVector(Vector v, Vector d) {
    Vector cross = v.perpi();
    if (Vector.dot(cross, d) > 0) {
      return cross;
    } else {
      return cross.scalei(-1);
    }
  }

  /**
   * Find if a vector n is inside unit vectors h, m, l.
   * <br>
   * Is n inside the <=60 deg angle created by unit vectors h and m or vectors m and l using
   * pre-generated dot products.
   * <p>
   * This includes vectors that are equal to the range-defining vectors h, m, and l.
   *
   * @param hlDot dot product of vector h and vector l
   * @param hmDot h dot m
   * @param lmDot l dot m
   * @param nhDot n dot h
   * @param nmDot n dot m
   * @param nlDot n dot l
   * @return if the vector n is within either vector range return true, otherwise return false
   */
  @SuppressWarnings("Duplicates")
  public static boolean inVectorDot(float hlDot, float hmDot, float lmDot,
      float nhDot, float nmDot, float nlDot) {
    return (hlDot >= 0 && hlDot < nhDot && hlDot < nlDot)
        || (nmDot > 0 && hmDot < nmDot && hmDot < nhDot)
        || (nmDot > 0 && lmDot < nmDot && lmDot < nlDot)
        || (nmDot > 0 && nmDot > nlDot && nmDot > nhDot);
  }

  /**
   * Detects if the line segments created by points a and b intersects with the line segment created
   * by points c and d. Excludes the endpoints.
   *
   * @param a point a
   * @param b point b
   * @param c point c
   * @param d point d
   * @return true if the collide false if otherwise
   */
  public static boolean interiorLineLineCollision(Vector a, Vector b, Vector c, Vector d) {
    // create relative direction vectors of the two line segments
    Vector a2b = Vector.sub(b, a);
    Vector c2d = Vector.sub(d, c);

    // calculate the vectors from the second line segment start to the first line segment start
    //  and calculate the vector from the second line segment end to the first line segment end
    Vector c2a = Vector.sub(a, c);
    Vector d2b = Vector.sub(b, d);

    // calculate the perpendicular lines to the original line segments (+ 90 deg)
    Vector p1 = a2b.perpi();
    Vector p2 = c2d.perpi();

    // calculate relative direction values

    // this is a multi step equation
    // Dot a refers to the first Dot product and dot b refers to the second dot product
    // The dot product gives a value that is relative to the angle and distance that two vectors might share
    // A higher dot product means that either the angle of the two vectors on the unit circle is closer or the
    // length that vector is going is higher.
    //
    // If the ca dot with p1 is greater than the cd dot with p1 then the three lines ca, cd, and ab
    // must form an enclosed triangle.
    // The length of p1 is proportional to ab.
    // Then p1 effectively creates a measuring vector to dot ca and cd to, to measure if the vector ca goes in enough of
    // the direction of the p1 normal to separate the vector cd from ab.
    // If either dots are negative that means that vector ab cd form two almost parallel lines that do not intersect
    // the same rules apply to ha and hb.

    float ha = Vector.dot(c2a, p1) / Vector.dot(c2d, p1);
    float hb = Vector.dot(d2b, p2) / Vector.dot(a2b, p2);

    return ha > 0.0 && ha < 1 && hb > 0.0 && hb < 1;
  }

  /**
   * Detects if the line segments created by points a and b intersects with the line segment created
   * by points c and d. Including endpoints.
   *
   * @param a point a
   * @param b point b
   * @param c point c
   * @param d point d
   * @return true if the collide false if otherwise
   */
  public static boolean lineLineCollision(Vector a, Vector b, Vector c, Vector d) {
    // create relative direction vectors of the two line segments
    Vector a2b = Vector.sub(b, a);
    Vector c2d = Vector.sub(d, c);

    // calculate the vectors from the second line segment start to the first line segment start
    //  and calculate the vector from the second line segment end to the first line segment end
    Vector c2a = Vector.sub(a, c);
    Vector d2b = Vector.sub(b, d);

    // calculate the perpendicular lines to the original line segments (+ 90 deg)
    Vector p1 = a2b.perpi();
    Vector p2 = c2d.perpi();

    // calculate relative direction values

    // this is a multi step equation
    // Dot a refers to the first Dot product and dot b refers to the second dot product
    // The dot product gives a value that is relative to the angle and distance that two vectors might share
    // A higher dot product means that either the angle of the two vectors on the unit circle is closer or the
    // length that vector is going is higher.
    //
    // If the ca dot with p1 is greater than the cd dot with p1 then the three lines ca, cd, and ab
    // must form an enclosed triangle.
    // The length of p1 is proportional to ab.
    // Then p1 effectively creates a measuring vector to dot ca and cd to, to measure if the vector ca goes in enough of
    // the direction of the p1 normal to separate the vector cd from ab.
    // If either dots are negative that means that vector ab cd form two almost parallel lines that do not intersect
    // the same rules apply to ha and hb.

    float ha = Vector.dot(c2a, p1) / Vector.dot(c2d, p1);
    float hb = Vector.dot(d2b, p2) / Vector.dot(a2b, p2);

    return ha >= 0.0 && ha <= 1 && hb >= 0.0 && hb <= 1;
  }

  /**
   * Find the point on line segment ab that is closest to point v.
   *
   * @param v point v
   * @param a vertex a
   * @param b vertex b
   * @return point in Vector form
   */
  public static Vector pnt2line(Vector v, Vector a, Vector b) {
    // calculate the vector from a to b
    Vector p = Vector.sub(b, a);
    // calculate the vector from a to v
    Vector p2 = Vector.sub(v, a);

    // Squared distance of line ab
    float sqrDist = Vector.magSqr(p);

    // calculate the dot of a to v with a to b
    float u = Vector.dot(p, p2) / sqrDist;

    // The beauty of using the squared distance is that the dot output can be broken down as
    //  the dot product of the normalized vector ab with the vector av divided by the length of ab.
    // The first part can be broken down as the point v casing a shadow on the line ab and the distance of
    //  the shadow tip to a is the output.
    // Then we take the shadow distance and divide it by the length of ab again and this value, if greater than
    //  1 means that the shadow was cast "past" point b on the line and if the value is less than 0 it was cast
    //  "behind" point a.
    // If it was cast ahead we cap the value of u to 1, or one length of the vector p,
    //  and if it was cast behind we set it to 0.
    // This works because the relative crossover point happens to be 1.

    if (u > 1) {
      u = 1;
    } else if (u < 0.0) {
      u = 0.0f;
    }

    // We then scale the vector p by the u value, the relative length of the vector p,
    //  to the get the point on the line.
    return Vector.addScaled(a, p, u);
  }

  /**
   * Find the closest normal vector to the direction vector.
   * <br>
   * From a list of collision normals and a direction vector, calculate a response normal that
   * follows how a set of convex collision normals might contain that direction vector. <br> The
   * response normal is a vector that is in the same direction of the direction vector. <br> The
   * normal scale is used to change the direction of the collision <br>
   * <br>
   * Explanation: <br> When a non-compressible object has multiple collision normals, depending on
   * the orientation of those normals a force vector may have a full collision response even though
   * it doesn't collide with, or directly oppose, any of the individual collision vectors. For
   * example, if two collision vectors are enacted on an object at 45 deg in the top right quadrant
   * and -45 deg in the bottom right quadrant directed towards the center (0,0), an object traveling
   * directly to the right doesn't have a full dot product from any one of the collision normals.
   * But, in reality, an object can't travel in that direction because of how the collision normals
   * create a abstract "corner". Thus this algorithm in a general sense detects that occurrence and
   * returns a full response in the direction of the input direction vector. This algorithm will
   * also detect and return the closest response normal if the direction vector does not fully
   * collide. In the case that the direction vector does not collide with the collision normals at
   * all return the zero vector. <br>
   * <br>
   * * for reference two vectors "collide" when the dot product of the two vectors is positive, and
   * the vectors fully collide when the dot product of the unit vector representations is 1.
   *
   * @param dirVector a unit vector in the direction of the force applied to the entity
   * @param normals         set of collision normals
   * @param nScale     a scale factor to apply to the collision normals -1 or 1
   * @return vector describing the distribution of force from one object to another
   */
  @SuppressWarnings("Duplicates")
  public static Vector distributionVector(Vector dirVector, List<Vector> normals, float nScale) {

    if (normals.isEmpty()) {
      return Vector.zero;
    }

    if (normals.size() == 1) {
      return normals.get(0).scalei(nScale);
    }

    // high low and middle vector normal variables
    Vector hNorm = Vector.zero;
    Vector lNorm = Vector.zero;
    Vector mNorm = Vector.zero;

    // loop through the collision normals
    for (Vector nNorm : normals) {
      // scale the normals to switch the direction if necessary
      nNorm = nNorm.scalei(nScale);
      // if the normal is equal to the high, low, middle, or zero vector, continue
      if (nNorm.roughEquals(hNorm, 0.01f) || nNorm.roughEquals(mNorm, 0.01f)
          || nNorm.roughEquals(lNorm, 0.01f) || nNorm.equals(Vector.zero)) {
        continue;
      }

      if (hNorm.equals(Vector.zero)) { // if the h vector is not set, capture this normal
        hNorm = nNorm;
      } else if (lNorm.equals(Vector.zero)) { // if the l vector is not set, capture this normal
        lNorm = nNorm;

        // if the h vector isn't opposite to the l vector an the vectors are >90 apart
        //  add a vector in between h and l that is at 90 deg from the h vector and in the
        //  direction of the l vector
        if (!hNorm.scalei(-1).equals(lNorm) && Vector.dot(lNorm, hNorm) < 0) {
          mNorm = pointedPVector(hNorm, lNorm);
        }
      } else {
        // if the m vector has not been set and the h vector is opposite to the l vector add a
        //  vector that is 90 deg to the h and l vector and is in the direction of the normal
        if (mNorm.equals(Vector.zero) && hNorm.scalei(-1).equals(lNorm)) {
          mNorm = pointedPVector(hNorm, nNorm);
          continue;
        }

        // create variables for dotted combinations of h, l, and m vectors
        float hlDot = Vector.dot(lNorm, hNorm);
        float hmDot = Vector.dot(hNorm, mNorm);
        float lmDot = Vector.dot(lNorm, mNorm);

        // create a dotted combination of the n normal and all of the h, l, and m vectors
        float nhDot = Vector.dot(nNorm, hNorm);
        float nlDot = Vector.dot(nNorm, lNorm);
        float nmDot = Vector.dot(nNorm, mNorm);

        // if the n vector is inside the h-m-l vector range, continue
        if (inVectorDot(hlDot, hmDot, lmDot, nhDot, nmDot, nlDot)) {
          continue;
        }

        // if the inverse of the n vector is inside the h-m-l vector range, the n normal completes
        //  a full collision circle and thus any vector will produce a full response
        // return the original vector
        if (inVectorDot(hlDot, hmDot, lmDot, -nhDot, -nmDot, -nlDot)) {
          return dirVector;
        }

        // if the n normal is closer to the h vector (and isn't in the h-m-l vector range)
        //  replace the m vector with the h vector and set the n normal as the new h vector
        if (nhDot > nlDot) {
          mNorm = hNorm;
          hNorm = nNorm;
        } else {
          // otherwise do the same with the l vector
          mNorm = lNorm;
          lNorm = nNorm;
        }

        // if the h normal is > then 90 deg away from the l normal, add a m normal between the
        //  two normals in the direction of the m normal
        if (Vector.dot(hNorm, lNorm) < 0) {
          mNorm = pointedPVector(hNorm, mNorm);
        }
      }
    }

    // create variables for dotted combinations of h, l, and m vectors
    float hmDot = Vector.dot(hNorm, mNorm);
    float lmDot = Vector.dot(lNorm, mNorm);
    float hlDot = Vector.dot(hNorm, lNorm);

    // create a dotted combination of the direction vector and all of the h, l, and m vectors
    float hVal = Vector.dot(hNorm, dirVector);
    float mVal = Vector.dot(mNorm, dirVector);
    float lVal = Vector.dot(lNorm, dirVector);

    // if the direction vector is inside the h-m-l vector range return the direction vector
    if (inVectorDot(hlDot, hmDot, lmDot, hVal, mVal, lVal)) {
      return dirVector;
    } else if (hVal > 0 && hVal > lVal) {
      // if the h normal is closer to the direction vector and is in the direction of the h normal,
      //  return the h normal
      return hNorm;
    } else if (lVal > 0) {
      // otherwise the l normal is closer to the direction vector, and if the direction vector is
      //  in the direction of the l normal, return the h normal
      return lNorm;
    }

    // if the direction vector is not in any of the normal directions return the zero vector
    return Vector.zero;
  }

  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="MassTree Logic">

  /**
   * Detect and filter out masses that are used multiple times.
   *
   * TODO: exfoliate
   *
   * @param parentTree input MassTree
   * @return modified MassTree
   */
  public static MassTree filterMass(MassTree parentTree) {
    if (parentTree.iMass == 0 || parentTree.entity == null || parentTree.hitChildren == null) {
      return parentTree;
    }

    parentTree.changed = false;

    for (int mtIdx = 0; mtIdx < parentTree.hitChildren.size(); mtIdx++) {
      MassTree cTree = parentTree.hitChildren.get(mtIdx);

      cTree = filterMass(cTree);
      if (cTree.entity == null) {
        parentTree.changed = true;
        continue;
      }

      if (cTree.changed) {
        parentTree.changed = true;
      }

      if (cTree.hCount == 0) {
        for (int idx = 0; idx < cTree.entity.active.size(); idx++) {
          if (cTree.inNormal.dot(cTree.entity.inNormal.get(idx)) > 0) {
            cTree.hCount++;
          }
        }
      }

      if (cTree.hCount > 1) {
        parentTree.changed = true;
        cTree.divScalar = 1.0f / cTree.hCount;
      }
    }

    if (!parentTree.changed) {
      return parentTree;
    }

    float massTotal = 1.0f / parentTree.entity.getFInvMass();

    for (MassTree mt : parentTree.hitChildren) {
      Vector mtIn = mt.customIn != null ? mt.customIn : mt.inNormal;
      if (mtIn.dot(parentTree.outNormal) > 0) {
        massTotal += ((1.0f / mt.iMass) * mt.divScalar / mtIn.dot(mt.outNormal) * mtIn
            .dot(parentTree.outNormal));
      }
    }

    parentTree.iMass = (1.0f / massTotal);

    return parentTree;
  }

  /**
   * Preform a collision calculation. <br>
   *   Filter out unused collision normals and calculate the output normal.
   *
   * TODO: exonerate
   *
   * @param masterList MassTree lookup
   * @param mLookup Manifold lookup
   * @param ex collision history
   * @param mTree operating tree
   * @param quick quick flag
   * @return processed MassTree
   */
  public static MassTree solveHits(HashMap<BasicEntity, ArrayList<MassTree>> masterList,
      HashMap<BasicEntity, ArrayList<Manifold>> mLookup, ArrayList<BasicEntity> ex,
      MassTree mTree, boolean quick) {
    Logger.pushDebugTime("solve", System.nanoTime());

    if (mTree.entity == null) {
      Logger.pushDebugTime("solve", System.nanoTime());
      return mTree;
    }

    Vector inputNormal = mTree.customIn != null ? mTree.customIn : mTree.inNormal;

    ArrayList<MassTree> iList = new ArrayList<>(mTree.hitChildren == null ? mTree.children : mTree.hitChildren);
    ArrayList<Vector> zList = new ArrayList<>(mTree.entity.zeros);

    if (!zList.isEmpty()) {
      if (distributionVector(inputNormal, zList, 1.0f).dot(inputNormal) > 0.99f) {
        Logger.pushDebugTime("solve", System.nanoTime());
        return new MassTree(0, inputNormal, inputNormal.negatei(), true);
      }
    }

    boolean always = addVectorList(zList, mTree.tmpZeros);

    for (int idx = 0; idx < iList.size(); idx++) {
      MassTree mt = iList.get(idx);
      Vector mtIn = mt.customIn != null ? mt.customIn : mt.inNormal;

      if (mt.iMass == 0 || mtIn.dot(mt.outNormal) <= 0.05f) {
        boolean out = addVectorList(zList, mtIn);
        always = always && out;
        iList.remove(idx);
        idx--;
      }
    }

    mTree.zeroNormal = inputNormal;

    if (!zList.isEmpty()) {
      Vector distributionNormal = distributionVector(mTree.zeroNormal, zList, 1.0f);
      float dot = distributionNormal.dot(mTree.zeroNormal);

      if (dot > 0.99f) {
        Logger.pushDebugTime("solve", System.nanoTime());
        return new MassTree(0, mTree.zeroNormal, mTree.zeroNormal.negatei(), false);
      } else if (dot > 0) {
        mTree.zeroNormal = pointedPVector(distributionNormal, mTree.zeroNormal);
      }
    }

    for (int idx = 0; idx < mTree.revChildren.size(); idx++) {
      MassTree nmt = mTree.revChildren.get(idx);
      if (mTree.zeroNormal.dot(nmt.inNormal) > 0.05f) {
        nmt = iterateMassTree2(nmt, ex, masterList, mLookup, quick);
        if (nmt.iMass == 0) {

          if (nmt.always) {
            mTree.entity.zeros.add(nmt.inNormal);

            if (distributionVector(inputNormal, mTree.entity.zeros, 1.0f).dot(inputNormal)
                > 0.99f) {
              Logger.pushDebugTime("solve", System.nanoTime());
              return new MassTree(0, mTree.zeroNormal, mTree.zeroNormal.negatei(), true);
            }
          }

          if (addVectorList(zList, nmt.inNormal)) {
            always = always && nmt.always;

            Vector distributionNormal = distributionVector(mTree.zeroNormal, zList, 1.0f);
            float dot = distributionNormal.dot(mTree.zeroNormal);

            if (dot > 0.99f) {
              Logger.pushDebugTime("solve", System.nanoTime());
              return new MassTree(0, mTree.zeroNormal, mTree.zeroNormal.negatei(), always);
            } else if (dot > 0) {
              mTree.zeroNormal = pointedPVector(distributionNormal, mTree.zeroNormal);
            }
          }
        } else {
          mTree.revChildren.remove(idx);
          iList.add(nmt);
          idx--;
        }
      } else {
        nmt.revIdx = idx;
        iList.add(nmt);
      }
    }

    if (quick) {
      mTree.iMass = 1;
      mTree.outNormal = mTree.inNormal;
      return mTree;
    }

    setStaticFrictionVal(mTree.entity, inputNormal);
    float iMass = mTree.entity.getFInvMass();

    ArrayList<Vector> nNorms = new ArrayList<>();
    ArrayList<Float> nWeights = new ArrayList<>();
    ArrayList<MassTree> hList = new ArrayList<>();
    ArrayList<Integer> eIdx = new ArrayList<>();

    Vector outNormal = mTree.zeroNormal;
    mTree.oNormalTotal = outNormal.scalei(1.0f / iMass);

    boolean zOut = true;
    boolean deflected = true;
    boolean revMod = false;

//    Logger.pushDebugTime("bulk", System.nanoTime());
    while (deflected) {
      deflected = false;
      for (int idx = 0; idx < iList.size(); idx++) {
        MassTree mt = iList.get(idx);
        Vector mtIn = mt.customIn != null ? mt.customIn : mt.inNormal;
        float dOut = mtIn.dot(outNormal);

        if (!(zOut && dOut > 0.99f) && dOut > 0.05) {

          if (mt.iMass < 0) {
            int rdx = mt.revIdx;

            mt = iterateMassTree2(mt, ex, masterList, mLookup, quick);
            if (mt.iMass == 0) {
              if (addVectorList(zList, mt.inNormal)) {

                if (mt.always) {
                  mTree.entity.zeros.add(mt.inNormal);

                  if (distributionVector(inputNormal, mTree.entity.zeros, 1.0f).dot(inputNormal)
                      > 0.99f) {
                    Logger.pushDebugTime("solve", System.nanoTime());
                    return new MassTree(0, mTree.zeroNormal, mTree.zeroNormal.negatei(), true);
                  }
                }

                always = always && mt.always;

                Vector distributionNormal = distributionVector(inputNormal, zList, 1.0f);
                float dot = distributionNormal.dot(inputNormal);

                if (dot > 0.99f) {
                  Logger.pushDebugTime("solve", System.nanoTime());
                  return new MassTree(0, mTree.zeroNormal, mTree.zeroNormal.negatei(), always);
                } else {
                  distributionNormal = distributionVector(outNormal, zList, 1.0f);
                  if (distributionNormal.dot(outNormal) > 0) {
                    mTree.zeroNormal = pointedPVector(distributionNormal, inputNormal);
                    outNormal = mTree.zeroNormal;
                    zOut = true;
                  }
                }
              }

              deflected = true;
              revMod = true;
              iList.remove(idx);
              mTree.revChildren.get(rdx).revRemove = true;
              idx--;

              continue;
            }

            revMod = true;
            mTree.revChildren.get(rdx).revRemove = true;
          }

          Vector rVec = pointedPVector(mtIn, inputNormal);
          float weight = mt.divScalar / (mt.iMass * Math.abs(rVec.pdot(mt.outNormal)));

          mTree.oNormalTotal = mTree.oNormalTotal.addi(rVec.scalei(weight));
          outNormal = mTree.oNormalTotal.normalizei();

          if (distributionVector(outNormal, zList, 1.0f).dot(outNormal) > 0) {
            outNormal = mTree.zeroNormal;
            zOut = true;
          } else {
            zOut = outNormal.dot(mTree.zeroNormal) > 0.99f;
          }

          deflected = true;
          hList.add(mt);
          nNorms.add(rVec);
          nWeights.add(weight);

          if (!mt.cIdx.isEmpty() || !mt.lEnds.isEmpty()) {
            eIdx.add(idx);
          }

          iList.remove(idx);
          idx--;
        }
      }
    }

    if (revMod) {
      for (int idx = 0; idx < mTree.revChildren.size(); idx++) {
        if (mTree.revChildren.get(idx).revRemove) {
          mTree.revChildren.remove(idx);
          idx--;
        }
      }
    }

//    Logger.pushDebugTime("bulk", System.nanoTime());


    if (hList.isEmpty() && !mTree.lEnds.isEmpty()) {
      boolean eCase1 = true;

      for (int i = 0; i < mTree.lEnds.size(); i++) {
        MassTree mt = mTree.lEnds.get(i);
        Vector mtIn = mt.customIn != null ? mt.customIn : mt.inNormal;
        float dOut = mtIn.dot(outNormal);

        if (dOut > 0 || mtIn.dot(inputNormal) > 0) {
          eCase1 = false;
        }
      }

      if (eCase1) {
        mTree.lEnds.clear();
      }
    }

    if (inputNormal.dot(outNormal) < 0.01) {
      Logger.pushDebugTime("solve", System.nanoTime());
      return new MassTree(0, inputNormal, inputNormal.negatei(), always);
    }

    if (zOut) {
      for (int idx = 0; idx < iList.size(); idx++) {
        MassTree mt = iList.get(idx);
        Vector mtIn = mt.customIn != null ? mt.customIn : mt.inNormal;

        if (mtIn.dot(outNormal) > 0.99f) {
          hList.add(mt);
          nNorms.add(new Vector(0, 0));
          nWeights.add(0f);

          if (!mt.cIdx.isEmpty() || !mt.lEnds.isEmpty()) {
            eIdx.add(idx);
          }
        }
      }
    }

    setStaticFrictionVal(mTree.entity, outNormal);
    iMass = mTree.entity.getFInvMass();

    if (iMass == 0) {
      Logger.pushDebugTime("solve", System.nanoTime());
      return new MassTree(0, inputNormal, inputNormal.negatei(), true);
    }

    float massTotal = 1.0f / iMass;

    for (int idx = 0; idx < hList.size(); idx++) {
      MassTree mt = hList.get(idx);
      if (distributionVector(mt.inNormal, zList, 1.0f).dot(mt.inNormal) > 0.99f) {
        hList.remove(idx);
        idx--;
        continue;
      }

      Vector mtIn = mt.customIn != null ? mt.customIn : mt.inNormal;
      if (mtIn.dot(outNormal) > 0) {
        mt.eScalar = mt.divScalar / mtIn.dot(mt.outNormal) * mtIn.dot(outNormal);
        massTotal += ((1.0f / mt.iMass) * mt.eScalar);
      }
    }

    mTree.hitChildren = hList;
    mTree.normals = nNorms;
    mTree.weights = nWeights;
    mTree.tcIdx = eIdx;
    mTree.outNormal = outNormal;
    mTree.iMass = (1.0f / massTotal);

    Logger.pushDebugTime("solve", System.nanoTime());
    return mTree;
  }

  /**
   * Run through a collision tree populating entity registers.
   *
   * Filters out entity loops.
   *
   * TODO: adjudicate
   *
   * @param parentTree input MassTree
   * @return modified MassTree
   */
  public static MassTree updateMassThin(MassTree parentTree) {
    ArrayList<Tuple<MassTree, ArrayList<BasicEntity>>> treeTable = new ArrayList<>();

    ArrayList<BasicEntity> basicEntities = new ArrayList<>();
    basicEntities.add(parentTree.entity);
    treeTable.add(new Tuple<>(parentTree, basicEntities));

    for (int tIdx = 0; tIdx < treeTable.size(); tIdx++) {
      Tuple<MassTree, ArrayList<BasicEntity>> tuple = treeTable.get(tIdx);
      MassTree currentTree = tuple.a;
      ArrayList<BasicEntity> list = new ArrayList<>(tuple.b);
      list.add(currentTree.entity);

      if (currentTree.iMass == 0 || currentTree.entity == null || currentTree.hitChildren == null) {
        continue;
      }

      if (!currentTree.hitChildren.isEmpty()) {
        for (int mtIdx = 0; mtIdx < currentTree.hitChildren.size(); mtIdx++) {
          MassTree childTree = currentTree.hitChildren.get(mtIdx);

          if (list.contains(childTree.entity)) {
            currentTree.tmpZeros.add(childTree.inNormal);
            currentTree.hitChildren.remove(mtIdx);
            mtIdx--;
            continue;
          }

          childTree.entity.inNormal.add(childTree.inNormal);
          treeTable.add(new Tuple<>(childTree, list));
        }
      }
    }

    return parentTree;
  }

  /**
   * Collect masses from a given entity and a collision normal.
   *
   * TODO: eradicate
   *
   * @param entity input entity
   * @param mLookup Manifold lookup
   * @param inputNormal input normal
   * @return MassTree
   */
  public static MassTree collectMass(BasicEntity entity,
      HashMap<BasicEntity, ArrayList<Manifold>> mLookup, Vector inputNormal) {
    return collectMass(new HashMap<>(), mLookup, entity, inputNormal, false);
  }

  /**
   * Collect masses from a given entity and a collision normal.
   *
   * TODO eight
   *
   * @param master MassTree lookup
   * @param mLookup Manifold lookup
   * @param entity operating entity
   * @param inputNormal input normal
   * @param quick quick flag
   * @return
   */
  public static MassTree collectMass(HashMap<BasicEntity, ArrayList<MassTree>> master,
      HashMap<BasicEntity, ArrayList<Manifold>> mLookup, BasicEntity entity, Vector inputNormal,
      boolean quick) {

    if (inputNormal.roughEquals(Vector.zero, 0.01f)) {
      return new MassTree(0);
    }


    Logger.pushDebugTime("getTree");
    MassTree mt = getMassTree(master, mLookup, new ArrayList<>(), entity, inputNormal, false);
    if (mt == null) {
      mt = collectMassHelper(master, mLookup, new ArrayList<>(), entity, inputNormal, quick);
      master.computeIfAbsent(entity, k -> new ArrayList<>()).add(mt);
    }

    Logger.pushDebugTime("getTree");

    if (mt.iMass == 0 || quick) {
      return mt;
    }


    Logger.pushDebugTime("post_processing");
    mt = updateMassThin(mt);
    mt = filterMass(mt);
    Logger.pushDebugTime("post_processing");

    return mt;
  }

  /**
   * Collect useful normals and their MassTree's.
   *
   * Populates MassTree data recursively.
   *
   * @param masterList MassTree lookup
   * @param mLookup Manifold lookup
   * @param hitList collision history
   * @param massTree operating tree
   * @param quick quick flag
   */
  public static void dloop(HashMap<BasicEntity, ArrayList<MassTree>> masterList,
      HashMap<BasicEntity, ArrayList<Manifold>> mLookup, ArrayList<BasicEntity> hitList,
      MassTree massTree, boolean quick) {

    for (Manifold nManifold : mLookup.get(massTree.entity)) {
      boolean swap = nManifold.b.equals(massTree.entity);
      float nScale = swap ? 1 : -1;
      BasicEntity B = swap ? nManifold.a : nManifold.b; // get the B entity

      for (Vector norm : nManifold.normals) {
        norm = norm.scalei(nScale);

        if (norm.dot(massTree.inNormal) < -0.99f) {
          continue;
        }

        ArrayList<Vector> nv = new ArrayList<>(massTree.entity.zeros);
        nv.addAll(massTree.tmpZeros);

        if (distributionVector(norm, nv, 1.0f).dot(norm) > 0.99f) {
          continue;
        }

        if (hitList.contains(B)) {
            addVectorList(massTree.tmpZeros, norm);

          massTree.lEnds.add(new MassTree(B, norm));
          continue;
        }

        if (norm.dot(massTree.inNormal) < 0.01f) {
          setStaticFrictionVal(B, norm);
          if (B.getFInvMass() == 0 || distributionVector(norm, B.zeros, 1.0f).dot(norm) > 0.99f) {
            addVectorList(massTree.entity.zeros, norm);
          } else {
            massTree.revChildren.add(new MassTree(B, norm));
          }

          continue;
        }

        MassTree mOut = getMassTree(masterList, mLookup, hitList, B, norm, false);
        if (mOut == null) {
          mOut = collectMassHelper(masterList, mLookup, hitList, B, norm, quick);

          if (!quick) {
            if (mOut.iMass == 0 && mOut.always) {
              masterList.computeIfAbsent(B, k -> new ArrayList<>()).add(mOut);
            } else if (mOut.iMass != 0) {
              masterList.computeIfAbsent(B, k -> new ArrayList<>()).add(mOut);
            }
          } else {
            if (mOut.iMass == 0 && mOut.always) {
              masterList.computeIfAbsent(B, k -> new ArrayList<>()).add(mOut);
            }
          }
        }

        if (quick) {
          if (mOut.iMass == 0) {
            if (mOut.always) {
              addVectorList(massTree.entity.zeros, norm);
            } else {
              addVectorList(massTree.tmpZeros, norm);
            }
          }
          continue;
        }

        if (mOut.iMass == 0) {
          if (mOut.always) {
            addVectorList(massTree.entity.zeros, norm);
          } else {
            addVectorList(massTree.tmpZeros, norm);
            massTree.lEnds.add(new MassTree(B, norm));
          }
        } else {
          if (!mOut.lEnds.isEmpty() || !mOut.cIdx.isEmpty()) {
            massTree.cIdx.add(massTree.children.size());
          }

          massTree.children.add(mOut);
          mOut.parent = massTree;
        }
      }
    }
  }

  /**
   * Create a MassTree from an Entity and its collision normals.
   *
   * Populates MassTree data recursively.
   *
   * @param masterList MassTree lookup
   * @param mLookup Manifold lookup
   * @param entity input entity
   * @param inputNormal input normal
   * @param quick quick flag
   */
  public static MassTree collectMassHelper(HashMap<BasicEntity, ArrayList<MassTree>> masterList,
      HashMap<BasicEntity, ArrayList<Manifold>> mLookup, ArrayList<BasicEntity> ex,
      BasicEntity entity, Vector inputNormal, boolean quick) {

    setStaticFrictionVal(entity, inputNormal);
    if (entity.getFInvMass() == 0 || mLookup.get(entity) == null) {
      return new MassTree(0, inputNormal, inputNormal.negatei(), true);
    }

    if (distributionVector(inputNormal, entity.zeros, 1.0f).dot(inputNormal) > 0.99f) {
      return new MassTree(0, inputNormal, inputNormal.negatei(), true);
    }

    MassTree massTree = new MassTree();
    massTree.inNormal = inputNormal;
    massTree.entity = entity;

    ArrayList<BasicEntity> hitList = new ArrayList<>(ex);
    hitList.add(entity);
    entity.lastInNormal = inputNormal;

    dloop(masterList, mLookup, hitList, massTree, quick);
    massTree = solveHits(masterList, mLookup, hitList, massTree, quick);

    if (!quick) {
      if (massTree.hitChildren != null) {
        massTree.always = massTree.hitChildren.stream().allMatch(massTree1 -> massTree1.always);
        massTree.always = massTree.always && massTree.tcIdx.isEmpty() && massTree.lEnds.isEmpty();
      }

      if (massTree.iMass > 0 && massTree.always) {
        if (massTree.hitChildren != null) {
          massTree.children = new ArrayList<>(massTree.hitChildren);
        } else {
          massTree.children.clear();
        }

        if (massTree.tcIdx != null) {
          massTree.cIdx = new ArrayList<>(massTree.tcIdx);
        } else {
          massTree.cIdx.clear();
        }
      }
    }

    return massTree;
  }


  /**
   * Search the MassTree lookup for a MassTree relating to an entity and a collision normal.
   *
   * TODO: emancipate
   *
   * @param masterList MassTree lookup
   * @param mLookup Manifold lookup
   * @param hitList collision history
   * @param lookupEntity input entity
   * @param normal input normal
   * @param check check flag
   * @return output MassTree, null if none exists
   */
  public static MassTree getMassTree(HashMap<BasicEntity, ArrayList<MassTree>> masterList,
      HashMap<BasicEntity, ArrayList<Manifold>> mLookup, ArrayList<BasicEntity> hitList,
      BasicEntity lookupEntity, Vector normal, boolean check) {

    if (masterList.containsKey(lookupEntity)) {
      ArrayList<MassTree> tList = masterList.get(lookupEntity);
      for (int idx = 0; idx < tList.size(); idx++) {
        MassTree lTree = tList.get(idx);
        if (lTree.inNormal.roughEquals(normal, 0.01f)) {
          return lTree;
        }
      }
    }

    return null;
  }

  /**
   * Populate a hollow MassTree with data.
   *
   * A hollow MassTree is one with only an entity and a collision normal.
   *
   * TODO: interpolate
   *
   * @param child hollow MassTree
   * @param ex collision history
   * @param masterList MassTree lookup
   * @param mLookup Manifold lookup
   * @param quick quick flag
   *
   * @return populated MassTree, if the MassTree contains data, returns the input MassTree
   */
  public static MassTree iterateMassTree2(MassTree child, ArrayList<BasicEntity> ex,
      HashMap<BasicEntity, ArrayList<MassTree>> masterList,
      HashMap<BasicEntity, ArrayList<Manifold>> mLookup, boolean quick) {

    Vector norm = child.inNormal;
    BasicEntity B = child.entity;

    MassTree mOut;
    if (child.iMass < 0.0f) {
      ArrayList<BasicEntity> hitList = new ArrayList<>(ex);
      hitList.add(B);

      mOut = getMassTree(masterList, mLookup, hitList, B, norm, false);
      if (mOut == null) {
        mOut = collectMassHelper(masterList, mLookup, hitList, B, norm, quick);

        if (!quick) {
          if (mOut.iMass == 0 && mOut.always) {
            masterList.computeIfAbsent(B, k -> new ArrayList<>()).add(mOut);
          } else if (mOut.iMass != 0) {
            masterList.computeIfAbsent(B, k -> new ArrayList<>()).add(mOut);
          }
        } else {
          if (mOut.iMass == 0 && mOut.always) {
            masterList.computeIfAbsent(B, k -> new ArrayList<>()).add(mOut);
          }
        }
      }
    } else {
      mOut = child;
    }

    return mOut;
  }

  /**
   * Propagate Velocities through a MassTree.
   * <p>
   * TODO: extrapolate
   *
   * @param map   MassTree
   * @param vNorm input normal
   * @param cVel  velocity magnitude
   */
  public static void forceEntities(MassTree map, Vector vNorm, float cVel) {
    Vector mtOutV = map.outNormal;
//    Vector inorm = map.customIn != null ? map.inNormal : map.inNormal;

    if (vNorm.dot(mtOutV) <= 0) {
      return;
    }

    Vector fVector = mtOutV.scalei(cVel * map.divScalar * Math.min(1.0f, vNorm.dot(mtOutV)));

    map.entity.addVelocity(fVector);
//    if (map.entity.push.roughEquals(Vector.zero, 0.0001f)) {
//      map.entity.push = fVector;
//    } else {
////      float od = Vector.down.dot(map.entity.push);
////      float or = Vector.right.dot(map.entity.push);
////
////      float nd = Vector.down.dot(fVector);
////      float nr = Vector.right.dot(fVector);
////
////      if (od >= 0 && nd >= 0) {
////        od = Math.max(od, nd);
////      } else if (od <= 0 && nd <= 0) {
////        od = Math.min(od, nd);
////      } else {
////        od = ((od + nd)/2.0f);
////      }
////
////      if (or >= 0 && nr >= 0) {
////        or = Math.max(or, nr);
////      } else if (or <= 0 && nr <= 0) {
////        or = Math.min(or, nr);
////      } else {
////        or = ((or + nr)/2.0f);
////      }
////
////      map.entity.push = new Vector(or, od);
////      fVector = fVector.subi(map.entity.push);
//      map.entity.push = map.entity.push.addi(fVector).scalei(0.5f);
////      fVector = fVector.subi(map.entity.push);
//    }

    if (map.hitChildren != null && !map.hitChildren.isEmpty()) {
      for (MassTree childTree : map.hitChildren) {
//        Vector cinorm = childTree.customIn != null ? childTree.inNormal : childTree.inNormal;
        float mag;
        if ((mag = fVector.mag()) > 0.0001f) {
          Engine.forceEntities(childTree, mtOutV, mag);
        }
      }
    }
  }

  /**
   * Propagate Shifts through a MassTree.
   * <p>
   * TODO: extrapolate
   *
   * @param map     MassTree
   * @param sScalar shift amount
   */
  public static void shiftEntities(MassTree map, float sScalar) {

    Vector mtOutV = map.outNormal;
    Vector inorm = map.customIn != null ? map.inNormal : map.inNormal;

    float oScalar = Math.min(5.0f, sScalar * 1.0f / inorm.dot(mtOutV));
//    Game.log("a", map.entity, sScalar * 1.0f / inorm.dot(mtOutV), inorm, mtOutV);
//    Game.log("o", map.entity, oScalar);
    Vector fVector = mtOutV.scalei(oScalar);

    if (map.entity.getShift().roughEquals(Vector.zero, 0.0000001f)) {
//      Game.log("1", map.entity, fVector);
      map.entity.setShift(fVector);
    } else {
      float od = Vector.down.dot(map.entity.getShift());
      float or = Vector.right.dot(map.entity.getShift());

      float nd = Vector.down.dot(fVector);
      float nr = Vector.right.dot(fVector);

      if (od >= 0 && nd >= 0) {
        od = Math.max(od, nd);
      } else if (od <= 0 && nd <= 0) {
        od = Math.min(od, nd);
      } else {
        od = ((od + nd) / 2.0f);
      }

      if (or >= 0 && nr >= 0) {
        or = Math.max(or, nr);
      } else if (or <= 0 && nr <= 0) {
        or = Math.min(or, nr);
      } else {
        or = ((or + nr) / 2.0f);
      }

      Vector o = new Vector(or, od);
//      map.entity.addShift(fVector);
      map.entity.setShift(o);
//      Game.log("A", map.entity, o);
//      fVector = fVector.subi(map.entity.getShift());
    }

    if (oScalar > POS_ERROR_MARGIN * FIX_SCALAR * 0.99f) {
      if (map.hitChildren != null && !map.hitChildren.isEmpty()) {
        for (MassTree childTree : map.hitChildren) {
          Vector cinorm = childTree.customIn != null ? childTree.inNormal : childTree.inNormal;
//          Game.log("p", mtOutV.dot(cinorm), mtOutV, cinorm);
          Engine.shiftEntities(childTree, oScalar * mtOutV.dot(cinorm));
        }
      }
    }
  }

  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Entity Refresh Methods">
  /**
   * Clear all temporary physics data from a list of BasicEntity.
   *
   * @param entities entity list
   */
  public static void clearAll(Iterable<BasicEntity> entities) {
    for (BasicEntity be : entities) {
      clearAll(be);
    }
  }

  /**
   * Clear all temporary physics data from a BasicEntity.
   *
   * @param be entity
   */
  public static void clearAll(BasicEntity be) {
    be.zeros.clear();
    be.lastInNormal = null;
    clearShifts(be);
  }

  /**
   * Clear all temporary shifting data from a list of BasicEntity.
   *
   * @param entities entity list
   */
  public static void clearShifts(Iterable<BasicEntity> entities) {
    for (BasicEntity be : entities) {
      clearShifts(be);
    }
  }

  /**
   * Clear all temporary shifting data from a BasicEntity.
   *
   * @param be BasicEntity
   */
  public static void clearShifts(BasicEntity be) {
    be.inNormal.clear();
    be.outNormal.clear();
    be.weights.clear();
    be.mShift.clear();
    be.pNormal.clear();
    be.active.clear();
    be.lookup.clear();
    be.hitIndex = 0;
    be.collector = new Vector(0, 0);
  }

  public static void removeChildEffect(MassTree parentTree) {
    if (parentTree.entity != null) {
      if (parentTree.hitIdx != -1) {

        parentTree.entity.lookup.set(parentTree.hitIdx, null);
        parentTree.entity.pNormal.set(parentTree.hitIdx, new Vector(0, 0));
        parentTree.entity.outNormal.set(parentTree.hitIdx, new Vector(0, 0));
        parentTree.entity.inNormal.set(parentTree.hitIdx, new Vector(0, 0));
        parentTree.entity.weights.set(parentTree.hitIdx, 0f);
        parentTree.entity.mShift.set(parentTree.hitIdx, new Vector(0, 0));
        parentTree.entity.active.set(parentTree.hitIdx, false);
      }

      if (parentTree.hitChildren != null && !parentTree.hitChildren.isEmpty()) {
        for (MassTree child : parentTree.hitChildren) {
          removeChildEffect(child);
        }
      }
    }
  }

  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Vector List Methods">

  public static boolean addVectorList(ArrayList<Vector> vectors, ArrayList<Vector> addList) {
    boolean always = true;
    for (Vector vector : addList) {
      boolean out = addVectorList(vectors, vector);
      always = always && out;
    }

    return always;
  }

  public static boolean inVectorList(ArrayList<Vector> vectors, Vector newVector) {
    for (Vector vector : vectors) {
      if (vector.roughEquals(newVector, 0.0001f)) {
        return true;
      }
    }
    return false;
  }

  public static boolean addVectorList(ArrayList<Vector> vectors, Vector newVector) {
    boolean b = false;
    for (Vector vector : vectors) {
      if (b = vector.roughEquals(newVector, 0.0001f)) {
        break;
      }
    }

    if (!b) {
      vectors.add(newVector);
    }

    return !b;
  }

  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Physics Logic">

  /**
   * Calculate the value of static friction on a given entity.
   *
   * Friction is defined as the relative mass associated with a given entity when pushed along a given input normal.
   *
   * @param entity input entity
   * @param norm input normal
   * @return
   */
  public static float setStaticFrictionVal(BasicEntity entity, Vector norm) {
    if (entity.getInvMass() == 0) {
      entity.setFInvMass(0);
      return Float.POSITIVE_INFINITY;
    }

//    float m2 = STATIC_FRICTION_MIN_SQR;
//    float o = STATIC_FRICTION_SLOPE;
//    float s = STATIC_FRICTION_START;
//    float x = (float) Math.pow(res.dot(entity.getLocalVelocity()), 2)
//        + force.scalei(-entity.getInvMass()).magSqr();
//
//    if (x > m2 * entity.getMass()) {
//      return (x - m2) / (x - m2 + o) * (1 - s) + s;
//    }

    float m = STATIC_FRICTION_MAX * entity.getStaticFriction();
    float s = (STATIC_FRICTION_SLOPE / 10.0f) * m;
    float x_out = norm.dot(entity.getLocalVelocity());
    float x = (float) Math.pow(Math.abs(x_out) + s, 2.0f);

    float factor = ((((x + m) / (x)) - 1.0f) * s * s);

    if (factor > 1) {
      entity.setFInvMass(entity.getInvMass() / factor);
    } else {
      entity.setFInvMass(entity.getInvMass());
    }

    return factor;
  }

  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Collision Detection">


  /**
   * Parse two entities, pairing off Shape types to the correct collision detection algorithms.
   *
   * @param a  entity a
   * @param sA shape of entity a
   * @param pA position of entity a
   * @param b  entity b
   * @param sB shape of entity b
   * @param pB position of entity b
   * @return a list of manifolds generated from the states of the entities, can be empty.
   */
  public static ArrayList<Manifold> routeEntityShapes(BasicEntity a, Shape sA, Vector pA,
      BasicEntity b, Shape sB, Vector pB) {
    // list initializer
    ArrayList<Manifold> m = new ArrayList<>();

    if (sA instanceof MultiShape && sB instanceof MultiShape) {
      // if both shapes have multiple parts re-parse each of the shapes from entity a
      //  with each shape from entity b.
      MultiShape msa = (MultiShape) sA;
      MultiShape msb = (MultiShape) sB;
      for (int i = 0; i < msa.size(); i++) {
        for (int j = 0; j < msb.size(); j++) {
          // When parsing the new shapes, it is necessary to add each sub-shape's shift offset
          //  to the position vector to preform the correct collision detection.
          m.addAll(routeEntityShapes(a, msa.getShape(i), pA.addi(msa.getShift(i)),
              b, msb.getShape(j), pB.addi(msb.getShift(j))));
        }
      }
    } else if (sA instanceof MultiShape) {
      // if either entity a or entity b exclusively has multiple shapes, re-parse each of the
      //  multiple shapes with the opposing entities singular shape.
      MultiShape ms = (MultiShape) sA;
      for (int i = 0; i < ms.size(); i++) {
        m.addAll(routeEntityShapes(a, ms.getShape(i), pA.addi(ms.getShift(i)), b, sB, pB));
      }
    } else if (sB instanceof MultiShape) {
      // if either entity a or entity b exclusively has multiple shapes, re-parse each of the
      //  multiple shapes with the opposing entities singular shape.
      MultiShape ms = (MultiShape) sB;
      for (int i = 0; i < ms.size(); i++) {
        m.addAll(routeEntityShapes(a, sA, pA, b, ms.getShape(i), pB.addi(ms.getShift(i))));
      }
    }

    // Route the correct shapes to the correct collision parsing algorithms
    if (sA instanceof Circle && sB instanceof Circle) {
      m.add(circleCircleCollision(a, (Circle) sA, pA, b, (Circle) sB, pB));
    } else if (sA instanceof Circle && sB instanceof PolygonShape) {
      m.add(circlePollyCollision(a, (Circle) sA, pA, b, (PolygonShape) sB, pB));
    } else if (sA instanceof PolygonShape && sB instanceof Circle) {
      m.add(circlePollyCollision(b, (Circle) sB, pB, a, (PolygonShape) sA, pA));
    } else if (sA instanceof PolygonShape && sB instanceof PolygonShape) {
      m.add(pollyPollyCollision(a, (PolygonShape) sA, pA, b, (PolygonShape) sB, pB));
    }

    // return the list of collected manifolds
    return m;
  }

  public static ArrayList<Manifold> routeEntityShapes(Entity a, Entity b) {
    return routeEntityShapes(a, a.getShape(), a.getPosition(), b, b.getShape(), b.getPosition());
  }




  /**
   * Process a circle to circle shape collision.
   *
   * @param a  entity a
   * @param c1 entity a's circle shape
   * @param v1 position of entity a's circle shape
   * @param b  entity b
   * @param c2 entity b's circle shape
   * @param v2 position of entity b's circle shape
   * @return if the collision was valid return a collision manifold, otherwise return null
   */
  public static Manifold circleCircleCollision(BasicEntity a, Circle c1, Vector v1,
      BasicEntity b, Circle c2, Vector v2) {

    Manifold m = new Manifold();

    // calculate vector from the center of shape a to the center of shape b
    Vector aTob = Vector.sub(v1, v2);
    float dist = Vector.magSqr(aTob); // distance (squared) from a to b

    // if the distance is less than or equal to the two radii (squared)
    //  then a collision has occurred
    if (dist <= (Math.pow(c1.getRadius() + c2.getRadius(), 2))) {
      // depth placeholder
      float depth;
      Vector normal;

      if (dist == 0) {
        // if the distance is equal to 0 the normal has to be artificially generated
        normal = new Vector(1, 0);
        depth = c1.getRadius() + c2.getRadius(); // the depth is both radii added together
      } else {
        normal = aTob.normalizei(); // calculate the normal
        // subtract the distance from the radius to get the depth
        depth = (float) ((c1.getRadius() + c2.getRadius()) - Math.sqrt(dist));
      }

      // add the collision information to the manifold
      m.normals.add(normal);
      m.vectors.add(normal.scalei(depth));
      m.a = a;
      m.b = b;

      float cDist = Vector.sub(v1, v2).magSqr();
      m.aEulerCount = cDist < c1.getRadius() * c1.getRadius() ? 1 : 0;
      m.aEulerCount = cDist < c2.getRadius() * c2.getRadius() ? 1 : 0;

      return m;
    }

    return null;
  }

  /**
   * Process a circle to polygon shape collision. This algorithm uses Euler's bridge method to
   * detect if a circle is inside a polygon, which is helpful to detect if the circle doesn't collide
   * with one of the polygon's edges.
   *
   * @param a  Entity a
   * @param c  Circle shape of Entity a
   * @param cP Position of the Circle in Entity a
   * @param b  Entity b
   * @param p  Polygon shape of Entity b
   * @param pP Position of the Polygon in Entity b
   * @return A collision manifold if the two shapes collide or null if they don't
   */
  public static Manifold circlePollyCollision(BasicEntity a, Circle c, Vector cP, BasicEntity b,
      PolygonShape p, Vector pP) {
    // Check if the polygon is valid, this algorithm will only work with polygons that are
    //  defined in a clockwise direction.
    if (!p.isValid()) {
      return null;
    }

    // Initialize manifold data storage.
    Manifold m = new Manifold();

    // Gather polygon vertices.
    ArrayList<Vector> verts = p.getVertices();

    // Set up vector placeholders.
    Vector shortVector = new Vector();
    float shortest = Float.MAX_VALUE;

    // Get the start and last node for calculations.
    Vector beforeNode = verts.get(verts.size() - 2).addi(pP);
    Vector midNode = verts.get(verts.size() - 1).addi(pP);
    Vector afterNode;

    // Calculate the last v point for edge calculations.
    Vector vBefore = pnt2line(cP, beforeNode, midNode);
    Vector pBefore = midNode.subi(beforeNode).perpi();

    // Get the max x position.
    float xMax = Math.max(beforeNode.getX(), midNode.getX());
    int inCount = 0; // Get the inner line count.

    for (int i = 0; i < verts.size(); i++) {
      // Set up values used in circle calculations.
      afterNode = verts.get(i).addi(pP);
      Vector vAfter = pnt2line(cP, midNode, afterNode);
      Vector pAfter = afterNode.subi(midNode).perpi();
      Vector cToVAfter = vAfter.subi(cP);
      float magAfter = cToVAfter.mag();
      xMax = Math.max(afterNode.getX(), xMax);

      ///// Find the Closest Point on the Polygon to the Circle /////

      if (magAfter < shortest) {
        shortest = magAfter;
        shortVector = cToVAfter;
      }

      ///// Interior Point Detection Section /////

      // Add one to the interior count if the vector from the circle's center crosses an edge of
      //  the polygon. With some exceptions for when it crosses two edges at once.
      inCount += interiorCross(beforeNode, midNode, afterNode, vAfter, pAfter, cP, xMax);

      ///// Collision Detection Code /////

      // Find collisions on corners.
      if (magAfter < c.getRadius() && magAfter > 0) {
        Vector endToEnd = afterNode.subi(beforeNode);

        // Make sure the corner edges make an angle less than 180 deg and is a valid corner
        //  corners will also always share v points.
        if (endToEnd.dot(pAfter) > 0 && endToEnd.dot(pBefore) < 0 && vBefore.equals(vAfter)) {
          Vector cornerNormal = cToVAfter.normalizei();
          m.normals.add(cornerNormal);
          // Scale the corner normal with the radius of the circle minus the distance from the
          //  center to get the penetration.
          m.vectors.add(cornerNormal.scalei(c.getRadius() - magAfter));
        }
      }

      // Make sure the point we are colliding with isn't an end point.
      boolean edgeCase = !(vAfter.equals(afterNode) || vAfter.equals(midNode));

      // Is the circle on the exterior side of the polygon, the point being reference
      //   against is within the radius of the circle, and isn't an end point.

      if (cToVAfter.dot(pAfter) <= 0 && magAfter < c.getRadius() && edgeCase) {
        Vector nAfter = pAfter.normalizei().scalei(-1);
        m.normals.add(nAfter);
        // Dot the normal of the edge with the vector from the center of the circle to the
        //  closest point on the line.
        // Because the vector from the center of the circle to the edge point is in the same
        //  direction from the edge normal we subtract the dot to the radius to get the penetration.
        m.vectors.add(nAfter.scalei(c.getRadius() - cToVAfter.dot(nAfter)));
      }

      // If the middle node is equal to the circle position, preform edge case calculations.
      if (cP.equals(midNode)) {
        Vector endToEnd = afterNode.subi(beforeNode);

        // Make sure the corner edges make an angle less than 180 deg and is a valid corner.
        if (endToEnd.dot(pAfter) < 0 && endToEnd.dot(pBefore) > 0) {
          Vector nAfter = pAfter.normalizei().scalei(-1);
          Vector nBefore = pBefore.normalizei().scalei(-1);

          m.normals.add(nAfter);
          m.normals.add(nBefore);
          m.vectors.add(nAfter.scalei(c.getRadius()));
          m.vectors.add(nBefore.scalei(c.getRadius()));
        } else {
          // Otherwise, average the two edges as a normal.
          Vector normal = pBefore.addi(pAfter).normalizei().scalei(-1);
          m.normals.add(normal);
          m.vectors.add(normal.scalei(c.getRadius()));
        }
      }

      // Cycle through the nodes.
      beforeNode = midNode;
      midNode = afterNode;

      vBefore = vAfter;
      pBefore = pAfter;
    }

    // If the euler count is odd then the circle is within the polygon.
    if (inCount % 2 == 1 && m.normals.isEmpty()) {
      // Add the closest point on the polygon plus the radius to the final position to get the
      //  circle out of the polygon.
      Vector normal = shortVector.normalizei().scalei(-1);
      Vector positionDiff = normal.scalei(shortest + c.getRadius());
      m.normals.add(normal);
      m.vectors.add(positionDiff);
    }

    m.aEulerCount = inCount;
    m.bEulerCount = Vector.sub(pP, cP).magSqr() < c.getRadius() * c.getRadius() ? 1 : 0;

    // If normals have been set, a collision has occurred so export the pertinent data.
    if (!m.normals.isEmpty()) {
      m.a = b;
      m.b = a;
      return m;
    }

    return null;
  }

  public static void draw() {
    RenderEngine.drawLine(new Vector(0, 0), new Vector(100, 200));
  }

  /**
   * Process a polygon to polygon shape collision. This algorithm uses Euler's bridge method to
   * detect if a polygon is inside a polygon, which is helpful to detect if one of the polygon's
   * edges doesn't collide with one of the other polygon's edges.
   *
   * @param a  Entity a
   * @param sA PolygonShape of Entity a
   * @param pA position of Entity a
   * @param b  Entity b
   * @param sB PolygonShape of Entity b
   * @param pB position of Entity b
   * @return a manifold representing
   */
  public static Manifold pollyPollyCollision(BasicEntity a, PolygonShape sA, Vector pA,
      BasicEntity b, PolygonShape sB, Vector pB) {

    ///// Variable Initialization /////

//    Game.log("=====");
//    Game.log(a, b);

    // set up the manifold object
    Manifold manifold = new Manifold();

    boolean collided = false; // has a edge-edge collision occurred
    int eulerCountA = 0; // crossings count for polygon a
    int eulerCountB = 0; // crossings count for polygon b

    // set up variables for nodes A, B, and C of polygon A and B
    Vector pA_nA = Vector.add(pA, sA.getVertices().get(sA.getVertices().size() - 2));
    Vector pA_nB = Vector.add(pA, sA.getVertices().get(sA.getVertices().size() - 1));

    Vector pB_nA = Vector.add(pB, sB.getVertices().get(sB.getVertices().size() - 2));
    Vector pB_nB = Vector.add(pB, sB.getVertices().get(sB.getVertices().size() - 1));

    Vector pA_nC, pB_nC;

    // find the maximum x value of the vertices calculated so far
    float xMax = Math.max(Math.max(pA_nB.getX(), pA_nA.getX()),
        Math.max(pB_nB.getX(), pB_nA.getX()));

    // list of floats representing the shortest separation distance from each vertex
    float[] distA = new float[sA.getVertices().size()];
    // list of normals representing the normals for the shortest separation vector
    Vector[] normalA = new Vector[sA.getVertices().size()];

    float[] distB = new float[sB.getVertices().size()];
    Vector[] normalB = new Vector[sB.getVertices().size()];

    // set all the distances to -1
    for (int i = 0; i < sA.getVertices().size(); i++) {
      distA[i] = -1;
    }

    for (int i = 0; i < sB.getVertices().size(); i++) {
      distB[i] = -1;
    }

    // loop through each polygon index
    // for each loop we pre-calculate/ lookup some variables
    for (int polA_i = 0; polA_i < sA.getSize(); polA_i++) {
      pA_nC = Vector.add(pA, sA.getVertex(polA_i)); // calculate the vertex's global position
      Vector polA_norm = pA_nC.perpi(pA_nB).normalizei(); // calculate the polygons normal value
      xMax = Math.max(pA_nC.getX(), xMax); // find the maximum x value
      Vector p1 = pA_nC.subi(pA_nB).normalizei();

      // Calculate if the line created by vertex B and C is crossed by the line
      //  stemming from the point pB projected infinitely to the right.
      //  The point A is used to detect edge cases and the xMax is used to project
      //  the line, effectively, to infinity.
      eulerCountA += interiorCross(pA_nA, pA_nB, pA_nC, pB, xMax);

      // preform a nested for loop comparision of each vertex of the first polygon to each
      //  vertex of the second polygon
      for (int polB_i = 0; polB_i < sB.getSize(); polB_i++) {
        // preform the same calculations at the start of the previous loop with the polygon 2's vertices
        pB_nC = Vector.add(pB, sB.getVertex(polB_i));
        xMax = Math.max(pB_nC.getX(), xMax);
        Vector polB_norm = pB_nC.perpi(pB_nB).normalizei();
        Vector p2 = pB_nC.subi(pB_nB).normalizei();

        // the interior cross only focuses on the second polygon's vertices and the first entities position
        //  so it only needs to happen once
        if (polA_i == 0) {
          eulerCountB += interiorCross(pB_nA, pB_nB, pB_nC, pA, xMax);
        }

        // calculate an exit distance based on the distance required to separate
        //  two vertices along the respective polygon normals
        float exitDistA = Vector.dot(Vector.sub(pA_nC, pB_nC), polA_norm);
        float exitDistB = Vector.dot(Vector.sub(pB_nC, pA_nC), polB_norm);

        float a2 = p2.dot(pA_nC.subi(pB_nC));
        float a1 = p1.dot(pB_nC.subi(pA_nC));
        float a3 = p2.dot(pA_nB.subi(pB_nB));
        float a4 = p1.dot(pB_nB.subi(pA_nB));
        float o = 0.05f;


//        Game.log(exitDistA, exitDistB, pA, pB, a1, a2,  a3, a4 , pA_nB, pA_nC, pB_nB, pB_nC, sA.getVertex(polA_i), sB.getVertex(polB_i), p1, p2, polA_norm, polB_norm);
        Vector currentPolA_norm = polA_norm;
        Vector currentPolB_norm = polB_norm;


//        if ((a1 > -o && a1 < 0) || (a3 < o && a3 > 0)) {
////          currentPolA_norm = pointedPVector(polA_norm, pB.subi(pA));
//////          Game.log("p1", currentPolA_norm);
////        }
////
////        if ((a2 > -o && a2 < 0) || (a4 < o && a4 > 0)) {
////          currentPolB_norm = pointedPVector(polB_norm, pA.subi(pB));
//////          Game.log("p2", currentPolB_norm);
////        }



        // if the exit distance is grater than or equal to zero,
        //  compare the current separation distance to the maximum of the current vertex
        if (exitDistA >= 0.0 && distA[polA_i] <= exitDistA) {
          // set the distance and normal to the new value
          distA[polA_i] = exitDistA;
          normalA[polA_i] = currentPolA_norm;
//          Game.log("A", polA_norm, polA_i);
        }

        // repeat these steps with the other polygon
        if (exitDistB >= 0.0 && distB[polB_i] <= exitDistB) {
          distB[polB_i] = exitDistB;
          normalB[polB_i] = currentPolB_norm;
//          Game.log("B", polB_norm, polB_i);
        }


        // if the lines created by the C and B vertices collide, the polygons have collided
        if (lineLineCollision(pA_nB, pA_nC, pB_nB, pB_nC)) {
          collided = true;
        }

        // set the middle vertex to the last and the current vertex to the middle vertex
        pB_nA = pB_nB;
        pB_nB = pB_nC;
      }

      // set the middle vertex to the last and the current vertex to the middle vertex
      pA_nA = pA_nB;
      pA_nB = pA_nC;

    }

    // find the normal with the shortest separation distance from both polygon's list
    float shortest_value = Float.MAX_VALUE;
    Vector shortest_normal = null;

    for (int index = 0; index < sA.getVertices().size(); index++) {
      if (distA[index] <= shortest_value && distA[index] != -1) {
        shortest_normal = normalA[index].negatei();
        shortest_value = distA[index];
      }
    }


    for (int index = 0; index < sB.getVertices().size(); index++) {
      if (distB[index] <= shortest_value && distB[index] != -1) {
        shortest_normal = normalB[index];
        shortest_value = distB[index];
      }
    }

    manifold.aEulerCount = eulerCountA;
    manifold.bEulerCount = eulerCountB;

    // if the polygons collided or the euler count for either polygon is odd the polygons are colliding
    if (collided || eulerCountA % 2 == 1 || eulerCountB % 2 == 1 && shortest_normal != null) {
      manifold.normals.add(shortest_normal);
      manifold.vectors.add(shortest_normal.scalei(shortest_value));
      manifold.a = a;
      manifold.b = b;
      return manifold;
    }

    return null;
  }

  public static int interiorCross(Vector bNode, Vector mNode, Vector aNode, Vector p, float xMax) {
    Vector vAfter = pnt2line(p, mNode, aNode);
    Vector pAfter = aNode.subi(mNode).perpi();

    return interiorCross(bNode, mNode, aNode, vAfter, pAfter, p, xMax);
  }

  /**
   * Detect if a vector stemming from p going to a position with the same y value but with an x
   * value of xMax (this represents the maximum x value in a polygon) crosses the line from midNode
   * to afterNode. The cross does not include the afterNode end position. This algorithm also
   * detects if a cross through the midNode is through a interior vertex (a point where the p vector
   * penetrates the polygon between two edges) and should be counted, or an exterior vertex (a point
   * where the p vector glances the vertex of a polygon) and shouldn't be counted. vAfter and pAfter
   * are used to detect if the p position lies on the leading side of one of the polygon's edges and
   * should be counted, or it lies on the trailing edge of the polygon's edge and shouldn't. <br>
   * <br>
   * <b>All nodes must be on the global coordinate plane.</b>
   *
   * @param beforeNode starting node of the polygon
   * @param midNode    middle node of the polygon
   * @param afterNode  ending node of the polygon
   * @param vAfter     point on the line made by midNode to afterNode that is closest to the p
   *                   position
   * @param pAfter     normal vector pointing outward from the edge made by midNode and afterNode
   * @param p          position being checked
   * @param xMax       maximum x position of the pointing vector
   * @return 1 if the vector from point p crosses the midNode to afterNode line and 0 if it doesn't
   */
  @SuppressWarnings("Duplicates")
  public static int interiorCross(Vector beforeNode, Vector midNode, Vector afterNode,
      Vector vAfter, Vector pAfter, Vector p, float xMax) {
    // Ensure the endNode is not on the same y plane as the position
    //  this is to prevent a tracing line that lies on the border of two edges from being
    //  counted twice.
    if (afterNode.getY() != p.getY()) {
      // Calculate an endpoint that is on the same plane as the circle but is further to the right
      //  then any of the points in the polygon.
      Vector checkEnd = new Vector(xMax + 1, p.getY());

      // If the position is on one of the edges of the polygon make sure it doesn't
      //  lie on the trailing edge of the polygon.
      // This is important because points on the trailing edge need to be counted but not points
      //  on leading edges. This is an artifact on how euler's algorithm detects if a point is inside
      //  a polygon.

      boolean collided = vAfter.equals(p) ? pAfter.dot(checkEnd.subi(p)) > 0.0
          : Engine.lineLineCollision(afterNode, midNode, p, checkEnd);

      // If the tracing lines collide and don't cross the start point of the edge,
      //  add one to the count.
      if (collided && midNode.getY() != p.getY()) {
        return 1;
      } else if (collided) {
        // Otherwise, the point must cross the start point.
        // In this case we check the points before and after the start point.
        // If the check line crosses this new line we know that the line passes through an
        //  interior vertex where two edges meet. Otherwise we know that the line passed through
        //  an exterior vertex and shouldn't be counted.

        // We shift the x position of the line endpoints in case the position is in an
        //  interior vertex that includes points to the left of the check position.
        Vector beforeNodeX = new Vector(xMax, beforeNode.getY());
        Vector afterNodeX = new Vector(xMax, afterNode.getY());

        // Preform a line line collision excluding endpoints.
        if (Engine.interiorLineLineCollision(beforeNodeX, afterNodeX, p, checkEnd)) {
          return 1;
        }
      }
    }

    return 0;
  }

  /**
   * Detect if all of shape e's positions are inside the other in the collision manifold.
   *
   * @param entity    Entity e
   * @param manifolds list of collision manifolds
   * @return true if b is inside a, false otherwise
   */
  public static boolean isInside(Entity entity, ArrayList<Manifold> manifolds) {
    for (Manifold m : manifolds) {
      if (m.a == entity) {
        if (m.bEulerCount % 2 != 1) {
          return false;
        }
      } else {
        if (m.aEulerCount % 2 != 1) {
          return false;
        }
      }
    }

    return true;
  }



  // </editor-fold>
}
