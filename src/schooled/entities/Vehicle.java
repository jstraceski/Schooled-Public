/**
 *
 */
package schooled.entities;

import java.awt.Color;
import java.util.ArrayList;
import schooled.Game;
import schooled.Window;
import schooled.containers.EntityHolder;
import schooled.containers.Room;
import schooled.engines.Engine;
import schooled.engines.RenderEngine;
import schooled.physics.BoundingBox;
import schooled.physics.Manifold;
import schooled.physics.MultiShape;
import schooled.physics.PolygonShape;
import schooled.physics.Shape;
import schooled.physics.Vector;
import schooled.visuals.sprite.LayeredSprite;
import schooled.visuals.sprite.Sprite;

public class Vehicle extends EnterableEntity {

  enum VehicleLayer {
    Base, Seats, Accesories, Top
  }

  private Sprite all, inside;
  public Shape tAreaShape = null, iAreaShape = null, iShape = null;
  private EntityArea tArea = null;

  public void updateLShapes() {
    if (hasShape("inside_shape")) {
      setInteriorShape(getShape("inside_shape"));
    }

    if (hasShape("inside_area")) {
      setInteriorArea(getShape("inside_area"));
    }

    if (hasShape("transition")) {
      setTransitionShape(getShape("transition"));
    }
  }

  @Override
  public void updateLSprites() {
    Sprite roof = null, base = null;

    if (hasLSprite("roof")) {
      roof = getLSprite("roof");
    }

    if (hasLSprite("#base")) {
      base = getLSprite("#base");
    }

    all = new LayeredSprite(roof, base);
    inside = base;
  }

  public void setTransitionShape(Shape newAreaShape){
    newAreaShape.setDebugColor(Color.green);
    if (tArea == null) {
      tArea = new EntityArea(getGame(), newAreaShape, this::vInteract);
      addChild(tArea);
    } else {
      tArea.setShape(newAreaShape);
    }
    tAreaShape = newAreaShape;
  }

  public void setInteriorArea(Shape newAreaShape){
    newAreaShape.setDebugColor(Color.orange);
    iAreaShape = newAreaShape;
  }

  public void setInteriorShape(Shape newShape){
    newShape.setDebugColor(Color.yellow);
    iShape = newShape;
  }

  private boolean vInteract(Entity e, ArrayList<Manifold> m) {
    if (e instanceof Player && !e.isParent(this)) {
      enterFunction(e);
    }
    return true;
  }

  public void exitFunction(Entity e) {
    if (getExitEvent() != null) {
      getExitEvent().act(e);
    }

    e.addPosition(getPosition());
    removeChild(e);

    player = false;
  }

  public void enterFunction(Entity e) {
    if (getEnterEvent() != null) {
      getEnterEvent().act(e);
    }

    e.setPosition(e.getPosition().subi(getPosition()));
    addChild(e);

    player = true;
  }

  public Vehicle(Game g) {
    super(g);
  }

  public Vehicle(Entity e) {
    super(e);
  }


  public void setSpriteData(Sprite inside, Sprite all) {
//    this.inside = inside;
//    inside.setState(State.IN);
//    this.all = all;
//    all.setState(State.OUT);
//
//    updateImage();
  }

  @Override
  public Shape getShape(BasicEntity entity) {
    if (entity.isParent(this)) {
      return iShape;
    }

    return super.getShape(entity);
  }

  @Override
  public BoundingBox getBoundingBox(BasicEntity entity) {
    if (entity.isParent(this)) {
      return iShape.getBoundingBox();
    }

    return super.getBoundingBox(entity);
  }

  @Override
  public void updateCycle() {
    updateImage();

    for (Entity e : getAllChildren()) {
      if (!Engine.collides(iAreaShape, getPosition(), e) && !Engine.collides(tAreaShape, getPosition(), e) && e instanceof Player) {
        exitFunction(e);
      }
    }
  }

  private boolean player = false;
  private boolean lastPlayer = !player;

  public void updateImage() {
    if (lastPlayer != player) {
      if (player) {
        setSprite(inside);
      } else {
        setSprite(all);
      }
    }
    lastPlayer = player;
  }

  @Override
  public Sprite getSprite() {
    if (hasSprite()) {
      return super.getSprite();
    }
    return getGame().getSprite("Car");
  }

  @Override
  public void setState(State state) {
    super.setState(state);
    if (all != null)
      all.setState(state);
    if (inside != null)
      inside.setState(state);
    if (iShape != null)
      iShape.setState(state);
    if (tAreaShape != null)
      tAreaShape.setState(state);
    if (iAreaShape != null)
      iAreaShape.setState(state);
    if (tArea != null)
      tArea.setState(state);
  }
}

