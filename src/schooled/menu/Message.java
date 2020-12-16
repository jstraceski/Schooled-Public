package schooled.menu;

import java.util.ArrayList;
import schooled.entities.Entity;
import schooled.event.Event;

/**
 * Object for storing message data.
 * <p>
 * Contains a string and optionally, and event to happen when its displayed and interacted with.
 * This is usually paired with a Dialogue object to store messages in a specific order. Messages can
 * also contain a list of choices stored as Message objects.
 * <p></p>
 * Program written by Joseph Straceski
 * <p>
 * contact: straceski.joseph@gmail.com, https://github.com/Crepox
 */
public class Message {

  private ArrayList<TextContext> textList = new ArrayList<>(); // the textList of the Message
  private Event interactEvent = null; // the event that happens when you interact with the displayed string
  private Event displayEvent = null; // the event that happens when the event string is displayed
  private ArrayList<Message> choiceList = new ArrayList<>(); // List of event string choices
  private int index = 0; // the choice index
  private Entity sender = null; // sending entity

  /**
   * Default constructor for an Message
   */
  public Message() {
  }

  /**
   * Construct an event string with a sender an event and a string.
   *
   * @param newText string
   * @param sender  sender entity
   * @param event   interaction event
   */
  public Message(String newText, Entity sender, Event event) {
    this.sender = sender;
    textList.add(new TextContext(newText));
    this.interactEvent = event;
  }

  /**
   * Construct an event string with an event and a string.
   *
   * @param newText string
   * @param event   interaction event
   */
  public Message(String newText, Event event) {
    textList.add(new TextContext(newText));
    this.interactEvent = event;
  }

  /**
   * Construct an event string with a string and entity.
   *
   * @param newText string
   * @param sender  sender entity
   */
  public Message(String newText, Entity sender) {
    this.sender = sender;
    setText(newText);
  }

  /**
   * Construct an event string with just a string.
   *
   * @param newText string
   */
  public Message(String newText) {
    setText(newText);
  }


  public Message clone() {
    Message out = new Message();
    if (textList != null) {
      for (TextContext textContext : textList) {
        textList.add(textContext.clone());
      }
    }

    out.interactEvent = interactEvent == null ? null : interactEvent.clone();
    out.displayEvent = displayEvent == null ? null : displayEvent.clone();

    if (choiceList != null) {
      for (Message message : choiceList) {
        choiceList.add(message.clone());
      }
    }

    out.index = index;
    out.sender = sender;

    return out;
  }

  /**
   * Get the string version of the Message.
   *
   * @return string
   */
  @Override
  public String toString() {
    StringBuilder string = new StringBuilder();
    for (TextContext textContext : textList) {
      string.append(textContext.getText());
    }
    return string.toString();
  }

  /**
   * Get the text of the Message.
   *
   * @return Text of the message in the form of a list of all the contained TextContexts
   */
  public ArrayList<TextContext> getText() {
    return textList;
  }

  /**
   * Set the text of the Message
   *
   * @param text text to set in String form
   */
  public void setText(String text) {
    textList.clear();
    if (text != null) {
      textList.add(new TextContext(text));
    }
  }

  /**
   * Set the text of the Message
   *
   * @param text text to set in TextContext form
   */
  public void setText(TextContext text) {
    textList.clear();
    if (text != null) {
      textList.add(text);
    }
  }

  /**
   * Add text to the Message.
   *
   * @param text text to set in String form
   */
  public void addText(String text) {
    addText(new TextContext(text));
  }

  /**
   * Add text to the Message.
   *
   * @param text text to set in TextContext form
   */
  public void addText(TextContext text) {
    textList.add(text);
  }

  /**
   * Does the event string have an event and a string.
   *
   * @return does  event string contain both objects
   */
  public boolean isEmpty() {
    return textList != null && textList.isEmpty();
  }

  /**
   * Get the event that occurs when interacting with the Message.
   *
   * @return event
   */
  public Event getInteractEvent() {
    return interactEvent;
  }

  /**
   * Set the event that occurs when interacting with the Message.
   *
   * @param interactEvent event
   */
  public void setInteractEvent(Event interactEvent) {
    this.interactEvent = interactEvent;
  }

  /**
   * Does the Message have an event.
   *
   * @return return true if the Message has an event
   */
  public boolean hasInteractEvent() {
    return this.interactEvent != null;
  }

  /**
   * Get the event that occurs when the message is displayed.
   *
   * @return display event
   */
  public Event getDisplayEvent() {
    return displayEvent;
  }

  /**
   * Set the event that occurs when the message is displayed.
   *
   * @param displayEvent event to set the display message to.
   */
  public void setDisplayEvent(Event displayEvent) {
    this.displayEvent = displayEvent;
  }

  /**
   * Does the Message have a display event or not.
   *
   * @return if the Message has an event return true otherwise return false.
   */
  public boolean hasDisplayEvent() {
    return this.displayEvent != null;
  }

  /**
   * Get the list of choices.
   *
   * @return choices
   */
  public ArrayList<Message> getChoiceList() {
    return choiceList;
  }

  /**
   * Get the selected choice if it exists.
   *
   * @return selected choice
   */
  public Message getChoice() {
    if (choiceList == null || choiceList.isEmpty()){
      return null;
    }

    return choiceList.get(index);
  }

  /**
   * Add a choice to the end of the list of choices.
   *
   * @param es choice
   */
  public void addChoice(Message es) {
    choiceList.add(es);
  }

  /**
   * Add a choice to a specific index in the choice list.
   *
   * @param es    choice
   * @param index choice index
   */
  public void addChoice(Message es, int index) {
    choiceList.set(index, es);
  }

  /**
   * Does the Message have a valid list of choices.
   *
   * @return true if the list of choices is not empty
   */
  public boolean hasChoices() {
    return !choiceList.isEmpty();
  }

  /**
   * Get the Entity sending the Message.
   *
   * @return sending entity
   */
  public Entity getSender() {
    return sender;
  }

  /**
   * Set the entity sending the given message.
   *
   * @param sender sending entity
   */
  public void setSender(Entity sender) {
    this.sender = sender;
  }

  /**
   * Does this Message have a sender.
   *
   * @return if the sender is not null return true, otherwise false
   */
  public boolean hasSender() {
    return sender != null;
  }

  /**
   * Get the current choice index.
   *
   * @return selected index
   */
  public int getIndex() {
    return index;
  }

  /**
   * Set the choice index.
   * <p>
   * If the selection is out of range of the choice array set it to 0.
   *
   * @param newIndex index to set
   */
  public void setIndex(int newIndex) {
    if (newIndex >= choiceList.size() || newIndex < 0) {
      index = 0;
    } else {
      index = newIndex;
    }
  }

  /**
   * Increment the selection index by one.
   * <p>
   * Automatically wraps around to a valid index.
   */
  public void incIndex() {
    index++;
    if (index >= choiceList.size()) {
      index = 0;
    }
  }

  /**
   * Decrement the selection index by one.
   * <p>
   * Automatically wraps around to a valid index.
   */
  public void decIndex() {
    index--;
    if (index < 0) {
      index = choiceList.size() - 1;
    }
  }

  /**
   * Get the selected event string choice.
   *
   * @return event string that is selected
   */
  public Message getSelected() {
    return choiceList.get(index);
  }
}
