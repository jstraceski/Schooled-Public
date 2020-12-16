package schooled.test;

import java.awt.Color;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import org.lwjgl.glfw.GLFW;
import schooled.Game;
import schooled.Window;
import schooled.datatypes.LOG_TYPE;
import schooled.engines.Engine;
import schooled.engines.Logger;
import schooled.engines.RenderEngine;
import schooled.entities.BasicEntity;
import schooled.menu.MenuEntity;
import schooled.menu.Origin;
import schooled.physics.Circle;
import schooled.physics.MassTree;
import schooled.physics.PolygonShape;
import schooled.physics.Vector;

public class Bauls {



  public static boolean EXIT = false;
  public static MassTree DRAW_TREE = null;
  public static ArrayList<BasicEntity> entities = new ArrayList<>();
  public static Window window = new Window();
  public static MenuEntity menuEntity = new MenuEntity(null, new Vector(20, 20), new PolygonShape(100, 20));
  public static ArrayList<BasicEntity> old = new ArrayList<>();
  public static boolean move = false;
  public static boolean mmove = false;
  public static int index = 0;
  public static boolean pause = false;
  public static boolean run = false;
  public static float tScale = 0.01f;

  public static void main(String[] args) {

//    Game.log(d);
//    Game.log(a/(a+b+d));
//    Game.log(b/(a+b+d));


    window.init(null);


    menuEntity.setOrigin(Origin.TOP_LEFT);
    menuEntity.setBackgroundColor(Color.red);




    entities.add(new BasicEntity(new Vector(500, 500), new PolygonShape(300, 50),  0));
    entities.add(new BasicEntity(new Vector(500, 500), new PolygonShape(300, 50),  0));
    entities.add(new BasicEntity(new Vector(500 - 150, 400), new PolygonShape(50, 300),  0));
    entities.add(new BasicEntity(new Vector(500 + 150, 400), new PolygonShape(50, 300),  0));







    for (int eIdx = 0; eIdx < entities.size(); eIdx++) {
      entities.get(eIdx).setName("e" + eIdx);
    }

    while (!window.shouldWindowClose()) {
      window.initWindow();
      Logger.calculateDebugTimesList();

      if (pause) {
        if (window.keyHit(GLFW.GLFW_KEY_P)) {
          pause = false;
        }

        RenderEngine.setColor(Color.black);

        for (BasicEntity entity : entities) {

          if (entity.getInvMass() == 0.0f) {
            RenderEngine.setColor(new Color(50f/255f, 100f/255f, 150f/255f));
          } else {
            RenderEngine.setColor(Color.black);
          }

          RenderEngine.traceEntity(entity);
        }

        if (DRAW_TREE != null) {
          RenderEngine.drawTree(DRAW_TREE);
        }
      } else {
        if (run) {
          for (BasicEntity entity : entities) {
            if (entity.getInvMass() != 0) {
              entity.addVelocity(new Vector(0f, 9.8f * tScale));
            }
          }

          Engine.process(entities, 0.001f);
        }
      }


      if (EXIT) {
        pause = true;
      }



      RenderEngine.setColor(Color.red);

      RenderEngine.setColor(Color.green);

      if (window.keyHit(GLFW.GLFW_KEY_K)) {
        entities.get(index).addVelocity(new Vector(0, -5));
      }

      if (window.keyHit(GLFW.GLFW_KEY_Q)) {
        index = Math.max(index - 1, 0);
      }

      if (window.keyHit(GLFW.GLFW_KEY_E)) {
        index = Math.min(index + 1, entities.size() - 1);
      }

      if (window.keyHit(GLFW.GLFW_KEY_U)) {
        entities.get(index).setMass(entities.get(index).getMass() + 0.5f);
      }

      if (window.keyHit(GLFW.GLFW_KEY_J)) {
        entities.get(index).setMass(entities.get(index).getMass() - 0.5f);
      }

      if (window.keyHit(GLFW.GLFW_KEY_R)) {
//        EXIT = false;
        pause = false;
      }

      if (window.keyHit(GLFW.GLFW_KEY_C)) {
        EXIT = false;
//        pause = false;
      }


      if (window.keyHit(GLFW.GLFW_KEY_S)) {
        try {
          FileOutputStream fileOutputStream = new FileOutputStream("test.sav");
          ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
          objectOutputStream.writeObject(entities);
        } catch (Exception e) {
          e.printStackTrace();
        }
      }

      if (window.keyHit(GLFW.GLFW_KEY_L)) {
        try {
          FileInputStream fileInputStream = new FileInputStream("test.sav");
          ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
          entities = (ArrayList<BasicEntity>) objectInputStream.readObject();

          for (int eIdx = 0; eIdx < entities.size(); eIdx++) {
            entities.get(eIdx).setName("e" + eIdx);
          }

          entities.get(0).setName("seed");
          old.clear();
        } catch (Exception e) {
          e.printStackTrace();
        }
      }

      if (window.keyHit(GLFW.GLFW_KEY_O)) {
        if (old.isEmpty()) {
          old.clear();
          for (BasicEntity basicEntity : entities) {
            old.add(basicEntity.clone());
          }
          run = true;
        } else {
          entities.clear();
          for (BasicEntity basicEntity : old) {
            entities.add(basicEntity.clone());
          }
          old.clear();
          run = false;
        }
      }

      if (window.keyHit(GLFW.GLFW_KEY_2)) {
        Logger.debugInput = !Logger.debugInput;
        Game.log(Logger.debugInput);
      }

      if (window.keyHit(GLFW.GLFW_KEY_P)) {
        pause = true;
      }

      if (window.keyHit(GLFW.GLFW_KEY_M)) {
        BasicEntity basicEntity = new BasicEntity(window.getMousePos(), new PolygonShape(40), 10.0f);
        basicEntity.setName("e" + entities.size());
        entities.add(basicEntity);

      }

      if (window.keyHit(GLFW.GLFW_KEY_TAB)) {
        if (!Game.LOG_FILTER.remove(LOG_TYPE.PHYSICS)) {
          Game.LOG_FILTER.add(LOG_TYPE.PHYSICS);
        }
      }

      if (window.keyHit(GLFW.GLFW_KEY_1)) {
        Game.DEBUG_LOG = !Game.DEBUG_LOG;
      }

      if (window.keyHit(GLFW.GLFW_KEY_SPACE)) {
        BasicEntity basicEntity = new BasicEntity(window.getMousePos(), new Circle(20), 10.0f);
        basicEntity.setName("e" + entities.size());
        entities.add(basicEntity);

      }

      if (window.mouseHit(GLFW.GLFW_MOUSE_BUTTON_1)) {
        move = !move;
        mmove = false;
      }

      if (window.mouseHit(GLFW.GLFW_MOUSE_BUTTON_2)) {
        mmove = !mmove;
        move = false;
      }
      menuEntity.setText("");
      if (entities.size() > 0 && entities.get(index) != null) {
        if (mmove) {
          entities.get(index).setPos(window.getMousePos());
        }

        if (move) {
          BasicEntity e = entities.get(index);
          Vector dir = window.getMousePos().subi(e.getPos());
          e.addVelocity(dir.normalizei().scalei(Math.min(5, dir.mag())));
          RenderEngine.drawArrow(e.getPos(), window.getMousePos());
        }



        menuEntity.setText(
            entities.get(index).getName() + " " + entities.get(index).getInvMass() + ", " + entities.get(index).getMass());
      }

      if (Logger.debugInput) {
        Logger.calculateDebugTimesList();
        menuEntity.addText(Logger.debugText);
      }

      RenderEngine.setColor(Color.black);
      for (BasicEntity entity : entities) {
        if (entity.getInvMass() == 0.0f) {
          RenderEngine.setColor(new Color(50f/255f, 100f/255f, 150f/255f));
        } else {
          RenderEngine.setColor(Color.black);
        }

        RenderEngine.traceEntity(entity);
      }
      RenderEngine.renderMenuEntity(menuEntity);

      window.update();
    }
  }

