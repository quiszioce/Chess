/**
 * Represents a bishop. Moves any number of squares diagonally
 * with no pieces blocking the path.
 */
public class Bishop extends Piece {

    public Bishop(String color) {
        super(color);
    }

    @Override
    public String getType() {
        return "Bishop";
    }

    /**
     * Valid if the move is diagonal (equal row and column distance)
     * and all squares between origin and destination are empty.
     */
    @Override
    public boolean isValidMove(int fromRow, int fromCol, int toRow, int toCol, Piece[][] board) {
        if (Math.abs(toRow - fromRow) != Math.abs(toCol - fromCol)) return false;
        return isPathClear(fromRow, fromCol, toRow, toCol, board);
    }
}
