package schooled.visuals.filters;

import static org.lwjgl.opengl.GL11.GL_TRIANGLE_STRIP;
import static org.lwjgl.opengl.GL11.glBegin;
import static org.lwjgl.opengl.GL11.glEnd;
import static org.lwjgl.opengl.GL11.glTexCoord2f;
import static org.lwjgl.opengl.GL11.glVertex2f;

import schooled.datatypes.Quad;
import schooled.datatypes.Tuple;
import schooled.engines.RenderEngine;
import schooled.physics.Vector;
import schooled.visuals.GLGraphicsContext;

public class WaveFilter extends GLFilter {

  public float amplitude;
  public float freq = 45.0f;
  public float div = 5f;

  public WaveFilter(float amplitude, float freq, float div) {
    this.amplitude = amplitude;
    this.freq = freq;
    this.div = div;
  }

  public WaveFilter(float amplitude) {
    this.amplitude = amplitude;
  }

  @Override
  public Tuple<Vector, Vector> glRenderQuadRaw(GLGraphicsContext glg, Vector pos, float qScale, Vector topLeft,
      Vector bottomRight, Vector topLeftT, Vector bottomRightT, float rotation) {
    glBegin(GL_TRIANGLE_STRIP); // start drawing the character square

    //TODO Fix rotation

    int div = (int) Math.floor(((bottomRight.getX() - topLeft.getX()) * qScale) + 0.5f) * 2;

    float step = (bottomRight.getX() - topLeft.getX()) / div;
    float stepT = (bottomRightT.getX() - topLeftT.getX()) / div;


    Vector newTopLeft = topLeft, newBottomRight = bottomRight;

    for (int i = 0; i < div; i++) {
      float x = step * i;

      float gx = pos.getX() + (topLeft.getX() + x) * qScale;
      float y = (float) Math.sin((getDT() + gx) * Math.PI / freq) * amplitude / qScale;

      float xt = stepT * i;

      Vector nv1 = new Vector(topLeft.getX() + x, y + topLeft.getY());
      Vector nv2 = new Vector(topLeft.getX() + x, y + bottomRight.getY());

      glTexCoord2f(topLeftT.getX() + xt, topLeftT.getY());
      glVertex2f(nv1.getX(), nv1.getY());

      glTexCoord2f(topLeftT.getX() + xt, bottomRightT.getY());
      glVertex2f(nv2.getX(), nv2.getY());

      newTopLeft = Vector.min(newTopLeft, nv1);
      newBottomRight = Vector.max(newBottomRight, nv2);
    }

    glEnd();

    return new Tuple<>(newTopLeft, newBottomRight);
  }
}
