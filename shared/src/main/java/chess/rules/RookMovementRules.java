package chess.rules;

import chess.ChessBoard;
import chess.ChessMove;
import chess.ChessPosition;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

public class RookMovementRules implements MovementRules {
    @Override
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition position) {
        var pieceMoves = new HashSet<ChessMove>();
        return null;
    }
}
