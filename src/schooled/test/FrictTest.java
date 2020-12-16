package schooled.test;

public class FrictTest {

  public static void main(String[] args) {

    float STATIC_FRICTION_SLOPE = 1.6180339887f;
    float STATIC_FRICTION_MAX = 10;

    float m = STATIC_FRICTION_MAX;
    float s = (STATIC_FRICTION_SLOPE / 10.0f) * m;
    float x = (float) Math.pow(0 + s, 2.0f);

    System.out.println((((x + m) / (x)) - 1.0f) * s * s);
  }
}
