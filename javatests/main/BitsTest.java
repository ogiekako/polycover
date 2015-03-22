package main;


import org.junit.Assert;
import org.junit.Test;

public class BitsTest {

  @Test
  public void testSetGet() throws Exception {
    Bits b = new Bits();
    b.set(1);
    b.set(3);
    b.set(63);
    b.set(64);
    b.set(0);
    Assert.assertTrue(b.get(1));
    Assert.assertTrue(b.get(3));
    Assert.assertTrue(b.get(63));
    Assert.assertTrue(b.get(64));
    Assert.assertTrue(b.get(0));
  }

  @Test
  public void testFlip() throws Exception {
    Bits b = new Bits();
    b.set(1);
    b.set(3);
    b.set(63);
    b.set(64);
    b.set(0);
    b.flip(63);
    b.flip(64);
    b.flip(128);
    Assert.assertTrue(b.get(1));
    Assert.assertTrue(b.get(3));
    Assert.assertFalse(b.get(63));
    Assert.assertFalse(b.get(64));
    Assert.assertTrue(b.get(0));
    Assert.assertTrue(b.get(128));
  }

  @Test
  public void testShift() throws Exception {
    Bits b = new Bits();
    b.set(5);
    Bits c = b.shift(27);
    Assert.assertTrue(b.get(5));
    Assert.assertTrue(c.get(32));
    Assert.assertFalse(c.get(5));
    Assert.assertFalse(b.get(32));
  }

  @Test
  public void testOr() throws Exception {
    Bits b = new Bits();
    b.set(3);
    Bits c = new Bits();
    c.set(3);
    c.set(32);
    b.or(c);
    Assert.assertTrue(b.get(3));
    Assert.assertTrue(b.get(32));
  }
}