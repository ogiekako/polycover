package main;

import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.util.List;
import java.util.Scanner;

import util.Debug;
import util.FileUtil;

public class JudgeTestLong {

  @Test
  public void testT() throws Exception {
    Stopwatch latencyMetric = new Stopwatch();
    String probPath = "problem/hexomino/T.yes";
    String ansPath = "ans/hexomino/T.ans";
    Scanner probIn = new Scanner(new File(probPath));
    Scanner ansIn = new Scanner(new File(ansPath));
    PolyArray prob = PolyArray.load(probIn);
    PolyArray ans = PolyArray.load(ansIn);
    int[][] result =
        Judge.newBuilder(prob, ans).setLatencyMetric(latencyMetric).build().judge();
    Assert.assertNull(Debug.toString(result), result);

    System.err.println(latencyMetric.summary());
  }

  @Test
  public void testYes() throws Exception {
    Stopwatch latencyMetric = new Stopwatch();
    List<String> problems = FileUtil.allFilesUnder(new File("problem"));
    for (String probPath : problems) {
      if (probPath.endsWith(".yes")) {
        String ansPath = probPath.replaceAll("^problem/", "ans/").replaceAll("\\.yes$", ".ans");
        if (!new File(ansPath).exists()) {
          ansPath = probPath.replaceAll("^problem/", "ans/").replaceAll("\\.yes$", ".dup");
        }
        System.err.printf("%s %s\n", probPath, ansPath);
        Scanner probIn = new Scanner(new File(probPath));
        Scanner ansIn = new Scanner(new File(ansPath));
        PolyArray prob = PolyArray.load(probIn);
        PolyArray ans = PolyArray.load(ansIn);
        int[][] result =
            Judge.newBuilder(prob, ans).setLatencyMetric(latencyMetric).build().judge();
        Assert.assertNull(Debug.toString(result), result);
        System.err.println(latencyMetric.summary());
      }
    }
  }

  @Test
  public void testNo() throws Exception {
    List<String> testAnsPaths = FileUtil.allFilesUnder(new File("ans/hexomino"));

    Stopwatch latencyMetric = new Stopwatch();
    List<String> problems = FileUtil.allFilesUnder(new File("problem/hexomino"));
    for (String probPath : problems) {
      if (probPath.endsWith(".no")) {
        for (String ansPath : testAnsPaths) {
          System.err.printf("%s %s\n", probPath, ansPath);
          Scanner probIn = new Scanner(new File(probPath));
          Scanner ansIn = new Scanner(new File(ansPath));
          PolyArray prob = PolyArray.load(probIn);
          PolyArray ans = PolyArray.load(ansIn);
          int[][] result =
              Judge.newBuilder(prob, ans).setLatencyMetric(latencyMetric).build().judge();
          Assert.assertNotNull(result);

          System.err.println(latencyMetric.summary());
        }
      }
    }
  }

}
