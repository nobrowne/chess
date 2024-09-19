package chess.rules;

import chess.ChessBoard;
import chess.ChessMove;
import chess.ChessPosition;

import java.util.ArrayList;
import java.util.Collection;

public class KnightMovementRules extends SharedMovementRules{
    @Override
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition position) {
        Collection<ChessMove> moves = new ArrayList<>();

        calculateMoves(board, position, 1, -2, moves, false);
        calculateMoves(board, position, 2, -1, moves, false);
        calculateMoves(board, position, 2, 1, moves, false);
        calculateMoves(board, position, 1, 2, moves, false);
        calculateMoves(board, position, -1, -2, moves, false);
        calculateMoves(board, position, -2, -1, moves, false);
        calculateMoves(board, position, -2, 1, moves, false);
        calculateMoves(board, position, -1, 2, moves, false);

        return moves;
    }
}
