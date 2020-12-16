package schooled.containers;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import schooled.Game;
import schooled.entities.Entity;
import schooled.event.Event;
import schooled.menu.Dialogue;
import schooled.menu.Menu;
import schooled.menu.MenuEntity;
import schooled.menu.Message;
import schooled.menu.Origin;
import schooled.physics.Vector;
import schooled.visuals.MessageRenderEngine;

/**
 * A Container for multiple rooms or entity containers.
 * <p>
 * Controls dialogue interactions as well.
 * <p></p>
 * Program written by Joseph Straceski
 * <p>
 * contact: straceski.joseph@gmail.com, https://github.com/Crepox
 */
public class World {

  private final ArrayList<Room> rooms; // The list of loaded rooms
  private Room loadedRoom = null; // storage for the room that's currently loaded
  private Dialogue dialogue = null; // storage for the dialogue that's currently loaded
  private boolean cycledIncrement = false; // Increment dialogue in step with the game
  private Entity cycledIncrementEntity = null; // The entity advancing the dialogue
  private HashMap<String, Room> lookup = new HashMap<String, Room>();
  private MenuEntity textHud = null; // The hud text area that displays info below the screen
  private Game game; // A game instance
  private ArrayList<Event> syncedEvents; // store synced world based actions
  private Menu displayMenu = new Menu();

  /**
   * World constructor, sets up the position of the hud, the room list, and the game instance.
   *
   * @param g game instance
   */
  public World(Game g) {
    rooms = new ArrayList<Room>();
    syncedEvents = new ArrayList<Event>();

    // This is self explanatory, the text area is basically a text box from a word processor.
    Vector position = g.getMenuScreenSize().clone();
    position.setX(position.getX() / 2.0f);

    textHud = new MenuEntity(g, position);
    textHud.setName("HUD");
    textHud.setOrigin(Origin.BOTTOM);
    Vector size = g.getMenuScreenSize().clone();
    size.setY(70);
    size.setX(size.getX() - 2 * 2);
    textHud.setSize(size);
    textHud.setBackgroundColor(Color.white);
    textHud.setBorder(Color.red, 2);
    textHud.updateCycle();
    game = g;
  }

  /**
   * Get the the game instance.
   *
   * @return the game instance
   */
  public Game getGame() {
    return game;
  }

  /**
   * Set the stored game instance.
   *
   * @param game the game instance
   */
  public void setGame(Game game) {
    this.game = game;
  }

  /**
   * Get the hud from the world.
   *
   * @return the text hud object
   */
  public MenuEntity getTextHud() {
    return textHud;
  }

  /**
   * Send a string as dialogue to the world from a specific entity.
   *
   * @param s the string text
   * @param e the entity sending the message
   */
  public void sendMessage(String s, Entity e) {
    setDialogue(new Dialogue(s, e));
  }

  /**
   * Send a string as dialogue to the world.
   *
   * @param s string text
   */
  public void sendMessage(String s) {
    setDialogue(new Dialogue(s, null));
  }

  /**
   * Get the stored dialogue object.
   *
   * @return dialogue object
   */
  public Dialogue getDialogue() {
    return dialogue;
  }

  /**
   * Set and update the stored dialogue for the world.
   *
   * @param m dialogue object
   */
  public void setDialogue(Dialogue m) {
    dialogue = m;
    resetCycledIncrement();
    updateRenderText();
  }

  /**
   * Does the world have current dialogue.
   *
   * @return if the world has dialogue return true
   */
  public boolean hasDialogue() {
    return dialogue != null;
  }

  /**
   * Remove loaded dialogue from the world.
   */
  public void removeDialogue() {
    setDialogue(null);
  }

  /**
   * Increase the loaded dialogue by one.
   */
  public void processDialogue() {
    processDialogue(null);
  }

  /**
   * Increase the loaded dialogue by one and preform dialogue events.
   * <p>
   * Uses the provided entity as a receiver for any dialogue event.
   *
   * @param e1 receiver entity
   */
  public void processDialogue(Entity e1) {
    if (dialogue != null) {
      processDialogue(dialogue.getSpeaker(), e1);
    }
  }

  /**
   * Increase the loaded dialogue by one and process any resulting events.
   * <p>
   * Uses receiver as a receiver and sender as a sender for any dialogue event.
   *
   * @param receiver receiver
   * @param sender   sender
   */
  public void processDialogue(Entity sender, Entity receiver) {
    // reset cycled flags
    if (cycledIncrement && cycledIncrementEntity.equals(receiver)) {
      resetCycledIncrement();
    }

    if (getDialogue() != null) { // is there dialogue loaded
      Dialogue d1 = getDialogue();

      d1.processInteract(sender, receiver); // process message interaction actions
      d1.increaseIndex(); // increase dialogue index
      if (d1.isFinished()) {
        if (sender != null) { // if there is a sender and the message is finished trigger the entity
          sender.dialogueFinish(d1);
        }
        if (d1.equals(getDialogue())) {
          removeDialogue(); // if the previous dialogue is finished and loaded remove it.
        }
      }

      if (getDialogue() != null) { // if there still is a dialogue loaded process the display event
        getDialogue().processDisplay(receiver, sender);
      }
    }


  }

  /**
   * Check if the world has loaded dialogue that is <i>not</i> finished.
   *
   * @return if the dialogue is loaded and <i>not</i> finished return true
   */
  public boolean hasValidDialogue() {
    return hasDialogue() && !getDialogue().isFinished();
  }

  /**
   * Check if the world has loaded dialogue that is finished.
   *
   * @return if the dialogue is loaded and is finished return true
   */
  public boolean hasFinishedDialogue() {
    return hasDialogue() && getDialogue().isFinished();
  }

