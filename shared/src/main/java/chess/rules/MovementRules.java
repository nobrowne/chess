package chess.rules;

import chess.ChessBoard;
import chess.ChessMove;
import chess.ChessPosition;

import java.util.Collection;

public interface MovementRule {
    Collection<ChessMove> pieceMoves (ChessBoard board, ChessPosition position);
}
