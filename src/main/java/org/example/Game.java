package org.example;

import java.util.Objects;
import java.util.Optional;

/**
 * Game.java
 * Desc: Orchestrates a Tic-Tac-Toe match between two Player instances on a Board.
 * NOTE: Model-only --  any user I/O should live in Player (ex: HumanPlayer) or a separate view.
 */
public final class Game {
    private final Board board;
    private final Player first;   // plays first turn
    private final Player second;  // plays second turn
    private Player current;       // whose turn it is

    /**
     * Constructs a game with the provided board and players.
     *
     * Preconditions:
     *  - board, first, second are non-null
     *  - first.getMark() != second.getMark()
     *  - first.getMark() and second.getMark() are X or O (never EMPTY)
     *
     * Postconditions:
     *  - current == first (first player starts)
     *
     * @throws IllegalArgumentException if any precondition is violated
     */
    public Game(Board board, Player first, Player second) {
        this.board = Objects.requireNonNull(board, "board");
        this.first = Objects.requireNonNull(first, "first");
        this.second = Objects.requireNonNull(second, "second");

        if (first.getMark() == second.getMark()) {
            throw new IllegalArgumentException("Players must have different marks");
        }
        if (first.getMark() == Mark.EMPTY || second.getMark() == Mark.EMPTY) {
            throw new IllegalArgumentException("Player marks must be X or O");
        }

        this.current = first; // X typically goes first; you control which Player is first.
    }

    /** @return the board in use (read-only via Board's API). */
    public Board board() { return board; }

    /** @return the player who was configured to go first. */
    public Player firstPlayer() { return first; }

    /** @return the player who was configured to go second. */
    public Player secondPlayer() { return second; }

    /** @return the player whose turn it currently is. */
    public Player currentPlayer() { return current; }

    /**
     * Runs the game loop until a winner is found or the board is full (draw).
     *
     * Contract:
     *  - Repeatedly asks the current player for a Move (polymorphic call).
     *  - Applies the move via Board.place(...).
     *  - After each successful placement, checks for winner or draw; otherwise swaps turns.
     *  - If a player proposes an invalid move (race/stale view), Board.place throws;
     *    the same player is asked again for another move.
     *
     * Postconditions:
     *  - Returns Optional.of(X/O) if that mark has a completed row/col/diag.
     *  - Returns Optional.empty() if the board fills without a winner (draw).
     */
    public Optional<Mark> run() {
        while (true) {
            // End conditions before asking for a move (covers externally pre-filled states)
            Optional<Mark> existing = board.winner();
            if (existing.isPresent()) return existing;
            if (board.isFull()) return Optional.empty();

            Move mv = current.nextMove(board);

            try {
                board.place(mv);           // single choke point for validation
                Optional<Mark> w = board.winner();
                if (w.isPresent()) return w;
                if (board.isFull()) return Optional.empty();

                // No end yet → swap players
                current = (current == first) ? second : first;

            } catch (IllegalArgumentException ex) {
                // Invalid move (out of bounds, occupied, etc.). Loop continues;
                // the SAME player will be asked again for a new move.
                // Keep this silent for model purity; a console view could log ex.getMessage().
            }
        }
    }
}