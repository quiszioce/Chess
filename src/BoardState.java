/**
 * Holds the current state of the chess board, including piece positions,
 * whose turn it is, and all move validation logic.
 */
public class BoardState {
    private Piece[][] board;
    private String currentTurn;

    /**
     * Creates a new BoardState with pieces in their standard starting positions
     * and sets the first turn to white.
     */
    public BoardState() {
        this.board = new Piece[8][8];
        this.currentTurn = "white";
        initializeBoard();
    }

    /**
     * Returns the piece at the given board position, or null if the square is empty.
     */
    public Piece getPiece(int row, int col) {
        return board[row][col];
    }

    /**
     * Places the given piece at the specified board position.
     * Pass null to clear a square.
     */
    public void setPiece(int row, int col, Piece piece) {
        board[row][col] = piece;
    }

    /**
     * Returns the color whose turn it currently is ("white" or "black").
     */
    public String getCurrentTurn() {
        return currentTurn;
    }

    /**
     * Switches the active turn from white to black or vice versa.
     * Called after every successfully completed move.
     */
    public void switchTurn() {
        currentTurn = currentTurn.equals("white") ? "black" : "white";
    }

    /**
     * Returns true if moving the piece from (fromRow, fromCol) to (toRow, toCol)
     * is fully legal — the piece can physically make the move AND the move does
     * not leave the moving player's own king in check.
     */
    public boolean isValidMove(int fromRow, int fromCol, int toRow, int toCol) {
        if (!isRawValidMove(fromRow, fromCol, toRow, toCol)) return false;
        return !moveLeavesKingInCheck(fromRow, fromCol, toRow, toCol);
    }

