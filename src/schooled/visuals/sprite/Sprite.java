package schooled.visuals.sprite;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import schooled.entities.State;
import schooled.loaders.SpriteLoader;
import schooled.menu.Origin;
import schooled.physics.Vector;

/**
 * Generalized container for visual data or sprites.
 * <p>
 * Contains methods and structure to store and organize image data. Can store different image
 * formats as well as generalize for other types of sprites such as animations.
 * <p></p>
 * Program written by Joseph Straceski
 * <p>
 * contact: straceski.joseph@gmail.com, https://github.com/Crepox
 */
public class Sprite {

  // <editor-fold defaultstate="collapsed" desc="Variables">

  public String name = ""; // important name variable
  public String file = ""; // important name variable
  public boolean round = true;

  // variable to store raw image data
  private Object image;
  private float rotation = 0;
  public int id = 0;

  private Sprite parent;

  // list of scaled instances of the given sprite
  private HashMap<Float, Sprite> scaledInstances = new HashMap<>();

  private float globalLayer = 0; // global rendering layer
  private float gameLayer = 0; // game logic layer
  private float localLayer = 0; // local rendering layer
  private float childLayer = 0; // child rendering layer

  // global layer is the top level order
  //
  private Vector spritePos = new Vector(0,0);
  private Vector spriteSize = new Vector(0,0);

  private Vector localLocation = new Vector(0, 0);
  private Vector internalOffset = null;
  private Vector gameLocation = new Vector(0,0);
  private Vector childPosition = new Vector(0,0);
  // layers are used when determining the drawing order of sprites


  // variables used in entity sprite management
  private State state = null; // image state, used to transition images
  private Origin origin = Origin.CENTER;

  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Constructors">

  /**
   * Null constructor, creates the shell of a sprite.
   */
  public Sprite() {
    image = null;
  }

  /**
   * Create a sprite with buffered image data.
   * <p>
   * (Non-GL implementation)
   *
   * @param o buffered image
   */
  public Sprite(BufferedImage o) {
    this.image = o;
  }

  /**
   * Create a sprite with byte buffered image data.
   * <p>
   * (GL implementation)
   *
   * @param o buffered image
   */
  public Sprite(ByteBufferImage o) {
    this.image = o;
  }

  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Programming Methods">

  @Override
  public String toString() {
    String out = "Sprite[name=" + name;

    if (!file.isEmpty()) {
      out += ", file=" + file;
    }

    if (getGameLayer() != 0) {
      out += ", g_l=" + getGameLayer();
    }

    if (getLocalLayer() != 0.0) {
      out += ", l_l=" + getLocalLayer();
    }

    if (getChildLayer() != 0.0) {
      out += ", c_l=" + getChildLayer();
    }

    return out + "]";
  }

  public boolean isParent(Sprite sprite) {
    return isParent(sprite, 0);
  }

  public boolean isParent(Sprite sprite, int depth) {
    if (depth > 2) {
      return false;
    }
    return sprite == parent || (parent != null && parent.isParent(sprite, depth + 1));
  }

  public void setParent(Sprite sprite) {
    parent = sprite;
  }

  /**
   * Set the input sprite to be a clone of the given sprite.
   *
   * @param so input sprite
   * @return clone of the given sprite in the form of the input sprite
   */
  public Sprite setClone(Sprite so) {
    if (isBufferedImage()) {
      BufferedImage bi = getBufferedImage();
      so.setRawData(new BufferedImage(bi.getColorModel(),
          bi.copyData(bi.getRaster().createCompatibleWritableRaster()),
          bi.getColorModel().isAlphaPremultiplied(), null));
    } else if (isByteBufferImage()) {
      so.setRawData(getByteBufferImage().clone());
    }

    so.setLocalLocation(getLocalLocation().clone());
    so.setGlobalLayer(getGlobalLayer());
    so.setLocalLayer(getLocalLayer());
    so.setGameLayer(getGameLayer());
    so.setChildPosition(getChildPosition());
    so.setChildLayer(getChildLayer());
    so.setGameLocation(getGameLocation());
    so.setOrigin(getOrigin());
    so.setName(name);
    so.file = file;
    so.setRotation(rotation);
    return so;
  }

  @Override
  public Sprite clone() {
    Sprite so = new Sprite();
    setClone(so);
    return so;
  }

  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Image Data Methods">

  public Vector getSpritePos() {
    return spritePos;
  }

  public void setSpritePos(Vector spritePos) {
    this.spritePos = spritePos;
  }

  public Vector getSpriteSize() {
    return spriteSize;
  }

