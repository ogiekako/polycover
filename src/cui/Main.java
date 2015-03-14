package cui;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import main.Judge;
import main.NoCellException;
import main.PolyArray;

public class Main {

  public static void main(String[] args) throws FileNotFoundException, NoCellException {
    int p = 0;
    int maxDepth = Integer.MAX_VALUE / 2;
    while (args[p].startsWith("--")) {
      String opt = args[p++];
      if (opt.equals("--maxdepth")) {
        maxDepth = Integer.valueOf(args[p++]);
      }
    }
    String problemFileName = args[p++];
    String candFileName = args[p++];
    Scanner problemIn = new Scanner(new File(problemFileName));
    Scanner candIn = new Scanner(new File(candFileName));
    PolyArray problem = PolyArray.load(problemIn);
    PolyArray cand = PolyArray.load(candIn);
    boolean ok =
        Judge.newBuilder(problem, cand).setEnabledCandDepth(maxDepth).build().judge() == null;
    System.out.println(ok ? "OK" : "NG");
  }
}
