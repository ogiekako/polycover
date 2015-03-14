package main;

import org.junit.Assert;
import org.junit.Test;

import util.Debug;

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
}