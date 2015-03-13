package util;

import java.util.Arrays;

public class Debug {

  public static void debug(Object... os) {
    System.err.println(Arrays.deepToString(os));
  }

  public static String toString(Object... os) {
    return Arrays.deepToString(os);
  }
}
