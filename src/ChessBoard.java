import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Map;

/**
 * The main UI class for the chess game.
 * Builds the Swing window, renders the board and pieces, and handles all
 * player interaction (selecting, moving, highlighting legal moves).
 */
public class ChessBoard {

    // Unicode chess symbols keyed by piece type, one map per color
    private static final Map<String, String> WHITE_SYMBOLS = Map.of(
        "King", "♔", "Queen", "♕", "Rook", "♖",
        "Bishop", "♗", "Knight", "♘", "Pawn", "♙"
    );
    private static final Map<String, String> BLACK_SYMBOLS = Map.of(
        "King", "♚", "Queen", "♛", "Rook", "♜",
        "Bishop", "♝", "Knight", "♞", "Pawn", "♟"
    );

    // Square background colors: base, selected, and legal-move hints (light/dark variant)
    private static final Color LIGHT_SQUARE = Color.WHITE;
    private static final Color DARK_SQUARE  = Color.BLACK;
    private static final Color HIGHLIGHT    = new Color(186, 202, 68);  // yellow-green for selected piece
    private static final Color HINT_LIGHT   = new Color(187, 203, 143); // muted green on light squares
    private static final Color HINT_DARK    = new Color(100, 111, 64);  // muted green on dark squares

    private final BoardState boardState;
    private final JPanel[][] squares     = new JPanel[8][8];
    private final boolean[][] legalMoves = new boolean[8][8];
    private JLabel statusLabel;
    private int selectedRow = -1;
    private int selectedCol = -1;
    private boolean gameOver = false;

