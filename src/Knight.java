/**
 * Represents a knight. Moves in an L-shape: two squares in one axis and
 * one square in the other. Knights can jump over other pieces.
 */
public class Knight extends Piece {

    public Knight(String color) {
        super(color);
    }

    @Override
    public String getType() {
        return "Knight";
    }

    /**
     * Valid if the move forms an L-shape (2+1 or 1+2 squares).
     * No path-clear check needed — knights jump over pieces.
     */
    @Override
    public boolean isValidMove(int fromRow, int fromCol, int toRow, int toCol, Piece[][] board) {
        int dr = Math.abs(toRow - fromRow);
        int dc = Math.abs(toCol - fromCol);
        return (dr == 2 && dc == 1) || (dr == 1 && dc == 2);
    }
}
