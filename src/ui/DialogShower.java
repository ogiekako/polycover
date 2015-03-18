package ui;

import java.awt.*;

import javax.swing.*;

public class DialogShower {

  public static void message(Component parent, String msg) {
    if (msg == null) {
      msg = "something is wrong.";
    }
    JOptionPane.showMessageDialog(parent, msg);
  }
}
