package ui;

import java.io.File;

import javax.swing.*;

import main.Prop;

public class Main {

  public static void main(String[] args) {
    Cont cont = new Cont();
    JFrame appFrame = new AppFrame(cont);
    new WAFrame(cont, appFrame);

    if (Prop.get("lastpath") != null) {
      File file = new File(Prop.get("lastpath"));
      if (file.exists()) {
        cont.load(appFrame, file);
      }
    }
    if (args.length > 0) {
      cont.load(appFrame, new File(args[0]));
    }
    if (args.length > 1) {
      cont.load(appFrame, new File(args[1]));
    }
  }
}
