package schooled.visuals.filters;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.glBegin;

import java.awt.Color;
import java.util.Random;
import org.lwjgl.opengl.GL11;
import org.lwjgl.system.CallbackI.V;
import schooled.Game;
import schooled.datatypes.Quad;
import schooled.datatypes.Tuple;
import schooled.engines.RenderEngine;
import schooled.physics.Vector;
import schooled.visuals.GLGraphicsContext;

public class GlitchFilter extends GLFilter {

  public long seed = 1337;
  public int hDiv = 4;
  public int vDiv = 10;
  public float freq = 3; //(hz)

  public GlitchFilter() {
  }

  public GlitchFilter(float freq) {
    this.freq = freq;
  }

  @Override
  public Tuple<Vector, Vector> glRenderQuadRaw(GLGraphicsContext glg, Vector pos, float qScale, Vector topLeft,
      Vector bottomRight, Vector topLeftT, Vector bottomRightT, float rotation) {

    //TODO FIX ROTATION
    Vector topRight = new Vector(bottomRight.getX(), topLeft.getY());
    Vector bottomLeft = new Vector(topLeft.getX(), bottomRight.getY());

    Vector tlmin = topLeft;
    Vector brmax = bottomRight;

    Vector toRight = topRight.subi(topLeft);
    Vector toBottom = bottomLeft.subi(topLeft);

    Vector topRightT = new Vector(bottomRightT.getX(), topLeftT.getY());
    Vector bottomLeftT = new Vector(topLeftT.getX(), bottomRightT.getY());

    Vector toRightT = topRightT.subi(topLeftT);
    Vector toBottomT = bottomLeftT.subi(topLeftT);

    Random random = new Random();
    random.setSeed(seed + (long) Math.floor(getDT()/50) % 50);
    random.nextFloat();
    float range = 10;

    for (int horizontalIndex = 1; horizontalIndex <= hDiv; horizontalIndex++) {
      for (int verticalIndex = 1; verticalIndex <= vDiv; verticalIndex++) {
        Vector subTopLeft = topLeft.addScaledi(toBottom, ((float) (verticalIndex - 1) / (float) vDiv)).addScaledi(toRight, ((float) (horizontalIndex - 1) / (float) hDiv));
        Vector subBottomRight = topLeft.addScaledi(toBottom, ((float) verticalIndex / (float) vDiv)).addScaledi(toRight, ((float) horizontalIndex / (float) hDiv));

        Vector subTopLeftT = topLeftT.addScaledi(toBottomT, ((float) (verticalIndex - 1) / (float) vDiv)).addScaledi(toRightT, ((float) (horizontalIndex - 1) / (float) hDiv));
        Vector subBottomRightT = topLeftT.addScaledi(toBottomT, ((float) verticalIndex / (float) vDiv)).addScaledi(toRightT, ((float) horizontalIndex / (float) hDiv));

        Vector shift = Vector.left.scalei(range/2 - random.nextFloat() * range);
        subTopLeft = subTopLeft.addi(shift);
        subBottomRight = subBottomRight.addi(shift);

        tlmin = Vector.min(subTopLeft, tlmin);
        brmax = Vector.max(subBottomRight, brmax);

        RenderEngine.glRenderQuad(glg, Vector.zero, qScale, subTopLeft, subBottomRight, subTopLeftT, subBottomRightT, rotation);
      }
    }

    return new Tuple<>(tlmin, brmax);
  }
}
