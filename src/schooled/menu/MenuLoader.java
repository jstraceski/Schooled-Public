/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package schooled.menu;

import java.awt.Color;
import java.util.function.Function;
import schooled.Game;
import schooled.engines.Logger;
import schooled.event.Event;
import schooled.physics.Vector;

/**
 * Testing and loading methods for generating menus.
 * <p></p>
 * Program written by Joseph Straceski
 * <p>
 * contact: straceski.joseph@gmail.com, https://github.com/Crepox
 */
public class MenuLoader {

  // static menu text
  // TODO: put in save file
  static String menuText = "How to Play:\n  WSAD to move\n  F to select\n  "
      + "Shift to Sprint\nSpace to shoot\n";

  /**
   * Generate the main menu.
   *
   * @param game game instance
   */
  public static void initMainMenuTest(Game game) {
    Menu mainMenu = new Menu(); // main menu screen
    Menu settings = new Menu(); // setting menu screen

    // ---------- Settings Menu ----------

    // create a back button for settings screen
    MenuButton backButton = new MenuButton(game, new Vector(50, 50), "<");
    backButton.setTextFit(TextContext.FitType.tight);
    backButton.setFontSize(32);
    backButton.setButtonEvent(new Event(() -> game.loadMenu(settings.getPreviousMenu(), false)));
    backButton.setBorder(Color.GREEN, 2);

    // Create a title text box for the settings area
    StringArea settingsLabel = new StringArea(game, new Vector(0, 50), "Settings");
    settingsLabel.setHorizontalCenter(true);
    settingsLabel.setBackgroundColor(Color.LIGHT_GRAY);
    settingsLabel.setBorder(Color.GREEN, 2);
    settingsLabel.setFontSize(32);

    // create a debug button for the text area
    MenuButton debugButton = new MenuButton(game, new Vector(0, 100),
        "Debug Mode: " + (Logger.debugInput ? "On" : "Off"));
    debugButton.setHorizontalCenter(true);
    debugButton.setButtonEvent(new Event(() -> {
      Logger.debugInput = !Logger.debugInput;
      debugButton.setText("Debug Mode: " + (Logger.debugInput ? "On" : "Off"));
    }));
    debugButton.setBorder(Color.GREEN, 2);

    // Create a label for the text enter area
    final StringArea areaLabel = new StringArea(game, new Vector(-5, 155), "Player Name:");
    areaLabel.setHorizontalCenter(true);
    areaLabel.setOrigin(Origin.TOP_RIGHT);
    areaLabel.setOriginPosition(new Vector(-5, 0));
    areaLabel.setBackgroundColor(Color.LIGHT_GRAY);
    areaLabel.setBorder(Color.GREEN, 2);

    // Create a text area with a static size
    TextArea textArea = new TextArea(game, new Vector(5, 155));
    textArea.setHorizontalCenter(true);
    textArea.setBackgroundColor(new Color(230, 230, 230));
    textArea.setOriginPosition(new Vector(5, 0));
    textArea.setBorder(Color.GREEN, 2);
    textArea.addText("a_a_a_a_a_a_a_a_a_a_");
    textArea.addText("b_b_b_b_b_b b_b_b_b_");
    textArea.addText("c_c_c_c c_c_c_c_c_c_");
    textArea.addText("d_d_d_d_d_d_d_d_d_d_");
    textArea.setEnterEvent(new Event(() -> game.getPlayer().setName(textArea.getText())));
    textArea.setSize(new Vector(300, 88));

    // Create a test button to cycle through the valid orientations
    final MenuButton orientationButton = new MenuButton(game, new Vector(0, 265),
        "Orentation : " + textArea.getOrigin().name());
    orientationButton.setHorizontalCenter(true);
    orientationButton.setButtonEvent(new Event(() -> {
      int i = textArea.getOrigin().ordinal() + 1;
      i = i > Origin.values().length - 1 ? 0 : i;
      i = i < 0 ? Origin.values().length - 1 : i;
      textArea.setOrigin(Origin.values()[i]);
      orientationButton.setText("Orentation : " + textArea.getOrigin().name());
      textArea.updateCycle();
      orientationButton.updateCycle();
    }));
    orientationButton.setBorder(Color.GREEN, 2);

    // Create a test button to cycle through the valid alignments
    final MenuButton alignmentButton = new MenuButton(game, new Vector(0, 295),
        "Alignment : " + textArea.getAlignment().name());
    alignmentButton.setHorizontalCenter(true);
    alignmentButton.setButtonEvent(new Event(() -> {
      int i = textArea.getAlignment().ordinal() + 1;
      i = i > Alignment.values().length - 1 ? 0 : i;
      i = i < 0 ? Alignment.values().length - 1 : i;
      textArea.setAlignment(Alignment.values()[i]);
      alignmentButton.setText("Alignment : " + textArea.getAlignment().name());
      textArea.updateCycle();
      alignmentButton.updateCycle();
    }));
    alignmentButton.setBorder(Color.GREEN, 2);

    // ---------- Main Menu ----------

    // Create a title label for the main screen
    StringArea titleLabel = new StringArea(game, new Vector(0, 50), "Schooled: An educational adventure");
    titleLabel.setHorizontalCenter(true);
    titleLabel.setBackgroundColor(Color.lightGray);
    titleLabel.setBorder(Color.CYAN, 2);
    titleLabel.setFontSize(32);

    // Create a start button for the main area
    MenuButton startButton = new MenuButton(game, new Vector(0, 130), "Start Game");
    startButton.setHorizontalCenter(true);
    startButton.setButtonEvent(new Event(() -> game.startGame()));
    startButton.setBorder(Color.GREEN, 2);

    // Create a button to navigate to the settings menu
    MenuButton settingsButton = new MenuButton(game, new Vector(0, 160), "Settings");
    settingsButton.setHorizontalCenter(true);
    settingsButton.setButtonEvent(new Event(() -> game.loadMenu(settings)));
    settingsButton.setBorder(Color.GREEN, 2);

    // Create a quit button for the main screen
    MenuButton quitButton = new MenuButton(game, new Vector(0, 190), "Quit");
    quitButton.setHorizontalCenter(true);
    quitButton.setButtonEvent(new Event(() -> game.setRunning(false)));
    quitButton.setBorder(Color.GREEN, 2);

    MenuEntity logo = new MenuEntity(game, new Vector(100, 360));
    logo.setHorizontalCenter(true);
    logo.setSprite(game.getSprite("EA logo").scale(3.0f));
    logo.setBackgroundColor(null);

    // add the menu entities to the menu containers
    settings.addEntity(backButton);
    settings.addEntity(debugButton);
    settings.addEntity(textArea);
    settings.addEntity(areaLabel);
    settings.addEntity(settingsLabel);
    settings.addEntity(orientationButton);
    settings.addEntity(alignmentButton);
    settings.update();

    mainMenu.addEntity(titleLabel);
    mainMenu.addEntity(startButton);
    mainMenu.addEntity(settingsButton);
    mainMenu.addEntity(quitButton);
    mainMenu.addEntity(logo);
    mainMenu.update();

    // add the menus to the game lookup table
    game.addMenu("settings", settings);
    game.addMenu("main", mainMenu);
  }

