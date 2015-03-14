package main;

import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

import util.Debug;

public class JudgeTestLong {


  // TODO: the following two tests takes too long time. Speed up judge.
  //    computeAllStates: 0.250000s.
  //    updatePossibleStatePairs: 21.039000s.
  //    solving: 3.274983m.
  @Test
  public void testYes() throws Exception {// 4m
    Stopwatch latencyMetric = new Stopwatch();
    List<String> problems = allFilesUnder(new File("problem"));
    for (String probPath : problems) {
      if (probPath.endsWith(".yes")) {
        String ansPath = probPath.replaceAll("^problem/", "ans/").replaceAll("\\.yes$", ".ans");
        Debug.debug(probPath, ansPath);
        Scanner probIn = new Scanner(new File(probPath));
        Scanner ansIn = new Scanner(new File(ansPath));
        PolyArray prob = PolyArray.load(probIn);
        PolyArray ans = PolyArray.load(ansIn);
        int[][]
            result =
            Judge.newBuilder(prob, ans).setLatencyMetric(latencyMetric).build().judge();
        Assert.assertNull(Debug.toString(result), result);

        System.err.println(latencyMetric.summary());
      }
    }
  }

  /*
      computeAllStates: 0.881000s.
      solving: 48.107000s.
      updatePossibleStatePairs: 3.184600m.
   */
  @Test
  public void testNo() throws Exception {// 42m
    // TODO: Add all *.ans files. Currenty the change will make this test too slow.
    String[] testAnsPaths = {"ans/hexomino/8.ans", "ans/hexomino/C.ans"};

    Stopwatch latencyMetric = new Stopwatch();
    List<String> problems = allFilesUnder(new File("problem"));
    for (String probPath : problems) {
      if (probPath.endsWith(".no")) {
        for (String ansPath : testAnsPaths) {
          Debug.debug(probPath, ansPath);
          Scanner probIn = new Scanner(new File(probPath));
          Scanner ansIn = new Scanner(new File(ansPath));
          PolyArray prob = PolyArray.load(probIn);
          PolyArray ans = PolyArray.load(ansIn);
          int[][]
              result =
              Judge.newBuilder(prob, ans).setLatencyMetric(latencyMetric).build().judge();
          Assert.assertNotNull(result);

          System.err.println(latencyMetric.summary());
        }
      }
    }
  }

  // Return absolutePaths.
  List<String> allFilesUnder(File dir) {
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