package org.example;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * BoardExhaustiveTest.java
 * Desc: Exhaustive / randomized DFS tests over all reachable tic-tac-toe states.
 * Builds fresh Board instances from a move history, so tests never reach into internals.
 */
public class BoardExhaustiveTest {

    /* ---------------------- Public “entrance” tests ---------------------- */

    @Test
    @Tag("slow") // mark this so you can exclude it in CI if needed
    @DisplayName("Exhaustive DFS from empty: all reachable states satisfy invariants")
    void exhaustive_fromEmpty_allStatesSound() {
        dfsCheckAll(new ArrayList<>(), Mark.X, /*limitLeaves=*/Long.MAX_VALUE, new Random(123));
    }

    @RepeatedTest(5)
    @DisplayName("Randomized root move: explore full subtree and check invariants")
    void randomized_root_then_fullSubtree() {
        Random rnd = new Random(42);
        // Pick a random legal root move for X, then explore everything beneath it
        int r = rnd.nextInt(3), c = rnd.nextInt(3);
        List<Move> history = new ArrayList<>();
        history.add(new Move(r, c, Mark.X));
        dfsCheckAll(history, Mark.O, /*limitLeaves=*/Long.MAX_VALUE, rnd);
    }

    /* ---------------------- Core DFS harness ---------------------- */

    /**
     * Depth-first search that rebuilds a fresh Board from the given history at each node,
     * asserts invariants, and recursively explores legal continuations.
     *
     * @param history   Sequence of already-played moves (applied in order).
     * @param turn      Whose turn it is to move next.
     * @param limitLeaves Stop early after visiting this many terminal nodes (use Long.MAX_VALUE for exhaustive).
     * @param rnd       Random for light occupied-cell negative tests.
     */
    private long dfsCheckAll(List<Move> history, Mark turn, long limitLeaves, Random rnd) {
        Board b = replay(history);
        int n = b.size();
        assertEquals(3, n, "These tests assume 3x3 standard board");

        // 1) Basic invariants on every node
        assertGridShapeAndDomain(b);
        assertFullMatchesCount(b);
        assertWinnerMatchesIndependentOracle(b);

        // 2) Spot-check occupied cells reject placement (try up to 2 random occupied cells)
        spotCheckOccupiedRejection(b, turn, rnd);

        // 3) If terminal node, count leaf and return
        Optional<Mark> w = b.winner();
        if (w.isPresent() || b.isFull()) {
            return 1L; // one terminal leaf visited
        }

        // 4) Enumerate legal moves (empties) and recurse
        long leaves = 0L;
        outer:
        for (int r = 0; r < n; r++) {
            for (int c = 0; c < n; c++) {
                if (b.getCell(r, c) == Mark.EMPTY) {
                    List<Move> next = new ArrayList<>(history);
                    next.add(new Move(r, c, turn));
                    leaves += dfsCheckAll(next, opposite(turn), limitLeaves, rnd);
                    if (leaves >= limitLeaves) break outer;
                }
            }
        }
        return leaves;
    }

    /* ---------------------- Oracles & helpers ---------------------- */

    private Board replay(List<Move> history) {
        Board b = new Board(3);
        for (Move mv : history) {
            // Sanity: history should be consistent
            assertNotNull(mv);
            assertTrue(mv.row() >= 0 && mv.row() < 3);
            assertTrue(mv.col() >= 0 && mv.col() < 3);
            assertTrue(mv.mark() == Mark.X || mv.mark() == Mark.O);
            b.place(mv);
        }
        return b;
    }

    /** Independent winner oracle (rows, cols, diags). */
    private Optional<Mark> refWinner(Board b) {
        int n = b.size();
        // rows
        for (int r = 0; r < n; r++) {
            Mark first = b.getCell(r, 0);
            if (first != Mark.EMPTY) {
                boolean all = true;
                for (int c = 1; c < n; c++) {
                    if (b.getCell(r, c) != first) { all = false; break; }
                }
                if (all) return Optional.of(first);
            }
        }
        // cols
        for (int c = 0; c < n; c++) {
            Mark first = b.getCell(0, c);
            if (first != Mark.EMPTY) {
                boolean all = true;
                for (int r = 1; r < n; r++) {
                    if (b.getCell(r, c) != first) { all = false; break; }
                }
                if (all) return Optional.of(first);
            }
        }
        // main diag
        Mark fm = b.getCell(0, 0);
        if (fm != Mark.EMPTY) {
            boolean all = true;
            for (int i = 1; i < n; i++) {
                if (b.getCell(i, i) != fm) { all = false; break; }
            }
            if (all) return Optional.of(fm);
        }
        // anti diag
        Mark fa = b.getCell(0, n - 1);
        if (fa != Mark.EMPTY) {
            boolean all = true;
            for (int i = 1; i < n; i++) {
                if (b.getCell(i, n - 1 - i) != fa) { all = false; break; }
            }
            if (all) return Optional.of(fa);
        }
        return Optional.empty();
    }

    private void assertWinnerMatchesIndependentOracle(Board b) {
        assertEquals(refWinner(b), b.winner(), "winner() must match a fresh independent scan");
    }

    private void assertFullMatchesCount(Board b) {
        int filled = 0;
        int n = b.size();
        for (int r = 0; r < n; r++) for (int c = 0; c < n; c++) {
            if (b.getCell(r, c) != Mark.EMPTY) filled++;
        }
        boolean isFull = (filled == n * n);
        assertEquals(isFull, b.isFull(), "isFull() must equal (filledCells == n^2)");
    }

    private void assertGridShapeAndDomain(Board b) {
        int n = b.size();
        // quick bounds probe + domain check
        assertThrows(IllegalArgumentException.class, () -> b.getCell(-1, 0));
        assertThrows(IllegalArgumentException.class, () -> b.getCell(0, -1));
        assertThrows(IllegalArgumentException.class, () -> b.getCell(n, 0));
        assertThrows(IllegalArgumentException.class, () -> b.getCell(0, n));
        for (int r = 0; r < n; r++) for (int c = 0; c < n; c++) {
            Mark m = b.getCell(r, c);
            assertTrue(m == Mark.X || m == Mark.O || m == Mark.EMPTY, "Cell must be X/O/EMPTY");
        }
    }

    /** Try a couple of random occupied cells and ensure place() rejects them. */
    private void spotCheckOccupiedRejection(Board b, Mark turn, Random rnd) {
        List<int[]> occupied = new ArrayList<>();
        int n = b.size();
        for (int r = 0; r < n; r++) for (int c = 0; c < n; c++) {
            if (b.getCell(r, c) != Mark.EMPTY) occupied.add(new int[]{r, c});
        }
        // Try up to two random occupied cells (keeps test fast)
        for (int i = 0; i < Math.min(2, occupied.size()); i++) {
            int[] rc = occupied.get(rnd.nextInt(occupied.size()));
            assertThrows(IllegalArgumentException.class,
                    () -> b.place(new Move(rc[0], rc[1], turn)),
                    "Expected occupied cell to be rejected");
        }
    }

    private Mark opposite(Mark m) {
        return (m == Mark.X) ? Mark.O : Mark.X;
    }
}