/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package schooled.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import schooled.physics.Vector;

/**
 * Testing class for distribution vectors.
 * <p></p>
 * Program written by Joseph Straceski
 * <p>
 * contact: straceski.joseph@gmail.com, https://github.com/Crepox
 */
public class TestParser {

  public static boolean inVectors(Vector a, Vector b, Vector x) {
    return (Vector.dot(a, b) < Vector.dot(a, x) && Vector.dot(a, b) < Vector.dot(b, x));
  }

  public static Vector nearCross(Vector vector, Vector near) {
    Vector cross = vector.perpi();
    if (Vector.dot(cross, near) > 0) {
      return cross;
    } else {
      return cross.scalei(-1);
    }
  }

  public static boolean inVectorDot(float hlDot, float hmDot, float lmDot,
      float nhDot, float nmDot, float nlDot) {
    return (hlDot >= 0 && hlDot < nhDot && hlDot < nlDot)
        || (nmDot > 0 && hmDot < nmDot && hmDot < nhDot)
        || (nmDot > 0 && lmDot < nmDot && lmDot < nlDot)
        || (nmDot > 0 && nmDot > nlDot && nmDot > nhDot);
  }

  public static float forceDistribution2(Vector forceNormal, List<Vector> normals) {
    Vector hNorm = new Vector(0, 0);
    Vector lNorm = new Vector(0, 0);
    Vector mNorm = new Vector(0, 0);

    for (Vector nNorm : normals) {
      nNorm = nNorm.scalei(-1);

      if (nNorm.equals(hNorm) || nNorm.equals(lNorm) || nNorm.equals(mNorm)) {
        continue;
      }

      if (hNorm.equals(Vector.zero)) {
        hNorm = nNorm;
      } else if (lNorm.equals(Vector.zero)) {
        lNorm = nNorm;

        if (!hNorm.scalei(-1).equals(lNorm) && Vector.dot(lNorm, hNorm) < 0) {
          mNorm = nearCross(hNorm, lNorm);
        }
      } else {
        if (mNorm.equals(Vector.zero) && hNorm.scalei(-1).equals(lNorm)) {
          mNorm = nearCross(hNorm, nNorm);
          continue;
        }

        float hlDot = Vector.dot(lNorm, hNorm);
        float hmDot = Vector.dot(hNorm, mNorm);
        float lmDot = Vector.dot(lNorm, mNorm);
        float nhDot = Vector.dot(nNorm, hNorm);
        float nlDot = Vector.dot(nNorm, lNorm);
        float nmDot = Vector.dot(nNorm, mNorm);

        if (inVectorDot(hlDot, hmDot, lmDot, nhDot, nmDot, nlDot)) {
          continue;
        }

        if (inVectorDot(hlDot, hmDot, lmDot, -nhDot, -nmDot, -nlDot)) {
          return 1.0f;
        }

        if (nhDot > nlDot) {
          mNorm = hNorm;
          hNorm = nNorm;
        } else {
          mNorm = lNorm;
          lNorm = nNorm;
        }

        if (Vector.dot(hNorm, lNorm) < 0) {
          mNorm = nearCross(hNorm, mNorm);
        }
      }
    }

    float hmDot = Vector.dot(hNorm, mNorm);
    float lmDot = Vector.dot(lNorm, mNorm);
    float hlDot = Vector.dot(hNorm, lNorm);
    float hVal = Vector.dot(hNorm, forceNormal);
    float mVal = Vector.dot(mNorm, forceNormal);
    float lVal = Vector.dot(lNorm, forceNormal);

    if (inVectorDot(hlDot, hmDot, lmDot, hVal, mVal, lVal)) {
      return 1.0f;
    } else if (hVal > 0 && hVal > lVal) {
      return hVal;
    } else if (lVal > 0) {
      return lVal;
    }

    return 0.0f;
  }

