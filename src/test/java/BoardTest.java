package org.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * BoardTest.java
 * Desc: Tests the overall board behavior in different circumstances to ensure code reliability.
 * */

@DisplayName("Board core behavior")
public class BoardTest {

    private Board b3;

    @BeforeEach
    void setup() {
        b3 = new Board(3);
    }

    /* -------------------- constructor & basic invariants -------------------- */

    @Test
    @DisplayName("Constructor: size >= 3 enforced; grid initially EMPTY; isFull false")
    void constructor_enforcesSize_andInitialState() {
        assertThrows(IllegalArgumentException.class, () -> new Board(0));
        assertThrows(IllegalArgumentException.class, () -> new Board(1));
        assertThrows(IllegalArgumentException.class, () -> new Board(2));

        Board b = new Board(3);
        assertEquals(3, b.size());
        // all empty
        for (int r = 0; r < b.size(); r++) {
            for (int c = 0; c < b.size(); c++) {
                assertEquals(Mark.EMPTY, b.getCell(r, c));
            }
        }
        assertFalse(b.isFull());
        assertEquals(Optional.empty(), b.winner());
    }

    /* -------------------- getCell bounds -------------------- */

    @Test
    @DisplayName("getCell: in-bounds works; out-of-bounds throws")
    void getCell_bounds() {
        assertDoesNotThrow(() -> b3.getCell(0, 0));
        assertDoesNotThrow(() -> b3.getCell(2, 2));

        // negatives
        assertThrows(IllegalArgumentException.class, () -> b3.getCell(-1, 0));
        assertThrows(IllegalArgumentException.class, () -> b3.getCell(0, -1));

        // >= size
        assertThrows(IllegalArgumentException.class, () -> b3.getCell(3, 0));
        assertThrows(IllegalArgumentException.class, () -> b3.getCell(0, 3));
    }

    /* -------------------- place: happy path & re-placement -------------------- */

    @Test
    @DisplayName("place: valid single placement updates cell")
    void place_valid_updatesCell() {
        Move m = new Move(0, 0, Mark.X);
        b3.place(m);
        assertEquals(Mark.X, b3.getCell(0, 0));
        assertEquals(Optional.empty(), b3.winner());
        assertFalse(b3.isFull());
    }

    @Test
    @DisplayName("place: placing into occupied cell throws")
    void place_intoOccupied_throws() {
        b3.place(new Move(0, 0, Mark.X));
        assertThrows(IllegalArgumentException.class, () -> b3.place(new Move(0, 0, Mark.O)));
    }

    @Test
    @DisplayName("place: null move or out-of-bounds indices throw")
    void place_null_or_oob_throw() {
        assertThrows(IllegalArgumentException.class, () -> b3.place(null));
        assertThrows(IllegalArgumentException.class, () -> b3.place(new Move(-1, 0, Mark.X)));
        assertThrows(IllegalArgumentException.class, () -> b3.place(new Move(0, -1, Mark.X)));
        assertThrows(IllegalArgumentException.class, () -> b3.place(new Move(3, 0, Mark.X)));
        assertThrows(IllegalArgumentException.class, () -> b3.place(new Move(0, 3, Mark.X)));
    }

    /* -------------------- winner detection -------------------- */

    @Test
    @DisplayName("winner: detects row win")
    void winner_row() {
        b3.place(new Move(1, 0, Mark.O));
        b3.place(new Move(1, 1, Mark.O));
        b3.place(new Move(1, 2, Mark.O));
        assertEquals(Optional.of(Mark.O), b3.winner());
    }

    @Test
    @DisplayName("winner: detects column win")
    void winner_column() {
        b3.place(new Move(0, 2, Mark.X));
        b3.place(new Move(1, 2, Mark.X));
        b3.place(new Move(2, 2, Mark.X));
        assertEquals(Optional.of(Mark.X), b3.winner());
    }

    @Test
    @DisplayName("winner: detects main diagonal win")
    void winner_diag_main() {
        b3.place(new Move(0, 0, Mark.X));
        b3.place(new Move(1, 1, Mark.X));
        b3.place(new Move(2, 2, Mark.X));
        assertEquals(Optional.of(Mark.X), b3.winner());
    }

    @Test
    @DisplayName("winner: detects anti-diagonal win")
    void winner_diag_anti() {
        b3.place(new Move(0, 2, Mark.O));
        b3.place(new Move(1, 1, Mark.O));
        b3.place(new Move(2, 0, Mark.O));
        assertEquals(Optional.of(Mark.O), b3.winner());
    }

    @Test
    @DisplayName("winner: mixed lines do not falsely trigger")
    void winner_noFalsePositive() {
        // X O X across a row -> no win unless all equal non-EMPTY
        b3.place(new Move(0, 0, Mark.X));
        b3.place(new Move(0, 1, Mark.O));
        b3.place(new Move(0, 2, Mark.X));
        assertEquals(Optional.empty(), b3.winner());
    }

