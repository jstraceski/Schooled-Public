/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package schooled.visuals.sprite;

import java.nio.ByteBuffer;
import org.lwjgl.opengl.GL;
import schooled.loaders.SpriteLoader;
import schooled.physics.Vector;

/**
 * Container for open gl texture data.
 * <p>
 * Stores internal x, y, width, and height for sections of an image in reference to the data buffer.
 * The original-width vs the width is the difference between the original width of the spite or game
 * width vs the current width in reference to scaled game objects.
 * <p></p>
 * Program written by Joseph Straceski
 * <p>
 * contact: straceski.joseph@gmail.com, https://github.com/Crepox
 */
public class ByteBufferImage {

  private ByteBuffer data; // byte-buffer storing the image data
  private float internalX = 0; // byte-buffer x position
  private float internalY = 0; // byte-buffer y position
  private float internalWidth; // byte-buffer width position
  private float internalHeight; // byte-buffer height position
  private int originalWidth = 0; // original image width
  private int originalHeight = 0; // original image height
  private int width; // current width
  private int height; // current height
  private int comp; // component value, rgb vs rgba
  private int texID = -1; // gl texture id

  /**
   * Basic Byte Buffer Image constructor.
   *
   * @param data buffer data
   * @param width buffer width
   * @param height buffer height
   * @param comp buffer component value, rgb vs rgba
   * @param id gl texture id
   */
  public ByteBufferImage(ByteBuffer data, int width, int height, int comp, int id) {
    this.data = data;

    this.width = width;
    this.internalWidth = width;
    this.originalWidth = width;

    this.height = height;
    this.internalHeight = height;
    this.originalHeight = height;

    this.comp = comp;
    this.texID = id;
  }

  /**
   * Basic Byte Buffer Image constructor.
   *
   * @param data buffer data
   * @param width buffer width
   * @param height buffer height
   * @param comp buffer component value, rgb vs rgba
   */
  public ByteBufferImage(ByteBuffer data, int width, int height, int comp) {
    this.data = data;

    this.width = width;
    this.internalWidth = width;
    this.originalWidth = width;

    this.height = height;
    this.internalHeight = height;
    this.originalHeight = height;

    this.comp = comp;
  }

  /**
   * Clone the image data.
   *
   * @return clone of the image data
   */
  @Override
  public ByteBufferImage clone() {
    ByteBufferImage bbi = new ByteBufferImage(getData(), getWidth(), getHeight(), getComp(),
        getTexID());
    bbi.setOriginalSize(getOriginalSize());
    bbi.setInternalPosition(getInternalPosition());
    bbi.setInternalSize(getInternalSize());
    return bbi;
  }

  public float getOriginalWidth() {
    return originalWidth;
  }

  public void setOriginalWidth(int originalWidth) {
    this.originalWidth = originalWidth;
  }

  public float getOriginalHeight() {
    return originalHeight;
  }

  public void setOriginalHeight(int originalHeight) {
    this.originalHeight = originalHeight;
  }

  public ByteBuffer getData() {
    return data;
  }

  public void setData(ByteBuffer data) {
    this.data = data;
  }

  public int getWidth() {
    return width;
  }

  public void setWidth(int width) {
    this.width = width;
  }

  public int getHeight() {
    return height;
  }

  public void setHeight(int height) {
    this.height = height;
  }

  public float getInternalWidth() {
    return internalWidth;
  }

  public void setInternalWidth(float internalWidth) {
    this.internalWidth = internalWidth;
  }

  public float getInternalHeight() {
    return internalHeight;
  }

  public void setInternalHeight(float internalHeight) {
    this.internalHeight = internalHeight;
  }

  public int getTexID() {
    if (texID == -1 && GL.getCapabilities() != null) {
      texID = SpriteLoader.glLoadImage(this.data, this.originalWidth, this.originalHeight, this.comp);
    }

    return texID;
  }

  public void setTexID(int texID) {
    this.texID = texID;
  }

  public int getComp() {
    return comp;
  }

  public void setComp(int comp) {
    this.comp = comp;
  }

  public float getInternalX() {
    return internalX;
  }

  public void setInternalX(float x) {
    this.internalX = x;
  }

  public float getInternalY() {
    return internalY;
  }

  public void setInternalY(float y) {
    this.internalY = y;
  }

  /**
   * Get the internal position as a vector.
   *
   * @return internal position
   */
  public Vector getInternalPosition() {
    return new Vector(getInternalX(), getInternalY());
  }

  /**
   * Set the internal position with a vector.
   *
   * @param pos internal position
   */
  public void setInternalPosition(Vector pos) {
    setInternalX(pos.getX());
    setInternalY(pos.getY());
  }

  /**
   * Get the original position as a vector.
   *
   * @return original position
   */
  public Vector getOriginalSize() {
    return new Vector(getOriginalWidth(), getOriginalHeight());
  }

  /**
   * Set the original position with a vector.
   *
   * @param pos original position
   */
  public void setOriginalSize(Vector pos) {
    setOriginalWidth(pos.getXi());
    setOriginalHeight(pos.getYi());
  }

  /**
   * Get the actual size of the image in a vector form.
   *
   * @return size
   */
  public Vector getSize() {
    return new Vector(getWidth(), getHeight());
  }

  /**
   * Set the actual size of the image with a vector.
   *
   * @param size size vector
   */
  public void setSize(Vector size) {
    setWidth(size.getXi());
    setHeight(size.getYi());
  }

  /**
   * Get the internal size of the image in a vector form.
   *
   * @return size
   */
  public Vector getInternalSize() {
    return new Vector(getInternalWidth(), getInternalHeight());
  }

  /**
   * Set the internal size of the image with a vector.
   *
   * @param size size vector
   */
  public void setInternalSize(Vector size) {
    setInternalWidth(size.getX());
    setInternalHeight(size.getY());
  }
}
