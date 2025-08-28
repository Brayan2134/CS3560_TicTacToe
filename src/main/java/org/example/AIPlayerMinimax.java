package org.example;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * AiPlayerMinimax.java
 * Desc: Minimax-based AI implementation.
 * NOTES (from docs) -- <a href="https://www.neverstopbuilding.com/blog/minimax">CLICK HERE</a>:
 * - Assumes optimal play by both sides.
 * - Scores: win = +10 - depth, loss = -10 + depth, draw = 0
 * - Chooses the move with the highest score from the AI's perspective.
 * - Ties are broken randomly among the best-scoring moves.
 */
public final class AIPlayerMinimax extends Player {

    //Flip to true to print a score matrix for this turn (### = occupied)
    private static final boolean DEBUG = false;

    public AIPlayerMinimax(Mark mark) {
        super(mark);
    }

    /**
     * Preconditions:
     *  - board != null
     * Postconditions:
     *  - Returns a legal Move (row/col point to EMPTY).
     *  - If multiple best moves exist, one is chosen at random.
     */
    @Override
    public Move nextMove(Board board) {
        if (board == null) throw new IllegalArgumentException("board cannot be null");

        final int n = board.size();
        // Snapshot current board
        Mark[][] state = new Mark[n][n];
        for (int r = 0; r < n; r++) {
            for (int c = 0; c < n; c++) {
                state[r][c] = board.getCell(r, c);
            }
        }

        List<int[]> empties = listEmpties(state);
        if (empties.isEmpty()) {
            throw new IllegalStateException("No empty cells remain");
        }

        int bestScore = Integer.MIN_VALUE;
        List<int[]> bestMoves = new ArrayList<>();
        Map<String, Integer> debugScores = DEBUG ? new HashMap<>() : null;

        for (int[] rc : empties) {
            int r = rc[0], c = rc[1];
            Mark[][] next = copyOf(state);
            next[r][c] = this.mark;

            // Opponent to move next
            int score = minimax(next, opposite(this.mark), /*depth=*/1,
                    /*alpha=*/Integer.MIN_VALUE, /*beta=*/Integer.MAX_VALUE,
                    /*perspective=*/this.mark);

            if (DEBUG) debugScores.put(r + "," + c, score);

            if (score > bestScore) {
                bestScore = score;
                bestMoves.clear();
                bestMoves.add(rc);
            } else if (score == bestScore) {
                bestMoves.add(rc);
            }
        }

        if (DEBUG) printScoreMatrix(state, debugScores);

        int pick = ThreadLocalRandom.current().nextInt(bestMoves.size());
        int[] rc = bestMoves.get(pick);
        return new Move(rc[0], rc[1], this.mark);
    }

    /**
     * Preconditions:
     *  - s != null
     *  - All rows of s are non-null and of equal length (square board).
     *  - Each entry of s ∈ {X, O, EMPTY}.
     *  - turn ∈ {X, O} (not EMPTY).
     *  - perspective ∈ {X, O} (not EMPTY).
     *  - alpha <= beta (initially).
     *
     * Postconditions:
     *  - Returns an integer score from the perspective of 'perspective':
     *      > 0 → winning outcome possible (higher = quicker win).
     *      < 0 → losing outcome inevitable (lower = faster loss).
     *      = 0 → draw under perfect play.
     *  - Alpha–beta pruning ensures performance but does not change the score result.
     */
    private int minimax(Mark[][] s, Mark turn, int depth, int alpha, int beta, Mark perspective) {
        Mark w = winnerOf(s);
        if (w != null) {
            // Prefer faster wins (bigger when shallower), prefer slower losses (less negative when deeper)
            return (w == perspective) ? (10 - depth) : (-10 + depth);
        }
        if (isFull(s)) return 0; // draw

        boolean maximizing = (turn == perspective);
        int n = s.length;

        if (maximizing) {
            int best = Integer.MIN_VALUE;
            for (int[] rc : listEmpties(s)) {
                int r = rc[0], c = rc[1];
                Mark[][] next = copyOf(s);
                next[r][c] = turn;
                int score = minimax(next, opposite(turn), depth + 1, alpha, beta, perspective);
                best = Math.max(best, score);
                alpha = Math.max(alpha, best);
                if (beta <= alpha) break; // prune
            }
            return best;
        } else {
            int best = Integer.MAX_VALUE;
            for (int[] rc : listEmpties(s)) {
                int r = rc[0], c = rc[1];
                Mark[][] next = copyOf(s);
                next[r][c] = turn;
                int score = minimax(next, opposite(turn), depth + 1, alpha, beta, perspective);
                best = Math.min(best, score);
                beta = Math.min(beta, best);
                if (beta <= alpha) break; // prune
            }
            return best;
        }
    }

