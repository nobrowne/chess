package chess;

import java.util.*;

/**
 * A chessboard that can hold and rearrange chess pieces.
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessBoard {

    private ChessPiece[][] board = new ChessPiece[8][8];

    public ChessBoard() {
    }

    /**
     * Copies an existing chessboard
     *
     * @param other The board being copied
     */
    public ChessBoard(ChessBoard other) {
        this.board = new ChessPiece[8][8];

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                ChessPiece piece = other.board[row][col];
                if (piece != null) {
                    this.board[row][col] = new ChessPiece(piece);
                } else {
                    this.board[row][col] = null;
                }
            }
        }
    }

    /**
     * Adds a chess piece to the chessboard
     *
     * @param position Where to add the piece to
     * @param piece    The piece to add
     */
    public void addPiece(ChessPosition position, ChessPiece piece) {
        int row = position.getRow() - 1;
        int col = position.getColumn() - 1;
        board[row][col] = piece;
    }

    /**
     * Gets a chess piece on the chessboard
     *
     * @param position The position to get the piece from
     * @return Either the piece at the position, or null if no piece is at that
     * position
     */
    public ChessPiece getPiece(ChessPosition position) {
        int row = position.getRow() - 1;
        int col = position.getColumn() - 1;

        return board[row][col];
    }

    /**
     * Gets the position of the given team's king
     *
     * @param teamColor Which team to get the king's position for
     * @return Either the position of the king, or null if no king is found on the board
     */
    public ChessPosition getKingPosition(ChessGame.TeamColor teamColor) {
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition position = new ChessPosition(row, col);
                ChessPiece piece = getPiece(position);

                if (piece != null && piece.getTeamColor() == teamColor && piece.getPieceType() == ChessPiece.PieceType.KING) {
                    return position;
                }
            }
        }
        return null;
    }

    /**
     * Gets all positions the given team can move to on their turn
     *
     * @param teamColor Which team to get all available positions for
     * @return The set of board positions that the team can move to
     */
    Set<ChessPosition> getAvailablePositions(ChessGame.TeamColor teamColor) {
        Set<ChessPosition> occupiablePositions = new HashSet<>();

        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition position = new ChessPosition(row, col);
                ChessPiece piece = getPiece(position);

                if (piece == null || piece.getTeamColor() == teamColor) {
                    continue;
                }

                Collection<ChessMove> pieceMoves = piece.pieceMoves(this, position);

                for (ChessMove move : pieceMoves) {
                    occupiablePositions.add(move.getEndPosition());
                }
            }
        }

        return occupiablePositions;
    }

    /**
     * Moves a piece from one position to another, promoting it if the piece is a pawn in the final row
     *
     * @param startPosition  The board position in which the piece is currently located
     * @param endPosition    The board position to the piece will be moved
     * @param promotionPiece The piece type the piece will become if it is promoted
     */
    public void movePiece(ChessPosition startPosition, ChessPosition endPosition, ChessPiece.PieceType promotionPiece) {
        ChessPiece piece = getPiece(startPosition);

        int startRow = startPosition.getRow() - 1;
        int startCol = startPosition.getColumn() - 1;
        board[startRow][startCol] = null;

        int endRow = endPosition.getRow() - 1;
        int endCol = endPosition.getColumn() - 1;

        if (piece.getPieceType() == ChessPiece.PieceType.PAWN && promotionPiece != null) {
            ChessGame.TeamColor teamColor = piece.getTeamColor();
            board[endRow][endCol] = new ChessPiece(teamColor, promotionPiece);
        } else {
            board[endRow][endCol] = piece;
        }
    }

    /**
     * Determines whether the given position is actually on the board
     *
     * @param position A position on the board
     * @return True if the position specified is on the board
     */
    public boolean isRealPosition(ChessPosition position) {
        int row = position.getRow();
        int col = position.getColumn();

        return row >= 1 && row <= 8 && col >= 1 && col <= 8;
    }

    /**
     * Gets all positions the given team can move to on their turn
     *
     * @param position A position on the board
     * @return True if the position is occupied by another piece
     */
    public boolean isOccupiedAt(ChessPosition position) {
        return getPiece(position) != null;
    }

    /**
     * Sets the board to the default starting board
     * (How the game of chess normally starts)
     */
    public void resetBoard() {
        board = new ChessPiece[8][8];

        board[0][0] = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.ROOK);
        board[0][1] = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.KNIGHT);
        board[0][2] = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.BISHOP);
        board[0][3] = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.QUEEN);
        board[0][4] = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.KING);
        board[0][5] = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.BISHOP);
        board[0][6] = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.KNIGHT);
        board[0][7] = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.ROOK);

        for (int col = 0; col < 8; col++) {
            board[1][col] = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);
        }

        board[7][0] = new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.ROOK);
        board[7][1] = new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.KNIGHT);
        board[7][2] = new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.BISHOP);
        board[7][3] = new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.QUEEN);
        board[7][4] = new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.KING);
        board[7][5] = new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.BISHOP);
        board[7][6] = new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.KNIGHT);
        board[7][7] = new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.ROOK);

        for (int col = 0; col < 8; col++) {
            board[6][col] = new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.PAWN);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessBoard that = (ChessBoard) o;
        return Objects.deepEquals(board, that.board);
    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(board);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int row = 7; row >= 0; row--) {
            sb.append("|");
            for (int col = 0; col < 8; col++) {
                ChessPiece piece = board[row][col];
                sb.append((piece != null) ? piece.toString() : " ");
                sb.append("|");
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}
