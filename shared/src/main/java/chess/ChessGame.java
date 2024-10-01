package chess;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {

    ChessBoard board = new ChessBoard();
    TeamColor teamTurn;

    public ChessGame() {
        board.resetBoard();
        teamTurn = TeamColor.WHITE;
    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return teamTurn;
    }

    /**
     * Sets which team's turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        teamTurn = team;
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    /**
     * Gets a valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        ChessPiece piece = board.getPiece(startPosition);

        if (piece == null) {
            return Collections.emptyList();
        }

        TeamColor teamColor = piece.getTeamColor();
        Collection<ChessMove> pieceMoves = piece.pieceMoves(board, startPosition);

        Set<ChessMove> invalidMoves = new HashSet<>();

        for (ChessMove move : pieceMoves) {
            ChessBoard boardCopy = new ChessBoard(board);
            ChessPosition endPosition = move.getEndPosition();
            boardCopy.movePiece(startPosition, endPosition);

            ChessGame simulatedGame = new ChessGame();
            simulatedGame.setBoard(boardCopy);

            if (simulatedGame.isInCheck(teamColor)) {
                invalidMoves.add(move);
            }
        }

        pieceMoves.removeAll(invalidMoves);

        return pieceMoves;
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to perform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
//        System.out.println("BEFORE MOVE:\n" + board.toString());

        ChessPosition startPosition = move.getStartPosition();
        ChessPosition endPosition = move.getEndPosition();
        ChessPiece.PieceType promotionPiece = move.getPromotionPiece();

        board.movePiece(startPosition, endPosition);
//        System.out.println("AFTER MOVE:\n" + board.toString());
    }

    /**
     * Determines if the given team is in check. This occurs when the position of the given team's king is in the list
     * of valid moves for the other team.
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        ChessPosition kingPosition = board.getKingPosition(teamColor);

        if (kingPosition == null) {
            throw new IllegalStateException("King not found for team: " + teamColor);
        }

        Set<ChessPosition> threatenedPositions = getThreatenedPositions(board, teamColor);

        return threatenedPositions.contains(kingPosition);
    }

    private Set<ChessPosition> getThreatenedPositions(ChessBoard board, TeamColor teamColor) {
        Set<ChessPosition> threatenedPositions = new HashSet<>();

        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition position = new ChessPosition(row, col);
                ChessPiece piece = board.getPiece(position);

                if (piece == null || piece.getTeamColor() == teamColor) {
                    continue;
                }

                Collection<ChessMove> pieceMoves = piece.pieceMoves(board, position);

                for (ChessMove move : pieceMoves) {
                    threatenedPositions.add(move.getEndPosition());
                }
            }
        }

        return threatenedPositions;
    }

    /**
     * Determines if the given team is in checkmate. This occurs when the given team's king IS IN CHECK and the given
     * team has no valid moves.
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Determines if the given team is in stalemate. This occurs when the given team's king is NOT IN CHECK and the
     * given team has no valid moves.
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        this.board = board;
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return board;
    }
}
