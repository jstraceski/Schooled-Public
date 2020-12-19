package schooled.test2;

import java.awt.Color;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.JFileChooser;
import org.json.JSONArray;
import org.json.JSONObject;
import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.tinyfd.TinyFileDialogs;
import schooled.Game;
import schooled.Window;
import schooled.datatypes.Tuple;
import schooled.engines.Engine;
import schooled.engines.RenderEngine;
import schooled.entities.Entity;
import schooled.event.AsyncEvent;
import schooled.event.Event;
import schooled.menu.Menu;
import schooled.menu.MenuButton;
import schooled.menu.MenuEntity;
import schooled.menu.Origin;
import schooled.menu.TextArea;
import schooled.menu.TextContext.FitType;
import schooled.physics.Vector;

public class JSONEditor {
  public static void main(String[] args) throws Exception {
    JSONEditor jsonEditor = new JSONEditor();
    jsonEditor.init();
  }

  Menu menuTop;
  Menu menuBottom;
  MenuButton button, save;
  MenuButton styleGuide;
  TextArea fileBox;
  Window window;
  JSONObject fileData = null;
  boolean modified = false;
  Vector current = new Vector();
  Vector scroll = new Vector(0, 1);
  ArrayList<MenuEntity> itemList = new ArrayList<>();
  ArrayList<MenuEntity> scrollList = new ArrayList<>();
  Path nPath;

  public void init() throws Exception {
    window = new Window();
    window.init(null);

    menuTop = new Menu(window);
    menuBottom = new Menu(window);

    button = new MenuButton((Game) null);
    button.setPos(new Vector(10, 30));
    button.setText("File");
    button.setOrigin(Origin.BOTTOM_LEFT);
    button.setBackgroundColor(Color.LIGHT_GRAY);
    button.setButtonEvent(new AsyncEvent(() -> {
      MemoryStack stack = MemoryStack.stackPush();
      PointerBuffer filters = stack.mallocPointer(1);
      filters.put(stack.UTF8("*.json"));
      filters.flip();
      String result = TinyFileDialogs.tinyfd_openFileDialog("Choose Data File", "resources/saves/", filters, null, false);
      if (result != null && !result.isEmpty()) {
        File file = new File(result);
        fileBox.setText(file.getAbsolutePath());
        if (file.getName().contains(".json")) {
          nPath = Paths.get(file.getAbsolutePath());
          fileData = new JSONObject(new String(Files.readAllBytes(nPath)));
          modified = true;
        }
      }
    }));

    save = new MenuButton((Game) null);
    save.setPos(new Vector(60, 30));
    save.setText("Save");
    save.setOrigin(Origin.BOTTOM_LEFT);
    save.setBackgroundColor(Color.LIGHT_GRAY);
    save.setButtonEvent(new AsyncEvent(() -> {
      Game.log("A");
    }));


    nPath = null;
    fileData = null;
    modified = true;

    fileBox = new TextArea((Game) null);
    fileBox.setPos(new Vector(10, 60));
    fileBox.setBackgroundColor(Color.LIGHT_GRAY);
    fileBox.setOrigin(Origin.BOTTOM_LEFT);
    fileBox.setEnterEvent(new Event(() -> {
      String file = fileBox.getText();
      if (file.contains(".json")) {
        nPath = Paths.get(file);
        fileData = new JSONObject(new String(Files.readAllBytes(nPath)));
        modified = true;
      }
    }));

    menuTop.addEntity(button);
    menuTop.addEntity(fileBox);
    menuTop.addEntity(save);
    menuTop.addEntity(window.getCursor(), false);

    RenderEngine.init(Window.DEFAULT_GRAPHICS_CONTEXT);

    while (true) {
      if (window.shouldWindowClose()) {
        break;
      }

      window.initWindow();
      window.refreshInputs();

      if (window.keyTyped(GLFW.GLFW_KEY_Z) && (window.keyDown(GLFW.GLFW_KEY_LEFT_CONTROL) || window.keyDown(GLFW.GLFW_KEY_RIGHT_CONTROL))) {
        undo();
      }

      if (window.getYScroll() != 0) {
        scrollList.forEach((entity -> entity.addPos(new Vector(0, window.getYScroll() * 20))));
      }

      if (modified) {
        generateJSONDisplay(fileData);
        modified = false;
      }

      Engine.updateAndProcess(menuTop, 0.001f);
      RenderEngine.totalRenderMenu(menuTop);

      window.update();
    }
  }


