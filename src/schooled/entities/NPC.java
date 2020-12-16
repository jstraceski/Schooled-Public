package schooled.entities;

import java.util.ArrayList;
import schooled.Game;
import schooled.containers.World;
import schooled.menu.Dialogue;
import schooled.physics.Shape;
import schooled.physics.Vector;
import schooled.visuals.sprite.Sprite;

/**
 * An Entity to represent a Door.
 * <p></p>
 * Program written by Joseph Straceski
 * <p>
 * contact: straceski.joseph@gmail.com, https://github.com/Crepox
 */
public class NPC extends Entity {

  private ArrayList<Dialogue> dialogues = new ArrayList<Dialogue>(); // list of dialogue
  private int dialogueIndex = 0; // the current index of the dialogue
  private boolean repeatLast = true; // repeat the last dialogue
  private boolean repeatAll = false; // repeat all of the dialogue
  private boolean repeatCustom = false; // repeat a custom dialogue

  private int customIndex = 0; // set the dialogue to a custom index

  /**
   * Construct an NPC entity
   *
   * @param g game instance
   * @param v npc position
   * @param s npc shape
   * @param d npc mass
   */
  public NPC(Game g, Vector v, Shape s, float d) {
    super(g, v, s, d);
  }


  public NPC(Game e) {
    super(e.getGame());
  }

  /**
   * Construct an NPC with a basic entity
   *
   * @param e the basic entity
   */
  public NPC(Entity e) {
    super(e.getGame(), e.getPosition(), e.getShape(), e.getMass());
  }

  /**
   * Add a Dialogue object to the NPC.
   *
   * @param d the dialogue
   */
  public void addDialogue(Dialogue d) {
    d.setAllSpeakers(this);
    dialogues.add(d);
  }

  /**
   * Set the current dialogue.
   *
   * @return the current dialogue
   */
  public Dialogue currentDialogue() {
    if (dialogues.size() > dialogueIndex) {
      return dialogues.get(dialogueIndex);
    } else {
      return null;
    }
  }

  /**
   * Get the dialogue at index i in the npc's dialogue list.
   *
   * @param i the index
   * @return the dialogue
   */
  public Dialogue getDialogue(int i) {
    if (i < dialogues.size()) {
      return dialogues.get(i);
    }
    return null;
  }

  /**
   * Set the npc to repeat none of the dialogue.
   */
  public void repeatNone() {
    repeatLast = false;
    repeatAll = false;
    repeatCustom = false;
  }

  /**
   * Set the npc to repeat all dialogue.
   */
  public void repeatAll() {
    repeatNone();
    repeatAll = true;
  }

  /**
   * Set the npc to repeat the last dialogue object in the dialogue list.
   */
  public void repeatLast() {
    repeatNone();
    repeatLast = true;
  }

  /**
   * Set the npc to repeat the dialogue at index i.
   *
   * @param i the index
   */
  public void repeatCustom(int i) {
    repeatNone();
    repeatAll = true;
    customIndex = i;
  }

  /**
   * Compute dialogue interactions.
   */
  private void dialogueInteraction(Entity entity) {
    World world = getGame().getWorld(); // get the world
    Dialogue wDialogue = world.getDialogue(); // get the current loaded dialogue
    Dialogue cDialogue = currentDialogue(); // get npc's current dialogue

    // if the world has dialogue and the sender is this npc and it equals the npc's current dialogue
    if (world.hasDialogue() && this.equals(wDialogue.getSpeaker())) {
      world.processDialogue(entity); // increase the worlds dialogue by one
    } else if (cDialogue != null) { // does the npc have dialogue
      world.setDialogue(cDialogue); // set the worlds dialogue to the current dialogue
    } else {
      world.sendMessage("Npc, more like never pants cool.", this); // set the default dialogue
    }
  }

  @Override
  public void dialogueFinish(Dialogue m) {
    if (!dialogues.isEmpty()) {
      dialogueIndex++;
      if (dialogueIndex >= dialogues.size()) {
        if (repeatAll) {
          for (Dialogue dialogue : dialogues) {
            dialogue.resetIndex();
          }
          dialogueIndex = 0;
        } else if (repeatLast) {
          dialogueIndex = dialogues.size() - 1;
          currentDialogue().resetIndex();
        } else if (repeatCustom) {
          dialogueIndex = customIndex;
          getDialogue(dialogueIndex).resetIndex();
        }
      }
    }
  }

  @Override
  public boolean interact(Entity e) {
    dialogueInteraction(e);
    return true;
  }

  @Override
  public Sprite getSprite() {
    if (hasSprite()) {
      return super.getSprite();
    }
    return Sprite.scale(getGame().getImage("NPC"), 2);
  }
}
