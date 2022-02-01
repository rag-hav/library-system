
import java.util.Scanner;

import static java.lang.Integer.parseInt;

public class Utils {
    static Scanner sc = new Scanner(System.in);

    public static int menu(String[] choices, String header) {

        System.out.println("\n" + header + ": ");
        System.out.println("\t0. Exit");
        for (int i = 0; i < choices.length; i++) {
            System.out.printf("\t%d. %s\n", i + 1, choices[i]);
        }
        int choice;
        while (true) {
            System.out.print("\nPlease Enter the number of your choice: ");
            choice = parseInt(sc.nextLine());

            if (choice >= 0 && choice <= choices.length)
                break;
            else
                System.out.println("Invalid Choice!!");
        }
        return choice;
    }

    public static String prompt(String question) {
        System.out.print("Enter " + question + ": ");
        return sc.nextLine();
    }

}