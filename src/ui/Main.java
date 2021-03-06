package ui;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import javax.swing.*;

import main.Prop;
import ui.view.AppFrame;

public class Main {

  static Logger logger = Logger.getLogger(Main.class.getName());

  public static void main(String[] args) {
    Cont cont = new Cont();
    JFrame appFrame = new AppFrame(cont);
    new WAFrame(cont, appFrame);

    if (Prop.get("lastpath") != null) {
      File file = new File(Prop.get("lastpath"));
      if (file.exists()) {
        try {
          cont.load(file);
        } catch (IOException e) {
          logger.info("File: " + file.getPath() + "\n" + e);
        }
      }
    }
    String lastprobfile = Prop.get("lastprobfile");
    if (lastprobfile != null) {
      try {
        cont.load(new File(lastprobfile));
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    if (args.length > 0) {
      try {
        Prop.set("lastprobfile", args[0]);
        cont.load(new File(args[0]));
      } catch (IOException e) {
        DialogShower.message(appFrame, e.getMessage());
      }
    }
    if (args.length > 1) {
      try {
        cont.load(new File(args[1]));
      } catch (IOException e) {
        DialogShower.message(appFrame, e.getMessage());
      }

    }
  }
}
