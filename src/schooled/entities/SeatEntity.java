package schooled.entities;

import schooled.Game;
import schooled.entities.Player.Pose;
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
public class SeatEntity extends ContainerEntity {

  private static final Vector DEFAULT_CHILD_POSITION = new Vector(3.5, -1.5);

  /**
   * Constructor to create a seat entity from a basic entity.
   *
   * @param be the basic entity
   */
  public SeatEntity(Entity be) {
    super(be);
    setChildPosition(DEFAULT_CHILD_POSITION);
  }

  public SeatEntity(Game be) {
    super(be);
    setChildPosition(DEFAULT_CHILD_POSITION);
  }

  @Override
  public void playerRemoved(Player p) {
    movePlayer(p);
    p.setSitting(false, null);
  }

  public SeatEntity setClone(SeatEntity seatEntity) {
    super.setClone(seatEntity);
    return seatEntity;
  }

  @Override
  public BasicEntity setClone(BasicEntity entity) {
    if (entity instanceof SeatEntity) {
      return setClone(this.getClass().cast(entity));
    } else {
      return super.setClone(entity);
    }
  }

  @Override
  public Entity clone() {
    return setClone(new SeatEntity(this));
  }

  @Override
  public void playerSet(Player p) {
    p.setPosition(Vector.zero.clone());
    p.setSitting(true, getCustomPose() != null ? getCustomPose() : Pose.SITTING);
  }

  @Override
  public float getStaticFriction() {
    return 3;
  }
}