  Vector shift = new Vector(0, 12);
  Vector pos = new Vector(20, 30);

  public void generateJSONDisplay(JSONObject total) {
    if (total == null) {
      return;
    }

    if (!total.optString("type", "").equals("save_data")) {
      return;
    }

    scrollList.forEach(Entity::removeFromContainer);
    itemList.clear();
    scrollList.clear();

    pos = new Vector(20, 30);

    JSONArray jsonArray = (JSONArray) total.get("cmds");

    for (Object object : jsonArray) {
      if (object instanceof JSONObject) {
        generateItem((JSONObject) object, pos.add(shift).clone());
      }
    }

    if (!itemList.isEmpty()) {
      MenuButton menuButton = setStyle(new MenuButton(null, null, "+"));
      Color c = Color.cyan;
      menuButton.setBackgroundColor(Color.cyan);

      menuButton.setUpdateEvent(new Event(() -> {
        if (!itemList.isEmpty()) {
          MenuEntity last = itemList.get(itemList.size() - 1);
          menuButton.setPos(last.getDefaultPos().addi(shift));
        }
      }));
      menuButton.setButtonEvent(new Event(() -> addObject(menuButton.getDefaultPos().addi(new Vector(60, 0)), "obj", c)));

      menuTop.addEntity(menuButton);
      scrollList.add(menuButton);
    }

    if (!itemList.isEmpty()) {
      MenuButton menuButton = setStyle(new MenuButton(null, null, "+"));
      Color c = Color.yellow;
      menuButton.setBackgroundColor(Color.yellow);

      menuButton.setUpdateEvent(new Event(() -> {
        if (!itemList.isEmpty()) {
          MenuEntity last = itemList.get(itemList.size() - 1);
          menuButton.setPos(last.getDefaultPos().addi(shift.addi(new Vector(10, 0))));
        }
      }));
      menuButton.setButtonEvent(new Event(() -> addObject(menuButton.getDefaultPos().addi(new Vector(60, 0)), "func", c)));

      menuTop.addEntity(menuButton);
      scrollList.add(menuButton);
    }
  }


  public void reposition(MenuEntity menuEntity) {

  }

  public MenuButton setStyle(MenuButton entity) {
    return (MenuButton) setStyle((MenuEntity) entity);
  }

  public TextArea setStyle(TextArea te) {
    te.setClearOnEdit(false);
    return (TextArea) setStyle((MenuEntity) te);
  }

  public MenuEntity setStyle(MenuEntity entity) {
    entity.setFontSize(16f);
    entity.setTextFit(FitType.base);
    entity.setBorderSize(0);
    entity.setPadding(2);
    entity.setOrigin(Origin.BOTTOM_LEFT);
    return entity;
  }

  public void addObject(Vector loc, String type, Color color) {
    TextArea textArea = setStyle(new TextArea(null, loc, "Default_Text"));
    menuTop.setSelectedArea(textArea);
    textArea.setBackgroundColor(color);

    textArea.setEnterEvent(new Event( () -> {
      menuTop.addEntity(textArea);
      JSONObject jsonObject = new JSONObject();
      jsonObject.put(type, textArea.getText());
      generateItem(jsonObject, getLastPos());
      textArea.removeFromContainer();
    }));

    textArea.setEscapeEvent(new Event(textArea::removeFromContainer));
    textArea.setUnselectedEvent(new Event(textArea::removeFromContainer));

    menuTop.addEntity(textArea);
    scrollList.add(textArea);
  }

  public Vector getLastPos() {
    if (!itemList.isEmpty()) {
      return itemList.get(itemList.size() - 1).getDefaultPos().addi(shift);
    }
    return pos;
  }

