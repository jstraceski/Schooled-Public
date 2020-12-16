/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package schooled.visuals.sprite;

import java.util.ArrayList;
import java.util.Arrays;
import schooled.Game;
import schooled.entities.State;
import schooled.menu.Origin;
import schooled.physics.Vector;

/**
 * A sprite with multiple layers.
 * <p>
 * Stores the layered sprite as a list of sprites and handles modification and generation methods from the parent sprite class.
 * <p></p>
 * Program written by Joseph Straceski
 * <p>
 * contact: straceski.joseph@gmail.com, https://github.com/Crepox
 */
public class LayeredSprite extends Sprite {

  private ArrayList<Sprite> images; // image list

  /**
   * Empty Constructor, creates the sprite list.
   */
  public LayeredSprite() {
    images = new ArrayList<>();
  }

  /**
   * Create a layered sprite out a single sprite.
   * @param sprite input sprite
   */
  public LayeredSprite(Sprite sprite) {
    this();
    addSprite(sprite);
  }

  public LayeredSprite(Sprite l1, Sprite l2) {
    this();
//
//    float max = Float.MIN_VALUE;
//    float min = Float.MAX_VALUE;
//
//
//    Sprite high = null;
//    Sprite low = null;
//
//    if (l1 != null && l2 != null) {
//      high = l1.getLocalLayer() > l2.getLocalLayer() ? l1 : l2;
//      low = l1.getLocalLayer() <= l2.getLocalLayer() ? l1 : l2;
//    } else if (l1 != null) {
//      low = l1;
//    } else if (l2 != null) {
//      low = l2;
//    }
//
//    ArrayList<Sprite> low_sprites = new ArrayList<>();
//    if (low != null) {
//      Game.log(low);
//      if (low instanceof LayeredSprite) {
//        low_sprites = ((LayeredSprite) low).getSprites();
//      } else {
//        low_sprites.add(low);
//      }
//    }
//
//    ArrayList<Sprite> high_sprites = new ArrayList<>();
//    if (high != null) {
//      if (high instanceof LayeredSprite) {
//        high_sprites = ((LayeredSprite) high).getSprites();
//      } else {
//        high_sprites.add(high);
//      }
//    }
//
//    for (Sprite sprite : low_sprites) {
//      Game.log(sprite);
//      max = Math.max(max, sprite.getLocalLayer());
//    }
//
//    for (Sprite sprite : high_sprites) {
//      min = Math.min(min, sprite.getLocalLayer());
//    }
//
//    float offset = max - min + 1;
//
//    for (Sprite sprite : low_sprites) {
//      addSprite(sprite);
//    }
//
//    for (Sprite sprite : high_sprites) {
//      addSprite(sprite,sprite.getLocalLayer() + offset);
//    }
    setName(l1.getName() + "_"+ l2.getName());
    addSprite(l1);
    addSprite(l2);
  }

  public LayeredSprite(LayeredSprite l1, LayeredSprite l2) {
    this();
    setName(l1.getName() + "_"+ l2.getName());

    float max = Float.MIN_VALUE;
    float min = Float.MAX_VALUE;

    LayeredSprite high = l1.getLocalLayer() > l2.getLocalLayer() ? l1 : l2;
    LayeredSprite low = l1.getLocalLayer() <= l2.getLocalLayer() ? l1 : l2;

    for (Sprite sprite : low.getSprites()) {
      max = Math.max(max, sprite.getLocalLayer());
    }

    for (Sprite sprite : high.getSprites()) {
      min = Math.min(min, sprite.getLocalLayer());
    }

    float offset = max - min + 1;

    for (Sprite sprite : low.getSprites()) {
      addSprite(sprite);
    }

    for (Sprite sprite : high.getSprites()) {
      addSprite(sprite,sprite.getLocalLayer() + offset);
    }
  }

