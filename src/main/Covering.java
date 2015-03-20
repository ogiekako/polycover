package main;

public class Covering {

  public int[][] array;

  public Covering setArray(int[][] array) {
    this.array = array;
    return this;
  }

  public boolean isNull() {
    return array == null;
  }

  public int height() {
    return array == null ? 0 : array.length;
  }

  public int width() {
    return array == null ? 0 : array[0].length;
  }

  public int get(int x, int y) {
    return array[x][y];
  }

  public void copyFrom(Covering res) {
    this.array = clone(res.array);
  }

  private static int[][] clone(int[][] array) {
    if (array == null) {
      return null;
    }
    int[][] res = new int[array.length][array[0].length];
    for (int i = 0; i < res.length; i++) {
      res[i] = array[i].clone();
    }
    return res;
  }
}
