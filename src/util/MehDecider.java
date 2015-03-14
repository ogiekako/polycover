package util;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Scanner;

import main.PolyAnalyzer;
import main.PolyArray;

public class MehDecider {

  /*
  Usage: java util.MehDecider problem/some.no problem
  output YES if under problem dir there is a .yes poly which is contained in some.no .
   */
  public static void main(String[] args) throws FileNotFoundException {
    if (args.length < 2) {
      System.err.println("Usage: java -cp bin util.MehDecider problem/some.no problem");
    }
    String path = args[0];
    String pDir = args[1];
    boolean meh = isMeh(path, pDir);
    System.out.println(meh ? "YES" : "NO");
  }

  public static boolean isMeh(String path, String probDir) throws FileNotFoundException {
    PolyArray target = PolyArray.load(new Scanner(new File(path)));
    PolyAnalyzer analyzer = PolyAnalyzer.of(target);

    List<String> ps = FileUtil.allFilesUnder(new File(probDir));
    boolean meh = false;
    for (String p : ps) {
      if (!p.endsWith(".yes")) {
        continue;
      }
      PolyArray yesPoly = PolyArray.load(new Scanner(new File(p)));
      if (analyzer.contains(yesPoly)) {
        meh = true;
        break;
      }
    }
    return meh;
  }
}
