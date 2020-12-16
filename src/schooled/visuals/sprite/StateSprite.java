package schooled.visuals.sprite;

import java.util.ArrayList;
import java.util.HashMap;
import schooled.Game;
import schooled.entities.State;
import schooled.gameobjects.Stat;

/**
 * Container for animated sprites.
 * <p></p>
 * Program written by Joseph Straceski
 * <p>
 * contact: straceski.joseph@gmail.com, https://github.com/Crepox
 */
public class StateSprite extends LayeredSprite {

  private HashMap<State, Sprite> sprites = new HashMap<>(); // list of sprites
  private State state = State.DEFAULT;

  // <editor-fold defaultstate="collapsed" desc="Constructors">

  public StateSprite() {
  }


  public StateSprite(HashMap<State, Sprite> newImages) {
    sprites = newImages;
  }


  public StateSprite(ArrayList<Sprite> sList, ArrayList<State> states) {
    for (int i = 0; i < sList.size(); i++) {
      sList.get(i).setParent(this);
      sprites.put(states.get(i), sList.get(i));
    }
  }

  public StateSprite(StateSprite a) {
    a.setClone(this);
  }

  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Programming Methods">

  public void setName(String string) {
    for (Sprite sprite : sprites.values()) {
      sprite.setName(string);
    }
  }

  public StateSprite setClone(StateSprite stateSprite) {
    super.setClone(stateSprite);

    for (State state : sprites.keySet()) {
      stateSprite.addFrame(sprites.get(state).clone(), state);
    }

    stateSprite.state = state;

    return stateSprite;
  }

  @Override
  public StateSprite clone() {
    StateSprite stateSprite = new StateSprite();
    setClone(stateSprite);
    return stateSprite;
  }

  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Control Methods">

  public void setState(State state) {
    this.state = state;

    Sprite sprite = getFrame();
    if (sprite != null) {
      sprite.reset();
    }
  }

  public State getState(){
    return state;
  }

  public Sprite getFrame(State nState) {
    if (sprites.containsKey(nState)) {
      return sprites.get(nState);
    }

    if (sprites.containsKey(State.DEFAULT)) {
      return sprites.get(State.DEFAULT);
    }

    return null;
  }

  public Sprite getFrame() {
    return getFrame(state);
  }

  public void addFrame(Sprite sprite) {
    addFrame(sprite, State.DEFAULT);
  }

  public void addFrame(Sprite sprite, State state) {
    sprite.setParent(this);
    sprites.put(state, sprite);
  }

  public boolean isLayered() {
    return true;
  }

  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Animation Data Accessors">


  @Override
  public boolean atFinishPoint() {
    Sprite sprite = getFrame();
    if (sprite != null) {
      return sprite.atFinishPoint();
    }
    return true;
  }

  @Override
  public boolean isFinished() {
    Sprite sprite = getFrame();
    if (sprite != null) {
      return sprite.isFinished();
    }
    return true;
  }

  /**
   * Get the list of layered sprites.
   *
   * @return sprite list
   */
  public ArrayList<Sprite> getSprites() {
    ArrayList<Sprite> sList = new ArrayList<>();
    sList.add(getFrame());
    return sList;
  }

  @Override
  public boolean hasData() {
    Sprite sprite = getFrame();
    return sprite != null && sprite.hasData();
  }

  @Override
  public Sprite getSubSpr(float x, float y, float width, float height) {
    StateSprite ls = new StateSprite();
    for (State key : sprites.keySet()) {
      ls.addFrame(sprites.get(key).getSubSpr(x, y, width, height), key);
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
    StateSprite ls = new StateSprite();
    for (State key : sprites.keySet()) {
      ls.addFrame(sprites.get(key).scale(f), key);
    }

    return ls;
  }

  /**
   * Clears the scales for each individual sprite in the list of sprites.
   */
  @Override
  public void clearScales() {
    sprites.values().forEach(Sprite::clearScales);
  }

  /**
   * Generate the input scale for each individual sprite in the list of sprites.
   * @param f input scalar
   */
  @Override
  public void generateScale(float f) {
    sprites.values().forEach((so) -> so.generateScale(f));
  }

  /**
   * Reset the base scale for each individual sprite in the list of sprites.
   * @param f base scale
   */
  @Override
  public void resetBaseScale(float f) {
    sprites.values().forEach((so) -> so.resetBaseScale(f));
  }

  public void reset(){
    Sprite sprite = getFrame();
    if (sprite != null) {
      sprite.reset();
    }
  }

  @Override
  public void update(float l) {
    Sprite sprite = getFrame();
    if (sprite != null) {
      sprite.update(l);
    }
  }

  @Override
  public String toString() {
    return "StateSprite[state=" + state + ", " + Game.toStr(sprites) + "]";
  }

  /**
   * Set the speed of all the layered sprites.
   * @param f animation speed scale
   */
  @Override
  public void setSpeed(float f) {
    sprites.values().forEach((so) -> so.setSpeed(f));
  }

  // </editor-fold>
}
