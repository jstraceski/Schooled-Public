package schooled.test;

import java.awt.Color;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import org.lwjgl.glfw.GLFW;
import schooled.Game;
import schooled.Window;
import schooled.engines.Engine;
import schooled.engines.RenderEngine;
import schooled.entities.BasicEntity;
import schooled.menu.MenuEntity;
import schooled.menu.Origin;
import schooled.physics.Manifold;
import schooled.physics.MassTree;
import schooled.physics.PolygonShape;
import schooled.physics.Vector;

public class Nico2 {



  public static void main(String[] args) {
    float c = 0.30771892f;
    float a = 0.8703605f;
    float b = 0.7423784f;
    float d = (b - c*b - c*a)/c;

//    Game.log(d);
//    Game.log(a/(a+b+d));
//    Game.log(b/(a+b+d));

    Window window = new Window();
    window.init(null);

    MenuEntity menuEntity = new MenuEntity(null, new Vector(20, 20), new PolygonShape(100, 20));
    menuEntity.setOrigin(Origin.TOP_LEFT);
    menuEntity.setBackgroundColor(Color.red);

    Vector c1 = new Vector(300, 100);

    PolygonShape polygonShape = new PolygonShape();
    polygonShape.addVertex(0, 56);
    polygonShape.addVertex(56, 0);
    polygonShape.addVertex(0, -56);
    polygonShape.addVertex(-56, 0);


    PolygonShape polygonShape7 = new PolygonShape();
    polygonShape7.addVertex(-40, 40);
//    polygonShape7.addVertex(0, 60);
    polygonShape7.addVertex(40, 40);
    polygonShape7.addVertex(40, -40);
    polygonShape7.addVertex(-40, -40);

    Vector c2 = c1.subi(new Vector(40, -40)).addScaledi(new Vector(-1, 1).normalizei(), (float) Math.sqrt(56 * 56 * 2)/2 - 1);

    BasicEntity e1 = new BasicEntity(new Vector(300, 100), polygonShape7, 1);
    BasicEntity e2 = new BasicEntity(c2, polygonShape, 0);



    PolygonShape polygonShape2 = new PolygonShape();
    polygonShape2.addVertex(0, 56);
    polygonShape2.addVertex(56/2, 0);
    polygonShape2.addVertex(0, -56);
    polygonShape2.addVertex(-56/2, 0);

    PolygonShape polygonShape4 = new PolygonShape();
    polygonShape4.addVertex(-40, 40);
    polygonShape4.addVertex(40, 40);
    polygonShape4.addVertex(40, -40);
    polygonShape4.addVertex(0, -60);
    polygonShape4.addVertex(-40, -40);

    PolygonShape polygonShape3 = new PolygonShape();
    polygonShape3.addVertex(0, 56/2);
    polygonShape3.addVertex(56, 0);
    polygonShape3.addVertex(0, -56/2);
    polygonShape3.addVertex(-56, 0);

    PolygonShape polygonShape10 = new PolygonShape();
    polygonShape10.addVertex(56, 0);
    polygonShape10.addVertex(0, -56);
    polygonShape10.addVertex(-56, 0);

    PolygonShape polygonShape8 = new PolygonShape();
    polygonShape8.addVertex(56, 0);
    polygonShape8.addVertex(0, -56/2);
    polygonShape8.addVertex(-56, 0);

    PolygonShape polygonShape9 = new PolygonShape();
    polygonShape9.addVertex(56f/2.0f/3.0f, 0);
    polygonShape9.addVertex(0, -56.0f/2.0f);
    polygonShape9.addVertex(-56f/2.0f/3.0f, 0);

    PolygonShape polygonShape5 = new PolygonShape();
    polygonShape5.addVertex(0, 56.0f/2.0f);
    polygonShape5.addVertex(56f/2.0f/3.0f, 0);
    polygonShape5.addVertex(0, -56.0f/2.0f);
    polygonShape5.addVertex(-56f/2.0f/3.0f, 0);


    BasicEntity e26 = new BasicEntity(c2.addi(new Vector(56*2+1, 0)), polygonShape8, 1);
    BasicEntity e27 = new BasicEntity(c2.addi(new Vector(56*2+1, 0)), polygonShape9, 1);
    BasicEntity e28 = new BasicEntity(c2.addi(new Vector(56*2+1, 0)), polygonShape10, 1);


    BasicEntity e9 = new BasicEntity(c2.addi(new Vector(56*2+1, 0)), polygonShape, 1);
    BasicEntity e13 = new BasicEntity(c2.addi(new Vector(56*2+1, 0)), polygonShape, 1);
    BasicEntity e14 = new BasicEntity(c2.addi(new Vector(56*2+1, 0)), polygonShape, 0);

    BasicEntity e5 = new BasicEntity(c2.addi(new Vector(56*2+1, 0)), polygonShape2, 1);
    BasicEntity e10 = new BasicEntity(c2.addi(new Vector(56*2+1, 0)), polygonShape2, 0);
    BasicEntity e8 = new BasicEntity(c2.addi(new Vector(56*2+1, 0)), polygonShape3, 1);
    BasicEntity e21 = new BasicEntity(c2.addi(new Vector(56*2+1, 0)), polygonShape4, 1);
    BasicEntity e22 = new BasicEntity(c2.addi(new Vector(56*2+1, 0)), polygonShape5, 1);
    BasicEntity e15 = new BasicEntity(c2.addi(new Vector(56*2+1, 0)), polygonShape3, 0);
    BasicEntity e11 = new BasicEntity(c2.addi(new Vector(56*2+1, 0)), polygonShape, 0);
    BasicEntity e16 = new BasicEntity(c2.addi(new Vector(56*2+1, 0)), polygonShape, 0);

    BasicEntity e18 = new BasicEntity(new Vector(359, 120), new PolygonShape(40, 40), 0);

    BasicEntity e17 = new BasicEntity(new Vector(359, 120), new PolygonShape(40, 40), 0);

    BasicEntity e19 = new BasicEntity(new Vector(359, 120), new PolygonShape(40, 40), 0);
    BasicEntity e4 = new BasicEntity(new Vector(359, 120), new PolygonShape(40, 40), 0);
    BasicEntity e12 = new BasicEntity(new Vector(359, 120), new PolygonShape(40, 40), 1);

    BasicEntity e20 = new BasicEntity(new Vector(359, 120), new PolygonShape(80, 80), 1);
    BasicEntity e6 = new BasicEntity(new Vector(359, 80), new PolygonShape(40, 40), 1);
    BasicEntity e7 = new BasicEntity(new Vector(398, 100), new PolygonShape(40, 40), 1);
    BasicEntity e23 = new BasicEntity(new Vector(398, 100), new PolygonShape(15, 15), 0);
    BasicEntity e3 = new BasicEntity(new Vector(318, 159), new PolygonShape(40, 40), 1f);

    ArrayList<BasicEntity> entities = new ArrayList<>(Arrays.asList(e1, e2, e3, e4, e5, e6, e7, e8, e9, e10, e11, e12, e13, e14, e15, e16, e17, e18, e19, e20, e21, e22, e23, e26, e27, e28));
    ArrayList<Manifold> manifolds = new ArrayList<>();
    HashMap<BasicEntity, ArrayList<Manifold>> map;
    MassTree data = new MassTree();

    boolean move = false;
    boolean mmove = false;
    int index = 0;

    ArrayList<BasicEntity> old = new ArrayList<>();
    boolean pause = false;
    boolean run = false;




    while (!window.shouldWindowClose()) {

      window.initWindow();

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
        RenderEngine.renderMenuEntity(menuEntity);

        window.update();
        continue;
      }



      RenderEngine.setColor(Color.red);

      for (Manifold manifold : manifolds) {
        Vector aPos = manifold.a.getPos();

        Vector bPos = manifold.b.getPos();
        for (Vector v : manifold.normals) {
          RenderEngine.drawLine(aPos, aPos.addScaledi(v, 20));
          RenderEngine.drawLine(bPos, bPos.addScaledi(v, -20));
        }
      }

      RenderEngine.setColor(Color.green);

      if (data.outNormal != null) {
        RenderEngine.drawLine(e1.getPos(), e1.getPos().addScaledi(data.outNormal, 20));
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

          e1 = entities.get(0);
          entities.get(0).setName("seed");
          old.clear();
        } catch (Exception e) {
          e.printStackTrace();
        }
      }

