/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package schooled.menu;

import org.lwjgl.glfw.GLFW;
import schooled.Window;
import schooled.containers.EntityHolder;
import schooled.entities.Entity;
import schooled.physics.Vector;

/**
 * Menu object that holds menu entities.
 * <p>
 * Organizes menu navigation and storage.
 * <p></p>
 * Program written by Joseph Straceski
 * <p>
 * contact: straceski.joseph@gmail.com, https://github.com/Crepox
 */
public class Menu extends EntityHolder {

  private Menu previousMenu; // previously loaded menu
  private MenuEntity selectedArea; // variable placeholder for the selected menuEntity
  private Vector customSize = null; // variable for a custom menu size
  public static Vector defaultMenuSize = new Vector(800, 450);
  private Window window;

  public Menu() {

  }

  public Menu(Window w) {
    window = w;
  }

  /**
   * Process an unloading of all the contained entities.
   */
  public void unloadEntities() {
    for (Entity me : this.getEntities()) {
      if (me instanceof MenuEntity) {
        ((MenuEntity) me).unload();
      }
    }
  }

  /**
   * Get the custom size of the menu if it as one.
   *
   * @return custom size
   */
  public Vector getCustomSize() {
    return customSize;
  }

  /**
   * Set the custom size of the menu.
   *
   * @param customSize custom size
   */
  public void setCustomSize(Vector customSize) {
    this.customSize = customSize;
  }

  /**
   * Does the Menu container have a custom size.
   *
   * @return true if the Menu has a custom size, false if otherwise.
   */
  public boolean hasCustomSize() {
    return customSize != null;
  }

  /**
   * Get the selected MenuEntity.
   *
   * @return selected entity
   */
  public MenuEntity getSelectedArea() {
    return selectedArea;
  }

  /**
   * Set the selected menuEntity
   *
   * @param selectedArea selected entity
   */
  public void setSelectedArea(MenuEntity selectedArea) {
    this.selectedArea = selectedArea;
  }

  /**
   * Get the previously loaded Menu container.
   *
   * If the Menu has no previous Menu this will return null.
   *
   * @return previous Menu
   */
  public Menu getPreviousMenu() {
    return previousMenu;
  }

  /**
   * Set the previously loaded Menu.
   *
   * @param previous previous Menu
   */
  public void setPreviousMenu(Menu previous) {
    this.previousMenu = previous;
  }

  /**
   * Add a entity to the container.
   *
   * Set its parent to the given container.
   *
   * @param menuEntity menu entity
   */
  public void addEntity(MenuEntity menuEntity) {
    super.addEntity(menuEntity);
    menuEntity.setParentMenu(this);
  }

  @Override
  public void updateCycle(float t) {
    if (selectedArea instanceof TextArea && window != null) {

      if (window.keyTyped(GLFW.GLFW_KEY_ENTER)) {
        ((TextArea) selectedArea).enter();
      }
      if (window.keyTyped(GLFW.GLFW_KEY_BACKSPACE)) {
        ((TextArea) selectedArea).backspace();
      }
      if (window.keyTyped(GLFW.GLFW_KEY_ESCAPE)) {
        ((TextArea) selectedArea).escape();
      }
      if (window.keyTyped(GLFW.GLFW_KEY_LEFT)) {
        ((TextArea) selectedArea).incIndex(-1);
      }
      if (window.keyTyped(GLFW.GLFW_KEY_RIGHT)) {
        ((TextArea) selectedArea).incIndex(1);
      }
      if (window.keyTyped(GLFW.GLFW_KEY_UP)) {
        ((TextArea) selectedArea).up();
      }
      if (window.mouseHit(GLFW.GLFW_MOUSE_BUTTON_LEFT)) {
        ((TextArea) selectedArea).checkDrag();
      }
      if (window.mouseDown(GLFW.GLFW_MOUSE_BUTTON_LEFT)) {
        ((TextArea) selectedArea).contDrag(window.getMousePos());
      }

      if (window.keyTyped(GLFW.GLFW_KEY_X) && (window.keyDown(GLFW.GLFW_KEY_LEFT_CONTROL) || window.keyDown(GLFW.GLFW_KEY_RIGHT_CONTROL))) {
        window.setClip(((TextArea) selectedArea).cut());
      }
      if (window.keyTyped(GLFW.GLFW_KEY_C) && (window.keyDown(GLFW.GLFW_KEY_LEFT_CONTROL) || window.keyDown(GLFW.GLFW_KEY_RIGHT_CONTROL))) {
        window.setClip(((TextArea) selectedArea).copy());
      }

      if (window.keyTyped(GLFW.GLFW_KEY_DOWN)) {
        ((TextArea) selectedArea).down();
      }
      String s = window.getTypeBuffer();
      if (!s.equals("")) {
        ((TextArea) selectedArea).addString(s);
      }
    }


    super.updateCycle(t);

    for (Entity entity : getEntities()){
      updateEntityPosition(entity);
    }
  }

  public void updateEntityPosition(Entity entity) {
    if (entity instanceof MenuEntity) {
      MenuEntity menuEntity = ((MenuEntity) entity);
      if (menuEntity.isHorizontalCenter()) {
        Vector v = (menuEntity.getDefaultPos());

        if (hasCustomSize()) {
          v.setX(getCustomSize().getX() / 2);
        } else {
          v.setX(defaultMenuSize.getX() / 2);
        }

        entity.setPosition(v);
      }

      if (menuEntity.isVerticalCenter()) {
        Vector v = (menuEntity.getDefaultPos());

        if (hasCustomSize()) {
          v.setY(getCustomSize().getY() / 2);
        } else {
          v.setY(defaultMenuSize.getY() / 2);
        }

        entity.setPosition(v);
      }
    }
  }

  public void preRender() {
    getEntities().forEach(entity -> entity.preRender(Window.DEFAULT_GRAPHICS_CONTEXT));
  }

  public void preRender(Object graphicsContext) {
    getEntities().forEach(entity -> entity.preRender(graphicsContext));
  }


  /**
   * Update all menu entities in the container.
   */
  public void update() {
    for (Entity entity : getEntities()){
      if (entity instanceof MenuEntity) {
        ((MenuEntity) entity).updateCycle();
      }
    }
  }


}
