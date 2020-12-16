package schooled.test;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import schooled.Game;
import schooled.containers.Room;
import schooled.containers.World;
import schooled.entities.CrateEntity;
import schooled.entities.Entity;
import schooled.loaders.SpriteLoader;
import schooled.menu.Dialogue;
import schooled.physics.Circle;
import schooled.physics.PolygonShape;
import schooled.physics.Vector;
import schooled.visuals.sprite.Sprite;

/**
 * Class of methods that generate or test things.
 * TODO: extrapolate tests to save data
 */
public class Testers {

  private static final String dir = "/resources/images";

  public static Sprite testImagesFromSheet(Game game) {
    HashMap<String, Sprite> data = SpriteLoader.parseSpriteSheet(dir + "/sheets/SpriteSheet.png",
        "", SpriteLoader::parseBufferedImage, 100, 100);
    BufferedImage test = new BufferedImage(200, 100, BufferedImage.TYPE_INT_RGB);
    Sprite[] bf = (Sprite[]) data.values().toArray();

    Graphics g = test.getGraphics();
    g.drawImage(bf[1].getBufferedImage(), 100, 0, null);
    g.drawImage(bf[0].getBufferedImage(), 0, 0, null);
    return new Sprite(test);
  }

  public static Sprite testImages(Game game) {
    HashMap<String, Sprite> data = SpriteLoader.parseSprites(dir);
    Sprite[] bf = (Sprite[]) data.values().toArray();
    BufferedImage test = new BufferedImage(200, 100, BufferedImage.TYPE_INT_RGB);
    Graphics g = test.getGraphics();
    g.drawImage(bf[1].getBufferedImage(), 100, 0, null);
    g.drawImage(bf[0].getBufferedImage(), 0, 0, null);
    return new Sprite(test);
  }

  public static Sprite testImageNames(Game game) {
    HashMap<String, Sprite> data = SpriteLoader.parseSprites(dir);
    BufferedImage test = new BufferedImage(700, 140, BufferedImage.TYPE_INT_RGB);
    Graphics g = test.getGraphics();
    int c = 0;
    for (String s : data.keySet()) {
      g.drawImage(data.get(s).getBufferedImage(), c * 100, 0, null);
      g.drawString(s, c * 100, 120);
      c++;
    }
    return new Sprite(test);
  }

  public static Sprite testScale(Game game) {
    HashMap<String, Sprite> data = SpriteLoader.parseSprites(dir);
    Sprite[] bf = (Sprite[]) data.values().toArray();
    Sprite sprite = Sprite.scale(bf[1], 2);
    BufferedImage test
        = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
    Graphics g = test.getGraphics();
    g.drawImage(sprite.getBufferedImage(), 0, 0, null);
    return new Sprite(test);
  }

  public static void loadRoom1(Game g, Room r) {
    r.addEntity(new Entity(g, new Vector(800, 600), new Circle(20.0f), 20.0f));

    Entity ent = new Entity(g, new Vector(400, 600), new PolygonShape(40.0f), 40.0f);
    r.addEntity(ent);
  }

  public static void loadRoom2(Game g, Room r) {

    Entity c1 = new Entity(g, new Vector(100, 100), new Circle(20.0f), 40.0f);
    c1.setVelocity(new Vector(0.5f, 0.0f));
    r.addEntity(c1);

    Entity c2 = new Entity(g, new Vector(400, 100), new Circle(20.0f), 40.0f);
    c2.setVelocity(new Vector(-1.0f, 0.0f));
    r.addEntity(c2);
  }

  public static void loadRoom3(Game g, Room r) {
    World w = r.getWorld();
    Entity c1 = new CrateEntity(w.getGame(), new Vector(130, 140), new PolygonShape(), 40.0f);
    ((PolygonShape) c1.getShape()).getVertices().add(new Vector(40.0f, 40.0f));
    ((PolygonShape) c1.getShape()).getVertices().add(new Vector(30.0f, -30.0f));
    ((PolygonShape) c1.getShape()).getVertices().add(new Vector(-40.0f, -40.0f));
    ((PolygonShape) c1.getShape()).getVertices().add(new Vector(-40.0f, 40.0f));
    //c1.setVelocity(new Vector(0.5, 0.0));
    r.addEntity(c1);

    Entity c2 = new Entity(g, new Vector(150, 110), new PolygonShape(50.0f), 40.0f);
    //c2.setVelocity(new Vector(-1.0, 0.0));
    r.addEntity(c2);
  }

  public static void loadMap2(Game g) {
    Sprite s = g.getImage("Classroom");
    //System.out.println(i.getHeight());
    Room r = new Room(g, s);
    g.getWorld().addAndLoad(r);
    r.addEntity(g.getPlayer());
    CrateEntity ce = new CrateEntity(g, new Vector(100, 100),
        new PolygonShape(20), 10f, true);
    r.addEntity(ce);

  }

  public static void loadMap(Game g) {
    Dialogue m = new Dialogue("Press Escape", g.getPlayer());
    g.getWorld().setDialogue(m);
    PolygonShape p = new PolygonShape(27, 5);
//    Entity test = new Entity(g, new Vector(10.5, 11.5), p, 10);
  }

  public static void TestRoom(Game g) {

    Room r = new Room(g, g.getImage("Map1"));

    Testers.loadRoom3(g, r);

    g.getWorld().addAndLoad(r);
    g.getWorld().addRoom(new Room(g, g.getImage("cobble")));
    r.addEntity(g.getPlayer());

  }

  public static void stressI() {
    Vector v = new Vector(0.51f, 0.49f);
    Vector v2 = new Vector(0, 0);

    for (int i = 0; i < 10000; i++) {
      v2.setX(v.getXi());
      v2.setY(v.getYi());
    }

    System.out.println(" I : " + v2);
  }

  public static void stressI2() {
    Vector v = new Vector(0.51f, 0.49f);
    Vector v2 = new Vector(0, 0);

    for (int i = 0; i < 10000; i++) {
      v2.setX(v.getXi2());
      v2.setY(v.getYi2());
    }

    System.out.println(" I : " + v2);
  }

}