  public void setSpriteSize(Vector spriteSize) {
    this.spriteSize = spriteSize;
  }

  /**
   * Get the raw image object of the sprite.
   *
   * @return image object
   */
  public Object getRawData() {
    return image;
  }

  /**
   * Set the raw image object of the sprite.
   *
   * @param object image object
   */
  public void setRawData(BufferedImage object) {
    image = object;
  }

  /**
   * Set the raw image object of the sprite.
   *
   * @param object image object
   */
  public void setRawData(ByteBufferImage object) {
    image = object;
  }

  /**
   * Does the sprite have valid image data.
   *
   * @return true if the image data is not null, false otherwise
   */
  public boolean hasData() {
    return getImageData() != null;
  }

  /**
   * Get the raw image data of the base sprite.
   *
   * @return raw image data
   */
  public Object getImageData() {
    return getScaled(1.0f).getRawData();
  }

  /**
   * Get the raw image data in the form of a ByteBufferImage.
   * <p>
   * If the image of the sprite is not a ByteBufferImage return null.
   *
   * @return ByteBufferImage data
   */
  public ByteBufferImage getByteBufferImage() {
    if (isByteBufferImage()) {
      return (ByteBufferImage) getImageData();
    }
    return null;
  }

  /**
   * Does the sprite has its data in ByteBufferImage form.
   *
   * @return true if the sprite is stored as a ByteBufferImage, false otherwise.
   */
  public boolean isByteBufferImage() {
    return getImageData() instanceof ByteBufferImage;
  }

  /**
   * Get the raw image data in the form of a BufferedImage.
   * <p>
   * If the image of the sprite is not a BufferedImage return null.
   *
   * @return BufferedImage data
   */
  public BufferedImage getBufferedImage() {
    if (isBufferedImage()) {
      return (BufferedImage) getImageData();
    }
    return null;
  }

  /**
   * Does the sprite has its data in BufferedImage form.
   *
   * @return true if the sprite is stored as a BufferedImage, false otherwise.
   */
  public boolean isBufferedImage() {
    return getImageData() instanceof BufferedImage;
  }

  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Scaling Methods">

  /**
   * Create a clone of the input sprite scaled by the input scalar.
   *
   * @param s input sprite
   * @param f scalar
   * @return cloned instance
   */
  public static Sprite scale(Sprite s, float f) {
    Sprite clonedInstance = s.clone();
    return clonedInstance.scale(f);
  }

  /**
   * Scale the given sprite by the scalar.
   *
   * @param f input scalar
   * @return given sprite
   */
  public Sprite scale(float f) {
    setLocalLocation(getLocalLocation().scalei(f));

    if (isBufferedImage()) {
      setRawData(SpriteLoader.scaleBufferedImage(getBufferedImage(), f));
    }

    if (isByteBufferImage()) {
      ByteBufferImage bbi = getByteBufferImage();
      bbi.setSize(bbi.getSize().scalei(f));
      setRawData(bbi);
    }

    return this;
  }

  /**
   * Get a scaled instance of the given sprite scaled to the input scalar.
   * <p>
   * Stores the scaled image in case it is needed again.
   *
   * @param scale input scalar
   * @return scaled image
   */
  public Sprite getScaled(float scale) {
    if (scaledInstances.containsKey(scale)) { // search the scaled instance list
      return scaledInstances.get(scale); // get the scaled instance
    } else {
      if (scale == 1.0f) {
        // if the scale is 1.0 and isn't in the instance list return the given sprite
        return this;
      }
      // if the scaled instance isn't in the list, scale the given sprite and put an instance
      //  in the list
      Sprite scaledInstance = scale(getScaled(1.0f), scale);
      scaledInstances.put(scale, scaledInstance);

      // return the scaled sprite
      return scaledInstance;
    }
  }

  /**
   * Generate a scaled instance of the given sprite and add it to the instance list.
   *
   * @param scale scalar value
   */
  public void generateScale(float scale) {
    addScaledInstance(scale, scale(getScaled(1.0f), scale));
  }

  /**
   * Add the input sprite as a scaled instance of the given sprite in the instance list.
   * <p>
   * The scale value is used as the key in the instance table.
   *
   * @param scale          input scalar
   * @param scaledInstance sprite to add
   */
  public void addScaledInstance(float scale, Sprite scaledInstance) {
    scaledInstances.put(scale, scaledInstance);
  }

  /**
   * Clear all the pre-generated scaled instances.
   */
  public void clearScales() {
    scaledInstances.clear();
  }

