package main;

import junit.framework.Assert;

import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import util.FileUtil;

public class FileValidityTest {

  @Test
  public void testAnsDup() throws Exception {
    List<String> fs = FileUtil.allFilesUnder(new File("ans"));
    class SP {

      String f;
      boolean dup;
      Poly poly;
    }
    List<SP> sps = new ArrayList<SP>();
    for (String f : fs) {
      SP sp = new SP();
      sp.f = f;
      Assert.assertTrue(f, f.endsWith(".dup") || f.endsWith(".ans"));
      sp.dup = f.endsWith(".dup");
      sp.poly = PolyArray.load(new Scanner(new File(f)));
      sps.add(sp);
    }
    for (SP u : sps) {
      for (SP v : sps) {
        Assert.assertFalse(u.f, u != v && u.f.replaceAll("\\..*", "").equals(v.f.replaceAll("\\..*", "")));
      }

      if (u.dup) {
        boolean ok = false;
        for (SP v : sps) {
          if (!v.dup && v.poly.equals(u.poly)) {
            ok = true;
            break;
          }
        }
        Assert.assertTrue(u.f, ok);
      } else {
        for (SP v : sps) {
          if (u != v && !v.dup && v.poly.equals(u.poly)) {
            Assert.fail(String.format("%s and %s are same.", u.f, v.f));
          }
        }
      }
    }
  }
}
