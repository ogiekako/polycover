package ui;

import main.Covering;

public class CoveringModel {

  private Covering covering;

  void setCovering(Covering covering) {
    this.covering = covering;
  }

  boolean isNull() {
    return covering == null;
  }

  public int get(int x, int y) {
    return covering.get(x,y);
  }

  public int height() {
    return isNull() ? 0 : covering.height();
  }

  public int width() {
    return isNull() ? 0 : covering.width();
  }
}
