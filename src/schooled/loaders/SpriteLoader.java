package schooled.loaders;

import static org.lwjgl.opengl.GL11.GL_RGB;
import static org.lwjgl.opengl.GL11.GL_RGBA;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_UNPACK_ALIGNMENT;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glGenTextures;
import static org.lwjgl.opengl.GL11.glPixelStorei;
import static org.lwjgl.opengl.GL11.glTexImage2D;
import static org.lwjgl.stb.STBImage.stbi_load_from_memory;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.function.Function;
import javax.imageio.ImageIO;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.CallbackI.P;
import schooled.Game;
import schooled.menu.IOUtil;
import schooled.physics.Vector;
import schooled.visuals.sprite.Animation;
import schooled.visuals.sprite.ByteBufferImage;
import schooled.visuals.sprite.Sprite;

/**
 * Sprite loading class.
 * <p>
 * Contains methods used to load and or modify sprites and images from files.
 * <p></p>
 * Program written by Joseph Straceski
 * <p>
 * contact: straceski.joseph@gmail.com, https://github.com/Crepox
 */
public class SpriteLoader {

  /**
   * Scales the size of a BufferedImage by a scalar value and rounds to the nearest int.
   *
   * @param i     buffered image
   * @param scale scalar value
   * @return the modified image
   */
  public static BufferedImage scaleBufferedImage(BufferedImage i, float scale) {
    try {
      int newWidth = Math.round(scale * i.getWidth());
      int newHeight = Math.round(scale * i.getHeight());

      BufferedImage bi = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);

      Graphics2D graphics = (Graphics2D) bi.getGraphics();
      graphics.drawImage(i, 0, 0, newWidth, newHeight, null);
      graphics.dispose();
      return bi;
    } catch (Exception e) {
      e.printStackTrace();
    }

