/**
 * Represents a king. Moves exactly one square in any direction.
 */
public class King extends Piece {

    public King(String color) {
        super(color);
    }

    @Override
    public String getType() {
        return "King";
    }

    /**
     * Valid if the destination is at most one square away in any direction.
     */
    @Override
    public boolean isValidMove(int fromRow, int fromCol, int toRow, int toCol, Piece[][] board) {
        return Math.abs(toRow - fromRow) <= 1 && Math.abs(toCol - fromCol) <= 1;
    }
}
