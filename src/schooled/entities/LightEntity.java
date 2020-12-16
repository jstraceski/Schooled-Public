package schooled.entities;

import schooled.Game;
import schooled.physics.Shape;
import schooled.physics.Vector;


/**
 * An entity that represents a light source. Can flicker.
 * <p>
 * Flicker equations:
 * <pre>
 * flickerValue = flickerAmplitude * sin(flickerBuffer/flickerPeriod * 2 * pi)
 * light = lightLevel + flickerValue
 * </pre>
 * Program written by Joseph Straceski
 * <p>
 * contact: straceski.joseph@gmail.com, https://github.com/Crepox
 */
public class LightEntity extends Entity {

  private float lightLevel = 50.5f; // light level of the torch, basically the radius of the light in air
  private boolean flickering = false; // is the light flickering
  private float time = 0; // a variable to store a time difference used in updating the state of the light
  private float speed = 0.1f; // speed at which the light is updated at
  private float flickerBuffer = 0; // the current flicker value
  private float flickerValue = 0; // the current flicker value
  private float flickerPeriod = 2.0f; // the number of seconds per cycle
  private float flickerAmplitude = 0.5f; // how extreme the flicker is.

  /**
   * Light Entity constructor, takes in a light level.
   *
   * @param g        the game entity
   * @param position the entity position
   * @param shape    the entity shape
   * @param mass     the mass of the entity
   * @param newLevel the light level of the light entity
   */
  public LightEntity(Game g, Vector position, Shape shape, float mass,
      float newLevel) {
    super(g, position, shape, mass);
    this.setCollides(false);
    this.setPhysics(false);
    if (newLevel > 0) {
      this.lightLevel = newLevel;
    }

  }

  /**
   * Basic light entity constructor.
   *
   * @param g        the game entity
   * @param position the entity position
   * @param shape    the entity shape
   * @param mass     the mass of the entity
   */
  public LightEntity(Game g, Vector position, Shape shape, float mass) {
    this(g, position, shape, mass, -1.0f);
  }

  /**
   * Get the modified light level of the entity.
   *
   * @return the light level
   */
  public float getLightLevel() {
    if (flickering) {
      return lightLevel + flickerValue;
    }
    return lightLevel;
  }

  /**
   * Set the base light level of the entity.
   *
   * @param lightLevel the light level
   */
  public void setLightLevel(float lightLevel) {
    this.lightLevel = lightLevel;
  }

  /**
   * Get the update speed of the light.
   *
   * @return the update speed in seconds
   */
  public float getSpeed() {
    return speed;
  }

  /**
   * Set the update speed of the light.
   *
   * @param speed the speed in seconds
   */
  public void setSpeed(float speed) {
    this.speed = speed;
  }

  /**
   * Get the flickerPeriod value.
   * <p>
   * The value added to flicker every update.
   *
   * @return the value of flickerPeriod
   * @see LightEntity LightEntity
   */
  public float getFlickerPeriod() {
    return flickerPeriod;
  }

  /**
   * Set the flickerPeriod value.
   * <p>
   * The number of s added to flicker every update.
   *
   * @param flickerPeriod the value of flickerPeriod
   * @see LightEntity LightEntity
   */
  public void setFlickerPeriod(float flickerPeriod) {
    this.flickerPeriod = flickerPeriod;
  }

  /**
   * Get the value of the flicker v.
   * <p>
   * The amplitude value of the flicker equation.
   *
   * @return the amplitude
   * @see LightEntity LightEntity
   */
  public float getFlickerAmplitude() {
    return flickerAmplitude;
  }

  /**
   * Set the value of the flicker amplitude.
   * <p>
   * The amplitude value of the flicker equation.
   *
   * @param flickerAmplitude the amplitude value
   * @see LightEntity LightEntity
   */
  public void setFlickerAmplitude(float flickerAmplitude) {
    this.flickerAmplitude = flickerAmplitude;
  }

  /**
   * Is the LightEntity flickering or not.
   *
   * @return if the lightEntity is flickering return true else false
   */
  public boolean isFlickering() {
    return flickering;
  }

  /**
   * Set the LightEntity to flicker or not.
   *
   * @param flickers if true the light will flicker with time
   */
  public void setFlickering(boolean flickers) {
    this.flickering = flickering;
  }

  @Override
  public void updateCycle(float l) {
    if (flickering) {
      time += l;

      // If the time is greater than the speed, update the flicker value
      //  and set the speed to zero.
      if (time > speed) {
        time = time - speed;
        addFlicker(speed);
      }
    }
  }

  /**
   * Add to time in seconds to the flickerValue equation.
   *
   * @param time the time difference in seconds
   */
  public void addFlicker(float time) {
    flickerBuffer += time;
    flickerValue = (float) Math.sin(flickerBuffer / flickerPeriod * 2 * Math.PI)
        * flickerAmplitude;
    if (flickerBuffer / flickerPeriod > 1.0f) {
      flickerBuffer = (flickerBuffer / flickerPeriod - 1.0f) * flickerPeriod;
    }
  }

}
