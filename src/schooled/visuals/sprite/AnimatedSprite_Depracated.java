/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package schooled.visuals.sprite;

import schooled.Game;

/**
 * Program written by Joseph Straceski.
 *
 * @author Joseph Straceski, web: <https://github.com/Crepox>, e-mail: straceski.joseph@gmail.com
 */
public class AnimatedSprite_Depracated {

  private Animation a;
  private Game g;

  public AnimatedSprite_Depracated(Game g, Animation a) {
    this.g = g;
    this.a = a;
  }

  public Sprite getImage() {
    return a.getFrame();
  }

  public void set(Object a) {
    this.a = (Animation) a;
  }

  @Override
  public AnimatedSprite_Depracated clone() {
    return new AnimatedSprite_Depracated(g, a.clone());
  }

  public void animate(long l) {
    a.animate(l);
  }

  public void change(Object o) {
    a.reset();
    set(o);
  }

  public void changeWithoutReset(Object o) {
    a.reset();
    set(o);
  }

  public boolean isAnimation() {
    return true;
  }

  public Animation getAnimation() {
    return a;
  }
}
