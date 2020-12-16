package schooled.entities;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import schooled.Game;
import schooled.datatypes.Tuple;
import schooled.entities.Player.Pose;
import schooled.event.Event;
import schooled.event.events.Move;
import schooled.physics.Shape;
import schooled.physics.Vector;
import schooled.visuals.sprite.Sprite;

/**
 * An abstraction for entities that can contain players.
 * <p></p>
 * Program written by Joseph Straceski
 * <p>
 * contact: straceski.joseph@gmail.com, https://github.com/Crepox
 */
public abstract class ContainerEntity extends EnterableEntity {

  private static final Vector DEFAULT_CHILD_POSITION = new Vector(0, 0);

  private Pose customPose = null;

  private Vector exitDownVector = new Vector(0, 7);
  private Vector exitUpVector = new Vector(0, -7);

  private boolean enterFlag = true;

  private boolean exitUp = true, exitLeft = false, exitRight = false, exitDown = true;

  private ArrayList<Vector> exitNormals = new ArrayList<>();

  public ContainerEntity setClone(ContainerEntity containerEntity) {
    super.setClone(containerEntity);
    containerEntity.exitDownVector = exitDownVector;
    containerEntity.exitUpVector = exitUpVector;

    containerEntity.enterFlag = enterFlag;
    containerEntity.customPose = customPose;

    containerEntity.exitUp = exitUp;
    containerEntity.exitLeft = exitLeft;
    containerEntity.exitRight = exitRight;
    containerEntity.exitDown = exitDown;

    ArrayList<Vector> eNorms = new ArrayList<>();
    for (Vector vector : exitNormals) {
      eNorms.add(vector.clone());
    }

    containerEntity.exitNormals = eNorms;

    return containerEntity;
  }

  public void clearExitNormals() {
    this.exitNormals.clear();
  }

  public void addExitNormal(Vector exitNormal) {

    List<Consumer<Boolean>> flagList = List.of(
        (a) -> exitUp = a, (a) -> exitDown = a,
        (a) -> exitLeft = a, (a) -> exitRight = a);

    Vector[] vectorList = {Vector.up, Vector.down, Vector.left, Vector.right};

    for (int i = 0; i < 4; i++) {
      if (vectorList[i].dot(exitNormal) > 0.99) {
        flagList.get(i).accept(false);
      }
    }

    this.exitNormals.add(exitNormal);
  }

  /**
   * Constructor for a container entity using the basic game component.
   *
   * @param g    the game instance
   */
  public ContainerEntity(Game g) {
    super(g);
    setChildPosition(DEFAULT_CHILD_POSITION);
  }

  /**
   * Constructor for a container entity using a basic entity.
   *
   * @param basicEntity the basic entity
   */
  public ContainerEntity(Entity basicEntity) {
    super(basicEntity);
    setChildPosition(DEFAULT_CHILD_POSITION);
  }

  /**
   * Remove the player from the container entity.
   *
   * @param player the player entity
   */
  public void movePlayer(Player player) {
    Vector facing = (player.isFacing(Vector.up) && exitUp) ? exitUpVector : exitDownVector;
    player.setFacingVector(facing);

    player.setPosition(facing.addi(getLocalPosition()));

    if (hasParent()) {
      getParent().addChild(player);
    }
  }

  /**
   * A function that gets called when a player is set as the contained entity.
   *
   * @param p the player object
   */
  public abstract void playerSet(Player p);

  /**
   * A function that gets called when a player exits the container entity.
   *
   * @param p the player object
   */
  public abstract void playerRemoved(Player p);

  @Override
  public void addChild(Entity entity) {
    super.addChild(entity);

    if (entity instanceof Player) {
      playerSet((Player) entity);
    }
  }

  @Override
  public void removeChild(Entity entity) {
    super.removeChild(entity);

    if (entity instanceof Player) {
      playerRemoved((Player) entity);
    }
  }

  /**
   * Remove a player from the container. (Used to prevent unnecessary instanceof checks)
   *
   * @param player the player entity
   */
  public void removePlayer(Player player) {
    if (getExitEvent() != null) {
      getExitEvent().act(player);
    }

    super.removeChild(player);
    playerRemoved(player);
  }

  /**
   * Set the player as the entity of the container. (Used to prevent unnecessary instanceof checks)
   *
   * @param player the player entity
   */
  public void setPlayer(Player player) {
    if (getEnterEvent() != null) {
      getEnterEvent().act(player);
    }

    super.addChild(player);
    playerSet(player);
  }

  public Pose getCustomPose() {
    return customPose;
  }

  public void setCustomPose(Pose customPose) {
    this.customPose = customPose;
  }

  /**
   * Toggle the given player in the container.
   *
   * @param p the player object
   */
  public void toggleContainer(Player p) {
    if (p.isParent(this)) {
      removePlayer(p);
    } else {
      setPlayer(p);
    }
  }

  @Override
  public boolean interact(Entity e) {
    super.interact(e);
    if (e instanceof Player) {
      if (enterFlag) {
        toggleContainer((Player) e);
      }
    }
    return false;
  }

  @Override
  public boolean collision(BasicEntity e) {
    if (e instanceof Entity && this.equals(((Entity) e).getParent())) {
      this.cancelCollision();
    }

    return true;
  }
}