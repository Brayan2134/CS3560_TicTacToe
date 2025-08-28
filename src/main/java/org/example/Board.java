package org.example;

import java.util.Optional;

/**
 * Board.java
 * Desc: model only.
 * Invariants:
 *  - grid is a square size×size matrix.
 *  - Every cell is always one of {X, O, EMPTY}.
 *  - The grid reference never escapes; all mutation happens via Board methods.
 */
public final class Board {
    private final int size; //Board dimension (e.g., 3 for a 3×3 board)

    private final Mark[][] grid; //Encapsulated board state; never exposed directly

    /**
     * Creates a new empty board of the given size, with all cells set to EMPTY.
     *
     * Preconditions:
     *  @param size >= 3 (standard Tic-Tac-Toe is 3; maybe a larger sizes allowed if you extend rules).
     *
     * Postconditions:
     *  - this.size == size
     *  - grid is allocated size×size
     *  - for all r,c in [0,size): grid[r][c] == Mark.EMPTY
     *
     * @throws IllegalArgumentException if size < 3
     */
    public Board(int size) {
        if (size < 3) {
            throw new IllegalArgumentException("size must be >= 3");
        }
        this.size = size;
        this.grid = new Mark[size][size];
        // initialize to EMPTY
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                grid[r][c] = Mark.EMPTY;
            }
        }
    }

    /**
     * Default constructor for a standard 3×3 board.
     *
     * Postconditions:
     *  - Equivalent to new Board(3)
     */
    public Board() {
        this(3);
    }

    /**
     * Places a move onto the board.
     *
     * Preconditions:
     *  @param mv != null
     *  - 0 <= mv.row() < size
     *  - 0 <= mv.col() < size
     *  - grid[mv.row()][mv.col()] == EMPTY
     *  - (Optional by design choice) mv.mark() corresponds to the correct player's turn
     *
     * Postconditions:
     *  - grid[mv.row()][mv.col()] == mv.mark()
     *
     * @throws IllegalArgumentException if any precondition is violated
     */
    public void place(Move mv) {
        // TODO: implement validation + placement
        throw new UnsupportedOperationException("TODO: Board.place");
    }

    /**
     * Returns the mark at (r, c).
     *
     * Preconditions:
     *  - 0 <= r < size
     *  - 0 <= c < size
     *
     * Postconditions:
     *  - Returns one of {X, O, EMPTY} representing the current cell value.
     *
     * @throws IllegalArgumentException if indices are out of bounds
     */
    public Mark getCell(int r, int c) {
        // TODO: index validation + return grid[r][c]
        throw new UnsupportedOperationException("TODO: Board.getCell");
    }

    /**
     * @return true iff every cell is non-EMPTY.
     *
     * Postconditions:
     *  - If result == true, then for all r,c: grid[r][c] != EMPTY.
     */
    public boolean isFull() {
        // TODO: scan for EMPTY; return accordingly
        throw new UnsupportedOperationException("TODO: Board.isFull");
    }

    /**
     * Checks for a winner (row, column, or diagonal).
     *
     * Postconditions:
     *  - If a player has three in a row, returns Optional.of(X) or Optional.of(O).
     *  - Otherwise returns Optional.empty().
     */
    public Optional<Mark> winner() {
        // TODO: scan rows, columns, diagonals
        throw new UnsupportedOperationException("TODO: Board.winner");
    }

    /**
     * Clears the board to all EMPTY.
     *
     * Postconditions:
     *  - For all r,c in [0,size): grid[r][c] == EMPTY
     */
    public void reset() {
        // TODO: set all cells to EMPTY
        throw new UnsupportedOperationException("TODO: Board.reset");
    }

    /** @return the dimension of the board. */
    public int size() {
        return size;
    }
}
