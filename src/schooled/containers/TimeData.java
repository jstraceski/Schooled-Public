/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package schooled.containers;

/**
 * An object to store time data.
 * <p></p>
 * Program written by Joseph Straceski
 * <p>
 * contact: straceski.joseph@gmail.com, https://github.com/Crepox
 */
public class TimeData {

  public boolean start = true; // did the timer start
  public long startTime; // system time at the start of measuring time
  public long time; // total time elapsed over the past cycles
  public long avg; // the average time elapsed over the past cycles
}