  public String toString() {
    return "l_sprite:[l:" + getLocalLayer() + ", " + Arrays.toString(getSprites().toArray()) + "]";
  }

  /**
   * Create a clone of the layered sprite.
   *
   * Clones every sprite in the layer list as well.
   *
   * @return layered sprite clone
   */
  @Override
  public LayeredSprite clone() {

    LayeredSprite l = new LayeredSprite();
    super.setClone(l);
    for (Sprite s : images) {
      Sprite sprite = s.clone();
      sprite.setParent(this);

      l.addSprite(sprite);
    }
    return l;
  }

  @Override
  public void update(float l) {
    getSprites().forEach(sprite -> sprite.update(l));
  }

  /**
   * Is the sprite layered.
   *
   * @return true
   */
  @Override
  public boolean isLayered() {
    return true;
  }

  @Override
  public void setState(State state) {
    images.forEach(so -> so.setState(state));
  }



  /**
   * Add a sprite to the layered sprite list.
   *
   * @param sprite sprite to add
   */
  public void addSprite(Sprite sprite) {
    if (sprite != null) {
      sprite.setParent(this);
      images.add(sprite);


    }
  }

  /**
   * Add a sprite and set its local layer.
   * @param sprite sprite to add
   * @param i local layer
   */
  public void addSprite(Sprite sprite, float i) {
    if (sprite != null) {
      sprite.setLocalLayer(i);
      addSprite(sprite);
    }
  }

  /**
   * Get the list of layered sprites.
   *
   * @return sprite list
   */
  public ArrayList<Sprite> getSprites() {
    return images;
  }

  @Override
  public boolean isFinished() {
    return images.stream().allMatch(Sprite::isFinished);
  }

  @Override
  public boolean atFinishPoint() {
    return images.stream().allMatch(Sprite::atFinishPoint);
  }
  
  /**
   * Does the layered sprite have data.
   *
   * @return true if there are any images that have data in the sprite list, false otherwise
   */
  @Override
  public boolean hasData() {
    boolean data = false;
    for (Sprite o : images) {
      if (o.hasData()) {
        data = true;
      }
    }
    return data;
  }

  /**
   * Cut out the same position on every sprite in the sprite list.
   *
   * @param x local x position
   * @param y local y position
   * @param width sub-sprite width
   * @param height sub-sprite height
   * @return
   */
  @Override
  public Sprite getSubSpr(float x, float y, float width, float height) {
    LayeredSprite ls = new LayeredSprite();
    for (Sprite so : images) {
      Sprite s2 = so.getSubSpr(x, y, width, height);
      ls.addSprite(s2);
    }
    return ls;
  }

  /**
   * Scale all of the layered sprites by the same scalar.
   *
   * @param f input scalar
   * @return scaled instance
   */
  @Override
  public Sprite scale(float f) {
    LayeredSprite ls = new LayeredSprite();
    for (Sprite so : images) {
      ls.addSprite(so.scale(f));
    }
    return ls;
  }

  @Override
  public Vector getSize() {
    return new Vector(0, 0);
  }

  @Override
  public Origin getOrigin() {
    return Origin.TOP_LEFT;
  }

  /**
   * Clears the scales for each individual sprite in the list of sprites.
   */
  @Override
  public void clearScales() {
    images.forEach(Sprite::clearScales);
  }

  /**
   * Generate the input scale for each individual sprite in the list of sprites.
   * @param f input scalar
   */
  @Override
  public void generateScale(float f) {
    images.forEach((so) -> so.generateScale(f));
  }

  /**
   * Reset the base scale for each individual sprite in the list of sprites.
   * @param f base scale
   */
  @Override
  public void resetBaseScale(float f) {
    images.forEach((so) -> so.resetBaseScale(f));
  }

  /**
   * Set the speed of all the layered sprites.
   * @param f animation speed scale
   */
  @Override
  public void setSpeed(float f) {
    images.forEach((so) -> so.setSpeed(f));
  }
}
