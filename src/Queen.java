/**
 * Represents a queen. Combines the movement of a rook and a bishop —
 * any number of squares horizontally, vertically, or diagonally,
 * with no pieces blocking the path.
 */
public class Queen extends Piece {

    public Queen(String color) {
        super(color);
    }

    @Override
    public String getType() {
        return "Queen";
    }

    /**
     * Valid if the move is either straight (rook-like) or diagonal (bishop-like),
     * with a clear path in either case.
     */
    @Override
    public boolean isValidMove(int fromRow, int fromCol, int toRow, int toCol, Piece[][] board) {
        boolean straight = (fromRow == toRow || fromCol == toCol);
        boolean diagonal = Math.abs(toRow - fromRow) == Math.abs(toCol - fromCol);
        if (!straight && !diagonal) return false;
        return isPathClear(fromRow, fromCol, toRow, toCol, board);
    }
}
