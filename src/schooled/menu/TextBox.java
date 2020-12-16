package schooled.menu;

import java.util.HashMap;
import schooled.Game;
import schooled.entities.BasicEntity;
import schooled.entities.Entity;
import schooled.physics.BoundingBox;
import schooled.physics.Shape;
import schooled.physics.Vector;
import schooled.visuals.sprite.LayeredSprite;
import schooled.visuals.sprite.Sprite;

public class TextBox extends MenuEntity {

  private HashMap<Origin, Sprite> boxVisuals = new HashMap<>();

  private float xMin, yMin, xDiv, yDiv;
  private int xCount, yCount;

  public TextBox(Game g) {
    super(g);
  }

  public TextBox(Entity e) {
    super(e.getGame());
  }

  public TextBox setClone(TextBox menuEntity) {
    super.setClone(menuEntity);

    HashMap<Origin, Sprite> nBoxList = new HashMap<>();
    for (Origin o : boxVisuals.keySet()) {
      nBoxList.put(o, boxVisuals.get(o).clone());
    }

    menuEntity.boxVisuals = nBoxList;
    menuEntity.xMin = xMin;
    menuEntity.yMin = yMin;
    menuEntity.xDiv = xDiv;
    menuEntity.yDiv = yDiv;

    menuEntity.xCount = xCount;
    menuEntity.yCount = yCount;

    return menuEntity;
  }

  @Override
  public TextBox clone() {
    return setClone(new TextBox(getGame()));
  }

  @Override
  public BasicEntity setClone(BasicEntity entity) {
    if (entity.getClass().isInstance(this)) {
      return setClone(this.getClass().cast(entity));
    } else {
      return super.setClone(entity);
    }
  }

  @Override
  public float[] calculateTextSize(Object gc, float scale) {
    float[] d = super.calculateTextSize(gc, scale);
    d = filterSize(d, scale);
    return d;
  }

  private float[] filterSize(float[] size, float scale) {
    if (boxVisuals.isEmpty()) {
      return size;
    }

    float x = size[0] / scale;
    float y = size[1] / scale;

    float lxDiv = xDiv * 1.0f;
    float lyDiv = yDiv * 1.0f;

    xCount = (int) Math.ceil(x / lxDiv);
    x = xCount * lxDiv;

    yCount = (int) Math.ceil(y / lyDiv);
    y = yCount * lyDiv;

    Vector foff = new Vector(-x/2.0f, -y/2.0f);

    LayeredSprite layeredSprite = new LayeredSprite();
    Sprite topLeft = boxVisuals.get(Origin.TOP_LEFT).clone();
    Sprite topRight = boxVisuals.get(Origin.TOP_RIGHT).clone();
    Sprite bottomLeft = boxVisuals.get(Origin.BOTTOM_LEFT).clone();
    Sprite bottomRight = boxVisuals.get(Origin.BOTTOM_RIGHT).clone();

    topLeft.setOrigin(Origin.BOTTOM_RIGHT);
    topLeft.setInternalOffset(foff);

    topRight.setOrigin(Origin.BOTTOM_LEFT);
    topRight.setInternalOffset(foff.addi(new Vector(xCount * lxDiv, 0)));

    bottomLeft.setOrigin(Origin.TOP_RIGHT);
    bottomLeft.setInternalOffset(foff.addi(new Vector(0, yCount * lyDiv)));

    bottomRight.setOrigin(Origin.TOP_LEFT);
    bottomRight.setInternalOffset(foff.addi(new Vector(xCount * lxDiv, yCount * lyDiv)));

    for (int i = 0; i < xCount; i++) {
      Sprite top = boxVisuals.get(Origin.TOP).clone();
      top.setOrigin(Origin.BOTTOM_LEFT);
      top.setInternalOffset(foff.addi(new Vector(i * lxDiv, 0)));
      layeredSprite.addSprite(top);
    }

    for (int i = 0; i < xCount; i++) {
      Sprite bottom = boxVisuals.get(Origin.BOTTOM).clone();
      bottom.setOrigin(Origin.TOP_LEFT);
      bottom.setInternalOffset(foff.addi(new Vector(i * lxDiv, yCount * lyDiv)));
      layeredSprite.addSprite(bottom);
    }

    for (int i = 0; i < yCount; i++) {
      Sprite left = boxVisuals.get(Origin.LEFT).clone();
      left.setOrigin(Origin.TOP_RIGHT);
      left.setInternalOffset(foff.addi(new Vector(0, i * lyDiv)));
      layeredSprite.addSprite(left);
    }

    for (int i = 0; i < yCount; i++) {
      Sprite right = boxVisuals.get(Origin.RIGHT).clone();
      right.setOrigin(Origin.TOP_LEFT);
      right.setInternalOffset(foff.addi(new Vector(xCount * lxDiv, i * lyDiv)));
      layeredSprite.addSprite(right);
    }

    for (int i = 0; i < xCount; i++) {
      for (int j = 0; j < yCount; j++) {
        Sprite center = boxVisuals.get(Origin.CENTER).clone();
        center.setOrigin(Origin.TOP_LEFT);
        center.setInternalOffset(foff.addi(new Vector(i * lxDiv, j * lyDiv)));
        layeredSprite.addSprite(center);
      }
    }

    layeredSprite.addSprite(topLeft);
    layeredSprite.addSprite(topRight);
    layeredSprite.addSprite(bottomLeft);
    layeredSprite.addSprite(bottomRight);

    setSprite(layeredSprite);

    return new float[] {x * scale, y * scale};
  }

  @Override
  public void updateLSprites() {
    Sprite sprite = getSprite();

    for (String s : getShapeMap().keySet()) {
      Shape shape = getShapeMap().get(s);
      BoundingBox box = shape.getBoundingBox();
      Vector pos = box.getMin();
      Vector size = box.getMax().subi(pos);
      Sprite sSpr = sprite.getSubSpr(pos.getXi(), pos.getYi(), size.getXi(), size.getYi());
      boxVisuals.put(Origin.valueOf(s.toUpperCase()), sSpr);
    }



    Vector tLeft = boxVisuals.get(Origin.TOP_LEFT).getSize();
    Vector tRight = boxVisuals.get(Origin.TOP_RIGHT).getSize();
    Vector top = boxVisuals.get(Origin.TOP).getSize();
    Vector left = boxVisuals.get(Origin.LEFT).getSize();
    Vector bLeft = boxVisuals.get(Origin.BOTTOM_LEFT).getSize();

    xMin = tLeft.getX() + tRight.getX();
    yMin = tLeft.getY() + bLeft.getY();

    xDiv = top.getX();
    yDiv = left.getY();
  }
}
