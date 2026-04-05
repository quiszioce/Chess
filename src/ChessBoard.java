import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Map;
import java.awt.RenderingHints;

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

    // Classic chess board colors
    private static final Color LIGHT_SQUARE  = new Color(240, 217, 181); // cream
    private static final Color DARK_SQUARE   = new Color(181, 136, 99);  // warm brown
    private static final Color HIGHLIGHT     = new Color(247, 247, 105); // yellow (selected)
    private static final Color HINT_LIGHT    = new Color(205, 210, 106); // legal move on light square
    private static final Color HINT_DARK     = new Color(170, 162, 58);  // legal move on dark square
    private static final Color BORDER_COLOR  = new Color(49, 46, 43);    // dark frame surround
    private static final Color STATUS_BG     = new Color(38, 36, 33);    // status bar background
    private static final Color COORD_COLOR   = new Color(181, 136, 99);  // coordinate label on light squares
    private static final Color COORD_COLOR_L = new Color(240, 217, 181); // coordinate label on dark squares

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

        JFrame frame = new JFrame("Alex's Chess Game");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        frame.getContentPane().setBackground(BORDER_COLOR);

        // --- Board grid ---
        JPanel boardPanel = new JPanel(new GridLayout(8, 8));
        boardPanel.setPreferredSize(new Dimension(480, 480)); // 60px per square
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

        // --- Rank labels (8 → 1) down the left side ---
        JPanel rankPanel = new JPanel(new GridLayout(8, 1));
        rankPanel.setBackground(BORDER_COLOR);
        rankPanel.setPreferredSize(new Dimension(22, 0));
        for (int row = 0; row < 8; row++) {
            JLabel lbl = new JLabel(String.valueOf(8 - row), SwingConstants.CENTER);
            lbl.setFont(new Font("Serif", Font.BOLD, 12));
            lbl.setForeground(new Color(200, 190, 180));
            rankPanel.add(lbl);
        }

        // --- File labels (a → h) along the bottom ---
        JPanel fileRow = new JPanel(new BorderLayout());
        fileRow.setBackground(BORDER_COLOR);
        fileRow.setPreferredSize(new Dimension(0, 20));

        // Spacer aligns file labels under the board (not under the rank panel)
        JPanel fileSpacer = new JPanel();
        fileSpacer.setBackground(BORDER_COLOR);
        fileSpacer.setPreferredSize(new Dimension(22, 0));

        JPanel filePanel = new JPanel(new GridLayout(1, 8));
        filePanel.setBackground(BORDER_COLOR);
        for (int col = 0; col < 8; col++) {
            JLabel lbl = new JLabel(String.valueOf((char) ('a' + col)), SwingConstants.CENTER);
            lbl.setFont(new Font("Serif", Font.BOLD, 12));
            lbl.setForeground(new Color(200, 190, 180));
            filePanel.add(lbl);
        }
        fileRow.add(fileSpacer, BorderLayout.WEST);
        fileRow.add(filePanel,  BorderLayout.CENTER);

        // --- Board wrapper: rank labels | board | file labels ---
        JPanel boardWrapper = new JPanel(new BorderLayout());
        boardWrapper.setBackground(BORDER_COLOR);
        boardWrapper.setBorder(BorderFactory.createEmptyBorder(12, 12, 0, 12));
        boardWrapper.add(rankPanel, BorderLayout.WEST);
        boardWrapper.add(boardPanel, BorderLayout.CENTER);
        boardWrapper.add(fileRow, BorderLayout.SOUTH);

        // --- Status bar ---
        statusLabel = new JLabel("White's turn", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Serif", Font.BOLD, 15));
        statusLabel.setForeground(new Color(240, 217, 181));

        JButton newGameButton = new JButton("New Game");
        newGameButton.setFont(new Font("Serif", Font.PLAIN, 13));
        newGameButton.setFocusPainted(false);
        newGameButton.setBackground(new Color(80, 70, 60));
        newGameButton.setForeground(new Color(240, 217, 181));
        newGameButton.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(120, 100, 80), 1),
            BorderFactory.createEmptyBorder(4, 12, 4, 12)
        ));
        newGameButton.addActionListener(e -> resetGame());

        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.setBackground(STATUS_BG);
        southPanel.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        southPanel.add(statusLabel,   BorderLayout.CENTER);
        southPanel.add(newGameButton, BorderLayout.EAST);

        frame.add(boardWrapper, BorderLayout.CENTER);
        frame.add(southPanel,   BorderLayout.SOUTH);
        frame.pack();
        frame.setLocationRelativeTo(null); // center on screen
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
                // Execute the move (movePiece also handles the rook for castling)
                boardState.movePiece(selectedRow, selectedCol, row, col);

                selectedRow = -1;
                selectedCol = -1;
                clearLegalMoves();
                handlePromotion(row, col); // promote before redraw if applicable
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
                statusLabel.setText("Checkmate!  " + winner + " wins!");
            } else {
                statusLabel.setText("Stalemate — it's a draw.");
            }
        } else if (inCheck) {
            statusLabel.setText(capitalize(turn) + "'s turn  —  Check!");
        } else {
            statusLabel.setText(capitalize(turn) + "'s turn");
        }
    }

    /**
     * Populates the legalMoves grid for the piece at (fromRow, fromCol) by
     * testing every board square as a potential destination.
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
     * Also draws small coordinate labels on the border squares.
     */
    private void refreshSquare(int row, int col) {
        JPanel panel = squares[row][col];
        panel.removeAll();
        panel.setLayout(new BorderLayout());

        boolean isSelected = (row == selectedRow && col == selectedCol);
        boolean isLight    = (row + col) % 2 == 0;

        // Determine background color
        Color bg;
        if (isSelected) {
            bg = HIGHLIGHT;
        } else if (legalMoves[row][col]) {
            bg = isLight ? HINT_LIGHT : HINT_DARK;
        } else {
            bg = isLight ? LIGHT_SQUARE : DARK_SQUARE;
        }
        panel.setBackground(bg);

        // Small coordinate labels on the edge squares
        // Rank number on the left column (col 0), file letter on the bottom row (row 7)
        if (col == 0 || row == 7) {
            JPanel coordLayer = new JPanel(new BorderLayout());
            coordLayer.setOpaque(false);
            Color coordColor = isLight ? COORD_COLOR : COORD_COLOR_L;

            if (col == 0) {
                JLabel rankLbl = new JLabel(String.valueOf(8 - row));
                rankLbl.setFont(new Font("Serif", Font.BOLD, 11));
                rankLbl.setForeground(coordColor);
                rankLbl.setBorder(BorderFactory.createEmptyBorder(2, 3, 0, 0));
                coordLayer.add(rankLbl, BorderLayout.NORTH);
            }

            if (row == 7) {
                JLabel fileLbl = new JLabel(String.valueOf((char) ('a' + col)));
                fileLbl.setFont(new Font("Serif", Font.BOLD, 11));
                fileLbl.setForeground(coordColor);
                fileLbl.setHorizontalAlignment(SwingConstants.RIGHT);
                fileLbl.setBorder(BorderFactory.createEmptyBorder(0, 0, 2, 3));
                coordLayer.add(fileLbl, BorderLayout.SOUTH);
            }

            panel.add(coordLayer, BorderLayout.CENTER);
        }

        // Render the piece as a Unicode symbol centered in the square
        Piece piece = boardState.getPiece(row, col);
        if (piece != null) {
            String symbol = piece.getColor().equals("white")
                ? WHITE_SYMBOLS.get(piece.getType())
                : BLACK_SYMBOLS.get(piece.getType());
            boolean isWhitePiece = piece.getColor().equals("white");
            panel.add(createPieceLabel(symbol, isWhitePiece), BorderLayout.CENTER);
        }

        panel.revalidate();
        panel.repaint();
    }

    /**
     * Creates a piece label that draws the symbol with a dark outline behind it.
     * This makes white pieces legible on both light and dark squares.
     */
    private JLabel createPieceLabel(String symbol, boolean isWhitePiece) {
        Color pieceColor  = isWhitePiece ? Color.WHITE : new Color(30, 20, 10);
        Color outlineColor = isWhitePiece
            ? new Color(60, 40, 20, 200)   // warm dark outline for white pieces
            : new Color(255, 255, 255, 60); // faint light outline for black pieces

        JLabel label = new JLabel(symbol, SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setFont(getFont());

                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth()  - fm.stringWidth(symbol)) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;

                // Draw outline by painting the symbol in the outline color at 1px offsets
                g2.setColor(outlineColor);
                for (int dx = -2; dx <= 2; dx++)
                    for (int dy = -2; dy <= 2; dy++)
                        if (dx != 0 || dy != 0)
                            g2.drawString(symbol, x + dx, y + dy);

                // Draw the piece itself on top
                g2.setColor(pieceColor);
                g2.drawString(symbol, x, y);
                g2.dispose();
            }
        };
        label.setFont(new Font("Serif", Font.PLAIN, 44));
        return label;
    }

    /**
     * Checks whether the piece at (row, col) is a pawn that has reached the
     * opposite back rank. If so, shows a dialog letting the player choose a
     * promotion piece (Queen, Rook, Bishop, or Knight) and replaces it.
     * Defaults to Queen if the dialog is dismissed without a selection.
     */
    private void handlePromotion(int row, int col) {
        Piece piece = boardState.getPiece(row, col);
        if (!(piece instanceof Pawn)) return;

        boolean isWhite = piece.getColor().equals("white");
        if (isWhite && row != 0) return;
        if (!isWhite && row != 7) return;

        String color = piece.getColor();
        Map<String, String> symbols = isWhite ? WHITE_SYMBOLS : BLACK_SYMBOLS;

        // Build labelled buttons using the Unicode piece symbols
        String[] options = {
            symbols.get("Queen")  + "  Queen",
            symbols.get("Rook")   + "  Rook",
            symbols.get("Bishop") + "  Bishop",
            symbols.get("Knight") + "  Knight"
        };

        int choice = JOptionPane.showOptionDialog(
            null,
            "Choose a piece to promote to:",
            "Pawn Promotion",
            JOptionPane.DEFAULT_OPTION,
            JOptionPane.PLAIN_MESSAGE,
            null,
            options,
            options[0]
        );

        Piece promoted = switch (choice) {
            case 1  -> new Rook(color);
            case 2  -> new Bishop(color);
            case 3  -> new Knight(color);
            default -> new Queen(color); // includes case 0 and dialog-closed (-1)
        };

        boardState.setPiece(row, col, promoted);
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
