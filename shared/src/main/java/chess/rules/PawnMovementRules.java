package chess.rules;

import chess.*;
import java.util.ArrayList;
import java.util.Collection;

public class PawnMovementRules extends SharedMovementRules {
  @Override
  public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition startPosition) {
    Collection<ChessMove> moves = new ArrayList<>();

    ChessPiece piece = board.getPiece(startPosition);
    ChessGame.TeamColor teamColor = piece.getTeamColor();
    int forwardDirection = getForwardDirection(teamColor);

    addForwardMoves(board, startPosition, forwardDirection, teamColor, moves);

    int rightDiagonal = 1;
    int leftDiagonal = -1;
    addCaptureMoves(board, startPosition, forwardDirection, rightDiagonal, teamColor, moves);
    addCaptureMoves(board, startPosition, forwardDirection, leftDiagonal, teamColor, moves);

    return moves;
  }

  private int getForwardDirection(ChessGame.TeamColor teamColor) {
    ChessGame.TeamColor white = ChessGame.TeamColor.WHITE;

    return (teamColor == white) ? 1 : -1;
  }

  private void addForwardMoves(
      ChessBoard board,
      ChessPosition startPosition,
      int forwardDirection,
      ChessGame.TeamColor teamColor,
      Collection<ChessMove> moves) {

    int row = startPosition.getRow();
    int col = startPosition.getColumn();

    ChessPosition oneForward = new ChessPosition(row + forwardDirection, col);
    ChessPosition twoForward = new ChessPosition(row + (2 * forwardDirection), col);

    if (isFirstMove(teamColor, startPosition)) {
      if (board.isRealPosition(twoForward)
          && !board.isOccupiedAt(twoForward)
          && !board.isOccupiedAt(oneForward)) {
        moves.add(new ChessMove(startPosition, oneForward, null));
        moves.add(new ChessMove(startPosition, twoForward, null));
      }
    }

    if (board.isRealPosition(oneForward) && !board.isOccupiedAt(oneForward)) {
      if (isPromotionRow(teamColor, oneForward)) {
        promotePiece(startPosition, oneForward, moves);
      } else {
        moves.add(new ChessMove(startPosition, oneForward, null));
      }
    }
  }

  private void addCaptureMoves(
      ChessBoard board,
      ChessPosition startPosition,
      int forwardDirection,
      int diagonalDirection,
      ChessGame.TeamColor teamColor,
      Collection<ChessMove> moves) {

    int row = startPosition.getRow();
    int col = startPosition.getColumn();

    ChessPosition endPosition = new ChessPosition(row + forwardDirection, col + diagonalDirection);

    if (board.isRealPosition(endPosition) && board.isOccupiedAt(endPosition)) {
      ChessPiece otherPiece = board.getPiece(endPosition);
      ChessGame.TeamColor otherPieceTeamColor = otherPiece.getTeamColor();
      if (teamColor != otherPieceTeamColor) {
        if (isPromotionRow(teamColor, endPosition)) {
          promotePiece(startPosition, endPosition, moves);
        } else {
          moves.add(new ChessMove(startPosition, endPosition, null));
        }
      }
    }
  }

  private boolean isFirstMove(ChessGame.TeamColor teamColor, ChessPosition startPosition) {
    ChessGame.TeamColor white = ChessGame.TeamColor.WHITE;
    ChessGame.TeamColor black = ChessGame.TeamColor.BLACK;
    int row = startPosition.getRow();

    return (teamColor == white && row == 2) || (teamColor == black && row == 7);
  }

  private boolean isPromotionRow(ChessGame.TeamColor teamColor, ChessPosition position) {
    ChessGame.TeamColor white = ChessGame.TeamColor.WHITE;
    ChessGame.TeamColor black = ChessGame.TeamColor.BLACK;
    int row = position.getRow();

    return (teamColor == white && row == 8) || (teamColor == black && row == 1);
  }

  private void promotePiece(
      ChessPosition startPosition, ChessPosition endPosition, Collection<ChessMove> moves) {
    moves.add(new ChessMove(startPosition, endPosition, ChessPiece.PieceType.QUEEN));
    moves.add(new ChessMove(startPosition, endPosition, ChessPiece.PieceType.BISHOP));
    moves.add(new ChessMove(startPosition, endPosition, ChessPiece.PieceType.KNIGHT));
    moves.add(new ChessMove(startPosition, endPosition, ChessPiece.PieceType.ROOK));
  }
}
