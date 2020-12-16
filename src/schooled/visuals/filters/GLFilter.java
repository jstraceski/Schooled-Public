package schooled.visuals.filters;

import static org.lwjgl.opengl.EXTFramebufferObject.GL_FRAMEBUFFER_EXT;
import static org.lwjgl.opengl.EXTFramebufferObject.glBindFramebufferEXT;
import static org.lwjgl.opengl.GL11.GL_NEAREST;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MAG_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MIN_FILTER;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glColor4f;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glTexParameteri;

import schooled.Game;
import schooled.datatypes.Quad;
import schooled.datatypes.Tuple;
import schooled.engines.RenderEngine;
import schooled.physics.BoundingBox;
import schooled.physics.Vector;
import schooled.visuals.FBOWrapper;
import schooled.visuals.GLGraphicsContext;

public class GLFilter {

  private float dt = 0;
  private GLFilter child = null;

  public void updateFilter(float t){
    dt += t;
    if (child != null) {
      child.updateFilter(t);
    }
  }

  public float getDT() {
    return dt;
  }

  public void start(GLGraphicsContext glgc) {
  }

  public void end(GLGraphicsContext glgc) {
  }

  public boolean isActive() {
    return true;
  }

  public void glRenderQuad(GLGraphicsContext glg, Vector pos, float qScale,
      Vector topLeft, Vector bottomRight, Vector topLeftT, Vector bottomRightT, float rotation) {
    if (child != null) {
      glg.fboDataChild.start(glg, child);

      glBindTexture(GL_TEXTURE_2D, glg.fboData.texId);

      glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
      glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

      Tuple<Vector, Vector> quad = glRenderQuadRaw(glg, pos, qScale, topLeft, bottomRight, topLeftT, bottomRightT, rotation);

      FBOWrapper lastParent = glg.fboData;
      glg.fboData = glg.fboDataChild;
      glg.fboDataChild = lastParent;

      glg.fboData.draw(glg, child, new BoundingBox(quad.a, quad.b), pos, qScale, Vector.zero);
      glg.fboData.end(glg, child);
    } else {
      glRenderQuadRaw(glg, pos, qScale, topLeft, bottomRight, topLeftT, bottomRightT, rotation);
    }
  }

  public Tuple<Vector, Vector> glRenderQuadRaw(GLGraphicsContext glg, Vector pos, float qScale,
      Vector topLeft, Vector bottomRight, Vector topLeftT, Vector bottomRightT, float rotation) {
    RenderEngine.glRenderQuad(glg, pos, qScale, topLeft, bottomRight, topLeftT, bottomRightT, rotation);
    return new Tuple<>(topLeft, bottomRight);
  }

  public void addChildFilter(GLFilter glFilter) {
    if (child != null) {
      child.addChildFilter(glFilter);
    } else {
      child = glFilter;
    }
  }
}