    /**
     * Returns true if the king of the given color is currently under attack
     * by any opponent piece.
     */
    public boolean isInCheck(String color) {
        int[] kingPos = findKing(color);
        if (kingPos == null) return false;
        String opponent = color.equals("white") ? "black" : "white";

        // Check whether any opponent piece can reach the king using raw move rules
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece p = board[r][c];
                if (p != null && p.getColor().equals(opponent)) {
                    if (isRawValidMove(r, c, kingPos[0], kingPos[1])) return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns true if the given color has at least one legal move available.
     * Used to detect checkmate (in check + no moves) and stalemate (not in check + no moves).
     */
    public boolean hasLegalMoves(String color) {
        for (int fromRow = 0; fromRow < 8; fromRow++) {
            for (int fromCol = 0; fromCol < 8; fromCol++) {
                Piece p = board[fromRow][fromCol];
                if (p == null || !p.getColor().equals(color)) continue;
                for (int toRow = 0; toRow < 8; toRow++) {
                    for (int toCol = 0; toCol < 8; toCol++) {
                        if (isValidMove(fromRow, fromCol, toRow, toCol)) return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Temporarily applies the move on the board, checks whether the moving
     * player's king ends up in check, then undoes the move and returns the result.
     */
    private boolean moveLeavesKingInCheck(int fromRow, int fromCol, int toRow, int toCol) {
        Piece moving   = board[fromRow][fromCol];
        Piece captured = board[toRow][toCol];

        // Apply the move
        board[toRow][toCol]     = moving;
        board[fromRow][fromCol] = null;

        boolean inCheck = isInCheck(moving.getColor());

        // Undo the move
        board[fromRow][fromCol] = moving;
        board[toRow][toCol]     = captured;

        return inCheck;
    }

    /**
     * Validates a move based solely on how the piece moves — does not account
     * for whether the move leaves the king in check. Used internally by isValidMove
     * and isInCheck (to avoid infinite recursion).
     */
    private boolean isRawValidMove(int fromRow, int fromCol, int toRow, int toCol) {
        Piece piece = board[fromRow][fromCol];
        if (piece == null) return false;
        if (fromRow == toRow && fromCol == toCol) return false;

        // Cannot capture your own piece
        Piece target = board[toRow][toCol];
        if (target != null && target.getColor().equals(piece.getColor())) return false;

        return switch (piece.getType()) {
            case "Pawn"   -> isValidPawnMove(fromRow, fromCol, toRow, toCol, piece.getColor());
            case "Rook"   -> isValidRookMove(fromRow, fromCol, toRow, toCol);
            case "Knight" -> isValidKnightMove(fromRow, fromCol, toRow, toCol);
            case "Bishop" -> isValidBishopMove(fromRow, fromCol, toRow, toCol);
            case "Queen"  -> isValidRookMove(fromRow, fromCol, toRow, toCol)
                          || isValidBishopMove(fromRow, fromCol, toRow, toCol);
            case "King"   -> isValidKingMove(fromRow, fromCol, toRow, toCol);
            default       -> false;
        };
    }

    /**
     * Validates pawn movement rules:
     * - One step forward into an empty square.
     * - Two steps forward from the starting row if both squares ahead are empty.
     * - One step diagonally forward only when capturing an opponent's piece.
     * Direction is upward (row decreases) for white and downward for black.
     */
    private boolean isValidPawnMove(int fromRow, int fromCol, int toRow, int toCol, String color) {
        int direction = color.equals("white") ? -1 : 1;
        int startRow  = color.equals("white") ? 6 : 1;

        // One step forward into an empty square
        if (toCol == fromCol && toRow == fromRow + direction && board[toRow][toCol] == null)
            return true;

        // Two steps forward from the starting row, both intermediate and target squares must be empty
        if (toCol == fromCol && fromRow == startRow
                && toRow == fromRow + 2 * direction
                && board[fromRow + direction][fromCol] == null
                && board[toRow][toCol] == null)
            return true;

        // Diagonal capture — only valid if there is an opponent piece on that square
        if (Math.abs(toCol - fromCol) == 1 && toRow == fromRow + direction
                && board[toRow][toCol] != null)
            return true;

        return false;
    }

    /**
     * Validates rook movement: must move in a straight line (same row or same column)
     * with no pieces blocking the path.
     */
    private boolean isValidRookMove(int fromRow, int fromCol, int toRow, int toCol) {
        if (fromRow != toRow && fromCol != toCol) return false;
        return isPathClear(fromRow, fromCol, toRow, toCol);
    }

    /**
     * Validates knight movement: must move in an L-shape (2 squares in one axis,
     * 1 square in the other). Knights can jump over other pieces.
     */
    private boolean isValidKnightMove(int fromRow, int fromCol, int toRow, int toCol) {
        int dr = Math.abs(toRow - fromRow);
        int dc = Math.abs(toCol - fromCol);
        return (dr == 2 && dc == 1) || (dr == 1 && dc == 2);
    }

    /**
     * Validates bishop movement: must move diagonally (equal row and column distance)
     * with no pieces blocking the path.
     */
    private boolean isValidBishopMove(int fromRow, int fromCol, int toRow, int toCol) {
        if (Math.abs(toRow - fromRow) != Math.abs(toCol - fromCol)) return false;
        return isPathClear(fromRow, fromCol, toRow, toCol);
    }

    /**
     * Validates king movement: can move exactly one square in any direction.
     */
    private boolean isValidKingMove(int fromRow, int fromCol, int toRow, int toCol) {
        return Math.abs(toRow - fromRow) <= 1 && Math.abs(toCol - fromCol) <= 1;
    }

    /**
     * Returns true if every square between the two positions (exclusive) is empty.
     * Works for straight lines (rook) and diagonals (bishop) by using step signs.
     */
    private boolean isPathClear(int fromRow, int fromCol, int toRow, int toCol) {
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

    /**
     * Scans the board and returns the [row, col] position of the given color's king.
     * Returns null if the king is not found (should not happen in a valid game).
     */
    private int[] findKing(String color) {
        for (int r = 0; r < 8; r++)
            for (int c = 0; c < 8; c++) {
                Piece p = board[r][c];
                if (p != null && p.getColor().equals(color) && p.getType().equals("King"))
                    return new int[]{r, c};
            }
        return null;
    }

    /**
     * Resets the board to its initial state: all pieces in starting positions
     * and the turn set back to white.
     */
    public void reset() {
        this.board = new Piece[8][8];
        this.currentTurn = "white";
        initializeBoard();
    }

    /**
     * Places all 32 pieces in their standard chess starting positions.
     * Rows 0-1 are black, rows 6-7 are white.
     */
    private void initializeBoard() {
        String[] backRank = {"Rook", "Knight", "Bishop", "Queen", "King", "Bishop", "Knight", "Rook"};
        for (int col = 0; col < 8; col++) {
            board[0][col] = new Piece(backRank[col], "black");
            board[1][col] = new Piece("Pawn", "black");
            board[6][col] = new Piece("Pawn", "white");
            board[7][col] = new Piece(backRank[col], "white");
        }
    }
}