  public void generateItem(JSONObject object, Vector location) {
    String label = "";
    String lookup = "";
    Color color = null;

    if (object.has("func")) {
      label = object.getString("func");
      color = Color.yellow;
    }
    if (object.has("obj")) {
      label = object.getString("obj");
      lookup = object.optString("lookup", "");
      color = Color.cyan;
    }

    if (!label.isEmpty() && color != null) {
      TextArea menuEntity = setStyle(new TextArea(null));

      if (!lookup.isEmpty()) {
        label = label + ": ";
        menuEntity.setLabel(label);
        menuEntity.setText(lookup);
      } else {
        menuEntity.setText(label);
      }

      menuEntity.setPos(location.clone());
      menuEntity.setBackgroundColor(color);

      menuTop.addEntity(menuEntity);
      itemList.add(menuEntity);
      scrollList.add(menuEntity);



      MenuButton menuButton = setStyle(new MenuButton((Game) null));
      menuButton.setText("<");
      menuButton.setBackgroundColor(Color.green);
      menuButton.setUpdateEvent(new Event(() -> {
        Vector oSet = new Vector(menuEntity.getWidth() + 2f, 0);
        menuButton.setPos(menuEntity.getDefaultPos().addi(oSet));
      }));
      menuButton.setButtonEvent(new Event(() -> {
        TextArea textArea = setStyle(new TextArea(null, null, getJSON(object)));
        menuTop.setSelectedArea(textArea);
        textArea.setOrigin(Origin.TOP_LEFT);
        textArea.setBackgroundColor(Color.gray);

        textArea.setEditEvent(new Event(() -> {
          Object out = modData(object, textArea.getText());
          if (out == null) {
            textArea.setBackgroundColor(Color.red);
          } else {
            textArea.setBackgroundColor(Color.gray);
          }
        } ));
        textArea.setUpdateEvent(new Event(() -> {
          Vector oSet = new Vector(menuButton.getWidth() + 2f, -menuButton.getHeight());
          textArea.setPos(menuButton.getDefaultPos().addi(oSet));
        }));
        textArea.setEscapeEvent(new Event(textArea::removeFromContainer));

        visualList.put(object, textArea);
        menuTop.addEntity(textArea);
        scrollList.add(menuButton);
      }));

      menuTop.addEntity(menuButton);
      scrollList.add(menuButton);
    }
  }

  HashMap<JSONObject, String> editList = new HashMap<>();
  HashMap<JSONObject, TextArea> visualList = new HashMap<>();

  public String getJSON(JSONObject object) {
    if (editList.containsKey(object)) {
      return editList.get(object);
    } else {
      return object.toString(2);
    }
  }

  public JSONObject modData(JSONObject object, String data) {
    return modData(object, data, true);
  }

  public JSONObject modData(JSONObject object, String data, boolean add) {
    if (add) {
      log(object, data);
    }
    JSONObject nobj;
    try {
      nobj = new JSONObject(data);
    } catch (Exception e) {
      editList.put(object, data);
      return null;
    }

    editList.remove(object);
    ArrayList<String> keySet =  new ArrayList<>(object.keySet());

    for (String key : keySet) {
      object.remove(key);
    }

    for (String key : nobj.keySet()) {
      object.put(key, nobj.get(key));
    }

    save();

    return  nobj;
  }

  ArrayList<Tuple<JSONObject, String>> history = new ArrayList<>();

  public void log(JSONObject object, String update) {
    if (hidx < 0) {
      if ((history.size() + hidx) > 0) {
        ArrayList<Tuple<JSONObject, String>> newHist = new ArrayList<>();

        int idx;
        for (idx = 0; idx < history.size() + hidx; idx++) {
          newHist.add(history.get(idx));
        }

        history = newHist;
      } else if ((history.size() + hidx) == 0) {
        Tuple<JSONObject, String> o = history.get(0);
        history = new ArrayList<>();
        history.add(o);
      }
    }

    hidx = 0;
    history.add(new Tuple<>(object, update));
  }

  public void save() {
    try {
      FileWriter myWriter = new FileWriter(nPath.toString());
      myWriter.write(fileData.toString(2));
      myWriter.close();
    } catch (IOException e) {
      System.out.println("An error occurred.");
      e.printStackTrace();
    }
  }

  int hidx = 0;

  public void undo() {
    if ((history.size() + hidx) > 1) {
      hidx--;
      Tuple<JSONObject, String> obj = history.get(history.size() - 1 + hidx);
      Object out = modData(obj.a, obj.b, false);

      if (visualList.containsKey(obj.a)) {
        visualList.get(obj.a).suppressEditEvent();
        visualList.get(obj.a).setText(obj.b);

        if (out == null) {
          visualList.get(obj.a).setBackgroundColor(Color.red);
        } else {
          visualList.get(obj.a).setBackgroundColor(Color.gray);
        }
      }
    }
  }
}
