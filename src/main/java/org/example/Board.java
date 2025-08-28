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
        if (mv == null) {
            throw new IllegalArgumentException("Move must not be null");
        }

        int r = mv.row();
        int c = mv.col();

        // bounds check
        if (r < 0 || r >= size || c < 0 || c >= size) {
            throw new IllegalArgumentException(
                    "Move out of bounds: (" + r + "," + c + ") for size " + size
            );
        }

        // availability check
        if (grid[r][c] != Mark.EMPTY) {
            throw new IllegalArgumentException(
                    "Cell already occupied at (" + r + "," + c + ")"
            );
        }

        // all checks passed → place the mark
        grid[r][c] = mv.mark();
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
        if (r < 0 || r >= size || c < 0 || c >= size) {
            throw new IllegalArgumentException(
                    "indices out of bounds: (" + r + "," + c + ") for size " + size
            );
        }
        return grid[r][c];
    }

    /**
     * @return true iff every cell is non-EMPTY.
     *
     * Postconditions:
     *  - If result == true, then for all r,c: grid[r][c] != EMPTY.
     */
    public boolean isFull() {
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                if (grid[r][c] == Mark.EMPTY) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Checks for a winner (row, column, or diagonal).
     *
     * Postconditions:
     *  - If a player has three in a row, returns Optional.of(X) or Optional.of(O).
     *  - Otherwise returns Optional.empty().
     */
    public Optional<Mark> winner() {
        // rows (first, second, third)
        for (int r = 0; r < size; r++) {
            Mark first = grid[r][0];
            if (first != Mark.EMPTY) {
                boolean allSame = true;
                for (int c = 1; c < size; c++) {
                    if (grid[r][c] != first) {
                        allSame = false;
                        break;
                    }
                }
                if (allSame) return Optional.of(first);
            }
        }

        // columns (first, second, third)
        for (int c = 0; c < size; c++) {
            Mark first = grid[0][c];
            if (first != Mark.EMPTY) {
                boolean allSame = true;
                for (int r = 1; r < size; r++) {
                    if (grid[r][c] != first) {
                        allSame = false;
                        break;
                    }
                }
                if (allSame) return Optional.of(first);
            }
        }

        // diagonal (top-left -> bottom-right)
        Mark firstMain = grid[0][0];
        if (firstMain != Mark.EMPTY) {
            boolean allSame = true;
            for (int i = 1; i < size; i++) {
                if (grid[i][i] != firstMain) {
                    allSame = false;
                    break;
                }
            }
            if (allSame) return Optional.of(firstMain);
        }

        // diagonal (top-right -> bottom-left)
        Mark firstAnti = grid[0][size - 1];
        if (firstAnti != Mark.EMPTY) {
            boolean allSame = true;
            for (int i = 1; i < size; i++) {
                if (grid[i][size - 1 - i] != firstAnti) {
                    allSame = false;
                    break;
                }
            }
            if (allSame) return Optional.of(firstAnti);
        }

        return Optional.empty();
    }

    /**
     * Clears the board to all EMPTY.
     *
     * Postconditions:
     *  - For all r,c in [0,size): grid[r][c] == EMPTY
     */
    public void reset() {
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                grid[r][c] = Mark.EMPTY;
            }
        }
    }

    /** @return the dimension of the board. */
    public int size() {
        return size;
    }
}
