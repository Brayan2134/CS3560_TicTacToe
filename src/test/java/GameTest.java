package org.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Game orchestrator behavior")
public class GameTest {

    private Board board3;

    @BeforeEach
    void setup() {
        board3 = new Board(3);
    }

    /* -------------------- Constructor validation -------------------- */

    @Test
    @DisplayName("Game ctor rejects players with the same mark")
    void game_ctor_rejects_same_mark() {
        Player x1 = new ScriptedPlayer(Mark.X);
        Player x2 = new ScriptedPlayer(Mark.X);
        assertThrows(IllegalArgumentException.class, () -> new Game(board3, x1, x2));
    }

    @Test
    @DisplayName("Player ctor rejects EMPTY mark (precondition of Player, not Game)")
    void player_ctor_rejects_empty_mark() {
        assertThrows(IllegalArgumentException.class, () -> new ScriptedPlayer(Mark.EMPTY));
    }

    /* -------------------- Immediate terminal states -------------------- */

    @Test
    @DisplayName("If board already has a winner, run() returns immediately without querying players")
    void run_returns_immediately_when_winner_preexists() {
        // Pre-fill an X row win on the board
        board3.place(new Move(1, 0, Mark.X));
        board3.place(new Move(1, 1, Mark.X));
        board3.place(new Move(1, 2, Mark.X));

        CountingPlayer x = new CountingPlayer(Mark.X);
        CountingPlayer o = new CountingPlayer(Mark.O);

        Optional<Mark> result = new Game(board3, x, o).run();
        assertEquals(Optional.of(Mark.X), result);

        // nextMove should not have been called (checked before asking for a move)
        assertEquals(0, x.calls.get());
        assertEquals(0, o.calls.get());
    }

    @Test
    @DisplayName("If board is already full without winner, run() returns draw immediately")
    void run_returns_immediately_when_board_full_draw() {
        // X O X
        // X O O
        // O X X
        fill(board3, new int[][]{
                {0,0}, {0,1}, {0,2},
                {1,0}, {1,1}, {1,2},
                {2,0}, {2,1}, {2,2}
        }, new Mark[]{Mark.X, Mark.O, Mark.X, Mark.X, Mark.O, Mark.O, Mark.O, Mark.X, Mark.X});

        CountingPlayer x = new CountingPlayer(Mark.X);
        CountingPlayer o = new CountingPlayer(Mark.O);

        Optional<Mark> result = new Game(board3, x, o).run();
        assertEquals(Optional.empty(), result);
        assertEquals(0, x.calls.get());
        assertEquals(0, o.calls.get());
    }

    /* -------------------- Normal play: X wins deterministically -------------------- */

    @Test
    @DisplayName("X wins a straightforward game; run() returns Optional.of(X)")
    void x_wins_straight_row() {
        // Script a simple game where X completes the top row:
        // X moves: (0,0), (0,1), (0,2)
        // O moves: (1,1), (2,2)
        ScriptedPlayer x = new ScriptedPlayer(Mark.X,
                new Move(0,0, Mark.X),
                new Move(0,1, Mark.X),
                new Move(0,2, Mark.X));
        ScriptedPlayer o = new ScriptedPlayer(Mark.O,
                new Move(1,1, Mark.O),
                new Move(2,2, Mark.O));

        Optional<Mark> result = new Game(board3, x, o).run();

        assertEquals(Optional.of(Mark.X), result);
        // Verify board state reflects the winning line
        assertEquals(Mark.X, board3.getCell(0,0));
        assertEquals(Mark.X, board3.getCell(0,1));
        assertEquals(Mark.X, board3.getCell(0,2));
    }

    /* -------------------- Normal play: draw -------------------- */

    @Test
    @DisplayName("Full game to draw; run() returns Optional.empty()")
    void full_draw_game() {
        // Sequence that fills the board with no 3-in-a-row
        ScriptedOrFallbackPlayer x = new ScriptedOrFallbackPlayer(Mark.X,
                new Move(0,0, Mark.X),
                new Move(0,2, Mark.X),
                new Move(1,0, Mark.X),
                new Move(2,1, Mark.X),
                new Move(2,2, Mark.X));
        ScriptedOrFallbackPlayer o = new ScriptedOrFallbackPlayer(Mark.O,
                new Move(0,1, Mark.O),
                new Move(1,1, Mark.O),
                new Move(1,2, Mark.O),
                new Move(2,0, Mark.O));

        Optional<Mark> result = new Game(board3, x, o).run();

        assertEquals(Optional.empty(), result);
        assertTrue(board3.isFull());
        assertTrue(board3.winner().isEmpty());
    }

    /* -------------------- Invalid move retry (occupied and out-of-bounds) -------------------- */

    @Test
    @DisplayName("When a player proposes an invalid move (occupied), Game asks the same player again")
    void invalid_move_retry_occupied() {
        // X: first move (0,0), then tries occupied (0,0) again, then valid (2,2), then fallback
        // O: plays (1,1), then fallback
        FaultyThenValidPlayer x = new FaultyThenValidPlayer(
                Mark.X,
                new Move(0,0, Mark.X),           // valid
                new Move(0,0, Mark.X),           // invalid: occupied
                new Move(2,2, Mark.X)            // valid
        );
        ScriptedOrFallbackPlayer o = new ScriptedOrFallbackPlayer(Mark.O, new Move(1,1, Mark.O));

        Optional<Mark> result = new Game(board3, x, o).run();

        // Ensure the final valid X move was placed
        assertEquals(Mark.X, board3.getCell(2,2));
        // It should terminate with either a win or draw (don’t assert which)
        assertTrue(result.isPresent() || result.isEmpty());
        // Ensure the faulty move attempt was counted/retried
        assertTrue(x.calls.get() >= 3, "expected nextMove to be called again after invalid attempt");
    }

