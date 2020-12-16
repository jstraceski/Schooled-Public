/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package schooled.menu;

import java.awt.Color;
import org.lwjgl.system.CallbackI.P;
import schooled.Game;
import schooled.engines.RenderEngine;
import schooled.entities.Entity;
import schooled.event.Event;
import schooled.physics.Vector;

/**
 * Menu entity with an editable text area.
 * <p>
 * Overrides the basic text modification methods.
 * <p></p>
 * Program written by Joseph Straceski
 * <p>
 * contact: straceski.joseph@gmail.com, https://github.com/Crepox
 */
public class TextArea extends MenuEntity {

  private float time = 0; // time register to track flashing cursor data
  private float period = 0.25f; // time in between flashes
  private float lastRight = -1;
  private int editIndex = -1;
  private String previousText, label, editText = "Click To Edit Text"; // current edit text
  private Event enterEvent = null; // preformed when the text area is selected and enter is pressed
  private Event escapeEvent = null;
  private Event unselectedEvent = null;
  private Event editEvent = null;
  private float backspaceTimer = 0.5f;
  private float backspaceCount = 0;
  private float backspaceTimerFast = 0.025f;
  private boolean lastBackspace = false;
  private boolean currentBackspace = false;
  private boolean customBackspace = false;
  private boolean clearOnEdit = true;
  private Vector editLocation = null;
  private Vector updateLoc = null;
  private boolean resetRight = false;
  private int lineIndex = -1;
  private int rowIndex = -1;
  private int updateLineIndex = -1;
  private boolean findStartIndex = false;
  private boolean findEndIndex = false;
  private int startIndex = -1;
  private int[] dispIndex = null;
  private boolean suppressEditEvent = false;
  private int dragCount = 0;
  /**
   * Construct an empty text area.
   *
   * @param g game instance
   */
  public TextArea(Game g) {
    super(g);
    init();
  }

  /**
   * Construct an empty text area with a position.
   * @param g game instance
   * @param v position
   */
  public TextArea(Game g, Vector v) {
    super(g, v);
    init();
  }

  /**
   * Constructor that sets the default text.
   *
   * @param g game instance
   * @param v text area position
   * @param s default text
   */
  public TextArea(Game g, Vector v, String s) {
    super(g, v, null);
    editText = s;
    editIndex = editText.length();
    init();
  }

  /**
   * Generalized initialization method.
   */
  private void init() {
    setOrigin(Origin.TOP_LEFT);
  }

  public int getLineIndex() {
    return lineIndex;
  }

  public void setLineIndex(Integer lineIndex) {
    if (lineIndex != null) {
      this.lineIndex = lineIndex;
    }
  }

  public int getUpdateLineIndex() {
    return updateLineIndex;
  }

  public void setUpdateLineIndex(int updateLineIndex) {
    this.updateLineIndex = updateLineIndex;
  }

  public void setIndexFromLocation(Vector location) {
    updateLoc = location.subi(this.getDefaultPos());
    forceUpdate();
  }

