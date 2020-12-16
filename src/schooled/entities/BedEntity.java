package schooled.entities;

import schooled.Game;
import schooled.physics.Vector;

/**
 * A Bed entity.
 * <p>
 * Stores data for entities players can sleep in.
 * <p></p>
 * Program written by Joseph Straceski
 * <p>
 * contact: straceski.joseph@gmail.com, https://github.com/Crepox
 */
public class BedEntity extends SeatEntity {

  private static final Vector DEFAULT_CHILD_POSITION = new Vector(0, -1.5);
  private boolean easyExit = true;

  /**
   * Bed entity constructor. Creates a bed entity from a basic entity.
   *
   * @param be the basic entity
   */
  public BedEntity(Entity be) {
    super(be);
    setChildPosition(DEFAULT_CHILD_POSITION);
  }

  public BedEntity(Game g) {
    super(g);
    setChildPosition(DEFAULT_CHILD_POSITION);
  }

  public BedEntity setClone(BedEntity bedEntity) {
    super.setClone(bedEntity);
    bedEntity.easyExit = easyExit;
    return bedEntity;
  }

  @Override
  public Entity clone() {
    return setClone(new BedEntity(this));
  }

  @Override
  public BasicEntity setClone(BasicEntity entity) {
    if (entity instanceof BedEntity) {
      return setClone(this.getClass().cast(entity));
    } else {
      return super.setClone(entity);
    }
  }

  @Override
  public void playerRemoved(Player p) {
    movePlayer(p);
    p.setSleeping(false);
  }

  @Override
  public void playerSet(Player p) {
    p.setPosition(Vector.zero.clone());
    p.setSleeping(true);
  }

  int struggleCount = 0;
  float shakeTime = 0;
  float shakeDuration = 0;
  float shakeFrequency = 0.1f;
  float freqTime = 0;
  float defaultDuration = 0.2f;
  boolean struggleExit = true;

  @Override
  public boolean interact(Entity e) {
    if (e instanceof Player && e.isParent(this)) {
      if (allowInput(e, ((Player) e).getFacing())) {
        return super.interact(e);
      }
    } else {
      return super.interact(e);
    }

    return false;
  }

  public boolean blockInput(Entity player, Vector vector) {
    if (struggleExit) {
      boolean allowInput = allowInput(player, vector);

      if (allowInput) {
        super.interact(player);
      }

      return allowInput;
    }

    return true;
  }

  public boolean allowInput(Entity player, Vector vector) {

    if (shakeTime <= 0) {
      struggleCount++;
    }

    if (struggleCount > 10) {
      struggleExit = false;
      return true;
    } else {
      shakeDuration = defaultDuration;
      freqTime = shakeFrequency;
    }

    return false;
  }

  private Vector pOffset = null;

  @Override
  public void updateCycle(float t) {
    if (shakeDuration > 0) {
      if (pOffset == null) {
        pOffset = getChildOffset() == null ? Vector.zero : getChildOffset();
      }

      shakeTime += t;
      freqTime += t;

      if (freqTime >= shakeFrequency) {
        float a = (float) (2 * Math.random() + 1);
        Vector v = Vector.up.roti((float) (Math.PI * 2 * Math.random())).scalei(a);

        setChildOffset(pOffset.addi(v));
        freqTime = 0;
      }

      if (shakeTime >= shakeDuration) {
        shakeTime = 0;
        shakeDuration = 0;
        setChildOffset(pOffset);
        pOffset = null;
      }
    }

  }
}
