package chess.rules;

import chess.ChessBoard;
import chess.ChessMove;
import chess.ChessPosition;
import java.util.ArrayList;
import java.util.Collection;

public class BishopMovementRules extends SharedMovementRules {
  @Override
  public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition startPosition) {
    Collection<ChessMove> moves = new ArrayList<>();

    calculateMoves(board, startPosition, 1, 1, moves, true);
    calculateMoves(board, startPosition, 1, -1, moves, true);
    calculateMoves(board, startPosition, -1, 1, moves, true);
    calculateMoves(board, startPosition, -1, -1, moves, true);

    return moves;
  }
}
