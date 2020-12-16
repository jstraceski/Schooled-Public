package schooled.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Scanner;
import org.json.JSONException;
import org.json.JSONString;
import schooled.Game;
import schooled.loaders.DataLoader;
import schooled.loaders.JsonLoader;
import schooled.loaders.Lookup;
import schooled.loaders.ObjData;
import schooled.loaders.PFlags;
import schooled.loaders.ParseData;

public class JSONTest {


  public static void main(String[] args) throws Exception {
//
    JSONObject dataFile = new JSONObject();

    dataFile.put("type", "save_data");
    String save_path = "C:/Users/Joe/Documents/GitHub/Schooled/resources/saves/";

    for (File file : Objects.requireNonNull((new File(save_path)).listFiles())) {
      if (file.isFile() && file.getAbsolutePath().contains(".sav")) {

        String path = file.getAbsolutePath();
        dataFile.put("cmds", loadPlainTextFile(null, null, path));
        path = path.replace(".sav", ".json");

        try {
          FileWriter myWriter = new FileWriter(path);
          myWriter.write(dataFile.toString(2));
          myWriter.close();
          System.out.println("Successfully wrote to the file: " + path);
        } catch (IOException e) {
          System.out.println("An error occurred.");
          e.printStackTrace();
        }
      }
    }
//    JsonLoader.parseJson("C:\\Users\\Joe\\Documents\\GitHub\\Schooled\\resources\\saves\\Data.json", new Lookup(), null, new PFlags());
  }

  public static JSONArray loadPlainTextFile(Game g, Lookup lookup, String f) {
    return loadPlainTextFile(g, lookup, new File(f));
  }
  public static JSONArray loadPlainTextFile(Game g, Lookup lookup, File f) {
    JSONArray output = new JSONArray();
    try {
      PFlags parseFlags = new PFlags(); // generate parsing data flag storage
      parseFlags.endData.addEndPair('{', '}');
      parseFlags.endData.addEndPair('(', ')');
      parseFlags.endData.addEndPair('"', '"');
      parseFlags.endData.addEndPair('[', ']');
      parseFlags.endData.addEndPair('<', '>');

      parseFlags.fileName = f.getName();
      Scanner in = new Scanner(new FileInputStream(f)); // create a scanner from a file

      while (in.hasNextLine()) { // while the file has lines
        parseFlags.lineCount++; // increment the line index
        String firstLine = in.nextLine().trim(); // read and trim the line

        // try to load an object from the current line
        ParseData obj = DataLoader.parseText(firstLine, lookup, parseFlags);
        // if the object is formatted as a multi-line object continue loading
        //  the object from the next line before parsing it.
        if (parseFlags.endData.isUnpaired()) {
          continue;
        }

        // if the line made a valid ParseData parse its data.
        if (obj != null) {
          output.put(parseParseData(obj, lookup, g, parseFlags, true));
        }

      }
      in.close();

    } catch (Exception e) {
      System.err.println("Could not loadRoomSize save file.");
      e.printStackTrace();
    }
    return output;
  }

  public static Object parseParseData(ParseData parseData, Lookup lookup, Game game, PFlags parseFlags, boolean top) {
    JSONObject jsonObject = new JSONObject();


    if (!top) {
      if ((parseData.isType("string") || parseData.isType("float") || parseData.isType("integer")
          || parseData.isType("boolean")) && jsonObject.isEmpty()) {

        if (parseData.hasName()) {
          jsonObject.put("obj", parseData.getDataType());
          jsonObject.put("lookup", parseData.getName());
          JSONArray jsonArray = new JSONArray();
          jsonArray.put(DataLoader.parseParseData(parseData, lookup, game, parseFlags));
          jsonObject.put("data", jsonArray);
          return jsonObject;
        }
        return DataLoader.parseParseData(parseData, lookup, game, parseFlags);
      }

      if (parseData.isType("lookup")) {
        JSONObject lObj = new JSONObject();
        lObj.put("lookup", parseData.get(0));
        return lObj;
      }

      if (parseData.isType("array")) {
        JSONArray arr = new JSONArray();
        for (String snip : parseData.getData()) {
          ParseData sndata = DataLoader.parseText(snip, lookup, parseFlags);
          arr.put(parseParseData(sndata, lookup, game, parseFlags, false));
        }
        return arr;
      }
    }

    if (parseData instanceof ObjData) {
      jsonObject.put("obj", parseData.getDataType());
      if (parseData.hasName()) {
       jsonObject.put("lookup", parseData.getName());
      }
    } else {
      jsonObject.put("func", parseData.getDataType());
    }

    if (parseData.hasTags()) {
      if (parseData.getTags().get(0).equals("noload")) {
        jsonObject.put("noload", true);
      }
    }

    JSONArray jsonArray = new JSONArray();
    for (String str : parseData.getData()) {
      ParseData ndata = DataLoader.parseText(str, lookup, parseFlags);
      Object obj = parseParseData(ndata, lookup, game, parseFlags, false);
      if (ndata.hasTags()) {
        if (!ndata.getTags().get(0).equals("noload")) {
          jsonObject.put(ndata.getTags().get(0), obj);
        }
      } else {
        jsonArray.put(obj);
      }
    }

    jsonObject.put("data", jsonArray);
    return jsonObject;
  }
}
