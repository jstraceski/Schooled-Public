package schooled.entities;

import schooled.Game;
import schooled.engines.Engine;
import schooled.engines.RenderEngine;
import schooled.menu.Origin;
import schooled.physics.Vector;
import schooled.visuals.sprite.ByteBufferImage;
import schooled.visuals.sprite.Sprite;

public class YoYoEntity extends ItemEntity {

  private static float springOffset = 0;
  private Vector yoyoOffset = new Vector(0, -18);
  private Sprite line, top, bottom, shadow, line_shadow = null;
  private float rotation = 0;

  public YoYoEntity(Game g) {
    super(g);
  }

  public YoYoEntity(YoYoEntity g) {
    super(g);
  }


  @Override
  public void updateLSprites() {
    super.updateLSprites();

    if (hasLSprite("line")) {
      line = getLSprite("line");
    }

    if (hasLSprite("line_shadow")) {
      line_shadow = getLSprite("line_shadow");
    }

    if (hasLSprite("top")) {
      top = getLSprite("top");
    }

    if (hasLSprite("bottom")) {
      bottom = getLSprite("bottom");
    }

    if (hasLSprite("shadow")) {
      shadow = getLSprite("shadow");
    }
  }

  @Override
  public float getAirDrag() {
    return 0.995f;
  }

  @Override
  public float getElasticConstant() {
    return 0.75f;
  }

  @Override
  public void setSprite(Sprite sprite) {
    super.setSprite(sprite);
  }

    @Override
  public Sprite getSprite() {
    if (getParent() == null) {
      return super.getSprite();
    }
    return getGame().getSprite("clear");
  }

  @Override
  public void updateCycle(float t) {
    super.updateCycle(t);


    if (super.getParent() == null) {
      return;
    }

    Entity yoyo = this;
    Vector pPos = new Vector();
    Vector pVel = super.getParent().getLocalVelocity();

    float springConstant = 0.6f;
    float dampingFactor = 0.0001f;
    float taughtFactor = 0.3f;

    float springNeutralLength = 0;
    float springMaxLength = 90;

    yoyo.addPos(pVel.scalei(-t * 60));

    Vector springDirection = pPos.subi(yoyo.getLocalPosition());
    if (springDirection.magSqr() > 0) {
      float measuredDistance = springDirection.mag();
      Vector springNormal = springDirection.normalizei(measuredDistance);
      float newLength1 = Math.max(0, springNeutralLength - springOffset);
      float newLength2 = Math.max(0, springMaxLength - springOffset);

      if (measuredDistance > springNeutralLength - springOffset) {
        float springStretch = measuredDistance - newLength1;
        yoyo.addVelocity(springNormal.scalei(springStretch * springConstant * t));

        float f = springNormal.dot(yoyo.getLocalVelocity());
        if (f > 0) {
          yoyo.addVelocity(springNormal.scalei(-f * dampingFactor));
        }
      }

      if (measuredDistance > springMaxLength - springOffset) {
        yoyo.setPos(pPos.addScaledi(springNormal, -newLength2));
        yoyo.addVelocity(springNormal.scalei(pVel.dot(springNormal)));

        float nVel = yoyo.getLocalVelocity().dot(springNormal);
        if (nVel < 0) {
          Vector newVelocityDirection = Engine.pointedPVector(springNormal, yoyo.getLocalVelocity());
          float magnitude = newVelocityDirection.dot(yoyo.getLocalVelocity());

          Vector tVelocity = newVelocityDirection.scalei(magnitude);
          tVelocity = tVelocity.addScaledi(springNormal, -nVel * taughtFactor);
          yoyo.setVelocity(tVelocity);
        }
      }
    }
  }

  @Override
  public void renderHook(Object gc, Vector gShift, float gScale) {
    super.renderHook(gc, gShift, gScale);

    if (super.getParent() == null) {
      return;
    }

    if (line != null) {
      Vector pgPos = super.getParent().getPosition().scalei(gScale).addi(gShift);
      Vector pPos = pgPos.addScaledi(yoyoOffset, gScale);

      Vector ygPos = getPosition().scalei(gScale).addi(gShift);
      Vector yPos = ygPos.addScaledi(yoyoOffset, gScale);

      Vector parentToYoYo = yPos.subi(pPos);

      if (parentToYoYo.magSqr() < 0.0001) {
        return;
      }


      float length = parentToYoYo.mag();

      rotation = (float) (Math.PI/2f + Math.atan2(-parentToYoYo.getY(), -parentToYoYo.getX()));

      Sprite current_line = line.getScaled(gScale);
      Sprite current_top = top.getScaled(gScale);
      Sprite current_bottom = bottom.getScaled(gScale);
      Sprite current_shadow = shadow.getScaled(gScale);
      Sprite current_line_shadow = line_shadow.getScaled(gScale);

      current_top.setOrigin(Origin.CENTER);
      current_bottom.setOrigin(Origin.CENTER);
      current_shadow.setOrigin(Origin.CENTER);
      current_line.setOrigin(Origin.TOP);
      current_line_shadow.setOrigin(Origin.TOP);

      Vector lineOffset = RenderEngine.getSpriteRenderShift(current_line, 1.0f);
      Vector topOffset = RenderEngine.getSpriteRenderShift(current_top, 1.0f);
      Vector bottomOffset = RenderEngine.getSpriteRenderShift(current_bottom, 1.0f);
      Vector shadowOffset = RenderEngine.getSpriteRenderShift(current_shadow, 1.0f);
      Vector lineShadowOffset = RenderEngine.getSpriteRenderShift(current_line_shadow, 1.0f);

      if (current_line.isByteBufferImage()) {
        ByteBufferImage bbline = line.getByteBufferImage();
        float div = line.getHeight() * gScale;
        Vector InternalSize = bbline.getInternalSize().scalei(new Vector(1, length / div));
        Vector setSize = bbline.getSize().scalei(gScale).scalei(new Vector(1, length / div));

        ByteBufferImage bbi = current_line.getByteBufferImage();
        bbi.setInternalSize(InternalSize);
        bbi.setSize(setSize);
        current_line.setRawData(bbi);

        ByteBufferImage bbis = current_line_shadow.getByteBufferImage();
        bbis.setInternalSize(InternalSize);
        bbis.setSize(setSize);
        current_line_shadow.setRawData(bbis);
      }

      RenderEngine.drawSprite(gc, current_line_shadow, pgPos.addi(lineShadowOffset), pgPos, rotation);
      RenderEngine.drawSprite(gc, current_shadow, ygPos.addi(shadowOffset), ygPos, 0);
      RenderEngine.drawSprite(gc, current_bottom, yPos.addi(bottomOffset), yPos, 0);
      RenderEngine.drawSprite(gc, current_line, pPos.addi(lineOffset), pPos, rotation);
      RenderEngine.drawSprite(gc, current_top, yPos.addi(topOffset), yPos, 0);
    }
  }
}
