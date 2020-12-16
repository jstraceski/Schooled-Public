package schooled.entities;

import org.lwjgl.system.CallbackI.V;
import schooled.Game;
import schooled.physics.Vector;

/**
 * A Seat entity.
 * <p>
 * Stores data for entities players can sit in.
 * <p></p>
 * Program written by Joseph Straceski
 * <p>
 * contact: straceski.joseph@gmail.com, https://github.com/Crepox
 */
public class VariableSeatEntity extends SeatEntity {

  private static final Vector DEFAULT_CHILD_POSITION = new Vector(3.5, -1.5);
  private Vector min = new Vector(3.5, -1.5).addi(new Vector(0, -3));
  private Vector max = new Vector(3.5, -1.5).addi(new Vector(0, 3));
  private float scale = 0.50f;
  private float deviation = 0.1f;

  private Vector vel = new Vector();

  /**
   * Constructor to create a seat entity from a basic entity.
   *
   * @param be the basic entity
   */
  public VariableSeatEntity(Entity be) {
    super(be);
    setChildPosition(DEFAULT_CHILD_POSITION);
  }

  public VariableSeatEntity(Game be) {
    super(be);
    setChildPosition(DEFAULT_CHILD_POSITION);
  }


  @Override
  public void playerRemoved(Player p) {
    movePlayer(p);
    p.setSitting(false, null);
  }

  public VariableSeatEntity setClone(VariableSeatEntity variableSeatEntity) {
    super.setClone(variableSeatEntity);

    variableSeatEntity.min = min;
    variableSeatEntity.max = max;
    variableSeatEntity.scale = scale;
    variableSeatEntity.deviation = deviation;
    variableSeatEntity.vel = new Vector(vel);

    return variableSeatEntity;
  }

  @Override
  public BasicEntity setClone(BasicEntity entity) {
    if (entity instanceof VariableSeatEntity) {
      return setClone(this.getClass().cast(entity));
    } else {
      return super.setClone(entity);
    }
  }

  @Override
  public Entity clone() {
    return new VariableSeatEntity(this);
  }

  @Override
  public void playerSet(Player p) {
    super.playerSet(p);
  }

  @Override
  public void updateCycle(float t) {
    super.updateCycle(t);
    if (!vel.roughEquals(Vector.zero, 0.01f)) {
      Vector nPos = getChildPosition().addi(vel);

      nPos = Vector.max(nPos, min);
      nPos = Vector.min(nPos, max);

      setChildPosition(nPos);

      vel.scale(0.75f);
    }
  }

  @Override
  public boolean blockInput(Entity player, Vector vector) {
    float d = scale + (float) (deviation * Math.random());
    vel = vector.scalei(d);
    return true;
  }

  @Override
  public float getStaticFriction() {
    return 3;
  }
}