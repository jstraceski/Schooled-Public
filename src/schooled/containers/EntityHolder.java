/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package schooled.containers;

import java.util.ArrayList;
import java.util.function.Consumer;
import org.lwjgl.glfw.GLFW;
import schooled.Game;
import schooled.entities.Entity;
import schooled.event.Event;
import schooled.menu.TextArea;

/**
 * General object that contains entities and entity related actions.
 * <p></p>
 * Program written by Joseph Straceski
 * <p>
 * contact: straceski.joseph@gmail.com, https://github.com/Crepox
 */
public abstract class EntityHolder {

  // store a list of entities
  ArrayList<Entity> entities = new ArrayList<Entity>();
  String name = "Entity Container";

  /**
   * Get the name of the container.
   *
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * Set the name of the entity container.
   *
   * @param name the name of the string
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Adds an entity into the container.
   *
   * @param e the entity
   */
  public void addEntity(Entity e) {
    addEntity(e, true);
  }

  public void addEntity(Entity e, boolean setContainer) {
    if (!entities.contains(e)) {
      entities.add(e);
      if (setContainer) {
        e.setContainer(this);
      }
    }
  }

  /**
   * Adds an entity into the container.
   *
   * @param e the entity
   */
  public boolean hasEntity(Entity e) {
    return entities.contains(e);
  }

  /**
   * Removes an entity from the container.
   *
   * @param e the entity reference
   */
  public void removeEntity(Entity e) {
    if (entities.remove(e)) {
      e.setContainer(null);
    }
  }

  /**
   * Update all menu entities in the container.
   */
  public void updateCycle(float t) {
    for (Entity entity : getEntities()){
      entity.updateCycle(t);
    }
  }

  /**
   * Update all menu entities in the container.
   */
  public void processInteractions() {
    ArrayList<Entity> entities = (ArrayList<Entity>) getEntities().clone();
    for (Entity entity : entities){
      entity.processInteractions();
    }
  }

  /**
   * Get the list of entities.
   *
   * @return the list of entities
   */
  public ArrayList<Entity> getEntities() {
    return entities;
  }

  public void forEntities(Consumer<Entity> func) {
    entities.forEach(func);
  }

  public void clearAll() {
    entities.clear();
  }
}