      if (window.keyHit(GLFW.GLFW_KEY_K)) {

        if (old.isEmpty()) {
          Engine.clearAll(entities);
          manifolds = Engine.findManifolds(entities);
          map = Engine.generateManifoldLookup(manifolds);
          data = Engine.collectMass(e1, map, new Vector(0, -1));

          if (data.inNormal != null) {
            old.clear();
            for (BasicEntity basicEntity : entities) {
              old.add(basicEntity.clone());
            }

            Engine.shiftEntities(data, 10);

            for (BasicEntity basicEntity : entities) {
              basicEntity.addPosition(basicEntity.getShift());
              basicEntity.setShift(Vector.zero.clone());
            }
          }

        } else {
          entities.clear();
          for (BasicEntity basicEntity : old) {
            entities.add(basicEntity.clone());
          }
          e1 = entities.get(0);
          old.clear();
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
          e1 = entities.get(0);
          old.clear();
          run = false;
        }
      }

      if (window.keyHit(GLFW.GLFW_KEY_F)) {
        e1.setVelocity(new Vector(0, 5));
      }

      if (run) {
        Engine.process(entities, 0.01f);
      }

      if (window.keyHit(GLFW.GLFW_KEY_P)) {
//        manifolds = Engine.findManifolds(entities);
//        map = Engine.generateMLookup(manifolds);
//        data = Engine.collectMass(e1, map, new ArrayList<>(), new Vector(0, 1));
//
//        Game.log(manifolds);
//        Game.log(data);
        pause = true;
      }

      if (window.keyHit(GLFW.GLFW_KEY_SPACE)) {
        manifolds = Engine.findManifolds(entities);
        map = Engine.generateManifoldLookup(manifolds);
        data = Engine.collectMass(e1, map, new Vector(0, -1));

        Engine.clearAll(entities);

        Game.log(manifolds);
        Game.log(data);
      }

      if (window.mouseHit(GLFW.GLFW_MOUSE_BUTTON_1)) {
        move = !move;
      }

      if (window.mouseHit(GLFW.GLFW_MOUSE_BUTTON_2)) {
        mmove = !mmove;
      }

      if (mmove) {
        entities.get(index).setPos(window.getMousePos());
      }

      if (move) {
        BasicEntity e = entities.get(index);
        Vector dir = window.getMousePos().subi(e.getPos());
        e.setVelocity(dir.normalizei().scalei(Math.min(5, dir.mag())));
      }

      menuEntity.setText(entities.get(index).getName() + " " + entities.get(index).getInvMass() + ", " + entities.get(index).getMass());

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
