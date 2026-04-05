import javax.swing.*;
import java.awt.*;

public class ChessBoard {
    public ChessBoard() {
        JFrame frame = new JFrame("Alex's Chess Board");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 400);
        frame.setLayout(new GridLayout(8, 8));

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                JPanel panel = new JPanel();
                if ((row + col) % 2 == 0) {
                    panel.setBackground(Color.WHITE);
                } else {
                    panel.setBackground(Color.BLACK);
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