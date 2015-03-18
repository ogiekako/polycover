package cui;

import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class JudgerTest {

  @Test
  public void testMain_OK() throws Exception {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    System.setOut(new PrintStream(out));
    Judger.main(new String[]{"problem/pentomino/X.yes", "ans/pentomino/X.ans"});
    Assert.assertEquals("OK\n", out.toString());
  }

  @Test
  public void testMain_NG() throws Exception {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    System.setOut(new PrintStream(out));
    Judger.main(new String[]{"problem/pentomino/I.yes", "ans/pentomino/X.ans"});
    Assert.assertEquals("NG\n", out.toString());
  }
}