  public static float forceDistribution(Vector force, List<Vector> normals) {
    Vector forceNormal = force.normalizei();
    Vector hNorm = new Vector(0, 0);
    Vector lNorm = new Vector(0, 0);
    Vector mNorm = new Vector(0, 0);
    boolean locked = false;

    for (Vector nNorm : normals) {
      nNorm = nNorm.scalei(-1);
      if (nNorm.equals(hNorm) || nNorm.equals(lNorm) || nNorm.equals(mNorm)) {
        continue;
      }

      if (hNorm.equals(Vector.zero)) {
        hNorm = nNorm;
      } else if (lNorm.equals(Vector.zero)) {
        float nDot = Vector.dot(nNorm, hNorm);
        if (nDot < 1 && nDot > -1) {
          lNorm = nNorm;
          mNorm = Vector.add(hNorm, lNorm).normalizei();
        } else if (nDot == -1) {
          lNorm = nNorm;
        }
      } else if (mNorm.equals(Vector.zero)) {
        Vector cross = hNorm.perpi();
        if (Vector.dot(cross, nNorm) > 0) {
          mNorm = cross;
        } else {
          mNorm = cross.scalei(-1);
        }
        if (mNorm.equals(Vector.zero)) {
        }

      } else if (!inVectors(hNorm, mNorm, nNorm) && !inVectors(lNorm, mNorm, nNorm)) {
        if (inVectors(hNorm.scalei(-1), mNorm.scalei(-1), nNorm)
            || inVectors(lNorm.scalei(-1), mNorm.scalei(-1), nNorm)
            || mNorm.scalei(-1).equals(nNorm)) {
          locked = true;
        } else if (Vector.dot(nNorm, hNorm) > Vector.dot(nNorm, lNorm)) {
          mNorm = hNorm;
          hNorm = nNorm;
        } else {
          mNorm = lNorm;
          lNorm = nNorm;
        }
        if (hNorm.equals(lNorm.scalei(-1))) {
          Vector cross = hNorm.perpi();

          if (Vector.dot(cross, mNorm) > 0) {
            mNorm = cross;
          } else {
            mNorm = cross.scalei(-1);
          }
        } else {
          mNorm = lNorm.addi(hNorm).normalizei();
        }
      }
    }

    float forceDistribution;
    if (!hNorm.equals(Vector.zero) && !mNorm.equals(Vector.zero) && !lNorm.equals(Vector.zero)
        && (locked || inVectors(hNorm, mNorm, forceNormal) || inVectors(lNorm, mNorm, forceNormal)
        || mNorm.equals(forceNormal))) {
      forceDistribution = 1;
    } else {
      float hVal = Vector.dot(hNorm, forceNormal);
      float lVal = Vector.dot(lNorm, forceNormal);
      float mVal = Vector.dot(mNorm, forceNormal);

      if (hVal > 0 && hVal > lVal && hVal > mVal) {
        forceDistribution = hVal;
      } else if (lVal > 0 && lVal > mVal) {
        forceDistribution = lVal;
      } else if (mVal > 0) {
        forceDistribution = mVal;
      } else {
        forceDistribution = 0;
      }
    }

    return forceDistribution;
  }

  public static ArrayList<ArrayList<Vector>> powerSet(ArrayList<Vector> originalSet) {
    ArrayList<ArrayList<Vector>> sets = new ArrayList<ArrayList<Vector>>();
    if (originalSet.isEmpty()) {
      sets.add(new ArrayList<Vector>());
      return sets;
    }
    ArrayList<Vector> list = new ArrayList<Vector>(originalSet);
    Vector head = list.get(0);
    ArrayList<Vector> rest = new ArrayList<Vector>(list.subList(1, list.size()));
    for (ArrayList<Vector> set : powerSet(rest)) {
      ArrayList<Vector> newSet = new ArrayList<Vector>();
      newSet.add(head);
      newSet.addAll(set);
      sets.add(newSet);
      sets.add(set);
    }
    return sets;
  }

  public static void main(String[] args) {
    t2(args);
  }

  public static void t1(String[] args) {

    ArrayList<Vector> norms = new ArrayList<>();
    norms.add(new Vector(0, -1).normalizei());
    norms.add(new Vector(0, 1).normalizei());
    norms.add(new Vector(-1, 1).normalizei());

    for (int i = 0; i < 360; i++) {
      float rad = (float) (((float) i) * Math.PI * 2 / 360.0);
      Vector force = new Vector(Math.cos(rad), Math.sin(rad));
      float a = forceDistribution(force.normalizei(), norms);
      float b = forceDistribution2(force.normalizei(), norms);
      if (a != b) {
        System.out.println(i + " " + a + " " + b + " " + Arrays.toString(norms.toArray()));
        //found = true;
        break;
      }
    }
    System.out.println(Arrays.toString(norms.toArray()));
  }

