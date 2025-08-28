package org.example;

/**
 * Mark.java
 * Desc: Lists the three possible cell values on the board.
 * Invariants:
 *  - A cell is always exactly one of {X, O, EMPTY}.
 *  - EMPTY is used only to indicate an unused cell.
 */
public enum Mark {
    X, O, EMPTY;
}