    /**
     * Constructs the chess window, initialises the board state, builds the
     * 8x8 grid of panels, attaches click listeners, and adds the status bar.
     */
    public ChessBoard() {
        boardState = new BoardState();

        JFrame frame = new JFrame("Alex's Chess Board");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        frame.setSize(400, 440);

        // Build the 8x8 grid
        JPanel boardPanel = new JPanel(new GridLayout(8, 8));
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                JPanel panel = new JPanel(new BorderLayout());
                squares[row][col] = panel;
                refreshSquare(row, col);

                final int r = row, c = col;
                panel.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        handleClick(r, c);
                    }
                });

                boardPanel.add(panel);
            }
        }

        // Status bar: turn label on the left, New Game button on the right
        statusLabel = new JLabel("White's turn", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Serif", Font.BOLD, 16));

        JButton newGameButton = new JButton("New Game");
        newGameButton.setFont(new Font("Serif", Font.PLAIN, 14));
        newGameButton.addActionListener(e -> resetGame());

        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
        southPanel.add(statusLabel, BorderLayout.CENTER);
        southPanel.add(newGameButton, BorderLayout.EAST);

        frame.add(boardPanel, BorderLayout.CENTER);
        frame.add(southPanel, BorderLayout.SOUTH);
        frame.setVisible(true);
    }

    /**
     * Resets all game state — board, selection, legal-move highlights, and the
     * status label — and redraws the full board for a fresh game.
     */
    private void resetGame() {
        boardState.reset();
        selectedRow = -1;
        selectedCol = -1;
        gameOver = false;
        clearLegalMoves();
        statusLabel.setText("White's turn");
        refreshAllSquares();
    }

    /**
     * Handles a click on the square at (row, col).
     *
     * First click:  selects the piece if it belongs to the current player,
     *               then highlights all its legal destinations.
     * Second click: if the destination is legal, moves the piece there and
     *               advances the turn; if illegal, tries to re-select a new
     *               friendly piece at the clicked square instead.
     * Same square:  deselects the currently selected piece.
     */
    private void handleClick(int row, int col) {
        if (gameOver) return;

        if (selectedRow == -1) {
            // Select a square only if it holds a piece belonging to the current player
            Piece piece = boardState.getPiece(row, col);
            if (piece != null && piece.getColor().equals(boardState.getCurrentTurn())) {
                selectedRow = row;
                selectedCol = col;
                computeLegalMoves(row, col);
                refreshAllSquares();
            }
        } else {
            // Clicking the selected square again deselects it
            if (row == selectedRow && col == selectedCol) {
                selectedRow = -1;
                selectedCol = -1;
                clearLegalMoves();
                refreshAllSquares();
                return;
            }

            if (boardState.isValidMove(selectedRow, selectedCol, row, col)) {
                // Execute the move
                boardState.setPiece(row, col, boardState.getPiece(selectedRow, selectedCol));
                boardState.setPiece(selectedRow, selectedCol, null);

                selectedRow = -1;
                selectedCol = -1;
                clearLegalMoves();
                refreshAllSquares();

                boardState.switchTurn();
                updateStatus();
            } else {
                // Invalid destination — clear selection, but re-select if the square
                // holds another friendly piece
                Piece clicked = boardState.getPiece(row, col);
                selectedRow = -1;
                selectedCol = -1;
                clearLegalMoves();

                if (clicked != null && clicked.getColor().equals(boardState.getCurrentTurn())) {
                    selectedRow = row;
                    selectedCol = col;
                    computeLegalMoves(row, col);
                }
                refreshAllSquares();
            }
        }
    }

    /**
     * Called after every move to evaluate the new game state and update the
     * status label. Detects checkmate, stalemate, and simple check.
     */
    private void updateStatus() {
        String turn = boardState.getCurrentTurn();
        boolean inCheck  = boardState.isInCheck(turn);
        boolean hasMoves = boardState.hasLegalMoves(turn);

        if (!hasMoves) {
            gameOver = true;
            if (inCheck) {
                String winner = turn.equals("white") ? "Black" : "White";
                statusLabel.setText("Checkmate! " + winner + " wins!");
            } else {
                statusLabel.setText("Stalemate! It's a draw.");
            }
        } else if (inCheck) {
            statusLabel.setText(capitalize(turn) + "'s turn — Check!");
        } else {
            statusLabel.setText(capitalize(turn) + "'s turn");
        }
    }

    /**
     * Populates the legalMoves grid for the piece at (fromRow, fromCol) by
     * testing every board square as a potential destination.
     * Called whenever a piece is selected so the hints can be rendered.
     */
    private void computeLegalMoves(int fromRow, int fromCol) {
        for (int r = 0; r < 8; r++)
            for (int c = 0; c < 8; c++)
                legalMoves[r][c] = boardState.isValidMove(fromRow, fromCol, r, c);
    }

    /**
     * Resets all entries in the legalMoves grid to false.
     * Called on deselect, after a move completes, or on new game.
     */
    private void clearLegalMoves() {
        for (int r = 0; r < 8; r++)
            for (int c = 0; c < 8; c++)
                legalMoves[r][c] = false;
    }

    /**
     * Redraws every square on the board. Called whenever the selection or
     * board state changes so the entire view stays consistent.
     */
    private void refreshAllSquares() {
        for (int r = 0; r < 8; r++)
            for (int c = 0; c < 8; c++)
                refreshSquare(r, c);
    }

    /**
     * Redraws a single square at (row, col): sets its background color based
     * on whether it is selected, a legal-move hint, or a plain square,
     * then renders the piece symbol (if any) as a centered label.
     */
    private void refreshSquare(int row, int col) {
        JPanel panel = squares[row][col];
        panel.removeAll();

        boolean isSelected = (row == selectedRow && col == selectedCol);
        boolean isLight    = (row + col) % 2 == 0;

        if (isSelected) {
            panel.setBackground(HIGHLIGHT);
        } else if (legalMoves[row][col]) {
            // Use the shade that matches the underlying square color
            panel.setBackground(isLight ? HINT_LIGHT : HINT_DARK);
        } else {
            panel.setBackground(isLight ? LIGHT_SQUARE : DARK_SQUARE);
        }

        // Render the piece as a Unicode symbol if the square is occupied
        Piece piece = boardState.getPiece(row, col);
        if (piece != null) {
            String symbol = piece.getColor().equals("white")
                ? WHITE_SYMBOLS.get(piece.getType())
                : BLACK_SYMBOLS.get(piece.getType());
            JLabel label = new JLabel(symbol, SwingConstants.CENTER);
            label.setFont(new Font("Serif", Font.PLAIN, 36));
            label.setForeground(piece.getColor().equals("white") ? Color.LIGHT_GRAY : Color.DARK_GRAY);
            panel.add(label);
        }

        panel.revalidate();
        panel.repaint();
    }

    /**
     * Returns the given string with its first character uppercased.
     * Used to format color names ("white" → "White") in the status label.
     */
    private String capitalize(String s) {
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    public static void main(String[] args) {
        new ChessBoard();
    }
}
