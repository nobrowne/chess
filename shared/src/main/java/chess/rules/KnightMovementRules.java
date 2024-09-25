package chess.rules;

import chess.ChessBoard;
import chess.ChessMove;
import chess.ChessPosition;

import java.util.ArrayList;
import java.util.Collection;

public class KnightMovementRules extends SharedMovementRules {
    @Override
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition startPosition) {
        Collection<ChessMove> moves = new ArrayList<>();

        calculateMoves(board, startPosition, 2, 1, moves, false);
        calculateMoves(board, startPosition, 1, 2, moves, false);
        calculateMoves(board, startPosition, 2, -1, moves, false);
        calculateMoves(board, startPosition, 1, -2, moves, false);
        calculateMoves(board, startPosition, -2, 1, moves, false);
        calculateMoves(board, startPosition, -1, 2, moves, false);
        calculateMoves(board, startPosition, -2, -1, moves, false);
        calculateMoves(board, startPosition, -1, -2, moves, false);

        return moves;
    }
}
