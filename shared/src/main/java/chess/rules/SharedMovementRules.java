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


        }
    }

    public abstract Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition position);
}
