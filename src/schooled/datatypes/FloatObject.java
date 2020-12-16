/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package schooled.datatypes;

/**
 * Object that just holds a float. Functions are self explanatory.
 * <p></p>
 * Program written by Joseph Straceski
 * <p>
 * contact: straceski.joseph@gmail.com, https://github.com/Crepox
 */
public class FloatObject {

  private float value;

  public FloatObject(float value) {
    this.value = value;
  }

  public float get() {
    return value;
  }

  public void set(float value) {
    this.value = value;
  }

  public void add(float value) {
    this.value += value;
  }
}
