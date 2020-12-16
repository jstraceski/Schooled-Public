package schooled.visuals.sprite;

import java.util.ArrayList;
import schooled.Game;
import schooled.entities.State;

/**
 * Container for animated sprites.
 * <p></p>
 * Program written by Joseph Straceski
 * <p>
 * contact: straceski.joseph@gmail.com, https://github.com/Crepox
 */
public class Animation extends Sprite {

  private int index = 0; // index of the animation, used to get the frame, delay, and finish list
  private ArrayList<Sprite> frames = new ArrayList<>(); // list of frames
  private ArrayList<Float> delays = new ArrayList<>(); // list of delays for each frame
  private ArrayList<Integer> finishList = new ArrayList<>();  // list of finishing indexes

  private float speed = 1; // speed of the animation (second scalar)
  private float time = 0; // time since last frame
  private float defaultDelay = 0.5f; // default frame duration

  private boolean running = true; // running flag, is the animation running
  private boolean end = false; // end the animation at the next valid frame
  private boolean loop = true; // does the animation loop when it reaches an end

  // <editor-fold defaultstate="collapsed" desc="Constructors">

  /**
   * Create an empty animation shell.
   */
  public Animation() {
    this(null, null);
  }

  /**
   * Create a animation from a list of sprites.
   *
   * @param newImages sprite list
   */
  public Animation(ArrayList<Sprite> newImages) {
    this(newImages, null);
  }

  /**
   * Create an animation from a list of sprites and their corresponding delays.
   *
   * @param newImages sprite list
   * @param newDelays delay list
   */
  public Animation(ArrayList<Sprite> newImages, ArrayList<Float> newDelays) {
    this(newImages, newDelays, true);
  }

  /**
   * Create an animation from a list of sprites and their corresponding delays.
   * <p>
   * Includes a flag to set the looping state of the animation. Setting the flag to true will cause
   * the animation to loop.
   *
   * @param newImages sprite list
   * @param newDelays delay list
   * @param newRepeat loop flag
   */
  public Animation(ArrayList<Sprite> newImages, ArrayList<Float> newDelays, boolean newRepeat) {
    loop = newRepeat;
    if (newImages != null) {
      frames = newImages;
      delays = newDelays;
    }
  }

  /**
   * Create a new animation as a clone of the input animation.
   *
   * @param a input animation
   */
  public Animation(Animation a) {
    a.setClone(this);
  }

  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Programming Methods">

  /**
   * Set the input animation to be a clone of the current animation.
   *
   * @param animation input animation
   * @return animation clone
   */
  public Sprite setClone(Animation animation) {
    super.setClone(animation);
    ArrayList<Sprite> clonedFrames = new ArrayList<Sprite>();
    for (Sprite i : frames) {
      clonedFrames.add(i.clone());
    }
    animation.setLoop(loop);
    animation.frames = clonedFrames;
    animation.delays = new ArrayList<>(delays);
    animation.setDefaultDelay(getDefaultDelay());
    return animation;
  }

  @Override
  public Animation clone() {
    Animation animation = new Animation();
    setClone(animation);
    return animation;
  }

  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Control Methods">

  /**
   * Get the frame and delay index.
   *
   * @return index
   */
  public int getIndex() {
    return index;
  }

  /**
   * Set the frame index.
   *
   * @param i index
   */
  public void setIndex(int i) {
    if (index + 1 >= frames.size()) {
      if (loop) {
        index = 0;
      } else {
        index = frames.size() - 1;
      }
    } else {
      index = Math.max(i, 0);
    }

    time = 0;
  }

  /**
   * Get the speed of the animation.
   * <p>
   * The speed is the time scalar.
   *
   * @return speed of the animation
   */
  public float getSpeed() {
    return speed;
  }

  /**
   * Set the speed of the animation.
   * <p>
   * The speed is the time scalar.
   *
   * @param speed speed of the animation
   */
  public void setSpeed(float speed) {
    this.speed = speed;
  }

  public float getDefaultDelay() {
    return defaultDelay;
  }

  public void setDefaultDelay(float defaultDelay) {
    this.defaultDelay = defaultDelay;
  }

  /**
   * Set the delay of every frame.
   *
   * @param f frame delay
   */
  public void setAllDelay(float f) {
    delays.replaceAll((a) -> (f));
  }

  /**
   * Is the animation accepting time updates.
   *
   * @return true if the animation is running, false otherwise
   */
  public boolean isRunning() {
    return running;
  }

  /**
   * Set the animation updating flag.
   *
   * @param running flag state
   */
  public void setRunning(boolean running) {
    this.running = running;
  }

  /**
   * Is the animation ending the next available finishing frame.
   *
   * @return true if the animation is ending, false otherwise
   */
  @Override
  public boolean isEnding() {
    return this.end;
  }

