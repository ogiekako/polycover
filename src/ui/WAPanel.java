package ui;

import java.awt.*;

import javax.swing.*;

public class WAPanel extends JPanel {

  private final WAModel wa;
  private static final int margin = 5;

  private static Color[]
      coverColor =
      {Color.cyan, Color.pink, Color.yellow, Color.orange, Color.green, Color.blue, Color.magenta,
       Color.red, Color.darkGray};
  private static Color[] coveredColor;

  static {
    int nc = coverColor.length;
    coveredColor = new Color[nc];
    for (int i = 0; i < nc; i++) {
      coveredColor[i] = coverColor[i].darker();
    }
  }

  WAPanel(WAModel wa) {
    this.wa = wa;
  }

  int ph, pw;
  int cL;
  int x1, y1;

  private void calcParams() {
    ph = wa.array.length;
    pw = wa.array[0].length;

    int H = getHeight(), W = getWidth();
    cL = Math.min((H - 2 * margin) / ph, (W - 2 * margin) / pw);

    y1 = (H - cL * ph) / 2;
    x1 = (W - cL * pw) / 2;
  }

  protected void paintComponent(Graphics g) {
    if (wa.isNull()) {
      return;
    }
    calcParams();
    super.paintComponent(g);
    drawCell(g);
    drawGrid(g);
  }

  private void drawGrid(Graphics g) {
    g.setColor(Color.black);

    int y2 = y1 + cL * ph;
    int x2 = x1 + cL * pw;
    for (int i = 0; i <= ph; i++) {
      int y = y1 + cL * i;
      g.drawLine(x1, y, x2, y);
    }
    for (int i = 0; i <= pw; i++) {
      int x = x1 + cL * i;
      g.drawLine(x, y1, x, y2);
    }
  }

  private void drawCell(Graphics g) {
    for (int i = 0; i < ph; i++) {
      for (int j = 0; j < pw; j++) {
        int p = wa.array[i][j];
        if (p > 0) {
          g.setColor(coverColor[p - 1]);
        } else if (p < 0) {
          g.setColor(coveredColor[-p - 1]);
        } else {
          g.setColor(Color.white);
        }

        int y = y1 + cL * i;
        int x = x1 + cL * j;
        g.fillRect(x + 1, y + 1, cL - 1, cL - 1);
      }
    }
  }
}
