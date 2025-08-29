package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Optional;

/**
 * GUIApp.java
 * Desc: Simple Swing GUI for Tic-Tac-Toe.
 * - Session-lifetime scoreboard (clears when app closes).
 * - Reset button resets the board but keeps scores for this session.
 * - Human (X) plays by clicking; AI (O) is minimax.
 *
 * NOTE: This GUI drives the model directly (no blocking loop).
 */
public final class GUIApp {

    private JFrame frame;
    private JButton[][] cells;
    private JLabel statusLabel;
    private JLabel scoreLabel;

    private JButton resetButton;

    // Session-lifetime scores (cleared when app exits)
    private int xWins = 0;
    private int oWins = 0;
    private int draws = 0;

    // Model
    private Board board;
    private MinimaxAIPlayer ai; // O

    private boolean gameOver = false;

    /**
     * Preconditions:
     *  - args may be null or empty (ignored).
     * Postconditions:
     *  - Launches the Swing EDT and shows the GUI.
     *  - Returns immediately after scheduling the UI (non-blocking).
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new GUIApp().start());
    }

    /**
     * Launches the GUI; safe to call from Startup.
     * Preconditions:
     *  - None.
     * Postconditions:
     *  - Schedules the GUIApp startup on the Swing EDT.
     *  - Returns immediately (non-blocking).
     */
    public static void launch() {
        SwingUtilities.invokeLater(() -> new GUIApp().start());
    }

    /**
     * Preconditions:
     *  - Must run on the Swing EDT.
     *  - No visible frame must be assumed before calling.
     * Postconditions:
     *  - Initializes model objects (Board size 3, AI as minimax O).
     *  - Constructs and displays the main window with a 3×3 grid, status, score, and Reset button.
     *  - Sets initial status to "Your turn (X)" and enables all cells.
     */
    private void start() {
        board = new Board(3);
        ai = new MinimaxAIPlayer(Mark.O);

        frame = new JFrame("Tic-Tac-Toe (GUI)");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout(8, 8));

        // Top: status + score bar
        JPanel top = new JPanel(new BorderLayout());
        statusLabel = new JLabel("Your turn (X). Click a square.", SwingConstants.LEFT);
        statusLabel.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
        scoreLabel = new JLabel(scoreText(), SwingConstants.RIGHT);
        scoreLabel.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
        top.add(statusLabel, BorderLayout.WEST);
        top.add(scoreLabel, BorderLayout.EAST);

