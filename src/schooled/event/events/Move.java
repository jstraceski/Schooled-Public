/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package schooled.event.events;

import schooled.Game;
import schooled.containers.EntityHolder;
import schooled.containers.Room;
import schooled.containers.World;
import schooled.entities.ContainerEntity;
import schooled.entities.Entity;
import schooled.event.Event;
import schooled.physics.Vector;

/**
 * A pre-built Event that moves an entity. Imutable.
 * <p></p>
 * Program written by Joseph Straceski
 * <p>
 * contact: straceski.joseph@gmail.com, https://github.com/Crepox
 */
public class Move extends Event {

  /**
   * Construct a pre-built move event.
   * <p>
   * Moves the Entity: entity to the location: position in Room: room.
   *
   * @param entity   entity to move
   * @param position end position
   * @param room     end room
   */
  public Move(Entity entity, Vector position, Room room, boolean load) {
    super(() -> {
      entity.setPosition(position);
      if (room == null || !room.equals(entity.getContainer())) {
        entity.setRoom(room);
        World world;
        if (load && (world = room.getWorld()) != null) {
          world.setLoadedRoom(room);
        }
      }
    });
  }

  /**
   * Construct a pre-built move event.
   * <p>
   * Moves the Entity: entity to the location: position in Room: room.
   *
   * @param entity   entity to move
   * @param position end position
   * @param entity   end entity
   */
  public Move(Entity entity, Vector position, Entity container) {
    super(() -> {
      entity.setPosition(position);
      if (container != null) {
        entity.setParent(container);
      }
    });
  }

  /**
   * Construct a pre-built move event.
   * <p>
   * Moves the Entity: entity to the location: position in the current room.
   *
   * @param entity   entity to move
   * @param position end position
   */
  public Move(Entity entity, Vector position) {
    this(entity, position, (Room) entity.getContainer(), false);
  }

}
