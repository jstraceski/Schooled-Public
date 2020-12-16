package schooled.physics;

import java.util.function.Function;
import schooled.Game;
import schooled.entities.Entity;
import schooled.event.Event;
import schooled.event.TimedEvent;

public class Mover {

  public static void moveTo(Entity e, Vector loc, float delta, MovementType type, Event event) {

    if (loc == null) {
      return;
    }

    if (delta == 0) {
      e.setPos(loc.clone());
      if (event != null) {
        event.act();
      }
    } else {

      TimedEvent tEvent = new TimedEvent(delta, event) {
        Vector start = e.getPos();
        Vector end = loc;
        float sVel = e.getVelocity().mag();
        float tDist = end.subi(start).mag();
        @Override
        public boolean test(float dt) {
          boolean b = super.test(dt);

          if (!b) {
            float linear = sVel * getTimeDiff() * 60 / tDist;

            float factor = 1;
            float pow = 1.5f;
            Function<Float, Float> func = aFloat -> 1 - (factor / (float) Math.pow((aFloat + (float) Math.pow(factor, 1f/pow)), pow));

            float parab = func.apply(getTimeDiff() / getTime()) / func.apply(1f);

            if (linear < parab) {
              e.setPos(start.addScaledi(end.subi(start), linear));
            } else {
              e.setPos(start.addScaledi(end.subi(start), parab));
            }

          } else {
            e.setPos(end);
          }

          return b;
        }
      };

      e.getGame().addTimedEvent(tEvent);
    }
  }
}
