package schooled.entities;

import schooled.Game;
import schooled.event.Event;
import schooled.physics.Shape;
import schooled.physics.Vector;

public abstract class EnterableEntity extends Entity {

  public EnterableEntity(Game g) {
    super(g);
  }

  public EnterableEntity(Entity be) {
    super(be);
  }

  public EnterableEntity(Game g, Vector v, Shape s, float mass) {
    super(g, v, s, mass);
  }

  private Event exitEvent = null, enterEvent = null;

  public EnterableEntity setClone(EnterableEntity enterableEntity) {
    super.setClone(enterableEntity);

    if (exitEvent != null) {
      enterableEntity.exitEvent = exitEvent.clone();
    }

    if (enterEvent != null) {
      enterableEntity.enterEvent = enterEvent.clone();
    }

    return enterableEntity;
  }

  public void setEnterEvent(Event enterEvent) {
    this.enterEvent = enterEvent;
  }

  public void setExitEvent(Event exitEvent) {
    this.exitEvent = exitEvent;
  }

  public Event getExitEvent() {
    return exitEvent;
  }

  public Event getEnterEvent() {
    return enterEvent;
  }
}
