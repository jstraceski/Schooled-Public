/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package schooled.visuals;

import java.util.ArrayList;
import schooled.Game;
import schooled.menu.MenuEntity;
import schooled.menu.Message;
import schooled.menu.TextArea;
import schooled.menu.TextContext;

/**
 * Message rendering engine.
 * <p>
 * Specific methods for rendering Messages to the screen.
 * <p></p>
 * Program written by Joseph Straceski
 * <p>
 * contact: straceski.joseph@gmail.com, https://github.com/Crepox
 */
public class MessageRenderEngine {

  /**
   * Add the choices text of a message to a text area.
   *
   * @param game     game instance
   * @param message  message
   * @param menuEntity text area
   */
  public static void addMessageChoices(Game game, Message message, MenuEntity menuEntity) {

    if (menuEntity.hasText()) { // does the text area have text already
      // if so set the previous line to end with a newline
      menuEntity.getTextList().get(menuEntity.getTextList().size() - 1).setNewLine(true);
    }

    // get the choices of a message
    ArrayList<Message> list = message.getChoiceList();
    for (int i = 0; i < list.size(); i++) {
      Message es = list.get(i); // get current message choice
      TextContext te = new TextContext(game, "    "); // Create a text context with a tab
      te.setNewLine(true); // set the text to end with a newline

      // if the choice index of the message equals the current choice being drawn add a carat
      //  before it, otherwise add a space
      if (i == message.getIndex()) {
        te.addText(">");
      } else {
        te.addText(" ");
      }

      // add the text of the choice to the text area
      te.addText(es.toString());
      menuEntity.addText(te);
    }
  }
}
