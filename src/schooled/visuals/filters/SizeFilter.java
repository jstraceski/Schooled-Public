package schooled.visuals.filters;

import static org.lwjgl.opengl.GL11.GL_QUADS;
import static org.lwjgl.opengl.GL11.glBegin;
import static org.lwjgl.opengl.GL11.glEnd;
import static org.lwjgl.opengl.GL11.glTexCoord2f;
import static org.lwjgl.opengl.GL11.glVertex2f;

import java.util.Random;
import schooled.Game;
import schooled.datatypes.Tuple;
import schooled.engines.RenderEngine;
import schooled.entities.Entity;
import schooled.physics.BoundingBox;
import schooled.physics.Vector;
import schooled.visuals.GLGraphicsContext;

public class SizeFilter extends GLFilter {

  public long seed = 1337;
  public int hDiv = 1;
  public int vDiv = 10;
  public float freq = 3; //(hz)
  public Entity entity;


  public SizeFilter(Entity e) {
    entity = e;
  }

  public SizeFilter(float freq) {
    this.freq = freq;
  }

  @Override
  public Tuple<Vector, Vector> glRenderQuadRaw(GLGraphicsContext glg, Vector pos, float qScale, Vector topLeft,
      Vector bottomRight, Vector topLeftT, Vector bottomRightT, float rotation) {

    topLeft = topLeft.addi(pos);
    bottomRight = bottomRight.addi(pos);

    Vector topRight = new Vector(bottomRight.getX(), topLeft.getY());
    Vector bottomLeft = new Vector(topLeft.getX(), bottomRight.getY());

    float scale = 1 + (0.25f * (float) Math.sin(3* getDT() / 100));

    if (entity != null) {
      BoundingBox bb = entity.getBB();
      Vector shapeTopLeft = bb.getMin().addi(entity.getPos());
      Vector shapeBottomRight = bb.getMax().addi(entity.getPos());
      Vector center = shapeTopLeft.addi(shapeBottomRight).scalei(0.5f);

      Vector tLShift = topLeft.subi(center);
      Vector bRShift = bottomRight.subi(center);
      Vector tRShift = topRight.subi(center);
      Vector bLShift = bottomLeft.subi(center);

      topLeft = center.addScaledi(tLShift, scale);
      bottomRight = center.addScaledi(bRShift, scale);
      topRight = center.addScaledi(tRShift, scale);
      bottomLeft = center.addScaledi(bLShift, scale);

      glBegin(GL_QUADS);
      {
        glTexCoord2f(topLeftT.getX(), topLeftT.getY());
        glVertex2f(topLeft.getX(), topLeft.getY());

        glTexCoord2f(bottomRightT.getX(), topLeftT.getY());
        glVertex2f(topRight.getX(), topRight.getY());

        glTexCoord2f(bottomRightT.getX(), bottomRightT.getY());
        glVertex2f(bottomRight.getX(), bottomRight.getY());

        glTexCoord2f(topLeftT.getX(), bottomRightT.getY());
        glVertex2f(bottomLeft.getX(), bottomLeft.getY());
      }
      glEnd();

    } else {
      Vector diagonal = bottomRight.subi(topLeft);

      topLeft = topLeft.addScaledi(diagonal, -(getDT() / 10));
      bottomRight = bottomRight.addScaledi(diagonal, (getDT() / 10));

      RenderEngine.glRenderQuad(glg, pos, qScale, topLeft, bottomRight, topLeftT, bottomRightT, rotation);
    }

    return new Tuple<>(topLeft, bottomRight);
  }
}
