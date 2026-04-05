/**
 * Abstract base class for all chess pieces.
 * Each subclass represents a specific piece type and implements its own
 * movement rules via isValidMove().
 */
public abstract class Piece {
    private String color;
    private boolean hasMoved = false;

    /**
     * @param color the owning player's color — "white" or "black"
     */
    public Piece(String color) {
        this.color = color;
    }

    /** Returns the piece type name (e.g. "Rook"). Defined by each subclass. */
    public abstract String getType();

    /**
     * Returns true if this piece can move from (fromRow, fromCol) to (toRow, toCol)
     * based solely on how this piece moves — does not account for king safety.
     * The board is passed in so pieces can check whether squares are empty or occupied.
     */
    public abstract boolean isValidMove(int fromRow, int fromCol, int toRow, int toCol, Piece[][] board);

    /** Returns the piece color ("white" or "black"). */
    public String getColor() {
        return color;
    }

    /** Sets the piece color. */
    public void setColor(String color) {
        this.color = color;
    }

    /** Returns true if this piece has moved at least once this game. */
    public boolean hasMoved() {
        return hasMoved;
    }

    /** Marks this piece as having moved. Called by BoardState.movePiece(). */
    public void setMoved() {
        hasMoved = true;
    }

    /**
     * Returns true if every square between the two positions (exclusive) is empty.
     * Works for straight lines (rook) and diagonals (bishop) by deriving step direction
     * from the sign of the row/column difference.
     */
    protected boolean isPathClear(int fromRow, int fromCol, int toRow, int toCol, Piece[][] board) {
        int rowStep = Integer.signum(toRow - fromRow);
        int colStep = Integer.signum(toCol - fromCol);
        int r = fromRow + rowStep;
        int c = fromCol + colStep;
        while (r != toRow || c != toCol) {
            if (board[r][c] != null) return false;
            r += rowStep;
            c += colStep;
        }
        return true;
    }
}
