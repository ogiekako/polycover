package main;

import junit.framework.Assert;
import org.junit.Test;


public class JudgeTest{
	private boolean[][] s2b(String[] ss) {
		boolean[][] res = new boolean[ss.length][ss[0].length()];
		for(int i=0;i<res.length;i++)for(int j=0;j<res[0].length;j++)res[i][j] = ss[i].charAt(j)=='#';
		return res;
	}

	@Test public void testJudge1() throws NoCellException {
		boolean[][] ac =s2b(new String[]{
			".#.#.",
			"##.##",
			"#...#",
			"#####"
		});
		PolyArray cover = new PolyArray(ac);
		boolean[][] acd = s2b(new String[]{
			"###",
			"###",
			"###"
		});
		PolyArray covered = new PolyArray(acd);
		int[][] res = Judge.judge(covered,cover);
		
		Assert.assertEquals(res,null);
	}
	@Test public void testJudge2()  throws NoCellException{
		boolean[][] ac =s2b(new String[]{
			".#...",
			"##.##",
			"#...#",
			"#####"
		});
		PolyArray cover = new PolyArray(ac);
		boolean[][] acd = s2b(new String[]{
			"###",
			"###",
			"###"
		});
		PolyArray covered = new PolyArray(acd);
		int[][] res = Judge.judge(covered,cover);
		Assert.assertTrue(res!=null);
	}
	@Test public void testJudge2_depth1()  throws NoCellException{
		boolean[][] ac =s2b(new String[]{
			".....",
			"##.##",
			"#...#",
			"#####"
		});
		PolyArray cover = new PolyArray(ac);
		boolean[][] acd = s2b(new String[]{
			"###",
			"###",
			"###"
		});
		PolyArray covered = new PolyArray(acd);
		int[][] res = Judge.judge(covered,cover,2,1);
		Assert.assertTrue(res==null);
	}
	@Test public void testJudge2_depth2()  throws NoCellException{
		boolean[][] ac =s2b(new String[]{
			".....",
			"##.##",
			"#...#",
			"#####"
		});
		PolyArray cover = new PolyArray(ac);
		boolean[][] acd = s2b(new String[]{
			"###",
			"###",
			"###"
		});
		PolyArray covered = new PolyArray(acd);
		int[][] res = Judge.judge(covered,cover,2,2);
		Assert.assertTrue(res!=null);
	}
	@Test public void testJudge2_num1() throws NoCellException{
		boolean[][] ac =s2b(new String[]{
			".....",
			"##.##",
			"#...#",
			"#####"
		});
		PolyArray cover = new PolyArray(ac);
		boolean[][] acd = s2b(new String[]{
			"###",
			"###",
			"###"
		});
		PolyArray covered = new PolyArray(acd);
		int[][] res = Judge.judge(covered,cover,1,Integer.MAX_VALUE);
		Assert.assertTrue(res==null);
	}
	
	@Test public void testJudge3()  throws NoCellException{
		boolean[][] ac = new boolean[][] {
			{true}
		};
		PolyArray cover = new PolyArray(ac);
		boolean[][] acd = new boolean[][] {
			{true}
		};
		PolyArray covered = new PolyArray(acd);
		int[][] res = Judge.judge(covered,cover);
		Assert.assertTrue(res!=null);
	}

	@Test public void testJudge4()  throws NoCellException{
		boolean[][] ac = new boolean[][] {
			{true}
		};
		PolyArray cover = new PolyArray(ac);
		boolean[][] acd = new boolean[][] {
			{true,true}
		};
		PolyArray covered = new PolyArray(acd);
		int[][] res = Judge.judge(covered,cover);
		Assert.assertTrue(res!=null);
	}
	@Test public void testJudge5() throws NoCellException {
		boolean[][] ac =s2b(new String[]{
			"##",
			"##"
		});
		PolyArray cover = new PolyArray(ac);
		boolean[][] acd = s2b(new String[]{
			"###",
			"###",
			"###"
		});
		PolyArray covered = new PolyArray(acd);
		int[][] res = Judge.judge(covered,cover);
		
		Assert.assertTrue(res!=null);
	}
}
