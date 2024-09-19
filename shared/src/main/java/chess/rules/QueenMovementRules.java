package chess.rules;

import chess.ChessBoard;
import chess.ChessMove;
import chess.ChessPosition;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class QueenMovementRules extends SharedMovementRules{
    @Override
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition position) {
        Collection<ChessMove> moves = new ArrayList<>();

        // Orthogonal moves
        calculateMoves(board, position, 1, 0, moves, true);
        calculateMoves(board, position, -1, 0, moves, true);
        calculateMoves(board, position, 0, -1, moves, true);
        calculateMoves(board, position, 0, 1, moves, true);

        calculateMoves(board, position, 1, -1, moves, true);
        calculateMoves(board, position, 1, 1, moves, true);
        calculateMoves(board, position, -1, -1, moves, true);
        calculateMoves(board, position, -1, 1, moves, true);

        return moves;
    }
}
