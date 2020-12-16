package schooled.test;

import java.awt.Color;
import java.util.ArrayList;
import org.lwjgl.glfw.GLFW;
import schooled.Window;
import schooled.engines.RenderEngine;
import schooled.entities.Entity;
import schooled.menu.MenuEntity;
import schooled.menu.Origin;
import schooled.menu.TextContext;
import schooled.physics.Circle;
import schooled.physics.PolygonShape;
import schooled.physics.Vector;
import schooled.visuals.GLFontContext;

public class PhysicsTestVectors {

  public static Vector lineLine(Vector a, Vector b, Vector c, Vector d) {
    Vector bma = Vector.sub(b, a);
    Vector dmc = Vector.sub(d, c);
    Vector pa = new Vector(-bma.getY(), bma.getX());
    Vector pb = new Vector(-dmc.getY(), dmc.getX());
    float ha = Vector.dot(Vector.sub(a, c), pa) / Vector.dot(dmc, pa);
    float hb = Vector.dot(Vector.sub(b, d), pb) / Vector.dot(bma, pb);
//        if (ha >= 0.0 && ha <= 1 && hb >= 0.0 && hb <= 1) {
    return Vector.add(c, Vector.scale(dmc, ha));
//        }
//        return null;
  }

  public static void main(String[] args) {
    Window window = new Window();
    window.init(null);
    GLFontContext font = GLFontContext.createFontFromPath("resources/fonts/SGK100.ttf");
    Entity cursor = new Entity(null, new Vector(200, 200), new Circle(20), 0);
    MenuEntity menuEntity = new MenuEntity(null, new Vector(20, 20), new PolygonShape(100, 20));
    menuEntity.setOrigin(Origin.TOP_LEFT);
    menuEntity.addText(new TextContext("Inside", font));
    menuEntity.setBackgroundColor(Color.red);
    menuEntity.updateCycle();

    boolean[] lastMouseBuffer = window.getMouseBuf();
    boolean[] currentMouseBuffer;

    boolean[] lastKeyBuffer = window.getMouseBuf();
    boolean[] currentKeyBuffer;

    ArrayList<Entity> entities = new ArrayList<>();

    entities.add(cursor);

    Vector vectorA = new Vector(100, 50);
    Vector mid = window.getSize().scalei(0.5f);

    while (true) {
      if (window.shouldWindowClose()) {
        break;
      }
      menuEntity.setBackgroundColor(Color.red);
      window.initWindow();

      Vector vectorB = cursor.getPosition().subi(mid);

//            if (vectorB.dot(vectorA) > 0 ) {
//                Vector normalA = vectorA.normalizei();
//                Vector normalB = vectorB.normalizei();
//
//                if (normalB.dot(vectorA.subi(vectorB)) < 0) {
//
//                    Vector vectorC = normalA.scalei((1.0f / normalB.dot(normalA)) * vectorB.mag());
//
//                    RenderEngine.setColor(null, Color.red);
//                    menuEntity.setBackgroundColor(Color.green);
//                    RenderEngine.drawLine(null, mid, mid.addi(vectorC));
//                }
//            } else {

      Vector out;

      if (vectorA.subi(vectorB).dot(vectorB.normalizei()) < 0) {
        if (vectorA.dot(vectorB) > vectorA.magSqr()
            && vectorB.subi(vectorA).dot(vectorB.normalizei()) > 0) {
          out = vectorB;
        } else {
          out = lineLine(vectorA, vectorA.addi(vectorA.perpi()), vectorB,
              vectorB.addi(vectorB.perpi()));

        }
      } else {
        out = vectorA;
      }

      RenderEngine.traceCircle(null, new Circle(out.mag()), mid, new Vector(0, 0), 1);
      out = out.normalizei().scalei(Math.min(out.mag(), vectorA.mag() + vectorB.mag()));

      RenderEngine.setColor(null, Color.gray);
      RenderEngine.drawLine(null, mid, mid.addi(vectorB));
      RenderEngine
          .drawLine(null, mid.addi(vectorB).addi(vectorB.normalizei().perpi().scalei(1000.0f)),
              mid.addi(vectorB));
      RenderEngine
          .drawLine(null, mid.addi(vectorB).addi(vectorB.normalizei().perpi().scalei(-1000.0f)),
              mid.addi(vectorB));

      RenderEngine.setColor(null, Color.black);
      RenderEngine.drawLine(null, mid, mid.addi(vectorA));
      RenderEngine
          .drawLine(null, mid.addi(vectorA).addi(vectorA.normalizei().perpi().scalei(1000.0f)),
              mid.addi(vectorA));
      RenderEngine
          .drawLine(null, mid.addi(vectorA).addi(vectorA.normalizei().perpi().scalei(-1000.0f)),
              mid.addi(vectorA));

      RenderEngine.setColor(null, Color.red);
      RenderEngine.drawLine(null, mid.addi(out), mid);

      {
        currentMouseBuffer = window.getMouseBuf();
        currentKeyBuffer = window.getKeyBuf();

        if (window.getMouseBuf()[GLFW.GLFW_MOUSE_BUTTON_1] && window.isMouseInside()) {
          cursor.setPosition(window.getMousePos());
        }

//                if (currentMouseBuffer[GLFW.GLFW_MOUSE_BUTTON_2] && !lastMouseBuffer[GLFW.GLFW_MOUSE_BUTTON_2] && window.isMouseInside()) {
//                    polygon.addVertex(window.getMousePos().subi(collider.getPosition()));
//                }

        if (currentKeyBuffer[GLFW.GLFW_KEY_W] && !lastKeyBuffer[GLFW.GLFW_KEY_W] && window
            .isMouseInside()) {
          cursor.getPosition().add(new Vector(0, -1));
        }

        if (currentKeyBuffer[GLFW.GLFW_KEY_S] && !lastKeyBuffer[GLFW.GLFW_KEY_S] && window
            .isMouseInside()) {
          cursor.getPosition().add(new Vector(0, 1));
        }

        if (currentKeyBuffer[GLFW.GLFW_KEY_A] && !lastKeyBuffer[GLFW.GLFW_KEY_A] && window
            .isMouseInside()) {
          cursor.getPosition().add(new Vector(-1, 0));
        }

        if (currentKeyBuffer[GLFW.GLFW_KEY_D] && !lastKeyBuffer[GLFW.GLFW_KEY_D] && window
            .isMouseInside()) {
          cursor.getPosition().add(new Vector(1, 0));
        }

        if (currentKeyBuffer[GLFW.GLFW_KEY_EQUAL] && !lastKeyBuffer[GLFW.GLFW_KEY_EQUAL] && window
            .isMouseInside()) {
          cursor.setShape(new Circle(((Circle) cursor.getShape()).getRadius() + 1));
        }

        if (currentKeyBuffer[GLFW.GLFW_KEY_MINUS] && !lastKeyBuffer[GLFW.GLFW_KEY_MINUS] && window
            .isMouseInside()) {
          cursor.setShape(new Circle(((Circle) cursor.getShape()).getRadius() - 1));
        }
//
//                if (window.getKeyBuf()[GLFW.GLFW_KEY_SPACE]) {
//                    polygon.clear();
//                }

        lastMouseBuffer = currentMouseBuffer.clone();
        lastKeyBuffer = currentKeyBuffer.clone();
      }

      RenderEngine.traceEntity(null, cursor, new Vector(0, 0), 1);
      //RenderEngine.traceEntity(null, collider, 1.0f, new Vector(0, 0), 1);
      RenderEngine.renderMenuEntity(null, menuEntity, 1);

      window.update();
    }

  }

}
