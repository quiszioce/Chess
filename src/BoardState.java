/**
 * Holds the current state of the chess board, including piece positions,
 * whose turn it is, and move validation / game-state detection logic.
 * Per-piece movement rules live in each Piece subclass.
 */
public class BoardState {
    private Piece[][] board;
    private String currentTurn;

    /**
     * Tracks the square a pawn can move TO for an en passant capture.
     * Set after a double pawn push; cleared after any other move.
     * Null when no en passant capture is available.
     */
    private int[] enPassantTarget;

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
     * Moves the piece from (fromRow, fromCol) to (toRow, toCol), marks it as moved,
     * updates the en passant target, and handles the side effects of special moves:
     * - Castling: also moves the rook to its post-castle square.
     * - En passant: removes the captured pawn from the board.
     * All move execution in ChessBoard should go through this method.
     */
    public void movePiece(int fromRow, int fromCol, int toRow, int toCol) {
        Piece piece = board[fromRow][fromCol];

        // Detect en passant capture before moving (destination is empty, pawn moves diagonally)
        boolean isEnPassant = piece instanceof Pawn
                && fromCol != toCol
                && board[toRow][toCol] == null;

        // Update en passant target: set on double pawn push, clear otherwise
        if (piece instanceof Pawn && Math.abs(toRow - fromRow) == 2) {
            enPassantTarget = new int[]{(fromRow + toRow) / 2, fromCol};
        } else {
            enPassantTarget = null;
        }

        // Execute the move
        board[toRow][toCol]     = piece;
        board[fromRow][fromCol] = null;
        piece.setMoved();

        // Remove the captured pawn for en passant (it sits on fromRow, not toRow)
        if (isEnPassant) {
            board[fromRow][toCol] = null;
        }

        // Castling: also move the rook to its post-castle square
        if (piece instanceof King && fromRow == toRow && Math.abs(toCol - fromCol) == 2) {
            boolean kingSide = toCol > fromCol;
            int rookFromCol  = kingSide ? 7 : 0;
            int rookToCol    = kingSide ? toCol - 1 : toCol + 1;
            Piece rook       = board[fromRow][rookFromCol];
            board[fromRow][rookToCol]   = rook;
            board[fromRow][rookFromCol] = null;
            rook.setMoved();
        }
    }

    /**
     * Returns true if moving the piece from (fromRow, fromCol) to (toRow, toCol)
     * is fully legal — the piece can physically make the move AND the move does
     * not leave the moving player's own king in check.
     * Castling additionally requires the king not to start in or pass through check.
     */
    public boolean isValidMove(int fromRow, int fromCol, int toRow, int toCol) {
        if (!isRawValidMove(fromRow, fromCol, toRow, toCol)) return false;

        Piece piece = board[fromRow][fromCol];

        // Castling has extra safety checks beyond moveLeavesKingInCheck
        if (piece instanceof King && fromRow == toRow && Math.abs(toCol - fromCol) == 2) {
            return isCastlingSafe(fromRow, fromCol, toCol);
        }

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

        // Check whether any opponent piece can attack the king square
        for (int r = 0; r < 8; r++)
            for (int c = 0; c < 8; c++) {
                Piece p = board[r][c];
                if (p != null && p.getColor().equals(opponent))
                    if (isRawValidMove(r, c, kingPos[0], kingPos[1])) return true;
            }
        return false;
    }

    /**
     * Returns true if the given color has at least one legal move available.
     * Used to detect checkmate (in check + no moves) and stalemate (not in check + no moves).
     */
    public boolean hasLegalMoves(String color) {
        for (int fromRow = 0; fromRow < 8; fromRow++)
            for (int fromCol = 0; fromCol < 8; fromCol++) {
                Piece p = board[fromRow][fromCol];
                if (p == null || !p.getColor().equals(color)) continue;
                for (int toRow = 0; toRow < 8; toRow++)
                    for (int toCol = 0; toCol < 8; toCol++)
                        if (isValidMove(fromRow, fromCol, toRow, toCol)) return true;
            }
        return false;
    }

