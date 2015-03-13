package ui;

import javax.swing.*;

public class Main {

  public static void main(String[] args) {
    Cont cont = new Cont();
    JFrame appFrame = new AppFrame(cont);
    new WAFrame(cont, appFrame);
  }
}
