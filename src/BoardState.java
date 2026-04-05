/**
 * Holds the current state of the chess board, including piece positions,
 * whose turn it is, and move validation / game-state detection logic.
 * Per-piece movement rules live in each Piece subclass.
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
     * Validates a move based on shared rules (not null, not same square, not
     * capturing own piece) then delegates to the piece's own isValidMove().
     * Does not account for king safety — used internally to avoid infinite recursion.
     */
    private boolean isRawValidMove(int fromRow, int fromCol, int toRow, int toCol) {
        Piece piece = board[fromRow][fromCol];
        if (piece == null) return false;
        if (fromRow == toRow && fromCol == toCol) return false;

        // Cannot capture your own piece
        Piece target = board[toRow][toCol];
        if (target != null && target.getColor().equals(piece.getColor())) return false;

        return piece.isValidMove(fromRow, fromCol, toRow, toCol, board);
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
     * Places all 32 pieces in their standard chess starting positions
     * using the correct subclass for each piece type.
     * Rows 0-1 are black, rows 6-7 are white.
     */
    private void initializeBoard() {
        for (int col = 0; col < 8; col++) {
            board[0][col] = createBackRankPiece(col, "black");
            board[1][col] = new Pawn("black");
            board[6][col] = new Pawn("white");
            board[7][col] = createBackRankPiece(col, "white");
        }
    }

    /**
     * Returns the correct back-rank piece for the given column and color.
     * Column order: Rook, Knight, Bishop, Queen, King, Bishop, Knight, Rook.
     */
    private Piece createBackRankPiece(int col, String color) {
        return switch (col) {
            case 0, 7 -> new Rook(color);
            case 1, 6 -> new Knight(color);
            case 2, 5 -> new Bishop(color);
            case 3    -> new Queen(color);
            case 4    -> new King(color);
            default   -> throw new IllegalArgumentException("Invalid column: " + col);
        };
    }
}
