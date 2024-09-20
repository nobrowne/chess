package chess.rules;

import chess.ChessBoard;
import chess.ChessMove;
import chess.ChessPosition;

import java.util.Collection;

public abstract class SharedMovementRules implements MovementRules{
    protected void calculateMoves(ChessBoard board, ChessPosition position,
                                  int rowIncrement,int columnIncrement, Collection<ChessMove> moves,
                                  boolean allowDistance) {

        int row = position.getRow();
        int column = position.getColumn();

        while (true) {
            row += rowIncrement;
            column += columnIncrement;

            ChessPosition movePosition = new ChessPosition(row, column);

            if (!board.isInBounds(movePosition)) {
                break;
            }

            ChessPosition newPosition = new ChessPosition(row, column);

            if (board.isOccupied(newPosition)) {
                if (board.getPiece(newPosition).getTeamColor() != board.getPiece(position).getTeamColor()) {
                    moves.add(new ChessMove(position, newPosition, null));
                }
                break;
            }

            moves.add(new ChessMove(position, newPosition, null));

            if (!allowDistance) {
                break;
            }
        }
    }

    public abstract Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition position);
}
