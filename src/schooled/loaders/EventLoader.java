package schooled.loaders;

import static schooled.loaders.DataLoader.parseObjData;
import static schooled.loaders.DataLoader.strToObj;
import static schooled.loaders.DataLoader.strToObjData;
import static schooled.loaders.DataLoader.toFloat;

import java.util.ArrayList;
import java.util.List;
import schooled.Game;
import schooled.entities.Entity;
import schooled.event.Event;
import schooled.loaders.JsonLoader.Func;

public class EventLoader {

  public static Event parseTimedEvent(float time, ObjData obj, Lookup lookup, Game game,
      PFlags flags) {
    Event e = (Event) strToObj(obj.get(1), lookup, game, flags);

    if (e.getLevel() == 3) {
      return new Event((a, b) -> game.addTimedEvent(time, new Event(() -> e.act(a, b))));
    } else if (e.getLevel() == 2) {
      return new Event((a) -> game.addTimedEvent(time, new Event(() -> e.act(a))));
    } else {
      return new Event(() -> game.addTimedEvent(time, e));
    }
  }

  public static Event parseMultiEvent(List<String> data, Lookup lookup, Game game, PFlags flags) {
    ArrayList<Event> events = new ArrayList<>();
    int maxLevel = 0;
    Entity ea = null, eb = null;

    for (String l : data) {
      ObjData objData = strToObjData(l, lookup, game, flags);

      if (objData.isType("entity")) {
        if (ea == null) {
          ea = (Entity) objData.getObj();
        } else {
          eb = (Entity) objData.getObj();
        }
      } else {
        events.add((Event) objData.getObj());
        maxLevel = Math.max(maxLevel, ((Event) objData.getObj()).getLevel());
      }
    }

    if (ea != null && eb != null) {
      Entity finalEa = ea;
      Entity finalEb = eb;
      return new Event(() -> events.forEach((Event event) -> event.act(finalEa, finalEb)));
    } else if (ea != null && maxLevel == 3) {
      Entity finalEa1 = ea;
      return new Event((a) -> events.forEach((Event event) -> event.act(a, finalEa1)));
    } else if (ea != null) {
      Entity finalEa2 = ea;
      return new Event(() -> events.forEach((Event event) -> event.act(finalEa2)));
    } else if (maxLevel == 3) {
      return new Event((a, b) -> events.forEach((Event event) -> event.act(a, b)));
    } else if (maxLevel == 2) {
      return new Event((a) -> events.forEach((Event event) -> event.act(a)));
    } else {
      return new Event(() -> events.forEach(Event::act));
    }
  }

  public static Event eventFromData(Func runnable, Lookup lookup) {
    Event e = new Event((Entity a, Entity b) -> {
      lookup.put("1", new ObjData("entity", a));
      lookup.put("2", new ObjData("entity", b));
      runnable.run();
    });
    e.add((Entity a) -> {
      lookup.put("1", new ObjData("entity", a));
      lookup.put("2", null);
      runnable.run();
    });
    e.add(() -> {
      lookup.put("1", null);
      lookup.put("2", null);
      runnable.run();
    });
    return e;
  }

  public static Event parseEvent(ObjData obj, Lookup lookup, Game game, PFlags flags)
      throws Exception {

    float time = -1;
    ArrayList<Event> eList = new ArrayList<>();

    for (String str : obj.getData()) {
      ParseData parseData = DataLoader.parseText(str, lookup, flags);
      if (parseData instanceof ObjData) {
        DataLoader.parseParseData(parseData, lookup, game, flags);
        ObjData objData = (ObjData) parseData;
        if (parseData.isType("number")) {
          time = toFloat(objData);
        } else if (parseData.isType("event")) {
          eList.add((Event) objData.getObj());
        }
      } else {
        eList.add(eventFromData(() -> DataLoader.parseParseData(parseData, lookup, game, flags), lookup));
      }
    }

    Event e = new Event((Entity a, Entity b) -> eList.forEach((event) -> event.act(a, b)));
    e.add((Entity a) -> eList.forEach((event) -> event.act(a)));
    e.add(() -> eList.forEach(Event::act));

    if (time > 0) {
      final float fTime = time;
      Event e2 = new Event((Entity a, Entity b) -> game.addTimedEvent(fTime, new Event(() -> e.act(a, b))));
      e2.add((Entity a) -> game.addTimedEvent(fTime, new Event(() -> e.act(a))));
      e2.add(() -> game.addTimedEvent(fTime, e));

      return e2;
    }

    return e;
  }
}
