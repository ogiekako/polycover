package util;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

import main.Poly;
import main.PolyArray;

public class FileUtil {

  // Return relative path.
  public static List<String> allFilesUnder(File dir) {
    File[] files = dir.listFiles();
    if (files == null) {
      return Collections.emptyList();
    }
    List<String> res = new ArrayList<String>();
    for (File file : files) {
      if (!file.exists()) {
        continue;
      }
      if (file.isDirectory()) {
        res.addAll(allFilesUnder(file));
      } else if (file.isFile()) {
        res.add(file.getPath());
      }
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
        poly = PolyArray.load(new Scanner(new File(p)));
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
}
