package schooled.test;

import java.awt.Color;
import java.util.ArrayList;
import schooled.Window;
import schooled.containers.Room;
import schooled.engines.RenderEngine;
import schooled.entities.Entity;
import schooled.entities.Player;
import schooled.entities.Player.Pose;
import schooled.loaders.JsonLoader;
import schooled.loaders.Lookup;
import schooled.loaders.PFlags;
import schooled.physics.MultiShape;
import schooled.physics.Vector;

public class TestJsonLoader {

  public static void main(String[] args) {
    Window window = new Window();
    window.init(null);


//    MenuEntity menuEntity = new MenuEntity(null, new Vector(20, 20), new PolygonShape(100, 20));
//    menuEntity.setOrigin(Origin.TOP_LEFT);
//    menuEntity.addText("aqua-light inc.");
//    menuEntity.setDefaultFontColor(new Color(160, 120, 160));
//    menuEntity.setFontSize(16 * 8);
//    menuEntity.setFilter(new GLFilter());
//    menuEntity.update();


    RenderEngine.init(Window.DEFAULT_GRAPHICS_CONTEXT);

    Lookup lookup = new Lookup();
    PFlags pFlags = new PFlags();

    try {
      JsonLoader.parseJson("resources/images/Vehicles/Bus-edata.json", lookup, null, pFlags);
      JsonLoader.parseJson("resources/images/Player/Player_v6-edata.json", lookup, null, pFlags);


      JsonLoader.parseJson("resources/images/Etc/Ball-edata.json", lookup, null, pFlags);
      JsonLoader.parseJson("resources/images/Etc/Flower-edata.json", lookup, null, pFlags);
      JsonLoader.parseJson("resources/images/Furniture/Door_Closed_Bottom_Up-edata.json", lookup, null, pFlags);

      JsonLoader.parseJson("resources/images/Rooms/School_Inside-edata.json", lookup, null, pFlags);
    } catch (Exception e) {
      e.printStackTrace();
    }

    Room room = ((Room) lookup.get("inside"));

    ArrayList<Entity> entities = new ArrayList<>();
    Player entity = ((Player) lookup.get("player"));
    entity.setPosition(new Vector(320, 320));
    entity.setPose(Pose.LYING_DOWN);
    room.addEntity(entity);
//    entity.setVelocity(new Vector(10, 0));

    MultiShape multiShape = ((MultiShape) entity.getShape());

    entities.add(entity);

    while (true) {
      if (window.shouldWindowClose()) {
        break;
      }
      window.initWindow();

      entity.updateCycle(0.001f);

      RenderEngine.setColor(Window.DEFAULT_GRAPHICS_CONTEXT, Color.white);
      RenderEngine.drawRoom(Window.DEFAULT_GRAPHICS_CONTEXT, room, 2, Vector.zero, window.getSize(), Vector.zero);



//      RenderEngine.drawEntities(Window.DEFAULT_GRAPHICS_CONTEXT, entities, 4f, Vector.zero, window.getSize(), Vector.zero);
//      RenderEngine.drawDebug(Window.DEFAULT_GRAPHICS_CONTEXT, entities, 4f, Vector.zero, Vector.zero);
      RenderEngine.setColor(Window.DEFAULT_GRAPHICS_CONTEXT, Color.black);

      RenderEngine.drawLine(Window.DEFAULT_GRAPHICS_CONTEXT, Vector.zero, new Vector(200, 200));
      window.update();
    }

  }
}
