package main;

import org.junit.Assert;
import org.junit.Test;

import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

public class PolyTest {

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
    Set<Poly> set = new HashSet<Poly>();
    Poly polyA = new Poly(a);
    Poly polyB = new Poly(b);
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
    Set<Poly> set = new HashSet<Poly>();
    Poly polyA = new Poly(a);
    Poly polyB = new Poly(b);
    set.add(polyA);
    set.add(polyB);
    Assert.assertEquals(2, set.size());
    Assert.assertNotSame(polyA, polyB);
  }

  @Test
  public void testTrim() throws Exception {
    Poly X = Poly.load(new Scanner("5 5\n.....\n..#..\n.###.\n..#..\n....."));
    Poly trimed = X.trim();
    Assert.assertEquals(3, trimed.getHeight());
    Assert.assertEquals(3, trimed.getWidth());
  }

  @Test
  public void testEquals() throws Exception {
    Poly X = Poly.load(new Scanner("5 5\n.....\n..#..\n.###.\n..#..\n....."));
    Poly X2 = Poly.load(new Scanner("5 5\n.....\n..#..\n.###.\n..#..\n....."));
    Poly trimed = X.trim();
    Assert.assertEquals(X, X2);
    Assert.assertEquals(3, trimed.getHeight());
    Assert.assertEquals(3, trimed.getWidth());
  }


  @Test
  public void testHash() throws Exception {
    Poly a = new Poly(new boolean[64][64]);
    a.flip(5, 5);
    a.flip(32, 32);
    a.flip(5, 5);
    a.flip(32, 32);
    a.flip(7, 7);
    Poly b = new Poly(new boolean[64][64]);
    b.flip(7, 7);
    Assert.assertEquals(a, b);
    HashSet<Poly> set = new HashSet<Poly>();
    set.add(a);
    set.add(b);
    Assert.assertEquals(1, set.size());
  }

}