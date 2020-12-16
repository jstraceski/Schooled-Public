package schooled.menu;

import java.util.ArrayList;
import schooled.Game;
import schooled.entities.Entity;
import schooled.event.Event;

/**
 * Structured group of Messages.
 * <p></p>
 * Program written by Joseph Straceski
 * <p>
 * contact: straceski.joseph@gmail.com, https://github.com/Crepox
 */
public class Dialogue {

  private static int sectionLength = 20;
  private ArrayList<Message> messageList; // list of messages in order of appearance
  private boolean blocking; // does the dialogue block user inputs
  private int index; // index of the dialogue object

  /**
   * Default constructor.
   */
  public Dialogue() {
    this("");
  }

  /**
   * Create a Dialogue with one message created from the input string.
   *
   * @param str input string
   */
  public Dialogue(String str) {
    this(str, null);
  }

  /**
   * Create a Dialogue with a message created from the input string and input entity.
   *
   * @param str input string
   * @param e   input entity
   */
  public Dialogue(String str, Entity e) {
    this((ArrayList<Message>) null, e);
    if (str != null && !str.isEmpty()) {
      add(new Message(str, e));
    }
  }

  /**
   * Create a dialogue out of a list of messages sent by an entity.
   * <p>
   * Sets the sender of all the messages to the entity.
   *
   * @param list message list
   * @param e    input entity
   */
  public Dialogue(ArrayList<Message> list, Entity e) {
    this(list);
    setAllSpeakers(e);
  }

  /**
   * Create a Dialogue from a list of messages.
   *
   * @param list input list
   */
  public Dialogue(ArrayList<Message> list) {
    messageList = new ArrayList<>();
    add(list);

    index = 0;
    blocking = false;
  }

  @Override
  public Dialogue clone() {
    Dialogue dialogue = new Dialogue(messageList);
    dialogue.index = index;
    dialogue.blocking = blocking;
    return dialogue;
  }

  @Override
  public String toString() {
    return messageList.stream().map(Message::toString).reduce("", (a, b) -> a + b);
  }

  /**
   * Create a new message from a string and add it to the message list.
   *
   * @param s string
   */
  public void add(String s) {
    messageList.add(new Message(s));
  }

  /**
   * Add a message to the list of messages.
   *
   * @param es input message
   */
  public void add(Message es) {
    if (es != null) {
      messageList.add(es);
    }
  }

  /**
   * Create a message from a string and an event and add it to the list of messages.
   *
   * @param s string
   * @param e event
   */
  public void add(String s, Event e) {
    messageList.add(new Message(s, e));
  }

  /**
   * Add a list of messages to the dialogue's existing list.
   *
   * @param list input message list
   */
  public void add(ArrayList<Message> list) {
    if (list != null) {
      messageList.addAll(list);
    }
  }


  /**
   * Get the speaker of the current message.
   *
   * @return speaker
   */
  public Entity getSpeaker() {
    return getSelected().getSender();
  }

  /**
   * Set the speaker, or sender, of all the messages to the input entity.
   *
   * @param sender speaker or sender
   */
  public void setAllSpeakers(Entity sender) {
    for (Message message : messageList) {
      message.setSender(sender);
    }
  }

  /**
   * Process an interaction events of the loaded message.
   *
   * @param e1 sending entity
   * @param e2 receiver entity
   */
  public void processInteract(Entity e1, Entity e2) {
    Message eventString;
    if ((eventString = getSelected()) != null && eventString.hasInteractEvent()) {
      eventString.getInteractEvent().act(e1, e2);
    }

    if (eventString != null && eventString.hasChoices()) {
      eventString.getChoice().getInteractEvent().act(e1, e2);
    }
  }

  /**
   * Process the displaying events of the loaded message.
   *
   * @param e1 sending entity
   * @param e2 receiver entity
   */
  public void processDisplay(Entity e1, Entity e2) {
    Message eventString;
    if ((eventString = getSelected()) != null && eventString.hasDisplayEvent()) {
      eventString.getDisplayEvent().act(e1, e2);
    }
  }

  /**
   * Get the current dialogue index.
   *
   * @return index
   */
  public int getIndex() {
    return index;
  }

  /**
   * Set the current dialogue index.
   *
   * @param i new index
   */
  public void setIndex(int i) {
    index = i;
  }

  /**
   * Reset the dialogue index to 0.
   */
  public void resetIndex() {
    index = 0;
  }

  /**
   * Increase the message index of the dialogue.
   */
  public void increaseIndex() {
    index++;
  }

  /**
   * Does the dialogue have a message after the current one.
   *
   * @return true if there is another message, otherwise false
   */
  public boolean hasNext() {
    return index + 1 < messageList.size();
  }

  public boolean isFinished() {
    return getSelected() == null;
  }

  /**
   * Get the currently selected message in the list of messages.
   *
   * @return selected message
   */
  public Message getSelected() {
    return getSelected(index);
  }

  /**
   * Get the message at index i in the message list.
   *
   * @param i index i
   * @return message
   */
  public Message getSelected(int i) {
    if (messageList == null || i >= messageList.size()) {
      return null;
    }
    return messageList.get(i);
  }

  /**
   * Get the list of messages in the dialogue.
   *
   * @return message list
   */
  public ArrayList<Message> getMessageList() {
    return messageList;
  }

  /**
   * Does the dialogue block player interactions.
   *
   * @return true if the dialogue is blocking
   */
  public boolean isBlocking() {
    return blocking;
  }

  /**
   * Set the blocking state of the current dialogue.
   *
   * @param blocking blocking state
   */
  public void setBlocking(boolean blocking) {
    this.blocking = blocking;
  }

  /**
   * Set the section length of messages.
   * <p>
   * The section length of a message is used to divide large bits of text up into separate
   * messages.
   *
   * @param i section length
   */
  public void setSectionLength(int i) {
    sectionLength = i;
  }

  /**
   * Method used to divide up an input string into separate, easier to display, messages.
   * <p>
   * Uses the default section length to divide them.
   *
   * @param str input string
   */
  private void divideMessage(String str) {
    divideMessage(str, sectionLength);
  }

  /**
   * Method used to divide up an input string into separate, easier to display, messages.
   * <p>
   * Uses the input section length to divide them.
   *
   * @param str              input string
   * @param newSectionLength input section length
   */
  private void divideMessage(String str, int newSectionLength) {
    while (str != null && !str.equals("")) {
      if (str.length() >= newSectionLength) {
        messageList.add(new Message(str.substring(0, newSectionLength)));
        str = str.substring(newSectionLength);
      } else {
        messageList.add(new Message(str));
        str = "";
      }
    }
  }
}