    /**
     * Temporarily applies the move on the board, checks whether the moving
     * player's king ends up in check, then fully undoes the move.
     * Also simulates en passant pawn removal so that discovered checks through
     * the captured pawn are detected correctly.
     */
    private boolean moveLeavesKingInCheck(int fromRow, int fromCol, int toRow, int toCol) {
        Piece moving   = board[fromRow][fromCol];
        Piece captured = board[toRow][toCol];

        // Detect if this is an en passant capture
        boolean isEnPassant = moving instanceof Pawn
                && fromCol != toCol
                && captured == null
                && enPassantTarget != null
                && toRow == enPassantTarget[0]
                && toCol == enPassantTarget[1];

        // Apply the move
        board[toRow][toCol]     = moving;
        board[fromRow][fromCol] = null;

        // Also remove the en passant captured pawn during simulation
        Piece epPawn = null;
        if (isEnPassant) {
            epPawn = board[fromRow][toCol];
            board[fromRow][toCol] = null;
        }

        boolean inCheck = isInCheck(moving.getColor());

        // Undo everything
        board[fromRow][fromCol] = moving;
        board[toRow][toCol]     = captured;
        if (isEnPassant) board[fromRow][toCol] = epPawn;

        return inCheck;
    }

    /**
     * Validates a move on piece-movement rules and shared constraints only —
     * does not account for king safety (used internally to avoid infinite recursion).
     * Handles en passant and castling as special cases before delegating to the piece.
     */
    private boolean isRawValidMove(int fromRow, int fromCol, int toRow, int toCol) {
        Piece piece = board[fromRow][fromCol];
        if (piece == null) return false;
        if (fromRow == toRow && fromCol == toCol) return false;

        Piece target = board[toRow][toCol];
        if (target != null && target.getColor().equals(piece.getColor())) return false;

        // Castling: king moves exactly 2 squares horizontally
        if (piece instanceof King && fromRow == toRow && Math.abs(toCol - fromCol) == 2)
            return isCastlingStructurallyValid(fromRow, fromCol, toCol);

        // En passant: pawn moves diagonally to the en passant target square
        if (piece instanceof Pawn && enPassantTarget != null
                && toRow == enPassantTarget[0] && toCol == enPassantTarget[1]
                && Math.abs(toCol - fromCol) == 1
                && board[toRow][toCol] == null) {
            int direction = piece.getColor().equals("white") ? -1 : 1;
            if (toRow == fromRow + direction) return true;
        }

        return piece.isValidMove(fromRow, fromCol, toRow, toCol, board);
    }

    /**
     * Checks the structural requirements for castling:
     * neither the king nor the chosen rook has moved, and
     * all squares between them are empty.
     */
    private boolean isCastlingStructurallyValid(int row, int kingCol, int toCol) {
        Piece king = board[row][kingCol];
        if (king.hasMoved()) return false;

        int rookCol = (toCol > kingCol) ? 7 : 0;
        Piece rook  = board[row][rookCol];
        if (!(rook instanceof Rook) || rook.hasMoved()) return false;

        int step = Integer.signum(rookCol - kingCol);
        for (int c = kingCol + step; c != rookCol; c += step)
            if (board[row][c] != null) return false;

        return true;
    }

    /**
     * Checks the safety requirements for castling:
     * the king must not currently be in check, must not pass through
     * an attacked square, and must not land on an attacked square.
     */
    private boolean isCastlingSafe(int row, int kingCol, int toCol) {
        if (isInCheck(board[row][kingCol].getColor())) return false;

        int step = Integer.signum(toCol - kingCol);
        if (moveLeavesKingInCheck(row, kingCol, row, kingCol + step)) return false;

        return !moveLeavesKingInCheck(row, kingCol, row, toCol);
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
     * Resets the board to its initial state: all pieces in starting positions,
     * turn set to white, and en passant target cleared.
     */
    public void reset() {
        this.board = new Piece[8][8];
        this.currentTurn = "white";
        this.enPassantTarget = null;
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