    return null;
  }

  /**
   * Load all the sprites from a path.
   * <p>
   * Load the sprites into the game instance.
   *
   * @param g    game instance
   * @param path path
   */
  public static void loadSprites(Game g, String path) {
    loadSprites(g, DataLoader.loadFile(path));
  }

  /**
   * Load sprites from a file or folder.
   * <p>
   * Load sprites into the game instance from an input file or folder. Parses all images as GL
   * images by default.
   *
   * @param g game instance
   * @param f file or folder
   */
  public static void loadSprites(Game g, File f) {
    g.resetSprites(parseSprites(f, SpriteLoader::parseGLImage));
  }

  /**
   * Load sprites from a file or folder.
   * <p>
   * Load sprites into the game instance from an input file or folder.
   *
   * @param game     game instance
   * @param file     file or folder
   * @param function sprite generation function
   */
  public static void loadSprites(Game game, File file, Function<String, Sprite> function) {
    game.resetSprites(parseSprites(file, function));
  }

  /**
   * Generate sprites from a file or folder.
   * <p>
   * Generates GL images by default.
   *
   * @param path path to file or folder
   * @return sprite data
   */
  public static HashMap<String, Sprite> parseSprites(String path) {
    return parseSprites(new File(path), SpriteLoader::parseGLImage);
  }

  /**
   * Generate sprites from a file or folder.
   * <p>
   * Generates GL images by default.
   *
   * @param file file or folder
   * @return sprite data
   */
  public static HashMap<String, Sprite> parseSprites(File file) {
    return parseSprites(file, SpriteLoader::parseGLImage);
  }

  /**
   * Generate sprites from a file or folder.
   * <p>
   * Use the input function to generate sprites from input paths.
   *
   * @param file     file or folder
   * @param function sprite generation function
   * @return sprite data
   */
  public static HashMap<String, Sprite> parseSprites(File file, Function<String, Sprite> function) {
    if (file != null) {
      // if the file is a folder loop through the files in that folder with the same function
      if (file.isDirectory()) {
        HashMap<String, Sprite> list = new HashMap<>();
        for (File subFile : file.listFiles()) {
          list.putAll(parseSprites(subFile, function));
        }
        return list;
      } else {
        // sort the files
        if (file.getName().contains(".png")) {
          // if the file is a .png image file
          if (file.getName().contains("-") && file.getName().contains("x") && !file.getName().contains("[")) {
            // if that file has sprite sheet data in the name divide it into an animation
            // parsing the animation also saves all the individual sprites
            return parseSpriteSheet(file, function);
          } else {
            // put the image into the sprite lookup under the name of the file
            HashMap<String, Sprite> list = new HashMap<>();
            String baseName = file.getName().substring(0, file.getName().lastIndexOf("."));
            list.put(baseName, function.apply(file.getAbsolutePath()));
            return list;
          }
        } else if (file.getName().contains(".json")) {
          // if the file is a json, use the json data to load sprites
          return parseJSON(file, function);
        }
      }
    }
    return new HashMap<>();
  }

  /**
   * Generate a GLImage sprite from an image path.
   *
   * @param imagePath path to the image
   * @return sprite with the GLImage
   */
  public static Sprite parseGLImage(String imagePath) {
    ByteBuffer imageBuffer; // ByteBuffer image buffer

    // attempt to load a byte buffer from a string path
    try {
      imageBuffer = IOUtil.ioResourceToByteBuffer(imagePath, 8 * 1024 * 1024);
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }

    // create width and height buffers
    IntBuffer wBuffer = BufferUtils.createIntBuffer(1);
    IntBuffer hBuffer = BufferUtils.createIntBuffer(1);
    IntBuffer cBuffer = BufferUtils.createIntBuffer(1);


    // load image data from the byte buffer
    ByteBuffer image = stbi_load_from_memory(imageBuffer, wBuffer, hBuffer, cBuffer, 0);
    if (image == null) {
      return null;
    }


    // get integer values from the integer buffers
    int width = wBuffer.get(0);
    int height = hBuffer.get(0);
    int channelCount = cBuffer.get(0);

    Sprite sprite;
    int texID = -1;
    // generate a texture id
    if (GL.getCapabilities() != null) {
      texID = glLoadImage(image, width, height, channelCount);
    }

    // generate a sprite from a buffer image from the image buffer and channel count
    sprite = new Sprite(new ByteBufferImage(image, width, height, channelCount, texID));
    sprite.file = imagePath;
    return sprite;
  }

  public static int glLoadImage(ByteBuffer image, int width, int height, int channelCount) {
    int texID = glGenTextures();

    // bind a GL_TEXTURE_2D structure to the texture id area
    glBindTexture(GL_TEXTURE_2D, texID);
    glPixelStorei(GL_UNPACK_ALIGNMENT, 2); // reset alignment

    // if the channel count is 3
    if (channelCount == 3) {
      if ((width & 1) != 0) {
        // set the data alignment
        glPixelStorei(GL_UNPACK_ALIGNMENT, 2 - (width & 1));
      }

      // load RGB image data from an image buffer
      glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB, width, height, 0, GL_RGB, GL_UNSIGNED_BYTE, image);
    } else {

      // load RGBA image data from an image buffer
      glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, image);
    }
    return texID;
  }

  /**
   * Generate a GLImage sprite from a file.
   * <p>
   * Slower than parseGLImage(String imagePath).
   *
   * @param imageFile image file
   * @return sprite with the GLImage
   */
  public static Sprite parseGLImage(File imageFile) {
    return parseGLImage(imageFile.getAbsolutePath());
  }

  /**
   * Generate a buffered image sprite from an input path.
   *
   * @param path file path
   * @return sprite
   */
  public static Sprite parseBufferedImage(String path) {
    return parseBufferedImage(new File(path));
  }

  /**
   * Generate a buffered image style sprite from an input file.
   *
   * @param file file
   * @return sprite
   */
  public static Sprite parseBufferedImage(File file) {
    try {
      return new Sprite(ImageIO.read(file));
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }
  }

  /**
   * Generate a sub-sprite from a larger sprite and a JSON frame data.
   * <p>
   * Using the JSON frame data cut out a smaller sprite from the input sprite.
   *
   * @param sprite sprite
   * @param fData  frame data
   * @return sub-sprite
   * @throws JSONException generated from getting a frame json object from the input frame data
   */
  public static Sprite parseJSONSubsprite(Sprite sprite, JSONObject fData) throws JSONException {
    JSONObject objectData = fData.getJSONObject("frame");
    int x = objectData.getInt("x");
    int y = objectData.getInt("y");
    int w = objectData.getInt("w");
    int h = objectData.getInt("h");

    return sprite.getSubSpr(x, y, w, h);
  }

  /**
   * Generate a sprite data from a json path.
   * <p>
   * By default, generates all sprites as GL images.
   *
   * @param path json path
   * @return animation and individual frame sprites labeled by name
   */
  public static HashMap<String, Sprite> parseJSON(String path) {
    String name = path.substring(path.lastIndexOf("/")).substring(0, path.lastIndexOf("."));
    return parseJSON(path, name, SpriteLoader::parseGLImage);
  }

  /**
   * Generate a sprite data from a json path.
   * <p>
   * Uses input function to generate all images.
   *
   * @param path     json path
   * @param function sprite generation function
   * @return animation and individual frame sprites labeled by name
   */
  public static HashMap<String, Sprite> parseJSON(String path, Function<String, Sprite> function) {
    String name = path.substring(path.lastIndexOf("/")).substring(0, path.lastIndexOf("."));
    return parseJSON(path, name, SpriteLoader::parseGLImage);
  }

  /**
   * Generate a sprite data from a json file.
   * <p>
   * By default, generates all sprites as GL images.
   *
   * @param file json file
   * @return animation and individual frame sprites labeled by name
   */
  public static HashMap<String, Sprite> parseJSON(File file) {
    String name = file.getName().substring(0, file.getName().lastIndexOf("."));
    return parseJSON(file.getAbsolutePath(), name, SpriteLoader::parseGLImage);
  }

  /**
   * Generate a sprite data from a json file.
   * <p>
   * Uses input function to generate all images.
   *
   * @param file     json file
   * @param function sprite generation function
   * @return animation and individual frame sprites labeled by name
   */
  public static HashMap<String, Sprite> parseJSON(File file, Function<String, Sprite> function) {
    String name = file.getName().substring(0, file.getName().lastIndexOf("."));
    return parseJSON(file.getAbsolutePath(), name, function);
  }

  public static HashMap<String, Sprite> loadLayer(String name, String layer, int layerOffset, JSONArray frameTags, JSONArray frameData, Sprite baseSprite)  throws JSONException{
    // iterate through all the json sprite objects in the json frame array
    HashMap<String, Sprite> spriteMap = new HashMap<>();

    if (!layer.isEmpty() && !layer.equals("#base")) {
      layer = "_" + layer;
    } else {
      layer = "";
    }

    for (int index = 0; index < frameTags.length(); index++) {
      JSONObject spriteData = (JSONObject) frameTags.get(index);

      // get the name of the image
      String iName = spriteData.optString("name");

      // get the indexes of the start and end frames of the current sprite
      int start = layerOffset + spriteData.optInt("from");
      int end = layerOffset + spriteData.optInt("to");

      if (end - start == 0) {
        // if the start and end indexes are the same load the json data as a single image sprite
        JSONObject frameObject = frameData.optJSONObject(start);
        // load a sub-sprite from the base sprite
        Sprite subSprite = parseJSONSubsprite(baseSprite, frameObject);
        subSprite.setName(name + "_" + iName + layer);
        spriteMap.put(name + "_" + iName + layer, subSprite);
      } else {
        // load the json data as an animation
        Animation animation = new Animation();
        for (; start <= end; start++) {
          // load the frame data for each frame starting from the starting index
          JSONObject frameObject = frameData.optJSONObject(start);
          // load frame duration information from the json data
          float duration = (float) frameObject.optInt("duration") / (1000.0f);
          // load the sub-sprite data from the base sprite and add the duration
          animation.addFrame(parseJSONSubsprite(baseSprite, frameObject), duration);
        }
        // load the name of the animation as base-name underscore animation-name
        animation.setName(name + "_" + iName + layer);
        spriteMap.put(name + "_" + iName + layer, animation);
      }
    }


    return spriteMap;
  }

  /**
   * Generate a sprite data from a json file.
   * <p>
   * The name is used to label the various sprites being loaded. It also is used as the name of the
   * base image. Currently formatted to load json data from Aesprite exports. Aseprite json data is
   * stored as a list of frames and then a list of animations referencing the frame list.
   * <p>
   * The image generation function takes in a sprite path and spits out a sprite. This is used to
   * make the function modular.
   *
   * @param path     path to json
   * @param name     base name of the sprite data
   * @param function image generation function
   * @return animation and individual frame sprites labeled by name
   */
  public static HashMap<String, Sprite> parseJSON(String path, String name,
      Function<String, Sprite> function) {
    HashMap<String, Sprite> spriteMap = new HashMap<>();
    try {
      // generate a json object from a json file
      JSONObject jsonFile = new JSONObject(new String(Files.readAllBytes(Paths.get(path))));

      if (jsonFile.has("type")) {
        return spriteMap;
      }

      // extract json data
      JSONObject metaData = (JSONObject) jsonFile.get("meta");
      JSONArray frameData = (JSONArray) jsonFile.get("frames");
      JSONArray frameTags = (JSONArray) metaData.get("frameTags");

      // from json metadata extract the reference image to divide
      String fullPath = metaData.getString("image");
      int idx = Math.min(Math.max(0, fullPath.lastIndexOf(File.separator) + 1), fullPath.length());
      String parentPath = fullPath.substring(idx);
      int idx2 = Math.min(Math.max(0, path.lastIndexOf(File.separator)), path.length());
      String imagePath = path.substring(0, idx2) + "/" + parentPath;
      Sprite baseSprite = function.apply(imagePath);

      if (metaData.has("layers")) {
        JSONArray layerList = (JSONArray) metaData.get("layers");
        int layerOffset = frameData.length() / layerList.length();
        for (int layer = 0; layer < layerList.length(); layer++) {
          JSONObject layerData = (JSONObject) layerList.get(layer);
          spriteMap.putAll(loadLayer(name, layerData.getString("name"), layerOffset * layer, frameTags, frameData, baseSprite));
        }
      } else {
        spriteMap.putAll(loadLayer(name, "", 0, frameTags, frameData, baseSprite));
      }


    } catch (Exception e) {
      e.printStackTrace();
    }
    return spriteMap;
  }

  /**
   * Generate sprite sheet data from a sprite sheet name.
   * <p>
   * example: name-20x40
   * <p>
   * In the above example the base name of the sprite would be "name" the width of each sprite in
   * the sheet would be 20 and the height would be 40. This information is packed into a vector. The
   * x value of the vector is the width, the y value is the height, and the name is the name.
   *
   * @param fileName sprite sheet name
   * @return sprite sheet data
   */
  private static Vector sheetDataFromName(String fileName) {
    String dataChar = "-";
    String divider = "x";
    int dataIndex = fileName.indexOf(dataChar);

    String data = fileName.substring(dataIndex);
    int w = Integer.parseInt(data.substring(1, data.indexOf(divider)));
    int h = Integer.parseInt(data.substring(data.indexOf(divider) + 1, data.indexOf(".")));
    String name = fileName.substring(0, dataIndex);
    Vector vector = new Vector(w, h);
    vector.setName(name);
    return vector;
  }

  /**
   * Generate an animation and sprite data from a sprite sheet.
   * <p>
   * This function uses a sprite generation function to load a sprite from a file path, this allows
   * loading animations and sprite data from animation sheets more modular. Individual sprite data
   * is labeled by the name of the file and the index of the sprite on the sheet going left to
   * right, top to bottom, stating with 0: name_index.
   *
   * @param file sheet file
   * @param f    sprite generation function
   * @return sprite data paired by the name of the sprite
   */
  public static HashMap<String, Sprite> parseSpriteSheet(File file, Function<String, Sprite> f) {
    Vector v = sheetDataFromName(file.getName());
    return parseSpriteSheet(file.getAbsolutePath(), v.getName(), f, (int) v.getX(), (int) v.getY());
  }

  /**
   * Generate a animation and sprite data from a sprite sheet path.
   * <p>
   * This function uses a sprite generation function to load a sprite from a file path, this allows
   * loading animations and sprite data from animation sheets more modular. Individual sprite data
   * is labeled by the name of the file and the index of the sprite on the sheet going left to
   * right, top to bottom, stating with 0: name_index.
   *
   * @param path sheet path
   * @param f    sprite generation function
   * @return sprite data paired by the name of the sprite
   */
  public static HashMap<String, Sprite> parseSpriteSheet(String path, Function<String, Sprite> f) {
    String fileName = path.substring(path.lastIndexOf("/"));
    Vector v = sheetDataFromName(fileName);
    return parseSpriteSheet(path, v.getName(), f, (int) v.getX(), (int) v.getY());
  }

  /**
   * Generate a animation and sprite data from a path, name, width, height, and parsing function.
   * <p>
   * This function uses a sprite generation function to load a sprite from a file path, this allows
   * loading animations and sprite data from animation sheets more modular. Individual sprite data
   * is labeled by the name of the file and the index of the sprite on the sheet going left to
   * right, top to bottom, stating with 0: name_index.
   *
   * @param path sheet path
   * @param name sheet path
   * @param f    sprite generation function
   * @return sprite data paired by the name of the sprite
   */
  public static HashMap<String, Sprite> parseSpriteSheet(String path, String name,
      Function<String, Sprite> f, int divWidth, int divHeight) {
    HashMap<String, Sprite> list = new HashMap<String, Sprite>();
    Animation animation = new Animation();
    try {
      Sprite o = f.apply(path);

      int imageNumber = ((o.getWidth() / divWidth) * o.getHeight() / divHeight);
      for (int i = 0; i < imageNumber; i++) {
        int i_x = i % (o.getWidth() / divWidth);
        int i_y = i / (o.getWidth() / divWidth);
        list.put(name + "_" + i, animation);
        animation.addFrame(o.getSubSpr(i_x * divWidth, i_y * divHeight, divWidth, divHeight));
      }
      list.put(name, animation);
      return list;

    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  /**
   * Generate GL sprites from a sprite sheet path.
   *
   * @param path file path
   * @return sprite data
   */
  public static HashMap<String, Sprite> parseGLSpriteSheet(String path) {
    return parseSpriteSheet(path, SpriteLoader::parseGLImage);
  }

  /**
   * Generate GL sprites from a sprite sheet.
   *
   * @param file sprite sheet
   * @return sprite data
   */
  public static HashMap<String, Sprite> parseGLSpriteSheet(File file) {
    return parseSpriteSheet(file, SpriteLoader::parseGLImage);
  }

  /**
   * Generate BufferedImage sprites from a sprite sheet path.
   *
   * @param path file path
   * @return sprite data
   */
  public static HashMap<String, Sprite> parseBufferedSpriteSheet(String path) {
    return parseSpriteSheet(path, SpriteLoader::parseBufferedImage);
  }

  /**
   * Generate BufferedImage sprites from a sprite sheet.
   *
   * @param file sprite sheet
   * @return sprite data
   */
  public static HashMap<String, Sprite> parseBufferedSpriteSheet(File file) {
    return parseSpriteSheet(file, SpriteLoader::parseBufferedImage);
  }
}
