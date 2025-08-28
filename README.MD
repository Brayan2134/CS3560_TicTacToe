# Tic-Tac-Toe (Java, Encapsulation & Inheritance)

## Introduction
Assignment 1, CS3560. <br/>
This project implements a simple **Tic-Tac-Toe game** in Java as a hands-on exercise in **encapsulation, inheritance, and polymorphism**.

The problem statement we address is:
> *Design and implement a Tic-Tac-Toe game in Java that not only works correctly but also demonstrates core object-oriented principles. Specifically, the project challenges us to:
Apply encapsulation to protect game state using private fields, invariants, and narrow interfaces.
Use inheritance and polymorphism to model interchangeable player types (e.g., Human vs. AI).
Separate concerns via the Model–View–Controller (MVC) pattern for testability and clarity.
Validate correctness through JUnit tests for the Board and Game classes.*

By solving this problem, we practice structuring an object-oriented Java project that is testable, extensible, and readable.

---

## Solution Outline
The project is structured around the **Model-View-Controller (MVC)** pattern:

- **Model (game state & rules)**
    - `Mark` – enum for `X`, `O`, `EMPTY`.
    - `Move` – immutable record of a single move (`row`, `col`, `mark`).
    - `Board` – encapsulates the grid, enforces valid moves, checks for winner/draw.

- **Controller / Orchestrator**
    - `Game` – runs the game loop, alternating between players, checking rules after each move.

- **Players (Inheritance & Polymorphism)**
    - `Player` – abstract base class with `nextMove(Board)`.
    - `HumanPlayer` – asks the user for input via console.
    - `RandomAIPlayer` – picks a random empty cell.
    - `MinimaxAIPlayer` - uses the Minimax algorithm to try to beat a user.

- **View**
    - `Startup` - entry point of program.
    - `ConsoleApp` – handles text I/O and prints the board between moves.
    - `GUIApp` - handles graphical I/O and shows board between moves.