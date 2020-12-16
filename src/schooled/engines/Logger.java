package schooled.engines;

import java.awt.Color;
import java.util.HashMap;
import schooled.Game;
import schooled.containers.TimeData;
import schooled.menu.MenuEntity;
import schooled.menu.Origin;
import schooled.menu.StringArea;
import schooled.physics.Vector;

public class Logger {

  public static MenuEntity loggerMenu;
  public static boolean debugInput = false;
  public static boolean debug = false;
  public static String debugText = "";
  public static int debugDecimals = 3;
  public static HashMap<String, TimeData> timeList = new HashMap<>();
  public static int refreshRate = 60 * 5;
  public static int refreshRateIndex = 0;
  public static final float SECOND = (float) 1e9;

  public static void init(Game game) {
    loggerMenu = new StringArea(game, new Vector(10, 10));
    loggerMenu.setOrigin(Origin.TOP_LEFT);
    loggerMenu.setBackgroundColor(Color.lightGray);
  }

  /**
   * Log a debug identifier with a time. This function creates a way of generating a time difference
   * from an string identifier. To use the function, you call it before and after the code you want
   * to time (using the same identifier), the function then calculates the time difference between
   * the two calls and stores it with in an identifier-indexed list. This allows the speed of
   * specific functions to be debugged.
   * <p>
   * (Theoretically, the time it takes to find a certain log value shouldn't change if the list
   * already contains the key and isn't modified. After the first loop of measurement the following
   * time calculations shouldn't be affected by looking up the debug identifier)
   *
   * @param string the identifier
   */
  public static void pushDebugTime(String string) {
    if (Logger.debug) {
      pushDebugTime(string, System.nanoTime());
    }
  }


  /**
   * Log a debug identifier with a time. This function creates a way of generating a time difference
   * from an string identifier. To use the function, you call it before and after the code you want
   * to time (using the same identifier), the function then calculates the time difference between
   * the two calls and stores it with in an identifier-indexed list. This allows the speed of
   * specific functions to be debugged.
   * <p>
   * (Theoretically, the time it takes to find a certain log value shouldn't change if the list
   * already contains the key and isn't modified. After the first loop of measurement the following
   * time calculations shouldn't be affected by looking up the debug identifier)
   *
   * @param string the identifier
   * @param time   the current time
   */
  public static void pushDebugTime(String string, long time) {
    if (Logger.debug) {
      if (timeList.containsKey(string)) {
        TimeData data = timeList.get(string);
        if (data.start) {
          data.startTime = time;
          data.start = false;
        } else {
          data.time += (time - data.startTime);
          data.start = true;
        }
      } else {
        TimeData data = new TimeData();
        data.startTime = time;
        data.start = false;
        timeList.put(string, data);
      }
    }
  }


  public static void calculateDebugTimesList() {
    debug = debugInput;
    refreshRateIndex++;
    if (refreshRateIndex >= refreshRate) {
      Logger.debugText = "";
    }

    for (String name : timeList.keySet()) {
      TimeData data = timeList.get(name);
      data.avg += data.time;
      data.time = 0;
      if (refreshRateIndex >= refreshRate) {
        double debugPrecision = Math.pow(10, Logger.debugDecimals);
        double timeDiff = Math.floor(debugPrecision * data.avg / SECOND) / debugPrecision;
        String timeText = name + " : " + timeDiff;
        debugText += timeText + "\n";
        Logger.loggerMenu.setText(Logger.debugText);
        data.avg = 0;
      }
    }
    if (refreshRateIndex >= refreshRate) {
      refreshRateIndex = 0;
    }
  }
}