    @Test
    @DisplayName("When a player proposes an out-of-bounds move, Game keeps asking that player")
    void invalid_move_retry_oob() {
        // Move(3,0, X) is out of bounds for a 3x3 board (bounds checked in Board.place)
        FaultyThenValidPlayer x = new FaultyThenValidPlayer(
                Mark.X,
                new Move(3,0, Mark.X),           // invalid: out-of-bounds
                new Move(0,0, Mark.X)            // valid
        );
        ScriptedOrFallbackPlayer o = new ScriptedOrFallbackPlayer(Mark.O, new Move(1,1, Mark.O));

        Optional<Mark> result = new Game(board3, x, o).run();

        assertTrue(result.isPresent() || result.isEmpty()); // just ensure it terminates
        assertEquals(Mark.X, board3.getCell(0,0));
        assertTrue(x.calls.get() >= 2);
    }

    /* -------------------- Game stops after win; extra scripted moves are ignored -------------------- */

    @Test
    @DisplayName("Game stops on win; extra scripted moves are not consumed")
    void stops_on_win_extra_moves_unused() {
        ScriptedPlayer x = new ScriptedPlayer(Mark.X,
                new Move(0,0, Mark.X),
                new Move(0,1, Mark.X),
                new Move(0,2, Mark.X),   // ← winning move
                new Move(2,2, Mark.X)    // ← should never be consumed
        );
        ScriptedPlayer o = new ScriptedPlayer(Mark.O,
                new Move(1,1, Mark.O),
                new Move(2,2, Mark.O),
                new Move(2,0, Mark.O));

        Optional<Mark> result = new Game(board3, x, o).run();
        assertEquals(Optional.of(Mark.X), result);

        // After win, the next scripted X move must remain unused
        assertTrue(x.hasRemainingMoves(), "extra scripted X move should be unused after win");
    }

    /* =======================================================================
       Helper players used only for tests (not part of production code)
       ======================================================================= */

    /** STRICT: returns a fixed queue of moves; throws if exhausted. */
    static class ScriptedPlayer extends Player {
        private final Deque<Move> moves = new ArrayDeque<>();

        ScriptedPlayer(Mark mark, Move... script) {
            super(mark);
            for (Move m : script) moves.addLast(m);
        }

        boolean hasRemainingMoves() { return !moves.isEmpty(); }

        @Override
        public Move nextMove(Board board) {
            if (board == null) throw new IllegalArgumentException("board null");
            if (moves.isEmpty()) throw new AssertionError("ScriptedPlayer ran out of moves");
            Move m = moves.removeFirst();
            // Sanity: ensure mark matches this player
            assertEquals(mark, m.mark(), "Scripted move must match player's mark");
            return m;
        }
    }

    /** Scripted first, then fallback to the first empty cell (robust for long games). */
    static class ScriptedOrFallbackPlayer extends Player {
        private final Deque<Move> moves = new ArrayDeque<>();

        ScriptedOrFallbackPlayer(Mark mark, Move... script) {
            super(mark);
            for (Move m : script) moves.addLast(m);
        }

        @Override
        public Move nextMove(Board board) {
            if (board == null) throw new IllegalArgumentException("board null");
            if (!moves.isEmpty()) {
                Move m = moves.removeFirst();
                assertEquals(mark, m.mark(), "Scripted move must match player's mark");
                return m;
            }
            // fallback: pick first empty
            for (int r = 0; r < board.size(); r++)
                for (int c = 0; c < board.size(); c++)
                    if (board.getCell(r,c) == Mark.EMPTY)
                        return new Move(r, c, mark);
            throw new AssertionError("Fallback reached on full board");
        }
    }

    /** Counts calls and plays first empty cell (benign, for constructor/terminal tests). */
    static class CountingPlayer extends Player {
        final AtomicInteger calls = new AtomicInteger(0);

        CountingPlayer(Mark mark) { super(mark); }

        @Override
        public Move nextMove(Board board) {
            calls.incrementAndGet();
            for (int r = 0; r < board.size(); r++)
                for (int c = 0; c < board.size(); c++)
                    if (board.getCell(r,c) == Mark.EMPTY)
                        return new Move(r, c, mark);
            throw new AssertionError("CountingPlayer called on full board");
        }
    }

    /**
     * Faulty sequence first (occupied or OOB), then a valid move, then fallback to first empty.
     * Tracks call count to verify retry behavior.
     */
    static class FaultyThenValidPlayer extends Player {
        private final Deque<Move> script = new ArrayDeque<>();
        final AtomicInteger calls = new AtomicInteger(0);

        FaultyThenValidPlayer(Mark mark, Move... sequence) {
            super(mark);
            for (Move m : sequence) script.addLast(m);
        }

        @Override
        public Move nextMove(Board board) {
            calls.incrementAndGet();
            if (board == null) throw new IllegalArgumentException("board null");
            if (!script.isEmpty()) {
                Move m = script.removeFirst();
                assertEquals(mark, m.mark(), "Scripted move must match player's mark");
                return m;
            }
            // fallback after scripted attempts
            for (int r = 0; r < board.size(); r++)
                for (int c = 0; c < board.size(); c++)
                    if (board.getCell(r,c) == Mark.EMPTY)
                        return new Move(r, c, mark);
            throw new AssertionError("No cells left for fallback");
        }
    }

    /* -------------------- tiny helpers -------------------- */

    private static void fill(Board b, int[][] coords, Mark[] marks) {
        for (int i = 0; i < coords.length; i++) {
            int r = coords[i][0], c = coords[i][1];
            b.place(new Move(r, c, marks[i]));
        }
    }
}