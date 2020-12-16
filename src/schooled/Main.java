package schooled;

import schooled.engines.Engine;
import schooled.engines.Logger;

/**
 * Sets up the physics engine, game engine, and window instance.
 * <p></p>
 * Program written by Joseph Straceski
 * <p>
 * contact: straceski.joseph@gmail.com, https://github.com/Crepox
 */
public class Main {

  /**
   * The java main function, you know the drill.
   *
   * @param args java args
   */
  public static void main(String[] args) {
    Window w = new Window();
    Engine e = new Engine();
    Game g = new Game();
    Logger.init(g);
    g.init(w, e); // initialize the game
    g.start(); // start the game
  }
}