  /**
   * Reset the scale of the base image.
   * <p>
   * Set the base scaled instance, or the scaled instance at 1.0f, as the base image scaled by the
   * input scalar.
   *
   * @param scale input scalar
   */
  public void resetBaseScale(float scale) {
    clearScales();

    Sprite so = scale(this, scale);
    scaledInstances.put(1.0f, so);
  }

  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Rendering Methods">


  public float getRotation() {
    return rotation;
  }

  public void setRotation(float rotation) {
    this.rotation = rotation;
  }

  public float getChildLayer() {
    return childLayer;
  }

  public void setChildLayer(float childLayer) {
    this.childLayer = childLayer;
  }

  public Vector getChildPosition() {
    return childPosition;
  }

  public void setChildPosition(Vector childPosition) {
    this.childPosition = childPosition;
  }

  public float getLocalLayer() {
    return localLayer;
  }

  public void setLocalLayer(float localLayer) {
    this.localLayer = localLayer;
  }

  public boolean hasGameLocation() {
    return !gameLocation.equals(Vector.zero);
  }

  public void setGameLocation(Vector offset) {
    gameLocation = offset;
  }

  public Vector getGameLocation() {
    return gameLocation;
  }

  public float getGameLayer() {
    return gameLayer;
  }

  public void setGameLayer(float gameLayer) {
    this.gameLayer = gameLayer;
  }

  public float getGlobalLayer() {
    return globalLayer;
  }

  public void setGlobalLayer(float globalLayer) {
    this.globalLayer = globalLayer;
  }

  public Origin getOrigin() {
    return origin;
  }

  public void setOrigin(Origin origin) {
    this.origin = origin;
  }

  public boolean hasOrigin() {
    return this.origin != null;
  }

  /**
   * Get the custom position of the sprite.
   * <p>
   * Used to describe an additional shift added to a image before rendering.
   *
   * @return custom position
   */
  public Vector getLocalLocation() {
    return localLocation;
  }

  /**
   * Set the custom position of the sprite.
   * <p>
   * Used to describe an additional shift added to a image before rendering. Handles null inputs as
   * setting the custom position to (0,0).
   *
   * @return custom position
   */
  public void setLocalLocation(Vector customPosition) {
    if (customPosition == null) {
      this.localLocation = new Vector(0, 0);
    } else {
      this.localLocation = customPosition;
    }
  }

  /**
   * Does the sprite have a custom position.
   *
   * @return true if the custom position isn't set to (0,0), false otherwise
   */
  public boolean hasLocalLocation() {
    return !localLocation.equals(Vector.zero);
  }

