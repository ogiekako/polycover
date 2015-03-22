package main;

import java.util.Arrays;

public class Bits {

  private long[] bits = new long[0];

  public void set(int i) {
    expand(i / 64 + 1);
    bits[i / 64] |= 1L << (i & 63);
  }

  public boolean get(int i) {
    expand(i / 64 + 1);
    return ((bits[i / 64] >>> (i & 63)) & 1L) == 1L;
  }

  private void expand(int n) {
    if (bits.length < n) {
      long[] nBits = new long[n];
      System.arraycopy(bits, 0, nBits, 0, bits.length);
      bits = nBits;
    }
  }

  public Bits clone() {
    Bits res = new Bits();
    res.bits = bits.clone();
    return res;
  }

  public void flip(int i) {
    expand(i / 64 + 1);
    bits[i / 64] ^= 1L << (i & 63);
    shrink();
  }

  private void shrink() {
    int nLen = bits.length;
    while (nLen > 0 && bits[nLen - 1] == 0) {
      nLen--;
    }
    long[] nBits = new long[nLen];
    System.arraycopy(bits, 0, nBits, 0, nBits.length);
    bits = nBits;
  }

  public Bits shift(int k) {
    Bits res = new Bits();
    for (int i = 0; i < bits.length * 64; i++) {
      if (get(i)) {
        res.set(i + k);
      }
    }
    return res;
  }

  public void or(Bits o) {
    expand(o.bits.length);
    for (int i = 0; i < o.bits.length; i++) {
      bits[i] |= o.bits[i];
    }
  }

  @Override
  public boolean equals(Object o) {
    Bits bits1 = (Bits) o;

    if (!Arrays.equals(bits, bits1.bits)) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(bits);
  }
}
