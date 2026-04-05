public class BoardState {
    private Piece[][] board;

    public BoardState() {
        this.board = new Piece[8][8];
        initializeBoard();
    }

    public Piece getPiece(int row, int col) {
        return board[row][col];
    }

    public void setPiece(int row, int col, Piece piece) {
        board[row][col] = piece;
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
