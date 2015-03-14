package util;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FileUtil {

  // Return absolutePaths.
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
}
