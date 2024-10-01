package chess;

import java.util.Collection;
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
     * Sets this game's chessboard with a given board
     *
     * @param board The new board to use
     */
    public void setBoard(ChessBoard board) {
        this.board = board;
    }

    /**
     * Gets the current chessboard
     *
     * @return The chessboard
     */
    public ChessBoard getBoard() {
        return board;
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
     * Switches which team's turn it is after a move is made
     */
    public void switchTeamTurn() {
        if (getTeamTurn() == TeamColor.WHITE) {
            setTeamTurn(TeamColor.BLACK);
        }
        else {
            setTeamTurn(TeamColor.WHITE);
        }
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    /**
     * Gets the valid moves for a piece at the given location
     *
     * @param startPosition The piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        ChessPiece piece = board.getPiece(startPosition);

        TeamColor teamColor = piece.getTeamColor();
        Collection<ChessMove> pieceMoves = piece.pieceMoves(board, startPosition);

        Set<ChessMove> invalidMoves = new HashSet<>();

        for (ChessMove move : pieceMoves) {
            ChessBoard boardCopy = new ChessBoard(board);
            ChessPosition endPosition = move.getEndPosition();
            boardCopy.movePiece(startPosition, endPosition, null);

            ChessGame simulatedGame = new ChessGame();
            simulatedGame.setBoard(boardCopy);

            if (simulatedGame.isInCheck(teamColor)) {
                invalidMoves.add(move);
            }
        }

        pieceMoves.removeAll(invalidMoves);

        return pieceMoves;
    }

    public boolean hasNoValidMoves(TeamColor teamColor) {
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition position = new ChessPosition(row, col);
                ChessPiece piece = board.getPiece(position);

                if (piece != null && piece.getTeamColor() == teamColor) {
                    Collection<ChessMove> validMoves = validMoves(position);

                    if (!validMoves.isEmpty()) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * Checks to see if the turn belongs to the team making a move
     *
     * @param teamColor The team making the move
     * @return True if the turn belongs to the team making the move
     */
    public boolean moveIsOnTurn(TeamColor teamColor) {
        return getTeamTurn() == teamColor;
    }

    /**
     * Makes a move in a chess game
     *
     * @param move The chess move to perform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        ChessPosition startPosition = move.getStartPosition();
        ChessPosition endPosition = move.getEndPosition();
        ChessPiece.PieceType promotionPiece = move.getPromotionPiece();

        ChessPiece piece = board.getPiece(startPosition);
        if (piece == null) {
            throw new InvalidMoveException("There is no piece at the specified location" + endPosition);
        }

        TeamColor teamColor = piece.getTeamColor();
        TeamColor teamTurn = getTeamTurn();
        if (!moveIsOnTurn(teamColor)) {
            throw new InvalidMoveException(String.format("You are on the %s team. It is currently the %s team's turn", teamColor, teamTurn));
        }

        Collection<ChessMove> validMoves = validMoves(startPosition);
        if (validMoves.contains(move)) {
            board.movePiece(startPosition, endPosition, promotionPiece);
            switchTeamTurn();
        }
        else {
            throw new InvalidMoveException("Invalid move");
        }
    }

    /**
     * Determines if the given team is in check. This occurs when the position of the given team's king is in the list
     * of valid moves for the other team.
     *
     * @param teamColor Which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        ChessPosition kingPosition = board.getKingPosition(teamColor);
        Set<ChessPosition> availablePositions = board.getAvailablePositions(teamColor);

        return availablePositions.contains(kingPosition);
    }

    /**
     * Determines if the given team is in checkmate. This occurs when the given team's king IS IN CHECK and the given
     * team has no valid moves.
     *
     * @param teamColor Which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        return isInCheck(teamColor) && hasNoValidMoves(teamColor);
    }

    /**
     * Determines if the given team is in stalemate. This occurs when the given team's king is NOT IN CHECK and the
     * given team has no valid moves.
     *
     * @param teamColor Which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        return !isInCheck(teamColor) && hasNoValidMoves(teamColor);
    }
}
