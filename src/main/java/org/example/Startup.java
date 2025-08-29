package org.example;

import java.util.Scanner;

public final class Startup {
    public static void main(String[] args) {
        printWelcomeArt();
        Scanner in = new Scanner(System.in);

        System.out.println("Select application mode:");
        System.out.println("  0: GUI");
        System.out.println("  1: CLI (console-based Tic-Tac-Toe)");

        int choice = -1;
        while (choice != 0 && choice != 1) {
            System.out.print("Enter 0 or 1: ");
            if (in.hasNextInt()) {
                choice = in.nextInt();
            } else {
                in.next(); // consume invalid token
            }
        }

        if (choice == 0) {
            System.out.println("\nLaunching GUI mode...\n");
            GUIApp.launch();
        } else {
            System.out.println("\nLaunching CLI mode...\n");
            ConsoleApp.main(args); // delegate
        }
    }

    private static void printWelcomeArt() {
        System.out.println(" __          __  _                            ");
        System.out.println(" \\ \\        / / | |                           ");
        System.out.println("  \\ \\  /\\  / /__| | ___ ___  _ __ ___   ___   ");
        System.out.println("   \\ \\/  \\/ / _ \\ |/ __/ _ \\| '_ ` _ \\ / _ \\  ");
        System.out.println("    \\  /\\  /  __/ | (_| (_) | | | | | |  __/  ");
        System.out.println("     \\/  \\/ \\___|_|\\___\\___/|_| |_| |_|\\___|  ");
        System.out.println();
        System.out.println("                WELCOME TO TIC-TAC-TOE         ");
        System.out.println();
    }
}
