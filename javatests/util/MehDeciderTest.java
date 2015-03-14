package util;

import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.util.List;

public class MehDeciderTest {

  @Test
  public void testIsMeh() throws Exception {
    List<String> problems = FileUtil.allFilesUnder(new File("problem/7"));
    for (String p : problems) {
      Debug.debug(p);
      boolean meh = MehDecider.isMeh(p, "problem");
      if (p.endsWith("meh")) {
        Assert.assertTrue(meh);
      } else if (p.endsWith("yes")) {
        Assert.assertTrue(meh);
      } else if (p.endsWith("no")) {
        Assert.assertFalse(meh);
      } else {
        throw new AssertionError();
      }
    }
  }
}
