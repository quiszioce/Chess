/**
 * Represents a single chess piece with a type (e.g. "King", "Pawn")
 * and a color ("white" or "black").
 */
public class Piece {
    private String type;
    private String color;

    /**
     * Creates a new piece with the given type and color.
     *
     * @param type  the kind of piece — "King", "Queen", "Rook", "Bishop", "Knight", or "Pawn"
     * @param color the owning player's color — "white" or "black"
     */
    public Piece(String type, String color) {
        this.type = type;
        this.color = color;
    }

    /** Returns the piece type (e.g. "Rook"). */
    public String getType() {
        return type;
    }

    /** Returns the piece color ("white" or "black"). */
    public String getColor() {
        return color;
    }

    /** Sets the piece type. */
    public void setType(String type) {
        this.type = type;
    }

    /** Sets the piece color. */
    public void setColor(String color) {
        this.color = color;
    }
}