    /**
     * Preconditions:
     *  - s != null
     *  - All rows of s are non-null and of equal length.
     *
     * Postconditions:
     *  - Returns a List of coordinate pairs [r,c] for every EMPTY cell in s.
     *  - Each pair satisfies 0 <= r,c < s.length.
     *  - If no empty cells exist, returns an empty list.
     */
    private static List<int[]> listEmpties(Mark[][] s) {
        int n = s.length;
        List<int[]> out = new ArrayList<>();
        for (int r = 0; r < n; r++) {
            for (int c = 0; c < n; c++) {
                if (s[r][c] == Mark.EMPTY) out.add(new int[]{r, c});
            }
        }
        return out;
    }

    /**
     * Preconditions:
     *  - s != null
     *  - All rows of s are non-null and of equal length.
     *
     * Postconditions:
     *  - Returns true if and only if every cell in s is non-EMPTY.
     *  - Returns false if at least one cell is EMPTY.
     */
    private static boolean isFull(Mark[][] s) {
        for (Mark[] row : s) for (Mark m : row) if (m == Mark.EMPTY) return false;
        return true;
    }

    /**
     * Preconditions:
     *  - s != null
     *  - All rows of s are non-null and of equal length.
     *
     * Postconditions:
     *  - Returns a new Mark[][] array with the same size and content as s.
     *  - Modifications to the returned array do not affect the original s.
     */
    private static Mark[][] copyOf(Mark[][] s) {
        int n = s.length;
        Mark[][] t = new Mark[n][n];
        for (int i = 0; i < n; i++) System.arraycopy(s[i], 0, t[i], 0, n);
        return t;
    }

    private static Mark opposite(Mark m) {
        return (m == Mark.X) ? Mark.O : Mark.X;
    }

    /**
     * Preconditions:
     *  - s != null
     *  - All rows of s are non-null and of equal length (square board).
     *  - Each entry of s ∈ {X, O, EMPTY}.
     *
     * Postconditions:
     *  - Returns X if a full row/column/diagonal is filled with X.
     *  - Returns O if a full row/column/diagonal is filled with O.
     *  - Returns null if no winner is found.
     */
    private static Mark winnerOf(Mark[][] s) {
        int n = s.length;
        // rows
        for (int r = 0; r < n; r++) {
            Mark first = s[r][0];
            if (first != Mark.EMPTY) {
                boolean all = true;
                for (int c = 1; c < n; c++) {
                    if (s[r][c] != first) { all = false; break; }
                }
                if (all) return first;
            }
        }
        // cols
        for (int c = 0; c < n; c++) {
            Mark first = s[0][c];
            if (first != Mark.EMPTY) {
                boolean all = true;
                for (int r = 1; r < n; r++) {
                    if (s[r][c] != first) { all = false; break; }
                }
                if (all) return first;
            }
        }
        // main diag
        Mark fm = s[0][0];
        if (fm != Mark.EMPTY) {
            boolean all = true;
            for (int i = 1; i < n; i++) {
                if (s[i][i] != fm) { all = false; break; }
            }
            if (all) return fm;
        }
        // anti diag
        Mark fa = s[0][n - 1];
        if (fa != Mark.EMPTY) {
            boolean all = true;
            for (int i = 1; i < n; i++) {
                if (s[i][n - 1 - i] != fa) { all = false; break; }
            }
            if (all) return fa;
        }
        return null;
    }

    /* -------------------- Optional debug printing -------------------- */
    private void printScoreMatrix(Mark[][] state, Map<String, Integer> scoresByCell) {
        final int n = state.length;
        System.out.println("\n[MINIMAX DEBUG] Score matrix for " + this.mark + " (### = occupied)");
        for (int r = 0; r < n; r++) {
            StringBuilder row = new StringBuilder();
            for (int c = 0; c < n; c++) {
                String cell;
                if (state[r][c] != Mark.EMPTY) {
                    cell = " ### ";
                } else {
                    Integer score = scoresByCell.get(r + "," + c);
                    cell = String.format(" %4d ", (score != null ? score : 0));
                }
                row.append(cell);
                if (c < n - 1) row.append("|");
            }
            System.out.println(row);
            if (r < n - 1) {
                StringBuilder sep = new StringBuilder();
                for (int c = 0; c < n; c++) {
                    sep.append("------");
                    if (c < n - 1) sep.append("+");
                }
                System.out.println(sep);
            }
        }
        System.out.println();
    }
}