  public static void t2(String[] args) {

    ArrayList<Vector> norms = new ArrayList<>();
    norms.add(new Vector(1, -1).normalizei());
    norms.add(new Vector(-1, 1).normalizei());
    norms.add(new Vector(-1, -1).normalizei());
    norms.add(new Vector(1, 0).normalizei());
    norms.add(new Vector(1, 1).normalizei());
    norms.add(new Vector(0, 1).normalizei());
    norms.add(new Vector(-1, 0).normalizei());
    norms.add(new Vector(0, -1).normalizei());
    // [Vector[x = 0.0, y = 1.0], Vector[x = 0.70710677, y = 0.70710677], Vector[x = 1.0, y = 0.0], Vector[x = -0.70710677, y = -0.70710677], Vector[x = -0.70710677, y = 0.70710677]]

    ArrayList<ArrayList<Vector>> vs = powerSet(norms);
//        System.out.println(Vector.dot(new Vector(1, -1).normalizei(), new Vector(1, 1).normalizei()));
//
//        int v1 = 0;
//        int v2 = 0;
//
//        for (List<Vector> newNorms : vs) {
//
//            long start = System.nanoTime();
//            for (int j = 0; j < 1000; j++) {
//                for (int i = 0; i < 360; i++) {
//                    float rad = (float) (((float) i) * Math.PI * 2 / 360.0);
//                    Vector force = new Vector(Math.cos(rad), Math.sin(rad));
//                    distributionVector(force, newNorms);
//                }
//            }
//            long t1 = System.nanoTime() - start;
//            System.out.println(t1);
//
//            start = System.nanoTime();
//            for (int j = 0; j < 1000; j++) {
//                for (int i = 0; i < 360; i++) {
//                    float rad = (float) (((float) i) * Math.PI * 2 / 360.0);
//                    Vector force = new Vector(Math.cos(rad), Math.sin(rad));
//                    forceDistribution2(force, newNorms);
//                }
//            }
//            long t2 = System.nanoTime() - start;
//            System.out.println(t2);
//            if (t1 > t2) {
//                v1++;
//            } else {
//                v2++;
//            }
//            System.out.println("------------------------");
//        }

    boolean found = false;
    for (ArrayList<Vector> newNorms : vs) {

      for (int i = 0; i < 360; i++) {
        float rad = (float) (((float) i) * Math.PI * 2 / 360.0);
        Vector force = new Vector(Math.cos(rad), Math.sin(rad));
        float a = forceDistribution(force.normalizei(), newNorms);
        float b = forceDistribution2(force.normalizei(), newNorms);
        if (a != b) {
          System.out.println(
              "ERROR " + i + " " + a + " " + b + " " + Arrays.toString(newNorms.toArray()));
          found = true;
          break;
        }
      }

      if (found) {
        break;
      }
    }

//        if (v1 > v2) {
//            System.out.println("V2 wins");
//        } else {
//            System.out.println("V1 wins");
//        }

  }

  public static void t3(String[] args) {

    ArrayList<Vector> norms = new ArrayList<>();
    norms.add(new Vector(1, -1).normalizei());
    norms.add(new Vector(-1, 1).normalizei());
    norms.add(new Vector(-1, -1).normalizei());
    norms.add(new Vector(1, 0).normalizei());
    norms.add(new Vector(1, 1).normalizei());
    norms.add(new Vector(0, 1).normalizei());
    norms.add(new Vector(-1, 0).normalizei());
    norms.add(new Vector(0, -1).normalizei());

    ArrayList<ArrayList<Vector>> vs = powerSet(norms);

    int v1 = 0;
    int v2 = 0;
    long diff = 0;

    for (List<Vector> newNorms : vs) {

      long start = System.nanoTime();
      for (int j = 0; j < 1000; j++) {
        for (int i = 0; i < 360; i++) {
          float rad = (float) (((float) i) * Math.PI * 2 / 360.0);
          Vector force = new Vector(Math.cos(rad), Math.sin(rad));
          forceDistribution(force.normalizei(), newNorms);
        }
      }
      long t1 = System.nanoTime() - start;

      start = System.nanoTime();
      for (int j = 0; j < 1000; j++) {
        for (int i = 0; i < 360; i++) {
          float rad = (float) (((float) i) * Math.PI * 2 / 360.0);
          Vector force = new Vector(Math.cos(rad), Math.sin(rad));
          forceDistribution2(force.normalizei(), newNorms);
        }
      }
      long t2 = System.nanoTime() - start;

      if (t1 > t2) {
        v1++;
      } else {
        v2++;
      }

      System.out.println(t1 - t2);
      System.out.println("------------------------");
    }

    if (v1 > v2) {
      System.out.println("V2 wins");
    } else {
      System.out.println("V1 wins");
    }

  }
}
