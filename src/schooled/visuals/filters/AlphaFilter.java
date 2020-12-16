package schooled.visuals.filters;

import java.awt.Color;
import schooled.engines.RenderEngine;
import schooled.physics.Vector;
import schooled.visuals.GLGraphicsContext;

public class AlphaFilter extends GLFilter {

  public float start;
  public float end;
  public float time = -1;

  public AlphaFilter(float start, float end, float time) {
    this.start = start;
    this.end = end;
    this.time = time;
  }

  public AlphaFilter(float end) {
    this.end = end;
  }

  @Override
  public void glRenderQuad(GLGraphicsContext glgc, Vector pos, float qScale, Vector topLeft,
      Vector bottomRight, Vector topLeftT, Vector bottomRightT, float rotation) {

    Color pColor = RenderEngine.getColor(glgc);
    float[] farr = pColor.getComponents(null);
    float alpha = end;
    if (time != -1) {
      alpha = Math.max(0.0f, Math.min(1.0f, start + ((end - start) * getDT() / time)));
    }

    RenderEngine.pushColor(glgc, new Color(farr[0], farr[1], farr[2], alpha));
    super.glRenderQuad(glgc, pos, qScale, topLeft, bottomRight, topLeftT, bottomRightT, rotation);
    RenderEngine.popColor(glgc);
  }
}
