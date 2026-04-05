import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Map;

public class ChessBoard {

    // Map of piece types to their corresponding Unicode symbols for white and black pieces
    private static final Map<String, String> WHITE_SYMBOLS = Map.of(
        "King", "♔", "Queen", "♕", "Rook", "♖",
        "Bishop", "♗", "Knight", "♘", "Pawn", "♙"
    );
    private static final Map<String, String> BLACK_SYMBOLS = Map.of(
        "King", "♚", "Queen", "♛", "Rook", "♜",
        "Bishop", "♝", "Knight", "♞", "Pawn", "♟"
    );

    private static final Color LIGHT_SQUARE = Color.WHITE;
    private static final Color DARK_SQUARE = Color.BLACK;
    private static final Color HIGHLIGHT = new Color(186, 202, 68);

    private final BoardState boardState;
    private final JPanel[][] squares = new JPanel[8][8];
    private JLabel statusLabel;
    private int selectedRow = -1;
    private int selectedCol = -1;
    private boolean gameOver = false;

    public ChessBoard() {
        boardState = new BoardState();

        JFrame frame = new JFrame("Alex's Chess Board");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        frame.setSize(400, 440);

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

    private void resetGame() {
        boardState.reset();
        selectedRow = -1;
        selectedCol = -1;
        gameOver = false;
        statusLabel.setText("White's turn");
        for (int row = 0; row < 8; row++)
            for (int col = 0; col < 8; col++)
                refreshSquare(row, col);
    }

    private void handleClick(int row, int col) {
        if (gameOver) return;

        if (selectedRow == -1) {
            // Select a square only if it has a piece belonging to the current player
            Piece piece = boardState.getPiece(row, col);
            if (piece != null && piece.getColor().equals(boardState.getCurrentTurn())) {
                selectedRow = row;
                selectedCol = col;
                refreshSquare(row, col);
            }
        } else {
            // Clicking the same square deselects
            if (row == selectedRow && col == selectedCol) {
                int prevRow = selectedRow, prevCol = selectedCol;
                selectedRow = -1;
                selectedCol = -1;
                refreshSquare(prevRow, prevCol);
                return;
            }

            if (boardState.isValidMove(selectedRow, selectedCol, row, col)) {
                boardState.setPiece(row, col, boardState.getPiece(selectedRow, selectedCol));
                boardState.setPiece(selectedRow, selectedCol, null);

                int prevRow = selectedRow, prevCol = selectedCol;
                selectedRow = -1;
                selectedCol = -1;
                refreshSquare(prevRow, prevCol);
                refreshSquare(row, col);

                boardState.switchTurn();
                updateStatus();
            } else {
                // Invalid move — try selecting a new piece of the same color instead
                Piece clicked = boardState.getPiece(row, col);
                int prevRow = selectedRow, prevCol = selectedCol;
                selectedRow = -1;
                selectedCol = -1;
                refreshSquare(prevRow, prevCol);

                if (clicked != null && clicked.getColor().equals(boardState.getCurrentTurn())) {
                    selectedRow = row;
                    selectedCol = col;
                    refreshSquare(row, col);
                }
            }
        }
    }

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

    private String capitalize(String s) {
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    private void refreshSquare(int row, int col) {
        JPanel panel = squares[row][col];
        panel.removeAll();

        boolean isSelected = (row == selectedRow && col == selectedCol);
        if (isSelected) {
            panel.setBackground(HIGHLIGHT);
        } else {
            panel.setBackground((row + col) % 2 == 0 ? LIGHT_SQUARE : DARK_SQUARE);
        }

        // Get the piece at the current position and add its symbol to the panel
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

    public static void main(String[] args) {
        new ChessBoard();
    }
}
