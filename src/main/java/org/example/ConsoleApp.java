package org.example;

import java.util.Optional;

public final class ConsoleApp {
    public static void main(String[] args) {
        System.out.println("=== Tic-Tac-Toe ===");
        System.out.println("You are X. Random AI is O.\n");

        Board board = new Board(3);
        Player human = new HumanPlayer(Mark.X);      // reads from System.in
        //Player ai = new RandomAIPlayer(Mark.O);
        Player ai = new MinimaxAIPlayer(Mark.O);

        Player current = human; // X starts
        Optional<Mark> winner = Optional.empty();

        while (true) {
            // Check end state before move
            winner = board.winner();
            if (winner.isPresent() || board.isFull()) break;

            System.out.println("Current board:");
            printBoard(board);

            // Ask current player
            Move mv = current.nextMove(board);

            try {
                board.place(mv);  // may throw if invalid
                // After a successful placement, show board
                System.out.printf("Player %s placed at (%d,%d)%n", current.getMark(), mv.row(), mv.col());
                printBoard(board);

                // Check end state
                winner = board.winner();
                if (winner.isPresent() || board.isFull()) break;

                // Swap players
                current = (current == human) ? ai : human;
            } catch (IllegalArgumentException ex) {
                System.out.println("Invalid move: " + ex.getMessage());
                // same player retries
            }
        }

        System.out.println("\n=== Final Result ===");
        if (winner.isPresent()) {
            System.out.println("Winner: " + winner.get());
        } else {
            System.out.println("Draw!");
        }
    }

    /** Simple text rendering of the board without exposing internal arrays. */
    private static void printBoard(Board board) {
        int n = board.size();
        for (int r = 0; r < n; r++) {
            StringBuilder row = new StringBuilder();
            for (int c = 0; c < n; c++) {
                Mark m = board.getCell(r, c);
                char ch = (m == Mark.X) ? 'X' : (m == Mark.O) ? 'O' : ' ';
                row.append(' ').append(ch).append(' ');
                if (c < n - 1) row.append('|');
            }
            System.out.println(row);
            if (r < n - 1) {
                System.out.println("---+---+---");
            }
        }
        System.out.println();
    }
}