  /**
   * Get the width of the given sprite.
   *
   * @return width of the sprite
   */
  public int getWidth() {
    if (isByteBufferImage()) {
      return (int) Math.floor(getByteBufferImage().getWidth() + 0.5f);
    }
    if (isBufferedImage()) {
      return getBufferedImage().getWidth();
    }

    try {
      throw new Exception("No Compatible Image Data for: " + this);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return 0;
  }

  /**
   * Get the height of the given sprite.
   *
   * @return height of the sprite
   */
  public int getHeight() {
    if (isByteBufferImage()) {
      return (int) Math.floor(getByteBufferImage().getHeight() + 0.5f);
    }
    if (isBufferedImage()) {
      return getBufferedImage().getHeight();
    }

    try {
      throw new Exception("No Compatible Image Data for: " + name);
    } catch (Exception e) {
      e.printStackTrace();
    }

    return 0;
  }

  /**
   * Get the size vector of the input.
   *
   * @return size vector
   */
  public Vector getSize() {
    return new Vector(getWidth(), getHeight());
  }

  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Game Logic Methods">

  public String getName() {
    return name;
  }

  public void setName(String string) {
    name = string;
  }

  /**
   * Get the state of the sprite.
   *
   * @return sprite state
   */
  public State getState() {
    return state;
  }

  /**
   * Set the state of the sprite.
   *
   * @param state sprite state
   */
  public void setState(State state) {
    this.state = state;
  }

  /**
   * Does the sprite have a state.
   *
   * @return true if the state isn't null, false otherwise
   */
  public boolean hasState() {
    return state != null;
  }

  /**
   * Ease of use function to check if a sprites state is equal to the input state.
   * <p>
   * Checks if the given entity has a state first. If the given entity doesn't have a state it
   * returns false by default.
   *
   * @param state2 state to check
   * @return true if the states were equal, false otherwise
   */
  public boolean isCurrentState(State state2) {
    return hasState() && getState().equals(state2);
  }

  public void setSpeed(float speed) {

  }

  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Generation Methods">

  /**
   * Generate a new sub-sprite sprite from the given sprite.
   *
   * Cut out a new sprite at position (x, y) with size (width, height) on the given sprite.
   *
   * @param x local x position
   * @param y local y position
   * @param width sub-sprite width
   * @param height sub-sprite height
   * @return new sub-sprite
   */
  public Sprite getSubSpr(float x, float y, float width, float height) {
    if (isBufferedImage()) {
      Vector pos = new Vector(x, y);
      Vector size = new Vector(width, height);
      Sprite sprite = new Sprite(getBufferedImage().getSubimage(pos.getXi(),
          pos.getYi(), size.getXi(), size.getYi()));
      sprite.setName(name);
      sprite.file = file;
      return sprite;
    }

    if (isByteBufferImage()) {
      ByteBufferImage bbi = getByteBufferImage().clone();

      // calculate the relative image scale to translate the width and height of the input
      //  to the width and height of the byte buffer image
      float xScale = bbi.getSize().getX() / bbi.getInternalSize().getX();
      float yScale = bbi.getSize().getY() / bbi.getInternalSize().getY();

      // set the size of the new image
      bbi.setSize(new Vector(width, height));

      // scale down the positions and width to the values relative to the original byte buffer image
      if (xScale != 1.0f) {
        x /= xScale;
        width /= xScale;
      }

      if (yScale != 1.0f) {
        y /= yScale;
        height /= yScale;
      }

      // set the internal positions and internal size
      bbi.setInternalPosition(bbi.getInternalPosition().addi(new Vector(x, y)));
      bbi.setInternalSize(new Vector(width, height));
      Sprite sprite = new Sprite(bbi);
      sprite.setName(name);
      sprite.file = file;
      return sprite;
    }
    return null;
  }

  /**
   * Create a new sprite of the given sprite repeated f times in the x and y directions.
   *
   * @param f repeated times
   * @return repeated sprite
   */
  public Sprite multiplyImage(Vector f) {
    if (isBufferedImage()) {
      //setRawData(ImageLoader.scaleBufferedImage(getBufferedImage(), f));
    }
    if (isByteBufferImage()) {
      ByteBufferImage bbi = getByteBufferImage();
      bbi.setInternalSize(bbi.getInternalSize().scalei(f));
      bbi.setSize(bbi.getSize().scalei(f));
      setRawData(bbi);
    }
    return this;
  }

  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Extension Methods">

  /**
   * Reset the given sprite.
   */
  public void reset() {
    //doesnt do anything
  }

  /**
   * Update the given sprite with a time difference.
   *
   * @param l time difference
   */
  public void update(float l) {
    //doesnt do anything
  }

  /**
   * Is the current sprite finished.
   *
   * @return true if the current sprite is finished, false otherwise
   */
  public boolean isFinished() {
    return true;
  }

  /**
   * Is the current sprite is at a finishing.
   *
   * @return true if the current sprite is at a finishing point, false otherwise
   */
  public boolean atFinishPoint() {
    return true;
  }

  /**
   * Is the sprite ending.
   *
   * @return true if the sprite is ending, false otherwise
   */
  public boolean isEnding() {
    return true;
  }

  /**
   * Set the ending state of the sprite.
   *
   * @param end ending state
   */
  public void setEnd(boolean end) {
    //doesnt do anything
  }

  /**
   * Is the sprite animated.
   *
   * @return true if the sprite is animated, false otherwise
   */
  public boolean isAnimated() {
    return false;
  }

  /**
   * Get the current sprite as an Animation.
   *
   * @return animation sprite
   */
  public Animation getAnimation() {
    if (isAnimated()) {
      return (Animation) this;
    }
    return null;
  }

  /**
   * Is the sprite layered.
   *
   * @return true if the sprite is layered, false otherwise
   */
  public boolean isLayered() {
    return false;
  }

  /**
   * Get the current sprite as an LayeredSprite.
   *
   * @return layered sprite
   */
  public LayeredSprite getLayered() {
    if (isLayered()) {
      return (LayeredSprite) this;
    }
    return null;
  }

  public boolean hasInternalShift() {
    return internalOffset != null;
  }

  public Vector getInternalOffset() {
    return internalOffset;
  }

  public void setInternalOffset(Vector internalOffset) {
    this.internalOffset = internalOffset;
  }

  public boolean hasChildPosition() {
    return childPosition != null && !childPosition.equals(Vector.zero);
  }
  // </editor-fold>

}