        // Center: 3x3 board
        JPanel grid = new JPanel(new GridLayout(3, 3, 4, 4));
        grid.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        cells = new JButton[3][3];
        Font f = new Font(Font.SANS_SERIF, Font.BOLD, 36);
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                JButton b = new JButton(" ");
                b.setFont(f);
                b.setFocusPainted(false);
                b.putClientProperty("r", r);
                b.putClientProperty("c", c);
                b.addActionListener(this::onHumanClick);
                cells[r][c] = b;
                grid.add(b);
            }
        }

        // Bottom: actions
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER));
        resetButton = new JButton("Reset");
        resetButton.addActionListener(e -> resetBoard());
        bottom.add(resetButton);

        frame.add(top, BorderLayout.NORTH);
        frame.add(grid, BorderLayout.CENTER);
        frame.add(bottom, BorderLayout.SOUTH);

        frame.setSize(420, 480);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    /* -------------------- UI handlers -------------------- */

    /**
     * Preconditions:
     *  - e != null and source is one of the 3×3 cell buttons.
     *  - The board and UI must be initialized (start() already called).
     * Postconditions:
     *  - If the clicked cell was EMPTY and game not over:
     *      - Places human X, re-renders, and checks for end state.
     *      - If game continues, requests an AI move, places O, re-renders, and checks for end state.
     *  - If the clicked cell was occupied or game over, no model state changes occur.
     *  - Status label reflects the next action or terminal outcome.
     *  - Board buttons become disabled if the game ends.
     * @throws IllegalArgumentException if the click resolves to invalid board indices (should not happen via guarded UI).
     */
    private void onHumanClick(ActionEvent e) {
        if (gameOver) return;

        JButton btn = (JButton) e.getSource();
        int r = (int) btn.getClientProperty("r");
        int c = (int) btn.getClientProperty("c");

        // Human plays X
        try {
            if (board.getCell(r, c) != Mark.EMPTY) {
                // ignore occupied clicks
                return;
            }
            board.place(new Move(r, c, Mark.X));
            render();
            if (checkEndThenMaybeLock()) return;

            // AI turn (O)
            Move aiMove = ai.nextMove(board);
            board.place(aiMove);
            render();
            if (checkEndThenMaybeLock()) return;

            statusLabel.setText("Your turn (X).");
        } catch (IllegalArgumentException ex) {
            // Shouldn't happen via guarded UI; show gentle status if it does
            statusLabel.setText("Invalid move: " + ex.getMessage());
        }
    }

    /**
     * Preconditions:
     *  - Board and UI have been initialized.
     * Postconditions:
     *  - Board is cleared to EMPTY in every cell.
     *  - gameOver flag is set to false; board buttons are enabled.
     *  - Status label updated to indicate a new round and X to move.
     *  - Scoreboard values (xWins, oWins, draws) are unchanged (persist for session).
     */
    private void resetBoard() {
        board.reset();
        gameOver = false;
        statusLabel.setText("New round. Your turn (X).");
        render();
        setBoardEnabled(true);
    }

    /* -------------------- rendering/end-state -------------------- */

    /**
     * Preconditions:
     *  - Board and UI have been initialized.
     * Postconditions:
     *  - Each cell button's text reflects the current board state (X, O, or blank).
     *  - Score label text is refreshed to match current counters.
     *  - Does not alter board state or gameOver flag.
     */
    private void render() {
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                Mark m = board.getCell(r, c);
                String text = (m == Mark.X) ? "X" : (m == Mark.O) ? "O" : " ";
                cells[r][c].setText(text);
            }
        }
        scoreLabel.setText(scoreText());
    }

    /**
     * Checks winner/draw; if game ended, updates scores/status and disables board.
     * Preconditions:
     *  - Board has a valid state; UI components exist.
     * Postconditions:
     *  - If a winner exists:
     *      - Increments xWins or oWins accordingly.
     *      - Updates status and score labels.
     *      - Sets gameOver = true and disables the board.
     *      - Returns true.
     *  - Else if the board is full (draw):
     *      - Increments draws.
     *      - Updates status and score labels.
     *      - Sets gameOver = true and disables the board.
     *      - Returns true.
     *  - Else:
     *      - Leaves gameOver = false, UI enabled.
     *      - Returns false.
     */
    private boolean checkEndThenMaybeLock() {
        Optional<Mark> w = board.winner();
        if (w.isPresent()) {
            if (w.get() == Mark.X) {
                xWins++;
                statusLabel.setText("You win! (X)");
            } else {
                oWins++;
                statusLabel.setText("AI wins! (O)");
            }
            scoreLabel.setText(scoreText());
            gameOver = true;
            setBoardEnabled(false);
            return true;
        }
        if (board.isFull()) {
            draws++;
            statusLabel.setText("Draw. Hit Reset to play again.");
            scoreLabel.setText(scoreText());
            gameOver = true;
            setBoardEnabled(false);
            return true;
        }
        return false;
    }

    /**
     * Preconditions:
     *  - cells[][] has been created in start().
     * Postconditions:
     *  - All 3×3 cell buttons have their enabled state set to 'enabled'.
     *  - No other UI or model state is modified.
     */
    private void setBoardEnabled(boolean enabled) {
        for (JButton[] row : cells) for (JButton b : row) b.setEnabled(enabled);
    }

    /**
     * Preconditions:
     *  - Score counters (xWins, oWins, draws) are non-negative integers.
     * Postconditions:
     *  - Returns a human-readable string summarizing the current session scores.
     *  - Does not mutate any state.
     */
    private String scoreText() {
        return String.format("Score. X = %d   O = %d   Draws = %d", xWins, oWins, draws);
    }
}