  public void setUpdateLocation(Vector location) {
    updateLoc = location;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  @Override
  public void setText(String string) {
    clearText();
    addString(string);
  }

  @Override
  public void clearText() {
    super.clearText();
    editText = "";
    editIndex = 0;
  }

  @Override
  public void addText(String textContext) {
    addString(textContext);
  }

  @Override
  public void addText(TextContext textContext) {
    addString(textContext.getText());
  }

  public void addString(String s) {
    replaceSelection(s, false);
    clearSelection();
    forceUpdate();
  }

  /**
   * Process a backspace character.
   *
   * Remove one character from the edit text.
   */
  public void backspace() {
    if (customBackspace) {
      if (!lastBackspace) {
        preformBackspace();
        backspaceCount = backspaceTimer;
      } else if (backspaceCount <= 0) {
        preformBackspace();
        backspaceCount = backspaceTimerFast;
      }

      currentBackspace = true;
    } else {
      preformBackspace();
    }
  }

  private void preformBackspace() {
    replaceSelection("", true);
    clearSelection();
  }

  public int[] getSelection() {
    if (startIndex != -1 && editIndex != -1 && startIndex != editIndex) {
      int begin = Math.min(startIndex, editIndex);
      int end = Math.max(startIndex, editIndex);
      end = Math.min(end, editText.length());
      return new int[]{begin, end};
    }

    return null;
  }

  public String getSelectionText() {
    int[] out = getSelection();
    if (out != null) {
      return editText.substring(out[0], out[1]);
    }
    return null;
  }

  public void replaceSelection(String s, boolean del) {
    int[] out = getSelection();

    if (editIndex == -1) {
      editText = s;
      editIndex = s.length();
    }

    if (out == null) {
        if (del) {
          if (editIndex > 0) {
            editText = editText.substring(0, editIndex - 1) + s + editText.substring(editIndex);
            editIndex = editIndex - 1 + s.length();
          }
        } else {
          editText = editText.substring(0, editIndex) + s + editText.substring(editIndex);
          editIndex = editIndex + s.length();
        }
    } else {
      editText = editText.substring(0, out[0]) + s + editText.substring(out[1]);
      editIndex = out[0] + s.length();
    }
  }

  /**
   * Sets the edit text to the empty string.
   *
   * @param e interaction entity
   * @return true
   */
  @Override
  public boolean interact(Entity e) {
    getParentMenu().setSelectedArea(this);
    setIndexFromLocation(e.getPos());
    startDrag();

    if (clearOnEdit) {
      editText = "";
    }

    return false;
  }

  /**
   * Preform an update with a time difference.
   *
   * TODO: implement flashing cursor
   *
   * @param f time difference
   */
  @Override
  public void updateCycle(float f) {
    backspaceCount -= f;
    lastBackspace = currentBackspace;
    currentBackspace = false;
    dragCount = dragCount - 1;

    if (getParentMenu().getSelectedArea() == this) {
      if (editLocation == null) {
        forceUpdate();
      }

      time += f;
      if (time > period) {
        time = -period;
      }
    }

    String customText = editText;
    if (label != null) {
      customText = label + customText;
    }

    int[] list = getSelection();
    if (list != null && (dispIndex == null || dispIndex[0] != list[0] || dispIndex[1] != list[1])) {
      super.clearText();

      String p1 = editText.substring(0, list[0]);

      if (!p1.isEmpty()) {
        super.addText(generateTextContext(p1, null, null, null));
      }

      String p2 = editText.substring(list[0], list[1]);

      if (!p2.isEmpty()) {
        TextContext textContext = generateTextContext(p2, null, null, null);
        textContext.setColor(Color.orange);
        super.addText(textContext);
      }

      String p3 = editText.substring(list[1]);

      if (!p3.isEmpty()) {
        super.addText(generateTextContext(p3, null, null, null));
      }

      dispIndex = list;
    }

    if (!customText.equals(previousText) || (dispIndex != null && list == null)) {
      super.clearText();
      super.addText(generateTextContext(customText, null, null, null));
      previousText = customText;

      if (!suppressEditEvent) {
        if (editEvent != null) {
          editEvent.act();
        }
      } else {
        suppressEditEvent = false;
      }
      dispIndex = null;
    }

    super.updateCycle(f);
  }

  @Override
  public void updateVisual(Object gc) {
    super.updateVisual(gc);
    if (resetRight) {
      lastRight = editLocation.getX();
      resetRight = false;
    }

    if (findStartIndex) {
      startIndex = editIndex;
      findStartIndex = false;
    }
  }

  @Override
  public void unload() {
    updateCycle();
  }

  /**
   * Set the event that is preformed when the entity is selected and enter is pressed.
   *
   * @param event enter event
   */
  public void setEnterEvent(Event event) {
    enterEvent = event;
  }

  public int getEditIndex() {
    return editIndex;
  }

  public float getLastRight() {
    return lastRight;
  }

  public void setLastRight(float lastRight) {
    this.lastRight = lastRight;
  }

  public void setEditIndex(Integer index) {
    if (index != null) {
      this.editIndex = index;
      forceUpdate();
    }
  }

  public Vector getUpdateLocation() {
    return updateLoc;
  }

  /**
   * Preform the enter event.
   */
  public void enter() {
    if (enterEvent != null) {
      enterEvent.act();
    } else {
      addString("\n");
    }
  }

  /**
   * Preform the enter event.
   */
  public void escape() {
    if (escapeEvent != null) {
      escapeEvent.act();
    }
  }

  public Event getEscapeEvent() {
    return escapeEvent;
  }

  public void setEscapeEvent(Event escapeEvent) {
    this.escapeEvent = escapeEvent;
  }

  public void setEditEvent(Event editEvent) {
    this.editEvent = editEvent;
  }
  public void setEditLoc(Vector location) {
    this.editLocation = location;
  }

  public boolean isClearOnEdit() {
    return clearOnEdit;
  }

  public void suppressEditEvent() {
    suppressEditEvent = true;
  }

  public void setClearOnEdit(boolean clearOnEdit) {
    this.clearOnEdit = clearOnEdit;
  }

  public void setRowIndex(Integer row) {
    if (row != null) {
      this.rowIndex = row;
    }
  }

  public void setUnselectedEvent(Event unselectedEvent) {
    this.unselectedEvent = unselectedEvent;
  }

  @Override
  public void renderHook(Object gc, Vector gShift, float gScale) {
    super.renderHook(gc, gShift, gScale);

    if (getParentMenu().getSelectedArea() == this && editLocation != null) {
      Vector tickLoc = getPos().subi(getSize().scalei(0.5f)).addi(editLocation);
      if (rowIndex > 0) {
        tickLoc.addi(new Vector(1, 0));
      }
      RenderEngine.fillRect(gc, tickLoc.addi(new Vector(0, 1)), tickLoc.addi(new Vector(1f, -9f)));
    }
  }

  public void incIndex(int i) {
    editIndex += i;
    if (editIndex < 0) {
      editIndex = 0;
    } else if (editIndex > editText.length()) {
      editIndex = editText.length();
    }
    resetRight = true;

    clearSelection();
    forceUpdate();
  }

  public void up() {
    if (lastRight == -1 && editLocation != null) {
      lastRight = editLocation.getX();
    }

    if (lineIndex == 0) {
      editIndex = 0;
    } else if (lineIndex > 0) {
      updateLineIndex = lineIndex -1;
    }

    clearSelection();
    forceUpdate();
  }

  public void down() {
    if (lastRight == -1 && editLocation != null) {
      lastRight = editLocation.getX();
    }
    updateLineIndex = lineIndex + 1;

    clearSelection();
    forceUpdate();
  }

  public void clearSelection() {
    startIndex = -1;
  }

  public void startDrag() {
    if (getParentMenu().getSelectedArea() == this) {
      findStartIndex = true;
    }
    clearSelection();
    forceUpdate();
  }

  public void contDrag(Vector vector) {
    setIndexFromLocation(vector);
    dragCount = 2;
  }

  public void checkDrag() {
    if (dragCount < 1) {
      clearSelection();
    }
  }

  public String cut() {
    String s = getSelectionText();
    replaceSelection("", false);
    clearSelection();
    return s;
  }

  public String copy() {
    return getSelectionText();
  }
}
