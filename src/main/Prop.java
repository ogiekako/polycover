package main;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Logger;

public class Prop {

  static Logger logger = Logger.getLogger(Prop.class.getName());
  static String path = "polycover.properties";
  static Properties prop = new Properties();

  static {
    try {
      prop.load(new FileInputStream(path));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static String get(String name) {
    Object o = prop.get(name);
    if (o == null) {
      return null;
    }
    return o.toString();
  }

  public static void set(String name, String value) {
    prop.setProperty(name, value);
    try {
      prop.store(new FileOutputStream(path), name + " = " + value);
    } catch (IOException e) {
      logger.warning(e.toString());
    }
  }
}
