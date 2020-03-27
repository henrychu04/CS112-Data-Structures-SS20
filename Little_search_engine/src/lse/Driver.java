package lse;

import java.io.*;
import java.util.*;

public class Driver {

    static Scanner stdin = new Scanner(System.in);
    public static void main(String[] args)
    throws FileNotFoundException {
        LittleSearchEngine lse = new LittleSearchEngine();
        System.out.print("Enter docsFile name: ");
        String docsFile = stdin.nextLine();

        System.out.print("Enter noiseWordsFile name: ");
        String noiseWordsFile = stdin.nextLine();

        lse.makeIndex(docsFile, noiseWordsFile);
    }
}