    /* -------------------- isFull progression & draw -------------------- */

    @Test
    @DisplayName("isFull: becomes true after filling all cells without a winner (draw)")
    void isFull_whenFilled_draw() {
        // Board:
        // X O X
        // X O O
        // O X X
        b3.place(new Move(0, 0, Mark.X));
        b3.place(new Move(0, 1, Mark.O));
        b3.place(new Move(0, 2, Mark.X));
        b3.place(new Move(1, 0, Mark.X));
        b3.place(new Move(1, 1, Mark.O));
        b3.place(new Move(1, 2, Mark.O));
        b3.place(new Move(2, 0, Mark.O));
        b3.place(new Move(2, 1, Mark.X));
        b3.place(new Move(2, 2, Mark.X));

        assertTrue(b3.isFull());
        assertEquals(Optional.empty(), b3.winner(), "This board should be a draw");
    }

    /* -------------------- reset behavior -------------------- */

    @Test
    @DisplayName("reset: clears all cells to EMPTY and removes winner")
    void reset_clears() {
        b3.place(new Move(0, 0, Mark.X));
        b3.place(new Move(1, 1, Mark.X));
        b3.place(new Move(2, 2, Mark.X));
        assertEquals(Optional.of(Mark.X), b3.winner());

        b3.reset();
        for (int r = 0; r < b3.size(); r++) {
            for (int c = 0; c < b3.size(); c++) {
                assertEquals(Mark.EMPTY, b3.getCell(r, c));
            }
        }
        assertFalse(b3.isFull());
        assertEquals(Optional.empty(), b3.winner());
    }

    /* -------------------- combinations up to 3 moves (no crashes, correct state) -------------------- */

    @Test
    @DisplayName("3-move sequences: state reflects last write; occupied throws on duplicates")
    void threeMoveSequences_basic() {
        // Move 1
        b3.place(new Move(0, 0, Mark.X));
        assertEquals(Mark.X, b3.getCell(0, 0));

        // Move 2
        b3.place(new Move(1, 1, Mark.O));
        assertEquals(Mark.O, b3.getCell(1, 1));

        // Move 3 (legal different cell)
        b3.place(new Move(2, 2, Mark.X));
        assertEquals(Mark.X, b3.getCell(2, 2));

        // Attempt re-place on any of those should throw
        assertThrows(IllegalArgumentException.class, () -> b3.place(new Move(0, 0, Mark.O)));
        assertThrows(IllegalArgumentException.class, () -> b3.place(new Move(1, 1, Mark.X)));
        assertThrows(IllegalArgumentException.class, () -> b3.place(new Move(2, 2, Mark.O)));
    }

    /* -------------------- randomized bounds & randomized legal play -------------------- */

    @RepeatedTest(5)
    @DisplayName("Random out-of-bounds indices throw; random legal play fills exactly N^2 cells")
    void fuzz_randomBounds_and_randomPlay() {
        final Random rnd = new Random(42); // deterministic seed per repetition start (JUnit re-seeds each run)

        // 1) Random out-of-bounds attempts (mix negatives and >= size)
        for (int i = 0; i < 50; i++) {
            int r = rnd.nextBoolean() ? -1 - rnd.nextInt(5) : 3 + rnd.nextInt(5);
            int c = rnd.nextBoolean() ? -1 - rnd.nextInt(5) : 3 + rnd.nextInt(5);
            // Constructing Move may already throw if negative; handle both paths
            try {
                Move m = new Move(r, c, Mark.X);
                assertThrows(IllegalArgumentException.class, () -> b3.place(m),
                        "Expected place() to reject out-of-bounds: (" + r + "," + c + ")");
            } catch (IllegalArgumentException ok) {
                // Move constructor itself enforces non-negative; that's fine.
            }
        }

        // 2) Random legal play until board is full: ensure exactly 9 successful placements
        Board b = new Board(3);
        int placed = 0;
        Mark turn = Mark.X;

        while (!b.isFull() && b.winner().isEmpty()) {
            int r = rnd.nextInt(3);
            int c = rnd.nextInt(3);
            try {
                b.place(new Move(r, c, turn));
                placed++;
                turn = (turn == Mark.X) ? Mark.O : Mark.X;
            } catch (IllegalArgumentException ignored) {
                // illegal (occupied) — try again
            }
        }

        assertEquals(9, placed, "Random legal play should result in exactly 9 successful placements unless someone wins early");

        // If someone happened to win early in this random sequence, the board may not be full.
        // But with a random seed and retry loop, we'll typically fill all cells at least once across repetitions.
        if (!b.isFull()) {
            // If not full because of an early win, ensure winner is present
            assertTrue(b.winner().isPresent(), "If not full, someone should have won");
        }
    }
}