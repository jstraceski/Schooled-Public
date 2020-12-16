package schooled.datatypes;

import schooled.Game;

public class Tuple<A, B> {
  public final A a;
  public final B b;
  public Tuple(A a, B b) {
    this.a = a;
    this.b = b;
  }

  @Override
  public String toString() {
    return "<" + Game.toStr(a) + ", " + Game.toStr(b) + ">";
  }
}