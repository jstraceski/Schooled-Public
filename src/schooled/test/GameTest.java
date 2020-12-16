package schooled.test;

import java.awt.Color;
import java.util.ArrayList;
import org.lwjgl.glfw.GLFW;
import schooled.Game;
import schooled.Window;
import schooled.engines.Engine;
import schooled.engines.Logger;
import schooled.engines.RenderEngine;
import schooled.entities.BasicEntity;
import schooled.entities.Entity;
import schooled.loaders.JsonLoader;
import schooled.loaders.Lookup;
import schooled.loaders.PFlags;
import schooled.menu.MenuEntity;
import schooled.menu.Origin;
import schooled.physics.Circle;
import schooled.physics.PolygonShape;
import schooled.physics.Vector;

public class GameTest {



  public static boolean PAUSE_NEXT = false;
  public static boolean pause = false;
  public static ArrayList<BasicEntity> entities = new ArrayList<>();
  public static Window window = new Window();
  public static MenuEntity menuEntity = new MenuEntity(null, new Vector(20, 20), new PolygonShape(100, 20));
  public static boolean move = false;
  public static boolean mmove = false;
  public static int index = 0;
  public static boolean run = true;
  public static long lastTime = 0;
  private static final float SECOND = (float) 1e9;

  private static BasicEntity body;
  private static Entity mind;

  public static void main(String[] args) {


    window.init(null);

    menuEntity.setOrigin(Origin.TOP_LEFT);
    menuEntity.setBackgroundColor(Color.red);

    Lookup lookup = new Lookup();
    PFlags pFlags = new PFlags();

    try {
      JsonLoader.parseJson("resources/images/Etc/Mind-edata.json", lookup, null, pFlags);
    } catch (Exception e) {
      e.printStackTrace();
    }

    mind = ((Entity) (lookup.get("mind")));
    mind.setShape(new Circle(20));
    mind.setMass(10);
    mind.getSprite().resetBaseScale(3f);
    mind.setSpriteShift(mind.getSpriteShift().scalei(3f));

    mind.getSprite().setOrigin(Origin.TOP_LEFT);

    body = new BasicEntity("body", new Vector(500, 300), new Circle(10),  -1);

    entities.add(mind);
    entities.add(body);

    timeDelta();
    Engine.AIR_DRAG = 0.992f;






    while (!window.shouldWindowClose()) {
      mainLoop();
    }
  }

  static float springOffset = 0;

  public static void mainLoop() {
    float dt = timeDelta();
    Game.log(dt);
    defaultHeader(dt);
    Vector moveDir = new Vector(0, 0);

    if (window.keyDown(GLFW.GLFW_KEY_W))
      moveDir.add(Vector.up);
    if (window.keyDown(GLFW.GLFW_KEY_S))
      moveDir.add(Vector.down);
    if (window.keyDown(GLFW.GLFW_KEY_A))
      moveDir.add(Vector.left);
    if (window.keyDown(GLFW.GLFW_KEY_D))
      moveDir.add(Vector.right);
    if (window.keyDown(GLFW.GLFW_KEY_SPACE))
      springOffset += 0.01;

    moveDir = moveDir.normalizei();

    float maxSpeed = 10;

    float springConstant = 1f;
    float dampingFactor = 0.001f;
    float taughtFactor = 0.3f;

    float springNeutralLength = 50;
    float springMaxLength = 400;

    body.addVelocity(moveDir.scalei(200 * dt));

    // clamp body velocity
    if (body.getLocalVelocity().magSqr() > maxSpeed * maxSpeed) {
      body.setVelocity(body.getLocalVelocity().normalizei().scalei(maxSpeed));
    }

    float sScalar = Math.max(0.0f, 1.0f - (0.10f) * dt * 60f);
    body.setVelocity(body.getLocalVelocity().scalei(sScalar));


    Vector rand = Vector.zero.addScaledi(
        Vector.up, (float) Math.random()).addScaledi(
        Vector.down, (float) Math.random()).addScaledi(
        Vector.left, (float) Math.random()).addScaledi(
        Vector.right, (float) Math.random());

    mind.addVelocity(rand.scalei(((float) Math.random()) * 200 * dt));
    mind.addVelocity(Vector.down.scalei(9.8f * 10 * dt));


    Vector springDirection = body.getPos().subi(mind.getPos());
    float measuredDistance = springDirection.mag();

    if (measuredDistance > 0) {

      mind.getSprite().setRotation(((float)Math.PI/2f) + (float) Math.atan2(springDirection.getY(), springDirection.getX()));

      Vector springNormal = springDirection.normalizei(measuredDistance);
      float newLength1 = Math.max(0, springNeutralLength - springOffset);
      float newLength2 = Math.max(0, springMaxLength - springOffset);

      if (measuredDistance > springNeutralLength - springOffset) {
        float springStretch = measuredDistance - newLength1;
        mind.addVelocity(springNormal.scalei(springStretch * springConstant * dt));

        float f = springNormal.dot(mind.getLocalVelocity());
        if (f > 0) {
          mind.addVelocity(springNormal.scalei(-f * dampingFactor));
        }
      }


      float[] hsv = Color.RGBtoHSB(255, 0, 0, new float[3]);
      float[] hsv2 = Color.RGBtoHSB(0, 255, 0, new float[3]);

//      float[] hsv = Color.red.getComponents(new float[4]);
//      float[] hsv2 = Color.green.getComponents(new float[4]);

      //<editor-fold desc="factor">
      float factor = 0;
      if (newLength2 - newLength1 > 0) {
        factor = (measuredDistance - newLength1) / (newLength2 - newLength1);
      }

      factor = Math.min(Math.max(factor, 0.0f), 1.0f);

      for (int i = 0; i < 3; i++) {

        float len = (hsv[i] - hsv2[i]);

        hsv[i] = hsv2[i] + (len) * factor;

        if (hsv[i] < 0) {
          hsv[i] += 1;
        }

        if (hsv[i] > 1) {
          hsv[i] -= 1;
        }
      }
      //</editor-fold>

//      Color color = new Color(hsv[0], hsv[1], hsv[2]);
      Color color = new Color(Color.HSBtoRGB(hsv[0], hsv[1], hsv[2]));

      RenderEngine.setColor(color);
      RenderEngine.drawLine(mind.getPos(), body.getPos());

      if (measuredDistance > springMaxLength - springOffset) {


        mind.setPos(body.getPos().addScaledi(springNormal, -newLength2));
        mind.addVelocity(springNormal.scalei(body.getLocalVelocity().dot(springNormal)));

        float nVel = mind.getLocalVelocity().dot(springNormal);
        if (nVel < 0) {
          Vector newVelocityDirection = Engine.pointedPVector(springNormal, mind.getLocalVelocity());
          float magnitude = newVelocityDirection.dot(mind.getVelocity());

          Vector tVelocity = newVelocityDirection.scalei(magnitude);
          tVelocity = tVelocity.addScaledi(springNormal, -nVel * taughtFactor);
          mind.setVelocity(tVelocity);
        }
      }
    }

    defaultFooter(dt);
  }

