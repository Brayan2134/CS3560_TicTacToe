package org.example;

import java.util.Scanner;

/**
 * HumanPlayer.java
 * Desc: Human-controlled player using console input (System.in).
 * Note: We do not close the injected Scanner if it's wrapping System.in.
 */
public final class HumanPlayer extends Player {
    private final Scanner in;

    /**
     * Convenience constructor that reads from System.in.
     * (Scanner is intentionally not closed to avoid closing System.in.)
     */
    public HumanPlayer(Mark mark) {
        this(mark, new Scanner(System.in));
    }

    /**
     * Inject a Scanner for testability.
     *
     * Preconditions:
     *  @param in != null
     *
     * Postconditions:
     *  - Prompts until it can return a Move pointing to an in-bounds EMPTY cell.
     */
    public HumanPlayer(Mark mark, Scanner in) {
        super(mark);
        if (in == null) throw new IllegalArgumentException("Scanner cannot be null");
        this.in = in;
    }

    @Override
    public Move nextMove(Board board) {
        if (board == null) throw new IllegalArgumentException("board cannot be null");

        final int size = board.size();

        while (true) {
            System.out.printf("Player %s, enter your move as 'row col' (0-%d): ", mark, size - 1);

            if (!in.hasNextInt()) {
                // consume non-integer token and reprompt
                String bad = in.next();
                System.out.printf("Invalid token '%s'. Please enter two integers.%n", bad);
                continue;
            }
            int r = in.nextInt();

            if (!in.hasNextInt()) {
                String bad = in.next();
                System.out.printf("Invalid token '%s'. Please enter two integers.%n", bad);
                continue;
            }
            int c = in.nextInt();

            // basic bounds check before we form the Move
            if (r < 0 || r >= size || c < 0 || c >= size) {
                System.out.printf("Out of bounds: (%d,%d). Valid range is 0..%d.%n", r, c, size - 1);
                continue;
            }

            // check availability for a better UX (Board.place will still enforce)
            if (board.getCell(r, c) != Mark.EMPTY) {
                System.out.printf("Cell (%d,%d) is occupied. Choose another.%n", r, c);
                continue;
            }

            // return a well-formed Move for this player's mark
            return new Move(r, c, mark);
        }
    }
}