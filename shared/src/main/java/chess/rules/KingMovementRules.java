package chess.rules;

import chess.ChessBoard;
import chess.ChessMove;
import chess.ChessPosition;

import java.util.ArrayList;
import java.util.Collection;

public class KingMovementRules extends SharedMovementRules{
    @Override
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition position) {
        Collection<ChessMove> moves = new ArrayList<>();

        // Orthogonal moves
        calculateMoves(board, position, 1, 0, moves, false);
        calculateMoves(board, position, -1, 0, moves, false);
        calculateMoves(board, position, 0, -1, moves, false);
        calculateMoves(board, position, 0, 1, moves, false);

        // Diagonal moves
        calculateMoves(board, position, 1, -1, moves, false);
        calculateMoves(board, position, 1, 1, moves, false);
        calculateMoves(board, position, -1, -1, moves, false);
        calculateMoves(board, position, -1, 1, moves, false);

        return moves;
    }
}
