public class BoardState {
    private Piece[][] board;
    private String currentTurn;

    public BoardState() {
        this.board = new Piece[8][8];
        this.currentTurn = "white";
        initializeBoard();
    }

    public Piece getPiece(int row, int col) {
        return board[row][col];
    }

    public void setPiece(int row, int col, Piece piece) {
        board[row][col] = piece;
    }

    public String getCurrentTurn() {
        return currentTurn;
    }

    public void switchTurn() {
        currentTurn = currentTurn.equals("white") ? "black" : "white";
    }

    // Full validation: piece rules + does not leave own king in check
    public boolean isValidMove(int fromRow, int fromCol, int toRow, int toCol) {
        if (!isRawValidMove(fromRow, fromCol, toRow, toCol)) return false;
        return !moveLeavesKingInCheck(fromRow, fromCol, toRow, toCol);
    }

    public boolean isInCheck(String color) {
        int[] kingPos = findKing(color);
        if (kingPos == null) return false;
        String opponent = color.equals("white") ? "black" : "white";
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

    // Simulate a move, check if it leaves own king in check, then undo
    private boolean moveLeavesKingInCheck(int fromRow, int fromCol, int toRow, int toCol) {
        Piece moving   = board[fromRow][fromCol];
        Piece captured = board[toRow][toCol];
        board[toRow][toCol]     = moving;
        board[fromRow][fromCol] = null;

        boolean inCheck = isInCheck(moving.getColor());

        board[fromRow][fromCol] = moving;
        board[toRow][toCol]     = captured;
        return inCheck;
    }

    // Raw piece movement rules only — no king-safety check
    private boolean isRawValidMove(int fromRow, int fromCol, int toRow, int toCol) {
        Piece piece = board[fromRow][fromCol];
        if (piece == null) return false;
        if (fromRow == toRow && fromCol == toCol) return false;

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

    private boolean isValidPawnMove(int fromRow, int fromCol, int toRow, int toCol, String color) {
        int direction = color.equals("white") ? -1 : 1;
        int startRow  = color.equals("white") ? 6 : 1;

        if (toCol == fromCol && toRow == fromRow + direction && board[toRow][toCol] == null)
            return true;

        if (toCol == fromCol && fromRow == startRow
                && toRow == fromRow + 2 * direction
                && board[fromRow + direction][fromCol] == null
                && board[toRow][toCol] == null)
            return true;

        if (Math.abs(toCol - fromCol) == 1 && toRow == fromRow + direction
                && board[toRow][toCol] != null)
            return true;

        return false;
    }

    private boolean isValidRookMove(int fromRow, int fromCol, int toRow, int toCol) {
        if (fromRow != toRow && fromCol != toCol) return false;
        return isPathClear(fromRow, fromCol, toRow, toCol);
    }

    private boolean isValidKnightMove(int fromRow, int fromCol, int toRow, int toCol) {
        int dr = Math.abs(toRow - fromRow);
        int dc = Math.abs(toCol - fromCol);
        return (dr == 2 && dc == 1) || (dr == 1 && dc == 2);
    }

    private boolean isValidBishopMove(int fromRow, int fromCol, int toRow, int toCol) {
        if (Math.abs(toRow - fromRow) != Math.abs(toCol - fromCol)) return false;
        return isPathClear(fromRow, fromCol, toRow, toCol);
    }

    private boolean isValidKingMove(int fromRow, int fromCol, int toRow, int toCol) {
        return Math.abs(toRow - fromRow) <= 1 && Math.abs(toCol - fromCol) <= 1;
    }

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

    private int[] findKing(String color) {
        for (int r = 0; r < 8; r++)
            for (int c = 0; c < 8; c++) {
                Piece p = board[r][c];
                if (p != null && p.getColor().equals(color) && p.getType().equals("King"))
                    return new int[]{r, c};
            }
        return null;
    }

    public void reset() {
        this.board = new Piece[8][8];
        this.currentTurn = "white";
        initializeBoard();
    }

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
