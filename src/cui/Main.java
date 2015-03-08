package cui;

import main.Judge;
import main.NoCellException;
import main.PolyArray;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws FileNotFoundException, NoCellException {
        String problemFileName = args[0];
        String candFileName = args[1];
        Scanner problemIn = new Scanner(new File(problemFileName));
        Scanner candIn = new Scanner(new File(candFileName));
        PolyArray problem = PolyArray.load(problemIn);
        PolyArray cand = PolyArray.load(candIn);
        boolean ok = Judge.newBuilder(problem, cand).build().judge() == null;
        System.out.println(ok ? "OK" : "NG");
    }
}
