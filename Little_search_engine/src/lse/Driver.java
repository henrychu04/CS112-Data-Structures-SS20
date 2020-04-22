package lse;

import java.io.*;
import java.util.*;

public class Driver {

    static Scanner stdin = new Scanner(System.in);
    public static void main(String[] args)
    throws FileNotFoundException {
        LittleSearchEngine lse = new LittleSearchEngine();
        String docsFile = "";

        while (docsFile.length() == 0) {
            try {
                System.out.print("Enter docsFile name or hit return to quit: ");
                docsFile = stdin.nextLine();

                if (docsFile.length() == 0) {
                    return;
                }

                Scanner sc = new Scanner(new File(docsFile));
            } catch (FileNotFoundException e) {
                docsFile = "";
                System.out.println("File not found");
            }
        }

        String noiseWordsFile = "";

        while (noiseWordsFile.length() == 0) {
            try {
                System.out.print("Enter noiseWordsFile name or hit return to quit: ");
                noiseWordsFile = stdin.nextLine();

                if (noiseWordsFile.length() == 0) {
                    return;
                }

                Scanner sc = new Scanner(new File(noiseWordsFile));
            } catch (FileNotFoundException e) {
                noiseWordsFile = "";
                System.out.println("File not found");
            }
        }
        
        lse.makeIndex(docsFile, noiseWordsFile);

        String kw1 = "", kw2 = "";

        while (true) {
            System.out.print("Enter first word to search for or hit return to quit: ");
            kw1 = stdin.nextLine();

            if (kw1.length() == 0) {
                return;
            }

            System.out.print("Enter second word to search for or hit return to quit: ");
            kw2 = stdin.nextLine();

            if (kw2.length() == 0) {
                return;
            }

            ArrayList<String> search = lse.top5search(kw1, kw2);

            if (search.size() != 0) {
                System.out.println("Result is " + search);
            }
        }
        
    }
}