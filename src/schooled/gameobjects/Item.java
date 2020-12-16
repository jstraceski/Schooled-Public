package schooled.gameobjects;

import java.awt.image.BufferedImage;

/**
 * Item abstraction.
 * <p></p>
 * Program written by Joseph Straceski
 * <p>
 * contact: straceski.joseph@gmail.com, https://github.com/Crepox
 */
public interface Item {

  /**
   * Trigger the primary action (i.e. left mouse)
   */
  void primaryAction();

  /**
   * Trigger the secondary action (i.e. right mouse)
   */
  void secondaryAction();

  /**
   * Is the item equipped or not.
   *
   * @return if the item is equipped return true
   */
  boolean isEquip();

  /**
   * Is the item being wielded.
   *
   * @return if the item is weld return true
   */
  boolean isWeild();

  /**
   * Get the item's stats.
   *
   * @return the item stats
   */
  Stat getStats();

  /**
   * Get the item's name.
   *
   * @return the name of the item
   */
  String getName();

  /**
   * Get the item's sprite.
   *
   * @return the sprite
   */
  BufferedImage getSprite();
}
