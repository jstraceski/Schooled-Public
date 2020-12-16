package schooled.audio;

import java.io.*;
import java.net.URL;
import javax.sound.sampled.*;
import javax.sound.sampled.FloatControl.Type;
import javax.swing.*;
import schooled.Game;
import schooled.datatypes.Tuple;

// To play sound using Clip, the process need to be alive.
// Hence, we use a Swing application.
public class Sound {

  private URL url;
  private Clip clip = null;
  private FloatControl volumeControl;
  private float volume = 1.0f;
  private static Tuple<Float, Float> nullLimits = new Tuple<>(0f, 0f);
  private static Tuple<Float, Float> volumeLimits = new Tuple<>(0f, 0f);

  // Constructor
  public Sound(String path) {
    load(path);
  }

  public void load(String path) {

    try {
      File file = new File(path);
      // Open an audio input stream.
      AudioInputStream audioIn = AudioSystem.getAudioInputStream(file);
      // Get a sound clip resource.
      clip = AudioSystem.getClip();
      // Open audio clip and load samples from the audio input stream.
      clip.open(audioIn);
      volumeControl = (FloatControl) clip.getControl(Type.MASTER_GAIN);
      volumeLimits = new Tuple<>(volumeControl.getMinimum(), volumeControl.getMaximum());

    } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
      e.printStackTrace();
    }
  }

  public void play() {
    if (clip != null) {
      clip.loop(0);
    }
  }

  public void stop() {
    if (clip != null) {
      clip.stop();
    }
  }

  public float getVolume() {
    if (clip != null) {
      return volumeControl.getValue();
    }
    return -1;
  }

  public void setVolume(float volume) {
    if (volumeControl != null) {
      volumeControl.setValue(volume);
    }
  }

  public Tuple<Float, Float> getVolumeLimits() {
    if (clip != null) {
      return volumeLimits;
    }
    return nullLimits;
  }
}