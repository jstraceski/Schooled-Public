package schooled.physics;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;
import schooled.Game;
import schooled.entities.BasicEntity;

public class MassTree implements Serializable {
  public float iMass = -1.0f;
  public boolean changed = false;
  public Vector inNormal = null;
  public Vector customIn = null;
  public Vector zeroNormal = null;
  public int revIdx = -1;
  public boolean revRemove = false;
  public float eScalar = 1.0f;
  public Vector rNormal = null;
  public float divScalar = 1;
  public Vector oNormalTotal = null;
  public Vector outNormal = null;
  public BasicEntity entity = null;
  public int hitIdx = -1;
  public boolean always = false;
  public int hCount = 0;


  @Override
  public MassTree clone() {
    MassTree massTree = new MassTree();
    massTree.iMass = iMass;
//    massTree.changed = changed;
    massTree.inNormal = inNormal;
    massTree.customIn = customIn;
    massTree.zeroNormal = zeroNormal;
    massTree.eScalar = eScalar;
//    massTree.rNormal = rNormal;
//    massTree.divScalar = divScalar;
    massTree.oNormalTotal = oNormalTotal;
    massTree.outNormal = outNormal;
    massTree.entity = entity;
    massTree.hitIdx = hitIdx;
    massTree.always = always;

    massTree.normals = normals;
    massTree.weights = weights;

    massTree.tmpZeros = new ArrayList<>(tmpZeros);
//    massTree.cIdx = new ArrayList<>(cIdx);

//    for (MassTree newTree : children) {
//      massTree.children.add(newTree.clone());
//    }

    if (hitChildren != null) {
      massTree.hitChildren = new ArrayList<>();
      for (MassTree newTree : hitChildren) {
        massTree.hitChildren.add(newTree.clone());
      }
    }

//    if (tcIdx != null) {
//      massTree.tcIdx = new ArrayList<>(tcIdx);
//    }

//    for (MassTree newTree : lEnds) {
//      massTree.lEnds.add(new MassTree(newTree.entity, newTree.inNormal));
//    }

    return massTree;
  }

  public MassTree() {
  }

  public MassTree(BasicEntity entity, Vector inNormal) {
    this.entity = entity;
    this.inNormal = inNormal;
  }

  public MassTree(float iMass) {
    this.iMass = iMass;
  }

  public MassTree(float iMass, Vector in, Vector r, boolean always) {
    this.inNormal = in;
    this.iMass = iMass;
    this.rNormal = r;
    this.always = always;
  }

  public ArrayList<Vector> normals = new ArrayList<>();
  public ArrayList<Float> weights = new ArrayList<>();
  public MassTree parent = null;

  public ArrayList<Vector> tmpZeros = new ArrayList<>();
  public ArrayList<MassTree> children = new ArrayList<>();
  public ArrayList<Integer> tcIdx = null;
  public ArrayList<Integer> cIdx = new ArrayList<>();
  public ArrayList<MassTree> lEnds = new ArrayList<>();
  public ArrayList<MassTree> revChildren = new ArrayList<>();
  public ArrayList<MassTree> hitChildren = null;

  public static int limit = -1;
  public static int count = 0;

  @Override
  public String toString() {
    if (limit == -1) {
      limit = 4;
      count = 0;
    }

    int cStart = count;

    count++;
    if (limit > 0 && count >= limit) {
      count = cStart;
      return entity != null ? entity.toString() : "INF";
    }

    ArrayList<String> data = new ArrayList<>();
    data.add("entity: " + Game.toStr(entity));
    data.add("im: " + Game.toStr(iMass));
    data.add("m: " + Game.toStr(1.0f/iMass));
    data.add("iNorm: " + Game.toStr(inNormal));
    data.add("oNorm: " + Game.toStr(outNormal));
//    data.add("eScalar: " + Game.toStr(eScalar));

    if (parent != null) {
      data.add("parent? : " + Game.toStr(parent.entity));
    }

//    data.add("divScalar: " + Game.toStr(divScalar));
    data.add("always: " + Game.toStr(always));

    if (tmpZeros != null) {
      data.add("tmpZeros: " + Game.toStr(tmpZeros));
    }

    if (entity != null) {
      data.add("zeroNormalsB: " + Game.toStr(entity.zeros));
    }

//    data.add("zNorm: " + Game.toStr(zeroNormal));
//    data.add("cIn: " + Game.toStr(customIn));
//    data.add("rnorm: " + Game.toStr(rNormal));

    if (children == null || children.isEmpty()) {
      data.add("child: {}");
    } else {
      String childStr = children.stream().map(Game::toStr).collect(Collectors.joining("\n"));
      String[] strings = childStr.split("\n");
      ArrayList<String> strList = new ArrayList<>(Arrays.asList(strings));
      strList = (ArrayList<String>) strList.stream().map(s -> ("\t" + s)).collect(Collectors.toList());

      data.add("child: ");
      data.addAll(strList);
    }

    if (hitChildren == null || hitChildren.isEmpty()) {
      data.add("hitChild: {}");
    } else {
      String childStr = hitChildren.stream().map(Game::toStr).collect(Collectors.joining("\n"));
      String[] strings = childStr.split("\n");
      ArrayList<String> strList = new ArrayList<>(Arrays.asList(strings));
      strList = (ArrayList<String>) strList.stream().map(s -> ("\t" + s)).collect(Collectors.toList());

      data.add("hitChild: ");
      data.addAll(strList);
    }


    data = (ArrayList<String>) data.stream().map(s -> ("\t" + s)).collect(Collectors.toList());

    Optional<String> dataStr = data.stream().reduce((s, s2) -> s + "\n" + s2);
    count = cStart;
    return "MT [\n" + dataStr.get() + "\n]";
  }
}
