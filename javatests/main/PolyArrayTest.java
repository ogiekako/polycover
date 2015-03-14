package main;

import junit.framework.Assert;

import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

public class PolyArrayTest {

  @Test
  public void samePolyShouldHaveSameHashCodeAndEqual() throws Exception {
    boolean[][] a = new boolean[][]{
        {true, true},
        {false, true}
    };
    boolean[][] b = new boolean[][]{
        {true, true},
        {false, true}
    };
    Set<PolyArray> set = new HashSet<PolyArray>();
    PolyArray polyA = new PolyArray(a);
    PolyArray polyB = new PolyArray(b);
    set.add(polyA);
    set.add(polyB);
    Assert.assertEquals(1, set.size());
    Assert.assertEquals(polyA, polyB);
  }

  @Test
  public void differentPolyShouldNotEqual() throws Exception {
    boolean[][] a = new boolean[][]{
        {true, true},
        {false, true}
    };
    boolean[][] b = new boolean[][]{
        {true, true},
        {false, false}
    };
    Set<PolyArray> set = new HashSet<PolyArray>();
    PolyArray polyA = new PolyArray(a);
    PolyArray polyB = new PolyArray(b);
    set.add(polyA);
    set.add(polyB);
    Assert.assertEquals(2, set.size());
    Assert.assertNotSame(polyA, polyB);
  }

}