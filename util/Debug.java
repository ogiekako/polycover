package util;

import java.util.Arrays;

public class Debug{
	public static void debug(Object...os) {
		System.err.println(Arrays.deepToString(os));
	}
}
