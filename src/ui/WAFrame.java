package ui;

import javax.swing.*;

public class WAFrame extends JDialog implements AbstView {

  public void update() {
    if (cont.wa.isNull()) {
      setVisible(false);
    } else {
      setVisible(true);
    }
    repaint();
  }

  private final Cont cont;

  public WAFrame(Cont cont, JFrame parent) {
    this.cont = cont;
    this.cont.addView(this);

    this.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
    this.setVisible(false);
    this.setSize(400, 400);
    WAPanel panel = new WAPanel(this.cont.wa);
    this.add(panel);
    this.setFocusableWindowState(false);
    this.setResizable(true);
  }
}
