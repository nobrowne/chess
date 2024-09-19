package chess;

import chess.rules.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {

    private final ChessGame.TeamColor pieceColor;
    private final PieceType type;

    public ChessPiece(ChessGame.TeamColor pieceColor, PieceType type) {
        this.pieceColor = pieceColor;
        this.type = type;
    }

    /**
     * The various different chess piece options
     */
    public enum PieceType {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    }

    /**
     * @return Which team this chess piece belongs to
     */
    public ChessGame.TeamColor getTeamColor() {
        return pieceColor;
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {
        return type;
    }

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        ChessPiece piece = board.getPiece(myPosition);

        MovementRules rules;
        switch (type) {
            case PieceType.KING -> rules = new KingMovementRules();
            case PieceType.QUEEN -> rules = new QueenMovementRules();
            case PieceType.BISHOP -> rules = new BishopMovementRules();
            case PieceType.KNIGHT -> rules = new KnightMovementRules();
            case PieceType.ROOK -> rules = new RookMovementRules();
            case PieceType.PAWN -> rules = new RookMovementRules();
            default -> throw new IllegalArgumentException("Type should not be null");
        }

        return rules.pieceMoves(board, myPosition);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChessPiece that = (ChessPiece) o;
        return pieceColor == that.pieceColor && type == that.type;
    }

    @Override
    public int hashCode() {
        return 71 * pieceColor.hashCode() + type.hashCode();
    }

    @Override
    public String toString() {
        String pieceSymbol = switch (type) {
            case KING -> "K";
            case QUEEN -> "Q";
            case BISHOP -> "B";
            case KNIGHT -> "N";
            case ROOK -> "R";
            case PAWN -> "P";
            default -> throw new IllegalArgumentException("Unexpected value: " + type);
        };

        pieceSymbol = (pieceColor == ChessGame.TeamColor.WHITE) ? pieceSymbol : pieceSymbol.toLowerCase();

        return pieceSymbol;
    }
}