  /**
   * Set the ending state of the animation.
   * <p>
   * If the animation is ending the next available finishing frame will be held.
   *
   * @param end ending state
   */
  public void setEnd(boolean end) {
    this.end = end;
  }

  /**
   * Does the animation loop at ending frames.
   *
   * @return true if the animation loops, false otherwise
   */
  public boolean doesLoop() {
    return loop;
  }

  /**
   * Set the animation looping state.
   * <p>
   * If true the animation will loop when finished, false the animation will hold.
   *
   * @param newRepeat animation state
   */
  public void setLoop(boolean newRepeat) {
    loop = newRepeat;
  }

  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Animation Data Accessors">

  /**
   * Get the current frame of the animation.
   * <p>
   * Uses the frame index to access and return the current frame.
   *
   * @return current frame, or null if the index is invalid
   */
  public Sprite getFrame() {
    if (frames.size() > index) {
      return frames.get(index);
    } else {
      return null;
    }
  }

  /**
   * Add a frame with default delay to the animation.
   *
   * @param sprite sprite frame
   */
  public void addFrame(Sprite sprite) {
    addFrame(sprite, defaultDelay);
  }

  /**
   * Add a frame with a delay.
   *
   * @param sprite new frame
   * @param delay  frame delay
   */
  public void addFrame(Sprite sprite, float delay) {
    sprite.setParent(this);
    frames.add(sprite);
    delays.add(delay);
  }

  /**
   * Get the list of finishing indexes.
   *
   * @return finishing list
   */
  private ArrayList<Integer> getFinishList() {
    return finishList;
  }

  /**
   * Set the list of finishing indexes.
   *
   * @param newList finishing list
   */
  public void setFinishList(ArrayList<Integer> newList) {
    finishList = newList;
  }

  @Override
  public int getWidth() {
    Sprite sprite;
    if ((sprite = getFrame()) != null) {
      return sprite.getWidth();
    }
    return 0;
  }

  @Override
  public int getHeight() {
    Sprite sprite;
    if ((sprite = getFrame()) != null) {
      return sprite.getHeight();
    }
    return 0;
  }

  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Animating Methods">

  /**
   * Update the animation with a time difference.
   *
   * @param l time difference in seconds
   */
  public void update(float l) {
    animate(l);
  }

  /**
   * Animate the given animation by a time difference.
   * <p>
   * Add the time difference to the frame clock.
   *
   * @param t time difference
   */
  public void animate(float t) {
    if (running) {
      if (!isFinished()) {
        time += t * speed;
      }

      if (time > delays.get(index)) {
        incrementIndex();
      }
    }
  }

  /**
   * Increase the frame index of the animation by one.
   */
  public void incrementIndex() {
    setIndex(index + 1);
  }

  /**
   * Reset the controlling variables of the animation.
   * <p>
   * Set the index to zero, the time to zero, the speed to 1.0, and the ending state to false.
   */
  public void reset() {
    index = 0;
    time = 0;
    speed = 1.0f;
    end = false;
  }

  /**
   * Is the animation at a finishing point.
   * <p>
   * A finishing point is either the last frame of the animation or a frame index designated on the
   * finishing list.
   *
   * @return true if the animation is on a finishing frame, false otherwise
   */
  @Override
  public boolean atFinishPoint() {
    return (!getFinishList().isEmpty() && getFinishList().contains(index))
        || index == frames.size() - 1;
  }

  /**
   * Is the animation scheduled to end.
   * <p>
   * True if the animation is at a finishing point and is scheduled to end. Also true if the
   * animation is scheduled to end or does not loop, and the index is past the last frame.
   *
   * @return true if the animation is finished, otherwise false
   */
  public boolean isFinished() {
    return (end && atFinishPoint()) || ((!doesLoop() || end) && index >= frames.size() - 1);
  }

  /**
   * Return true, because this is an animation.
   *
   * @return true
   */
  @Override
  public boolean isAnimated() {
    return true;
  }

  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Generation Methods">

  /**
   * Generate a scaled version of every frames of the animation.
   *
   * @param scale scalar value
   */
  @Override
  public void generateScale(float scale) {
    for (Sprite o : frames) {
      o.generateScale(scale);
    }
  }

  /**
   * Reset the base scale of every frame of the animation.
   *
   * @param scale input scalar
   */
  @Override
  public void resetBaseScale(float scale) {
    for (Sprite o : frames) {
      o.resetBaseScale(scale);
    }
  }

  /**
   * Get a scaled instance of the current frame.
   *
   * @param f scale
   * @return scaled frame
   */
  @Override
  public Sprite getScaled(float f) {
    return getFrame().getScaled(f);
  }

  @Override
  public void setState(State state) {
    frames.forEach(sprite -> sprite.setState(state));
  }

  // </editor-fold>


  @Override
  public String toString() {
    return "A[" + super.toString() + ", s:" + getFrame() + "]";
  }
}
