package cui;

import main.Judge;
import main.NoCellException;
import main.PolyArray;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws FileNotFoundException, NoCellException {
        String coveredFileName = args[0];
        String coverFileName = args[1];
        Scanner coveredIn = new Scanner(new File(coveredFileName));
        Scanner coverIn = new Scanner(new File(coverFileName));
        PolyArray covered = PolyArray.load(coveredIn);
        PolyArray cover = PolyArray.load(coverIn);
        boolean ok = Judge.judge(covered, cover) == null;
        System.out.println(ok ? "OK" : "NG");
    }
}
