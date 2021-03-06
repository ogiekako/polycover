package util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

import main.Poly;

public class FileUtil {

  // Return relative path.
  public static List<String> allFilesUnder(File fileOrDir) {
    if (!fileOrDir.isDirectory()) {
      return Collections.singletonList(fileOrDir.getPath());
    }
    File[] files = fileOrDir.listFiles();
    if (files == null) {
      return Collections.emptyList();
    }
    List<String> res = new ArrayList<String>();
    for (File file : files) {
      if (!file.exists()) {
        continue;
      }
      res.addAll(allFilesUnder(file));
    }
    return res;
  }

  public static List<String> allFilesUnder(File dir, String suffix) {
    List<String> res = new ArrayList<String>();
    for (String s : allFilesUnder(dir)) {
      if (s.endsWith(suffix)) {
        res.add(s);
      }
    }
    return res;
  }

  public static List<Poly> allPolysUnder(File dir, String suffix) {
    List<String> paths = allFilesUnder(dir, suffix);
    List<Poly> ps = new ArrayList<Poly>();
    for (String p : paths) {
      Poly poly;
      try {
        poly = Poly.load(new Scanner(new File(p)));
      } catch (FileNotFoundException e) {
        throw new IllegalArgumentException(p);
      }
      poly.setFilePath(p);
      ps.add(poly);
    }
    return ps;
  }

  public static List<String> allFilesUnder(File... dirs) {
    List<String> res = new ArrayList<String>();
    for (File dir : dirs) {
      res.addAll(allFilesUnder(dir));
    }
    return res;
  }

  public static void savePoly(Poly poly, File file) throws FileNotFoundException {
    PrintWriter pw = new PrintWriter(file);
    pw.println(poly.getHeight() + " " + poly.getWidth());
    for (int i = 0; i < poly.getHeight(); i++) {
      String s = "";
      for (int j = 0; j < poly.getWidth(); j++) {
        s += poly.get(i, j) ? "#" : ".";
      }
      pw.println(s);
    }
    pw.flush();
  }
}
