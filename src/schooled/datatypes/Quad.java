package schooled.datatypes;

import schooled.Game;

public class Quad<A, B, C, D> {
  public final A a;
  public final B b;
  public final C c;
  public final D d;
  public Quad(A a, B b, C c, D d) {
    this.a = a;
    this.b = b;
    this.c = c;
    this.d = d;
  }

  @Override
  public String toString() {
    return "<" + Game.toStr(a) + ", " + Game.toStr(b) + ", " + Game.toStr(c) + ", " + Game.toStr(d) + ">";
  }
}