  public static void pauseLoop() {
    while (pause && !window.shouldWindowClose()) {
      mainLoop();
    }
  }

  public static float timeDelta() {
    long nowTime = System.nanoTime();
    long outTime = nowTime - lastTime;
    lastTime = nowTime;
    return outTime/SECOND;
  }

  public static void defaultHeader(float dt) {
    window.initWindow();
    Logger.calculateDebugTimesList();
  }

  public static void defaultFooter(float dt) {

    if (pause) {
      if (window.keyHit(GLFW.GLFW_KEY_P)) {
        pause = false;
      }
    } else {
      if (run) {
        Engine.process(entities, dt);
      }
    }

    if (PAUSE_NEXT) {
      pause = true;
    }

    if (window.keyHit(GLFW.GLFW_KEY_R)) {
      pause = false;
    }

    if (window.keyHit(GLFW.GLFW_KEY_C)) {
      PAUSE_NEXT = false;
    }

    if (window.keyHit(GLFW.GLFW_KEY_P)) {
      pause = true;
    }

    if (window.keyHit(GLFW.GLFW_KEY_1)) {
      Game.DEBUG_LOG = !Game.DEBUG_LOG;
    }

    if (window.mouseHit(GLFW.GLFW_MOUSE_BUTTON_1)) {
      move = !move;
      mmove = false;
    }

    if (window.mouseHit(GLFW.GLFW_MOUSE_BUTTON_2)) {
      mmove = !mmove;
      move = false;
    }

    if (window.keyHit(GLFW.GLFW_KEY_Q)) {
      index = Math.max(index - 1, 0);
    }

    if (window.keyHit(GLFW.GLFW_KEY_E)) {
      index = Math.min(index + 1, entities.size() - 1);
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
    }


    for (BasicEntity entity : entities) {
      if (entity.getInvMass() == 0.0f) {
        RenderEngine.setColor(new Color(50f/255f, 100f/255f, 150f/255f));
      } else {
        RenderEngine.setColor(Color.black);
      }

      RenderEngine.traceEntity(entity);

      if (entity.getName().equals("mind")) {
        RenderEngine.setColor(Color.white);
        RenderEngine.drawEntitySprite(Window.DEFAULT_GRAPHICS_CONTEXT, (Entity) mind, mind.getSprite(),Vector.zero, 1.0f);
      }
    }

    RenderEngine.setColor(Color.black);
    RenderEngine.renderMenuEntity(menuEntity);

    window.update();
  }
}
