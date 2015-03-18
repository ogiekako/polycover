package util;

import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MehDeciderTest {

  @Test
  public void testIsMeh() throws Exception {
    List<String> problems = FileUtil.allFilesUnder(new File("problem"));
    List<String> badPaths = new ArrayList<String>();
    for (String p : problems) {
      MehDecider.Type type = MehDecider.decide(p, "problem");
      if (p.endsWith(".meh")) {
        if (type != MehDecider.Type.Meh) {
          badPaths.add(p);
        }
      } else if (p.endsWith(".yes")) {
        if (type != MehDecider.Type.Yes) {
          badPaths.add(p);
        }
      } else if (p.endsWith(".no")) {
        if (type != MehDecider.Type.No) {
          badPaths.add(p);
        }
      } else {
        throw new AssertionError();
      }
    }
    if (!badPaths.isEmpty()) {
      throw new AssertionError(badPaths);
    }
  }
}
