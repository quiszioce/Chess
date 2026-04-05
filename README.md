# Chess

A fully playable two-player chess game built in Java with a Swing GUI.

## Features

- Full chess rule enforcement for all six piece types
- Legal move highlighting — select a piece to see all valid destinations
- Check, checkmate, and stalemate detection
- Special moves: castling, en passant, and pawn promotion
- Turn management with a status bar
- New Game button to reset at any point

## How to Run

**Requirements:** JDK 17 or later

```bash
# Compile
javac src/*.java -d out

# Run
java -cp out ChessBoard
```

Or open the project in an IDE (IntelliJ, Eclipse, VS Code) and run `ChessBoard.java`.

## How to Play

- Click a piece to select it — its legal moves are highlighted in green
- Click a highlighted square to move there
- Click the selected piece again to deselect
- Clicking an invalid square automatically re-selects if you clicked a friendly piece
- **Castling:** move the king two squares toward a rook
- **En passant:** available for one move immediately after an opponent's double pawn push
- **Pawn promotion:** a dialog appears when a pawn reaches the back rank

## Project Structure

```
src/
├── ChessBoard.java    # Swing UI, input handling, rendering
├── BoardState.java    # Board state, move validation, game logic
├── Piece.java         # Abstract base class for all pieces
├── Pawn.java
├── Rook.java
├── Knight.java
├── Bishop.java
├── Queen.java
└── King.java
```

## Tech Stack

- **Language:** Java
- **UI:** Java Swing
- **Build:** Javac (no external dependencies)

## Future Improvements

- Move history panel with algebraic notation
- AI opponent
- Draw conditions (50-move rule, threefold repetition)
