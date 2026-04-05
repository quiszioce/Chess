import javax.swing.*;
import java.awt.*;
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

    public ChessBoard() {
        BoardState boardState = new BoardState();

        JFrame frame = new JFrame("Alex's Chess Board");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 400);
        frame.setLayout(new GridLayout(8, 8));

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                JPanel panel = new JPanel(new BorderLayout());
                panel.setBackground((row + col) % 2 == 0 ? Color.WHITE : Color.BLACK);
                
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

                frame.add(panel);
            }
        }

        frame.setVisible(true);
    }

    public static void main(String[] args) {
        new ChessBoard();
    }
}