package ui.view;

import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.*;

import main.Cell;
import ui.Cont;
import ui.Model;

public class PolyPanel extends JPanel implements View {

  public void update() {
    getParent().getParent().repaint();
  }

  private static final int margin = 5;
  private static final Color cellColor = Color.cyan;
  private int selectX = 0, selectY = 0;

  private final Model poly;
  private final Cont cont;

  PolyPanel(Model _poly, final Cont cont) {
    this.cont = cont;
    cont.addView(this);
    poly = _poly;
    MouseAdapter mouseAdaptor = new MouseAdapter() {
      @Override
      public void mousePressed(MouseEvent e) {
        if (cont.isRunning()) {
          return;
        }
        if (poly.isNull()) {
          return;
        }
        int mx = e.getX(), my = e.getY();
        Cell c = getCell(mx, my);
        if (c != null) {
          cont.flip(poly, c.x, c.y);
        }
      }
    };
    addMouseListener(mouseAdaptor);
    addKeyListener(new KeyAdapter() {

      @Override
      public void keyTyped(KeyEvent e) {
        if (cont.isRunning()) {
          return;
        }
        if (poly.isNull()) {
          return;
        }
        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
          cont.flip(poly, selectX, selectY);
        } else if (e.getKeyCode() == KeyEvent.VK_UP && selectX > 0) {
          selectX--;
        } else if (e.getKeyCode() == KeyEvent.VK_LEFT && selectY > 0) {
          selectY--;
        } else if (e.getKeyCode() == KeyEvent.VK_RIGHT && selectY < poly.getWidth() - 1) {
          selectY++;
        } else if (e.getKeyCode() == KeyEvent.VK_DOWN && selectX < poly.getHeight() - 1) {
          selectX++;
        } else if (e.getKeyCode() == KeyEvent.VK_Z) {
          undo();
        }
        cont.updateView();
      }

      @Override
      public void keyPressed(KeyEvent e) {
        keyTyped(e);
      }
    });
    setFocusable(true);
    requestFocusInWindow();
  }

  private void undo() {
    cont.undo();
  }

  private Cell getCell(int mx, int my) {
    calcParams();

    int cx = (my - y1) / cL;
    int cy = (mx - x1) / cL;
    if (0 <= cx && cx < ph && 0 <= cy && cy < pw) {
      return new Cell(cx, cy);
    }
    return null;
  }

  int ph, pw;
  int cL;
  int x1, y1;

  private void calcParams() {
    ph = poly.getHeight();
    pw = poly.getWidth();

    int H = getHeight(), W = getWidth();
    cL = Math.min((H - 2 * margin) / ph, (W - 2 * margin) / pw);

    y1 = (H - cL * ph) / 2;
    x1 = (W - cL * pw) / 2;

  }

  protected void paintComponent(Graphics g) {
    if (poly.isNull()) {
      return;
    }

    calcParams();
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
        boolean hasCell = poly.get(i, j);
        boolean isSelected = i == selectX && j == selectY;
        Color color;
        if (hasCell && !isSelected) {
          color = cellColor;
        } else if (!hasCell && !isSelected) {
          color = Color.WHITE;
        } else if (hasCell) {
          color = Color.BLUE;
        } else {
          color = Color.LIGHT_GRAY;
        }

        g.setColor(color);

        int y = y1 + cL * i;
        int x = x1 + cL * j;
        g.fillRect(x + 1, y + 1, cL - 1, cL - 1);
      }
    }
  }
}
