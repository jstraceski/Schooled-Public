package schooled.gameobjects;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * A statistics class. Not yet implemented.
 */
public class Stat {

  private int attack = 0;
  private int defense = 0;
  private int intelegence = 0;
  private int speed = 0;
  private int luck = 0;
  private int accuracy = 0;

  public Stat(HashMap<String, Integer> stats) {
    String[] names = (String[]) stats.keySet().toArray();

    for (int i = 0; i < names.length; i++) {
            /*switch (names[i]) {
                case ("attack"):
                    attack = stats.get(names[i]);
                    break;
                case ("defense"):
                    defense = stats.get(names[i]);
                    break;
                case ("intelegence"):
                    intelegence = stats.get(names[i]);
                    break;
                case ("speed"):
                    speed = stats.get(names[i]);
                    break;
                case ("luck"):
                    luck = stats.get(names[i]);
                    break;
                case ("accuracy"):
                    accuracy = stats.get(names[i]);
                    break;
            }*/
    }
  }

  public Stat(int[] stats) {
    attack = stats[1];
    defense = stats[2];
    intelegence = stats[3];
    speed = stats[4];
    luck = stats[5];
    accuracy = stats[6];
  }

  public Stat() {
  }

  public Stat addStats(ArrayList<Stat> stats) {
    return new Stat();
  }

  public int getAttack() {
    return attack;
  }

  public void setAttack(int attack) {
    this.attack = attack;
  }

  public int getDefense() {
    return defense;
  }

  public void setDefense(int defense) {
    this.defense = defense;
  }

  public int getIntelegence() {
    return intelegence;
  }

  public void setIntelegence(int intelegence) {
    this.intelegence = intelegence;
  }

  public int getSpeed() {
    return speed;
  }

  public void setSpeed(int speed) {
    this.speed = speed;
  }

  public int getLuck() {
    return luck;
  }

  public void setLuck(int luck) {
    this.luck = luck;
  }

  public int getAccuracy() {
    return accuracy;
  }

  public void setAccuracy(int accuracy) {
    this.accuracy = accuracy;
  }

}