  /**
   * Check if the loaded dialogue is not empty, that means the dialogue loaded, not finished, and
   * containing text.
   *
   * @return if the dialogue isn't empty return true
   */
  public boolean hasNonEmptyDialogue() {
    return hasValidDialogue() && !getDialogue().getSelected().isEmpty();
  }

  /**
   * Update the hud using the loaded dialogue.
   */
  public void updateRenderText() {
    if (hasNonEmptyDialogue()) {
      textHud.clearText(); // clear the old text away

      Message message = dialogue.getSelected(); // obtain the current message

      if (message.hasSender()) { // does the dialogue have a sender
        textHud.addText(message.getSender().getName() + ": ");
        // add the sender's name to any displayed dialogue
      }

      textHud.addText(message.getText()); // render base message text

      if (message.hasChoices()) {
        // if the dialogue has a choice render the dialogue with the choices
        MessageRenderEngine.addMessageChoices(getGame(), message, textHud);
      }

      // update the text in the text area and calculate line wraps and line heights
      textHud.updateCycle();
      textHud.setVisible(true);
    } else {
      // if the dialogue is empty remove and clear the text hud
      textHud.clearText();
      textHud.setVisible(false);
      textHud.updateCycle();
    }
  }

  /**
   * Reset the cycled increment to false.
   */
  public void resetCycledIncrement() {
    cycledIncrement = false;
    cycledIncrementEntity = null;
  }

  /**
   * Increment the dialogue with the provided entity the next game tick.
   *
   * @param e the incrementing entity
   */
  public void processInteraction(Entity e) {
    cycledIncrement = true;
    cycledIncrementEntity = e;
  }

  /**
   * Search the room lookup table for a room with the given name.
   *
   * @param s the rooms name
   * @return if the room exists return a reference to the room instance, otherwise return null
   */
  public Room getLookupRoom(String s) {
    return lookup.get(s);
  }

  /**
   * Do the game tick on the world.
   */
  public void updateCycle(float time) {
    getLoadedRoom().updateCycle(time);
    displayMenu.updateCycle(time);
    getTextHud().updateCycle(time);
  }

  public void postEngineCheck(Entity player) {
    updateEvents();
    updateRenderText();

    // apply selections/interactions made by any player objects
    player.applyInteractions();
    // Must be placed outside of process -> player.applyInteractions
    player.clearInteractions();

    if (cycledIncrement && cycledIncrementEntity.weakIncrement()) {
      if (isBlocking()) {
        processDialogue(cycledIncrementEntity);
      } else {
        removeDialogue();
      }
    }

    resetCycledIncrement();
  }

  public void preRender(Object graphicsContext) {
    ArrayList<Entity> entities = new ArrayList<>(getLoadedRoom().getAllEntities());
    entities.forEach(Entity::preRender);
    displayMenu.preRender(graphicsContext);
    getTextHud().preRender(graphicsContext);
  }

  public ArrayList<Entity> getDisplayText() {
    return displayMenu.getEntities();
  }

  public Menu getDisplayMenu() {
    return displayMenu;
  }


  public void addDisplayText(MenuEntity menuEntity) {
    displayMenu.addEntity(menuEntity);
  }

  public void removeDisplayText(MenuEntity menuEntity) {
    displayMenu.removeEntity(menuEntity);
  }

  /**
   * Adds a room to the room lookup table, if a room with that name already exists, add a increasing
   * numerical value to the name so each room has a unique name.
   *
   * @param room the room to add
   */
  public void addRoom(Room room) {
    rooms.add(room);

    String nameID, s = room.getName();
    int i = 0;

    while (lookup.containsKey(nameID = (i == 0 ? s : (s + i)))) {
      i++;
    }

    room.setName(nameID);
    lookup.put(nameID, room);
    room.setWorld(this);
  }

  /**
   * Add the room to the world and set it to the loaded room.
   *
   * @param room the room
   */
  public void addAndLoad(Room room) {
    addRoom(room);
    setLoadedRoom(room);
  }

  /**
   * Get the room that's currently loaded.
   *
   * @return the loaded room
   */
  public Room getLoadedRoom() {
    return loadedRoom;
  }

  /**
   * Set the given room the the worlds loaded room.
   *
   * @param loadedRoom the room to load
   */
  public void setLoadedRoom(Room loadedRoom) {
    this.loadedRoom = loadedRoom;
    loadedRoom.loadRoomWindow();
    updateRenderText();
  }

  /**
   * Set the given room the the worlds loaded room. Also, set the room of the entity to the given
   * room.
   *
   * @param loadedRoom the room to load
   * @param e          the entity that is being moved to the room
   */
  public void loadEntityRoom(Room loadedRoom, Entity e) {
    this.loadedRoom = loadedRoom;
    loadedRoom.loadRoomWindow();
    e.setRoom(loadedRoom);
    updateRenderText();
  }

  /**
   * Add an event that is synced with the world update.
   *
   * @param event the event to preform
   */
  public void addSyncedEvent(Event event) {
    syncedEvents.add(event);
  }


  /**
   * Update synced event
   */
  public void updateEvents() {
    for (Event e : syncedEvents) {
      e.act();
    }
    syncedEvents.clear();
  }

  /**
   * Is the current dialogue blocking player interactions.
   *
   * @return if the current dialogue is blocking player interactions return true, otherwise false
   */
  public boolean isBlocking() {
    if (hasDialogue()) {
      return dialogue.isBlocking();
    }
    return false;
  }

  public void updateVisuals() {

  }
}
