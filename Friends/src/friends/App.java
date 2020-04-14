package friends;

import java.io.File;
import java.util.Scanner;
import java.io.IOException;
import java.util.ArrayList;

public class App {

    static Scanner stdin = new Scanner(System.in);

    public static void main(String[] args)
    throws IOException {

        Graph g = null;
        
        while (g == null) {
            try {
                System.out.print("Enter text file: ");
                String fileName = stdin.nextLine();
                Scanner sc = new Scanner(new File(fileName));
                g = new Graph(sc);
            } catch (IOException e) {
                System.out.println("File not found");
            }
        }

        while (true) {
            String input = "";
            
            System.out.print("Enter 1 for Shortest Path, 2 for Clique, 3 for Connectors, or hit return to quit: ");

            while (true) {
                input = stdin.nextLine();

                if (input.length() == 0) {
                    break;
                } else if (input.equals("1") || input.equals("2") || input.equals("3")) {
                    break;
                } else {
                    System.out.print("Enter 1, 2, or 3: ");
                }
            }

            if (input.length() == 0) {
                break;
            } else {
                int inputInt = Integer.parseInt(input);
            
                switch (inputInt) {
                    case 1:
                        System.out.println("\t -- Shortest Path --");

                        ArrayList<String> personList = new ArrayList<String>();

                        while (personList.size() == 0) {
                            String p1 = "", p2 = "";

                            System.out.print("Enter person one: ");
                            p1 = stdin.nextLine();
                    
                            System.out.print("Enter person two: ");
                            p2 = stdin.nextLine();
                    
                            personList = Friends.shortestChain(g, p1, p2);
                        }
                
                        System.out.print("personList: ");

                        for (int i = 0; i < personList.size(); i++) {
                            if (i == personList.size() - 1) {
                                System.out.println(personList.get(i));
                            } else {
                                System.out.print(personList.get(i) + " --> ");
                            }
                        }

                        break;
                    case 2:
                        System.out.println("\t -- Clique --");

                        String school = "";
                        ArrayList<ArrayList<String>> cliques = new ArrayList<ArrayList<String>>();

                        while (cliques.size() == 0) {
                            System.out.print("Enter school name: ");
                            school = stdin.nextLine();
                            cliques = Friends.cliques(g, school);
                        }

                        System.out.println(school + " cliques are " + cliques);
                        break;
                    case 3:
                        System.out.println("\t -- Connectors --");
                        ArrayList<String> connectors = Friends.connectors(g);

                        if (connectors.size() == 0) {
                            System.out.println("No connectors in graph");
                        } else {
                            System.out.println("Connectors are " + connectors);
                        }
                        break;
                    default:
                        break;
                }
            }
        }
    }
}