  /**
   * Create and load a pause menu into the game instance.
   *
   * TODO: extrapolate this to a save file
   *
   * @param game game instance
   */
  public static void initPauseMenuTest(final Game game) {
    Menu pauseMenu = new Menu(); // create the pause menu

    // Create the quit button
    MenuButton quitButton = new MenuButton(game, new Vector(0, 40), "Quit");
    quitButton.setHorizontalCenter(true);
    quitButton.setButtonEvent(new Event(() -> game.setRunning(false)));
    quitButton.setBackgroundColor(Color.lightGray);
    quitButton.setBorder(Color.BLUE, 2);

    // Create the main menu button to navigate to the main menu
    MenuButton mainButton = new MenuButton(game, new Vector(0, 70), "Main Menu");
    mainButton.setHorizontalCenter(true);
    mainButton.setButtonEvent(new Event(() ->
      {
        game.setGameLoaded(false);
        game.loadMainMenu();
      }));

    mainButton.setBackgroundColor(Color.lightGray);
    mainButton.setBorder(Color.BLUE, 2);

    // Create a settings menu button to navigate to the settings menu
    MenuButton settingsButton = new MenuButton(game, new Vector(0, 100), "Settings");
    settingsButton.setHorizontalCenter(true);
    settingsButton.setButtonEvent(new Event(() -> game.loadMenu(game.getMenuFromList("settings"))));
    settingsButton.setBackgroundColor(Color.lightGray);
    settingsButton.setBorder(Color.BLUE, 2);

    // Create the instruction box text area
    StringArea instrBox = new StringArea(game, new Vector(0, 200), menuText);
    instrBox.setTextColor(Color.WHITE);
    instrBox.setHorizontalCenter(true);
    instrBox.setAlignment(Alignment.LEFT);
    instrBox.setBackgroundColor(Color.darkGray);
    instrBox.setBorder(Color.red, 2);
    addRainbowText(instrBox, "COOOL ", TextContext.BOLD);
    addRainbowText(instrBox, "STUFF!!", TextContext.NEWLINE);
    addRainbowText(instrBox, "BROOO!", (byte) (TextContext.BOLD | TextContext.NEWLINE));
    instrBox.addText("BOLDNESESS", Color.WHITE, null, TextContext.BOLD);
    instrBox.addText("NORMALNESS", Color.ORANGE, null, null);

    // add all of the menu objects to the pause menu
    pauseMenu.addEntity(quitButton);
    pauseMenu.addEntity(mainButton);
    pauseMenu.addEntity(settingsButton);
    pauseMenu.addEntity(instrBox);

    // add the pause menu to the menu lookup
    game.addMenu("pause", pauseMenu);
  }

  /**
   * Add text with a rainbow color to the string area.
   *
   * @param stringArea string area
   * @param str string to add
   * @param style style to add to the string
   */
  public static void addRainbowText(StringArea stringArea, String str, byte style) {
    int index = 0;
    int i;
    boolean bold = false;
    if ((style & TextContext.BOLD) > 0) {
      bold = true;
    }

    Function<Integer, Color> getRainbowColor = integer -> {
      switch (integer % 6) {
        case (0):
          return Color.red;
        case (1):
          return Color.orange;
        case (2):
          return Color.yellow;
        case (3):
          return Color.green;
        case (4):
          return Color.blue;
        case (5):
          return Color.magenta;
        default:
          return Color.BLACK;
      }
    };

    for (i = 0; i < str.length() - 1; i++) {

      if (bold) {
        stringArea.addText(str.charAt(i) + "", getRainbowColor.apply(index), null, TextContext.BOLD);
      } else {
        stringArea.addText(str.charAt(i) + "", getRainbowColor.apply(index), null, null);
      }

      if (str.charAt(i) != ' ') {
        index += 1;
      }
    }

    stringArea.addText(str.substring(str.length() - 1), getRainbowColor.apply(index), null, style);
  }
}
