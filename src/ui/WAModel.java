package ui;

public class WAModel implements AbstModel {

  int[][] array;

  void setArray(int[][] array) {
    this.array = array;
  }

  public boolean isNull() {
    return array == null;
  }
}
