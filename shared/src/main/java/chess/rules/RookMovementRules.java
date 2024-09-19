package chess.rules;

import chess.ChessBoard;
import chess.ChessMove;
import chess.ChessPosition;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

public class RookMovementRules extends SharedMovementRules {
    @Override
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition position) {
        Collection<ChessMove> moves = new ArrayList<>();

        calculateMoves(board, position, 1, 0, moves, true);
        calculateMoves(board, position, -1, 0, moves, true);
        calculateMoves(board, position, 0, -1, moves, true);
        calculateMoves(board, position, 0, 1, moves, true);

        return moves;
    }
}
