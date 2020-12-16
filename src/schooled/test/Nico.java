package schooled.test;

import static org.lwjgl.opengl.GL11.glGenTextures;

import java.awt.Color;
import schooled.Window;
import schooled.engines.RenderEngine;
import schooled.menu.MenuEntity;
import schooled.menu.Origin;
import schooled.physics.PolygonShape;
import schooled.physics.Vector;
import schooled.visuals.filters.RotateFilter;
import schooled.visuals.filters.SizeFilter;
import schooled.visuals.filters.WaveFilter;

public class Nico {

  public static void main(String[] args) {
    Window window = new Window();
    window.init(null);


    MenuEntity menuEntity = new MenuEntity(null, new Vector(200, 200), new PolygonShape(100, 20));
    menuEntity.setOrigin(Origin.TOP_LEFT);
    menuEntity.addText("laissez-fuert");
    menuEntity.setDefaultFontColor(new Color(222, 180, 222, 255));
    menuEntity.setFontSize(16 * 8);

//    menuEntity.addFilter(new GlitchFilter());
    menuEntity.addFilter(new WaveFilter(10f));
    menuEntity.addFilter(new RotateFilter(menuEntity));
    menuEntity.addFilter(new SizeFilter(menuEntity));
    menuEntity.updateCycle();


    RenderEngine.init(Window.DEFAULT_GRAPHICS_CONTEXT);

    float limit = 250;
    float dt    = -limit;

    while (true) {
      if (window.shouldWindowClose()) {
        break;
      }
      window.initWindow();

      menuEntity.updateCycle(0.1f);

      if (dt < 0) {
        menuEntity.setBackgroundColor(new Color(189, 251, 249));
      } else {
        menuEntity.setBackgroundColor(new Color(249, 251, 189));
      }

      if (dt > limit) {
        dt = -limit;
      }

      dt = dt + 1;

      menuEntity.preRender(Window.DEFAULT_GRAPHICS_CONTEXT);
      RenderEngine.renderMenuEntity(Window.DEFAULT_GRAPHICS_CONTEXT, menuEntity, 1);
      window.update();
    }

  }
}
