package schooled.visuals.filters;

import static org.lwjgl.opengl.GL11.GL_QUADS;
import static org.lwjgl.opengl.GL11.GL_TEXTURE;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.glBegin;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glEnd;
import static org.lwjgl.opengl.GL11.glTexCoord2f;
import static org.lwjgl.opengl.GL11.glVertex2f;

import java.awt.Color;
import org.lwjgl.system.CallbackI.V;
import schooled.Game;
import schooled.datatypes.Tuple;
import schooled.engines.RenderEngine;
import schooled.entities.Entity;
import schooled.physics.BoundingBox;
import schooled.physics.Vector;
import schooled.visuals.GLGraphicsContext;

public class RotateFilter extends GLFilter {

  public float aLim = (float) Math.PI / 8f;
  public float freq = 3; //(hz)
  public Entity entity;


  public RotateFilter(Entity e) {
    entity = e;
  }

  public RotateFilter(float freq) {
    this.freq = freq;
  }

  @Override
  public Tuple<Vector, Vector> glRenderQuadRaw(GLGraphicsContext glg, Vector pos, float qScale, Vector topLeft,
      Vector bottomRight, Vector topLeftT, Vector bottomRightT, float rotation) {


    topLeft = topLeft.addi(pos);
    bottomRight = bottomRight.addi(pos);
    Vector topRight = new Vector(bottomRight.getX(), topLeft.getY());
    Vector bottomLeft = new Vector(topLeft.getX(), bottomRight.getY());

    Vector minTot;
    Vector maxTot;

    float angle = rotation + aLim * (float) Math.sin(getDT() / 100);

    if (entity != null) {
      BoundingBox bb = entity.getBB();
      Vector shapeTopLeft = bb.getMin().addi(entity.getPos());
      Vector shapeBottomRight = bb.getMax().addi(entity.getPos());
      Vector center = shapeTopLeft.addi(shapeBottomRight).scalei(0.5f);

      Vector tLShift = topLeft.subi(center);
      Vector bRShift = bottomRight.subi(center);
      Vector tRShift = topRight.subi(center);
      Vector bLShift = bottomLeft.subi(center);

      topLeft = tLShift.roti(angle).addi(center);
      bottomRight = bRShift.roti(angle).addi(center);
      topRight = tRShift.roti(angle).addi(center);
      bottomLeft = bLShift.roti(angle).addi(center);

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

      Vector minA = Vector.min(topLeft, topRight);
      Vector minB = Vector.min(bottomLeft, bottomRight);
      minTot = Vector.min(minA, minB);

      Vector maxA = Vector.max(topLeft, topRight);
      Vector maxB = Vector.max(bottomLeft, bottomRight);
      maxTot = Vector.max(maxA, maxB);

    } else {
      Vector center = topLeft.addi(bottomRight).scalei(0.5f);

      Vector tLShift = topLeft.subi(center);
      Vector bRShift = bottomRight.subi(center);
      Vector tRShift = topRight.subi(center);
      Vector bLShift = bottomLeft.subi(center);

      topLeft = tLShift.roti(angle).addi(center);
      bottomRight = bRShift.roti(angle).addi(center);
      topRight = tRShift.roti(angle).addi(center);
      bottomLeft = bLShift.roti(angle).addi(center);

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

      Vector minA = Vector.min(topLeft, topRight);
      Vector minB = Vector.min(bottomLeft, bottomRight);
      minTot = Vector.min(minA, minB);

      Vector maxA = Vector.max(topLeft, topRight);
      Vector maxB = Vector.max(bottomLeft, bottomRight);
      maxTot = Vector.max(maxA, maxB);
    }

    return new Tuple<>(minTot, maxTot);
  }
}
