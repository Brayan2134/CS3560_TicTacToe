package org.example;

import java.util.Objects;

/**
 * Move.java
 * Desc: An immutable value object that represents one single move.
 * NOTE: Board bounds are validated inside Board, not here!!!!
 * Invariants:
 *  - row >= 0, col >= 0
 *  - mark in {X, O} (never EMPTY here)
 *  - once constructed, values NEVER change!!
 */
public final class Move {
    private final int row;
    private final int col;
    private final Mark mark;

    /**
     * Creates a move at (row, col) with the given mark.
     * Preconditions:
     *  @param row >= 0
     *  @param col >= 0
     *  @param mark != null
     *  @param mark != Mark.EMPTY
     *
     * Postconditions:
     *  - A new immutable Move is created with the provided values.
     *  - Calling row(), col(), mark() will return the same values.
     *
     * @throws IllegalArgumentException if row/col are negative or mark is null/EMPTY
     */
    public Move(int row, int col, Mark mark) {
        if (row < 0 || col < 0) {
            throw new IllegalArgumentException("row/col must be non-negative");
        }
        if (mark == null || mark == Mark.EMPTY) {
            throw new IllegalArgumentException("mark must be X or O");
        }
        this.row = row;
        this.col = col;
        this.mark = mark;
    }

    /**
     * @return the row index of this move
     *
     * Postconditions:
     *  - Returns the same value that was provided in the constructor.
     *  - Result >= 0
     */
    public int row() { return row; }

    /**
     * @return the column index of this move
     *
     * Postconditions:
     *  - Returns the same value that was provided in the constructor.
     *  - Result >= 0
     */
    public int col() { return col; }

    /**
     * @return the mark (X or O) used in this move
     *
     * Postconditions:
     *  - Returns the same non-null, non-EMPTY Mark provided in the constructor.
     */
    public Mark mark() { return mark; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Move)) return false;
        Move move = (Move) o;
        return row == move.row && col == move.col && mark == move.mark;
    }

    @Override
    public int hashCode() {
        return Objects.hash(row, col, mark);
    }

    @Override
    public String toString() {
        return "Move{" + "row=" + row + ", col=" + col + ", mark=" + mark + '}';
    }
}
