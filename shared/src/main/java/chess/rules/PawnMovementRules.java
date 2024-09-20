package chess.rules;

import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPosition;
import chess.ChessPiece;

import java.util.ArrayList;
import java.util.Collection;

public class PawnMovementRules extends SharedMovementRules{
    @Override
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition position) {
        Collection<ChessMove> moves = new ArrayList<>();

        // Get identifying information about pawn
        ChessPiece piece = board.getPiece(position);
        ChessGame.TeamColor team = piece.getTeamColor();
        int direction = (team == ChessGame.TeamColor.WHITE) ? 1 : -1;

        // Forward moves
        addForwardMoves(board, position, direction, moves);

        // Capture moves
        addCaptureMoves(board, position, direction, moves);

        return moves;
    }

    private void addForwardMoves(ChessBoard board, ChessPosition position,
                                 int direction, Collection<ChessMove> moves) {
        int row = position.getRow();
        int column = position.getColumn();

        ChessPosition oneForwardStep = new ChessPosition(row + direction, column);

        if (board.isInBounds(oneForwardStep) && !board.isOccupied(oneForwardStep)) {
            if (isPromotionRow(oneForwardStep, board.getPiece(position).getTeamColor())) {
                promotePiece(board.getPiece(position), position, oneForwardStep, moves);
            }
            else {
                moves.add(new ChessMove(position, oneForwardStep, null));
            }

            if (isFirstMove(position, board.getPiece(position))) {
                ChessPosition twoForwardSteps = new ChessPosition(row + (2 * direction), column);
                if (board.isInBounds(twoForwardSteps) && !board.isOccupied(twoForwardSteps)) {
                    moves.add(new ChessMove(position, twoForwardSteps, null));
                }
            }
        }
    }

    private void addCaptureMoves(ChessBoard board, ChessPosition position,
                                 int direction, Collection<ChessMove> moves) {
        int row = position.getRow();
        int column = position.getColumn();

        ChessPosition leftDiagonal = new ChessPosition(row + direction, column - 1);
        ChessPosition rightDiagonal = new ChessPosition(row + direction, column + 1);

        if (board.isInBounds(leftDiagonal) && board.isOccupied(leftDiagonal) &&
        board.getPiece(leftDiagonal).getTeamColor() != board.getPiece(position).getTeamColor()) {
            if (isPromotionRow(leftDiagonal, board.getPiece(position).getTeamColor())) {
                promotePiece(board.getPiece(position), position, leftDiagonal, moves);
            }
            else {
                moves.add(new ChessMove(position, leftDiagonal, null));
            }
        }

        if (board.isInBounds(rightDiagonal) && board.isOccupied(rightDiagonal) &&
        board.getPiece(rightDiagonal).getTeamColor() != board.getPiece(position).getTeamColor()) {
            if (isPromotionRow(rightDiagonal, board.getPiece(position).getTeamColor())) {
                promotePiece(board.getPiece(position), position, rightDiagonal, moves);
            }
            else {
                moves.add(new ChessMove(position, rightDiagonal, null));
            }
        }
    }

    private boolean isFirstMove(ChessPosition position, ChessPiece piece) {
        int row = position.getRow();
        return (piece.getTeamColor() == ChessGame.TeamColor.WHITE && row == 2) ||
                (piece.getTeamColor() == ChessGame.TeamColor.BLACK && row == 7);
    }

    private boolean isPromotionRow(ChessPosition position, ChessGame.TeamColor team) {
        return (team == ChessGame.TeamColor.WHITE && position.getRow() == 8) ||
                (team == ChessGame.TeamColor.BLACK && position.getRow() == 1);
    }

    private void promotePiece(ChessPiece piece, ChessPosition position,
                              ChessPosition endPosition, Collection<ChessMove> moves) {
        moves.add(new ChessMove(position, endPosition, ChessPiece.PieceType.QUEEN));
        moves.add(new ChessMove(position, endPosition, ChessPiece.PieceType.BISHOP));
        moves.add(new ChessMove(position, endPosition, ChessPiece.PieceType.KNIGHT));
        moves.add(new ChessMove(position, endPosition, ChessPiece.PieceType.ROOK));
    }
}
