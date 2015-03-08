package main;

import org.junit.Assert;
import org.junit.Test;
import util.Debug;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

public class JudgeTest {
    private boolean[][] s2b(String[] ss) {
        boolean[][] res = new boolean[ss.length][ss[0].length()];
        for (int i = 0; i < res.length; i++) for (int j = 0; j < res[0].length; j++) res[i][j] = ss[i].charAt(j) == '#';
        return res;
    }

    @Test
    public void testJudge1() throws NoCellException {
        boolean[][] ac = s2b(new String[]{
                ".#.#.",
                "##.##",
                "#...#",
                "#####"
        });
        PolyArray candidate = new PolyArray(ac);
        boolean[][] acd = s2b(new String[]{
                "###",
                "###",
                "###"
        });
        PolyArray problem = new PolyArray(acd);
        int[][] res = Judge.newBuilder(problem, candidate).build().judge();

        Assert.assertNull(res);
    }

    @Test
    public void testJudge2() throws NoCellException {
        boolean[][] ac = s2b(new String[]{
                ".#...",
                "##.##",
                "#...#",
                "#####"
        });
        PolyArray candidate = new PolyArray(ac);
        boolean[][] acd = s2b(new String[]{
                "###",
                "###",
                "###"
        });
        PolyArray problem = new PolyArray(acd);
        int[][] res = Judge.newBuilder(problem, candidate).build().judge();
        Assert.assertNotNull(res);
    }

    @Test
    public void testJudge2_depth1() throws NoCellException {
        boolean[][] ac = s2b(new String[]{
                ".....",
                "##.##",
                "#...#",
                "#####"
        });
        PolyArray candidate = new PolyArray(ac);
        boolean[][] acd = s2b(new String[]{
                "###",
                "###",
                "###"
        });
        PolyArray problem = new PolyArray(acd);
        int[][] res = Judge.newBuilder(problem, candidate).setNumCandidates(2).setEnabledCandDepth(1).build().judge();
        Assert.assertTrue(res == null);
    }

    @Test
    public void testJudge2_depth2() throws NoCellException {
        boolean[][] ac = s2b(new String[]{
                ".....",
                "##.##",
                "#...#",
                "#####"
        });
        PolyArray candidate = new PolyArray(ac);
        boolean[][] acd = s2b(new String[]{
                "###",
                "###",
                "###"
        });
        PolyArray problem = new PolyArray(acd);
        int[][] res = Judge.newBuilder(problem, candidate).setNumCandidates(2).setEnabledCandDepth(2).build().judge();
        Assert.assertNotNull(res);
    }

    @Test
    public void testJudge2_num1() throws NoCellException {
        boolean[][] ac = s2b(new String[]{
                ".....",
                "##.##",
                "#...#",
                "#####"
        });
        PolyArray candidate = new PolyArray(ac);
        boolean[][] acd = s2b(new String[]{
                "###",
                "###",
                "###"
        });
        PolyArray problem = new PolyArray(acd);
        int[][] res = Judge.newBuilder(problem, candidate).setNumCandidates(1).build().judge();
        Assert.assertTrue(Debug.toString(res), res == null);
    }

    @Test
    public void testJudge3() throws NoCellException {
        boolean[][] ac = new boolean[][]{
                {true}
        };
        PolyArray candidate = new PolyArray(ac);
        boolean[][] acd = new boolean[][]{
                {true}
        };
        PolyArray problem = new PolyArray(acd);
        int[][] res = Judge.newBuilder(problem, candidate).build().judge();
        Assert.assertTrue(res != null);
    }

    @Test
    public void testJudge4() throws NoCellException {
        boolean[][] ac = new boolean[][]{
                {true}
        };
        PolyArray candidate = new PolyArray(ac);
        boolean[][] acd = new boolean[][]{
                {true, true}
        };
        PolyArray problem = new PolyArray(acd);
        int[][] res = Judge.newBuilder(problem, candidate).build().judge();
        Assert.assertTrue(res != null);
    }

    @Test
    public void testJudge5() throws NoCellException {
        boolean[][] ac = s2b(new String[]{
                "##",
                "##"
        });
        PolyArray candidate = new PolyArray(ac);
        boolean[][] acd = s2b(new String[]{
                "###",
                "###",
                "###"
        });
        PolyArray problem = new PolyArray(acd);
        int[][] res = new Judge(problem, candidate).judge();

        Assert.assertTrue(res != null);
    }

    // TODO: the following two tests takes too long time. Speed up judge.
    @Test
    public void testYes() throws Exception {// 20m
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
                int[][] result = Judge.newBuilder(prob, ans).setLatencyMetric(latencyMetric).build().judge();
                Assert.assertNull(Debug.toString(result), result);

                System.err.println(latencyMetric.summary());
            }
        }
    }

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
                    int[][] result = Judge.newBuilder(prob, ans).setLatencyMetric(latencyMetric).build().judge();
                    Assert.assertNotNull(result);

                    System.err.println(latencyMetric.summary());
                }
            }
        }
    }

    // Return absolutePaths.
    List<String> allFilesUnder(File dir) {
        File[] files = dir.listFiles();
        if (files == null) return Collections.emptyList();
        List<String> res = new ArrayList<String>();
        for (File file : files) {
            if (!file.exists()) continue;
            if (file.isDirectory()) {
                res.addAll(allFilesUnder(file));
            } else if (file.isFile()) {
                res.add(file.getPath());
            }
        }
        return res;
    }
}