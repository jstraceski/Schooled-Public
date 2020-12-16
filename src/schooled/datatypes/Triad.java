package schooled.datatypes;

import schooled.Game;

public class Triad<A, B, C> {
  public final A a;
  public final B b;
  public final C c;
  public Triad(A a, B b, C c) {
    this.a = a;
    this.b = b;
    this.c = c;
  }

  @Override
  public String toString() {
    return "<" + Game.toStr(a) + ", " + Game.toStr(b) + ", " + Game.toStr(c) + ">";
  }
}