package schooled.visuals;

import static org.lwjgl.opengl.EXTFramebufferObject.glBindFramebufferEXT;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.EXTFramebufferObject.*;

import java.awt.Color;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GLCapabilities;
import schooled.Game;
import schooled.engines.RenderEngine;
import schooled.physics.BoundingBox;
import schooled.physics.Vector;
import schooled.visuals.filters.GLFilter;

public class FBOWrapper {
  public int fboId = 0;
  public int texId = 0;
  private int depthId = 0;

  public int width = 1920;
  public int height = 1080;

  public void start(GLGraphicsContext glgc, GLFilter filter) {
    glBindTexture(GL_TEXTURE_2D, 0);
    glBindFramebufferEXT(GL_FRAMEBUFFER_EXT, fboId);
    glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    filter.start(glgc);
  }

  public void end(GLGraphicsContext glgc, GLFilter filter) {
    filter.end(glgc);
  }

  public void draw(GLGraphicsContext glgc, GLFilter filter, BoundingBox bb, Vector pos,
      float scale, Vector shift) {

    glBindFramebufferEXT(GL_FRAMEBUFFER_EXT, 0);

    glEnable(GL_TEXTURE_2D);
    glBindTexture(GL_TEXTURE_2D, texId);

    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

    Vector minPos = bb.getMin().addi(pos).scalei(scale).addi(shift);
    Vector maxPos = bb.getMax().addi(pos).scalei(scale).addi(shift);

    RenderEngine.pushColor(glgc, Color.white);

    filter.glRenderQuad(glgc, Vector.zero, scale, minPos, maxPos,
        new Vector(minPos.getX()/width,1f - (minPos.getY()/height)),
        new Vector(maxPos.getX()/width,1f - (maxPos.getY()/height)), 0);

    RenderEngine.popColor(glgc);

    glDisable(GL_TEXTURE_2D);
  }


  private void init() {
    fboId = glGenFramebuffersEXT();
    texId = glGenTextures();
    depthId = glGenRenderbuffersEXT();

    glBindFramebufferEXT(GL_FRAMEBUFFER_EXT, fboId);
    glBindTexture(GL_TEXTURE_2D, texId);

    glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
    glTexImage2D(GL_TEXTURE_2D,0, GL_RGBA8, width, height, 0, GL_RGBA, GL_INT, (java.nio.ByteBuffer) null);
    glFramebufferTexture2DEXT(GL_FRAMEBUFFER_EXT, GL_COLOR_ATTACHMENT0_EXT, GL_TEXTURE_2D, texId, 0);

    glBindRenderbufferEXT(GL_RENDERBUFFER_EXT, depthId);
    glRenderbufferStorageEXT(GL_RENDERBUFFER_EXT, GL14.GL_DEPTH_COMPONENT24, width, height);
    glFramebufferRenderbufferEXT(GL_FRAMEBUFFER_EXT, GL_DEPTH_ATTACHMENT_EXT, GL_RENDERBUFFER_EXT, depthId);

    glBindFramebufferEXT(GL_FRAMEBUFFER_EXT, 0);
  }

  public void setSize(Vector v) {
    setSize(v.getXi(), v.getYi());
  }

  public void setSize(int w, int h) {
    width = w;
    height = h;

    glDeleteFramebuffersEXT(fboId);
    glDeleteTextures(texId);

    init();
  }
}
