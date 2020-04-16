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

        System.out.print("Enter first word to search for: ");
        String kw1 = stdin.nextLine();

        System.out.print("Enter second word to search for: ");
        String kw2 = stdin.nextLine();

        ArrayList<String> search = lse.top5search(kw1, kw2);

        if (search.size() != 0) {
            System.out.println("Result is " + search);
        }
    }
}