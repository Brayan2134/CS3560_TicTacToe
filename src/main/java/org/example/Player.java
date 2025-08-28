package org.example;

/**
 * Player.java
 * NOTE: Abstract player type. Concrete players (human/AI) provide the nextMove(...) behavior.
 * Invariants:
 *  - mark is either X or O (never EMPTY)
 */
public abstract class Player {
    protected final Mark mark;

    /**
     * Preconditions:
     *  @param mark != null, AND, mark != Mark.EMPTY
     *
     * Postconditions:
     *  - this.mark == mark
     *
     * @throws IllegalArgumentException if mark is null or EMPTY
     */
    protected Player(Mark mark) {
        if (mark == null || mark == Mark.EMPTY) {
            throw new IllegalArgumentException("Player mark must be X or O");
        }
        this.mark = mark;
    }

    /** @return this player's mark (X or O). */
    public Mark getMark() {
        return mark;
    }

    /**
     * Produce the player's next move given the current board.
     *
     * Preconditions:
     *  @param board != null
     *
     * Postconditions:
     *  - Returns a Move with row/col in [0, board.size()] and mark == getMark().
     *  - The Move may still be rejected by Board.place(...) if a race or stale view occurred,
     *    but concrete players should generally aim to return a valid, currently-empty cell.
     */
    public abstract Move nextMove(Board board);
}
