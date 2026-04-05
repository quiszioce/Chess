/**
 * Represents a rook. Moves any number of squares horizontally or vertically
 * with no pieces blocking the path.
 */
public class Rook extends Piece {

    public Rook(String color) {
        super(color);
    }

    @Override
    public String getType() {
        return "Rook";
    }

    /**
     * Valid if the move is along the same row or same column,
     * and all squares between origin and destination are empty.
     */
    @Override
    public boolean isValidMove(int fromRow, int fromCol, int toRow, int toCol, Piece[][] board) {
        if (fromRow != toRow && fromCol != toCol) return false;
        return isPathClear(fromRow, fromCol, toRow, toCol, board);
    }
}
