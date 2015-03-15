package ui;

import java.awt.*;

import javax.swing.*;

public class DialogShower {

  public static void error(Component parent, String msg) {
    if (msg == null) {
      msg = "something is wrong.";
    }
    JOptionPane.showMessageDialog(parent, msg);
  }
}
