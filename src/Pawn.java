/**
 * Represents a pawn. Moves forward one square, two from the starting row,
 * and captures diagonally one square forward.
 * Direction is upward (decreasing row) for white and downward for black.
 */
public class Pawn extends Piece {

    public Pawn(String color) {
        super(color);
    }

    @Override
    public String getType() {
        return "Pawn";
    }

    /**
     * Pawn movement rules:
     * - One step forward into an empty square.
     * - Two steps forward from the starting row if both squares ahead are empty.
     * - One step diagonally forward only when capturing an opponent's piece.
     */
    @Override
    public boolean isValidMove(int fromRow, int fromCol, int toRow, int toCol, Piece[][] board) {
        int direction = getColor().equals("white") ? -1 : 1;
        int startRow  = getColor().equals("white") ? 6 : 1;

        // One step forward into an empty square
        if (toCol == fromCol && toRow == fromRow + direction && board[toRow][toCol] == null)
            return true;

        // Two steps forward from the starting row — both squares ahead must be empty
        if (toCol == fromCol && fromRow == startRow
                && toRow == fromRow + 2 * direction
                && board[fromRow + direction][fromCol] == null
                && board[toRow][toCol] == null)
            return true;

        // Diagonal capture — only valid if an opponent's piece occupies that square
        if (Math.abs(toCol - fromCol) == 1 && toRow == fromRow + direction
                && board[toRow][toCol] != null)
            return true;

        return false;
    }
}