  public static void pauseLoop() {

    while (pause && !window.shouldWindowClose()) {
      window.initWindow();
      Logger.calculateDebugTimesList();

      if (pause) {
        if (window.keyHit(GLFW.GLFW_KEY_P)) {
          pause = false;
        }

        RenderEngine.setColor(Color.black);

        for (BasicEntity entity : entities) {

          if (entity.getInvMass() == 0.0f) {
            RenderEngine.setColor(new Color(50f/255f, 100f/255f, 150f/255f));
          } else {
            RenderEngine.setColor(Color.black);
          }

          RenderEngine.traceEntity(entity);
        }

        if (DRAW_TREE != null) {
          RenderEngine.drawTree(DRAW_TREE);
        }
      }


      if (EXIT) {
        pause = true;
      }



      RenderEngine.setColor(Color.red);

      RenderEngine.setColor(Color.green);

      if (window.keyHit(GLFW.GLFW_KEY_K)) {
        entities.get(index).addVelocity(new Vector(0, -5));
      }

      if (window.keyHit(GLFW.GLFW_KEY_Q)) {
        index = Math.max(index - 1, 0);
      }

      if (window.keyHit(GLFW.GLFW_KEY_E)) {
        index = Math.min(index + 1, entities.size() - 1);
      }

      if (window.keyHit(GLFW.GLFW_KEY_U)) {
        entities.get(index).setMass(entities.get(index).getMass() + 0.5f);
      }

      if (window.keyHit(GLFW.GLFW_KEY_J)) {
        entities.get(index).setMass(entities.get(index).getMass() - 0.5f);
      }

      if (window.keyHit(GLFW.GLFW_KEY_R)) {
//        EXIT = false;
        pause = false;
      }

      if (window.keyHit(GLFW.GLFW_KEY_C)) {
        EXIT = false;
//        pause = false;
      }


      if (window.keyHit(GLFW.GLFW_KEY_S)) {
        try {
          FileOutputStream fileOutputStream = new FileOutputStream("test.sav");
          ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
          objectOutputStream.writeObject(entities);
        } catch (Exception e) {
          e.printStackTrace();
        }
      }

      if (window.keyHit(GLFW.GLFW_KEY_L)) {
        try {
          FileInputStream fileInputStream = new FileInputStream("test.sav");
          ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
          entities = (ArrayList<BasicEntity>) objectInputStream.readObject();

          for (int eIdx = 0; eIdx < entities.size(); eIdx++) {
            entities.get(eIdx).setName("e" + eIdx);
          }

          entities.get(0).setName("seed");
          old.clear();
        } catch (Exception e) {
          e.printStackTrace();
        }
        EXIT = true;
      }

      if (window.keyHit(GLFW.GLFW_KEY_O)) {
        if (old.isEmpty()) {
          old.clear();
          for (BasicEntity basicEntity : entities) {
            old.add(basicEntity.clone());
          }
          run = true;
        } else {
          entities.clear();
          for (BasicEntity basicEntity : old) {
            entities.add(basicEntity.clone());
          }
          old.clear();
          run = false;
        }
      }



      if (window.keyHit(GLFW.GLFW_KEY_P)) {
        pause = true;
      }

      if (window.keyHit(GLFW.GLFW_KEY_M)) {
        BasicEntity basicEntity = new BasicEntity(window.getMousePos(), new PolygonShape(40), 10.0f);
        basicEntity.setName("e" + entities.size());
        entities.add(basicEntity);

      }

      if (window.keyHit(GLFW.GLFW_KEY_TAB)) {
        if (!Game.LOG_FILTER.remove(LOG_TYPE.PHYSICS)) {
          Game.LOG_FILTER.add(LOG_TYPE.PHYSICS);
        }
      }

      if (window.keyHit(GLFW.GLFW_KEY_1)) {
        Game.DEBUG_LOG = !Game.DEBUG_LOG;
      }

      if (window.keyHit(GLFW.GLFW_KEY_SPACE)) {
        BasicEntity basicEntity = new BasicEntity(window.getMousePos(), new Circle(20), 10.0f);
        basicEntity.setName("e" + entities.size());
        entities.add(basicEntity);

      }

      if (window.mouseHit(GLFW.GLFW_MOUSE_BUTTON_1)) {
        move = !move;
        mmove = false;
      }

      if (window.mouseHit(GLFW.GLFW_MOUSE_BUTTON_2)) {
        mmove = !mmove;
        move = false;
      }

      if (entities.size() > 0 && entities.get(index) != null) {
        if (mmove) {
          entities.get(index).setPos(window.getMousePos());
        }

        if (move) {
          BasicEntity e = entities.get(index);
          Vector dir = window.getMousePos().subi(e.getPos());
          e.addVelocity(dir.normalizei().scalei(Math.min(5, dir.mag())));
          RenderEngine.drawArrow(e.getPos(), window.getMousePos());
        }


        menuEntity.setText(
            entities.get(index).getName() + " " + entities.get(index).getInvMass() + ", " + entities
                .get(index).getMass() + "\n" + Logger.debugText);
      }

      RenderEngine.setColor(Color.black);
      for (BasicEntity entity : entities) {
        if (entity.getInvMass() == 0.0f) {
          RenderEngine.setColor(new Color(50f/255f, 100f/255f, 150f/255f));
        } else {
          RenderEngine.setColor(Color.black);
        }

        RenderEngine.traceEntity(entity);
      }
      RenderEngine.renderMenuEntity(menuEntity);

      window.update();
    }
  }
}
