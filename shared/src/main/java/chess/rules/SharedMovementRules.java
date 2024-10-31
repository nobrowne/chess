package chess.rules;

import chess.*;
import java.util.Collection;

public abstract class SharedMovementRules implements MovementRules {
  public abstract Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition startPosition);

  protected void calculateMoves(
      ChessBoard board,
      ChessPosition startPosition,
      int rowDirection,
      int colDirection,
      Collection<ChessMove> moves,
      boolean canMoveLongRange) {
    int row = startPosition.getRow();
    int col = startPosition.getColumn();
    ChessPiece piece = board.getPiece(startPosition);
    ChessGame.TeamColor teamColor = piece.getTeamColor();

    while (true) {
      row += rowDirection;
      col += colDirection;

      ChessPosition endPosition = new ChessPosition(row, col);

      if (!board.isRealPosition(endPosition)) {
        break;
      }

      if (board.isOccupiedAt(endPosition)) {
        ChessPiece otherPiece = board.getPiece(endPosition);
        ChessGame.TeamColor otherPieceTeamColor = otherPiece.getTeamColor();
        if (teamColor != otherPieceTeamColor) {
          moves.add(new ChessMove(startPosition, endPosition, null));
        }
        break;
      }
      moves.add(new ChessMove(startPosition, endPosition, null));

      if (!canMoveLongRange) {
        break;
      }
    }
  }
}
