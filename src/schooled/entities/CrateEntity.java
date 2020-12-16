package schooled.entities;

import schooled.Game;
import schooled.physics.PolygonShape;
import schooled.physics.Shape;
import schooled.physics.Vector;
import schooled.visuals.sprite.Sprite;

/**
 * An Entity to represent a crate or box.
 * <p></p>
 * Program written by Joseph Straceski
 * <p>
 * contact: straceski.joseph@gmail.com, https://github.com/Crepox
 */
public class CrateEntity extends Entity {

  private boolean breakable = false; // is the crate breakable

  /**
   * Construct a CrateEntity and set its breakability.
   *
   * @param g         the game instance
   * @param position  the crate position
   * @param shape     the shape
   * @param mass      the mass
   * @param breakable is the crate breakable or not
   */
  public CrateEntity(Game g, Vector position, Shape shape, float mass,
      boolean breakable) {
    super(g, position, shape, mass);
    this.breakable = breakable;
    setSpriteShift(new Vector(0, -14));
  }

  /**
   * Construct a crate entity from basic entity data.
   *
   * @param g        the game instance
   * @param position the crate position
   * @param shape    the shape
   * @param mass     the mass
   */
  public CrateEntity(Game g, Vector position, Shape shape, float mass) {
    this(g, position, shape, mass, false);
  }

  /**
   * Construct a crate from a BasicEntity.
   *
   * @param be the game instance
   */
  public CrateEntity(Entity be) {
    super(be);
    setSpriteShift(new Vector(0, -7));
  }

  public CrateEntity(Game be) {
    super(be);
    setSpriteShift(new Vector(0, -7));
  }

  @Override
  public Sprite getSprite() {
    if (hasSprite()) {
      return super.getSprite();
    }
    return getGame().getImage("Crate");
  }

  /**
   * Check if the crate is breakable.
   *
   * @return if the crate is breakable return true
   */
  public boolean isBreakable() {
    return breakable;
  }

  /**
   * Set if the crate can be broken.
   */
  public void setBreakable(boolean breakable) {
    this.breakable = breakable;
  }

  /**
   * Custom command to break apart a crate, and create a few smaller crate objects.
   */
  public void breakCrate() {
    Sprite i = getSprite();
    Vector v = getPosition();
    float speed = 0.1f;
    Vector shift = this.getSpriteShift().scalei(0.5f);

    PolygonShape ps = ((PolygonShape) getShape());
    int l = ((int) (ps.getHeight() / 4.0f));
    int lpluss = ps.getHeight() / 2.0f > l + l ? 1 : 0;
    int d = ((int) (ps.getWidth() / 4.0f));
    int dpluss = ps.getWidth() / 2.0f > d + d ? 1 : 0;

    int h = i.getHeight() / 2;
    int hpluss = i.getHeight() > h + h ? 1 : 0;
    int w = i.getWidth() / 2;
    int wpluss = i.getWidth() > h + h ? 1 : 0;

    Vector ve1 = new Vector(l + lpluss / 2.0f, d + dpluss / 2.0f);
    CrateEntity ce = new CrateEntity(getGame(), Vector.add(v, ve1),
        new PolygonShape(d, d + dpluss, l, l + lpluss), this.getMass() / 4);
    ce.setSprite(i.getSubSpr(w, h, w + wpluss, h + hpluss));
    ce.setVelocity(ve1.scalei(speed));
    ce.setSpriteShift(shift);
    getContainer().addEntity(ce); // bottom right

    Vector ve2 = new Vector(l + dpluss / 2.0f, -d);
    CrateEntity ce2 = new CrateEntity(getGame(), Vector.add(v, ve2),
        new PolygonShape(d, d + dpluss, l, l), this.getMass() / 4);
    ce2.setSprite(i.getSubSpr(w, 0, w, h + hpluss));
    ce2.setVelocity(ve2.scalei(speed));
    ce2.setSpriteShift(shift);
    getContainer().addEntity(ce2); // top rigth

    Vector ve3 = new Vector(-l, -d);
    CrateEntity ce3 = new CrateEntity(getGame(), Vector.add(v, ve3),
        new PolygonShape(d, d, l, l), this.getMass() / 4);
    ce3.setSprite(i.getSubSpr(0, 0, w, h));
    ce3.setVelocity(ve3.scalei(speed));
    ce3.setSpriteShift(shift);
    getContainer().addEntity(ce3); // top left

    Vector ve4 = new Vector(-l, d + lpluss / 2.0f);
    CrateEntity ce4 = new CrateEntity(getGame(), Vector.add(v, ve4),
        new PolygonShape(d, d, l, l + lpluss), this.getMass() / 4);
    ce4.setSprite(i.getSubSpr(0, h, w + wpluss, h));
    ce4.setVelocity(ve4.scalei(speed));
    ce4.setSpriteShift(shift);
    getContainer().addEntity(ce4); // bottom left
    //ce4.setBreakable(true);

    getContainer().getEntities().remove(this);
  }

  @Override
  public boolean collision(BasicEntity e) {
    if (e instanceof Player && breakable) {
      if (e.getVelocity().mag() - getVelocity().mag() > 3) {
        breakCrate();
      }
    }
    return true;
  }

  @Override
  public boolean interact(Entity e) {
    if (breakable) {
      breakCrate();
      return true;
    }
    return false;
  }
}
