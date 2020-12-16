package schooled.test;

import java.awt.Color;
import java.util.ArrayList;
import org.lwjgl.glfw.GLFW;
import schooled.Window;
import schooled.engines.RenderEngine;
import schooled.entities.Entity;
import schooled.menu.MenuEntity;
import schooled.menu.Origin;
import schooled.physics.Circle;
import schooled.physics.PolygonShape;
import schooled.physics.Vector;
import schooled.visuals.GLFontContext;
import schooled.visuals.GLGraphicsContext;

public class PhysicsTest {

  public static void main(String[] args) {
    Window window = new Window();
    window.init(null);
    GLGraphicsContext glgc = new GLGraphicsContext(
        GLFontContext.createFontFromPath("resources/fonts/SGK100.ttf"));
    Window.DEFAULT_GRAPHICS_CONTEXT = glgc;

    Entity cursor = new Entity(null, new Vector(200, 200), new Circle(20), 0);
    MenuEntity menuEntity = new MenuEntity(null, new Vector(20, 20), new PolygonShape(100, 20));
    menuEntity.setOrigin(Origin.TOP_LEFT);
    menuEntity.addText("Inside");
    menuEntity.setBackgroundColor(Color.red);
    menuEntity.updateCycle();

    ArrayList<Entity> entities = new ArrayList<>();

    entities.add(cursor);

    PolygonShape polygon = new PolygonShape();
    polygon.addPoint(-1, 0);
    polygon.addPoint(-1, -1);
    polygon.addVertex(1, -.5f);
    polygon.addPoint(0, 1);
    polygon.addPoint(2, 1);
    polygon.addPoint(2, -2);
    polygon.addVertex(-0, -1.8f);
    polygon.addPoint(-2, -2);
    polygon.addPoint(-2, 0);

    polygon.scale(50);

    polygon.flipHorizontal();

    Entity collider = new Entity(null, new Vector(300, 300), polygon, 0);

    boolean[] lastMouseBuffer = window.getMouseBuf();
    boolean[] currentMouseBuffer;

    boolean[] lastKeyBuffer = window.getMouseBuf();
    boolean[] currentKeyBuffer;


    RenderEngine.init(glgc);
    while (true) {
      if (window.shouldWindowClose()) {
        break;
      }
      window.initWindow();

      RenderEngine.setColor(glgc, Color.black);
      RenderEngine.traceEntity(glgc, collider, new Vector(0, 0), 1);


      currentMouseBuffer = window.getMouseBuf();
      currentKeyBuffer = window.getKeyBuf();

      if (window.getMouseBuf()[GLFW.GLFW_MOUSE_BUTTON_1] && window.isMouseInside()) {
        cursor.setPosition(window.getMousePos());
      }

      if (currentMouseBuffer[GLFW.GLFW_MOUSE_BUTTON_2] && !lastMouseBuffer[GLFW.GLFW_MOUSE_BUTTON_2]
          && window.isMouseInside()) {
        polygon.addVertex(window.getMousePos().subi(collider.getPosition()));
      }

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

      if (window.getKeyBuf()[GLFW.GLFW_KEY_SPACE]) {
        polygon.clear();
      }

      lastMouseBuffer = currentMouseBuffer.clone();
      lastKeyBuffer = currentKeyBuffer.clone();

      RenderEngine.traceEntity(glgc, cursor, new Vector(0, 0), 1);
      //RenderEngine.traceEntity(null, collider, 1.0f, new Vector(0, 0), 1);
      RenderEngine.renderMenuEntity(glgc, menuEntity, 1);

      window.update();
    }

  }
}
