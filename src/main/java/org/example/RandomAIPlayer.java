package org.example;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Very simple AI: selects a random EMPTY cell.
 */
public final class RandomAIPlayer extends Player {

    public RandomAIPlayer(Mark mark) {
        super(mark);
    }

    /**
     * Preconditions:
     *  - board != null
     *
     * Postconditions:
     *  - If any EMPTY cell exists, returns a Move targeting a random EMPTY cell with this player's mark.
     *  - If no EMPTY cell exists, throws IllegalStateException.
     *
     * @throws IllegalStateException if the board is full (no valid moves).
     */
    @Override
    public Move nextMove(Board board) {
        if (board == null) throw new IllegalArgumentException("board cannot be null");

        final int size = board.size();
        List<int[]> empties = new ArrayList<>();

        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                if (board.getCell(r, c) == Mark.EMPTY) {
                    empties.add(new int[]{r, c});
                }
            }
        }

        if (empties.isEmpty()) {
            throw new IllegalStateException("No empty cells remain");
        }

        int idx = ThreadLocalRandom.current().nextInt(empties.size());
        int[] rc = empties.get(idx);
        return new Move(rc[0], rc[1], mark);
    }
}