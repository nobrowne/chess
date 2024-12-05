package ui;

import static ui.EscapeSequences.*;

import chess.*;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Collection;

public class BoardDrawer {
  private static final PrintStream OUT = new PrintStream(System.out, true, StandardCharsets.UTF_8);
  private static final String EMPTY_SQUARE = "   ";
  private static final String BORDER_BG_COLOR = SET_BG_COLOR_BLUE;
  private static final String EMPTY_BORDER_SQUARE = BORDER_BG_COLOR + EMPTY_SQUARE + RESET_BG_COLOR;
  private static final String BORDER_TEXT_COLOR = SET_TEXT_COLOR_WHITE;

  private static final String HIGHLIGHT_CURRENT = SET_BG_COLOR_YELLOW;
  private static final String HIGHLIGHT_VALID_MOVE = SET_BG_COLOR_ORANGE;
  private static final String HIGHLIGHT_CAPTURE = SET_BG_COLOR_RED;

  public static void drawBoard(
      ChessGame game, boolean isWhitePerspective, ChessPosition chosenPiece) {
    Collection<ChessMove> validMoves = chosenPiece != null ? game.validMoves(chosenPiece) : null;
    ChessBoard board = game.getBoard();

    drawHorizontalBorder(isWhitePerspective);

    for (int arrayRow = 0; arrayRow < 8; arrayRow++) {
      int displayRow = isWhitePerspective ? 8 - arrayRow : arrayRow + 1;
      drawRow(board, arrayRow, displayRow, isWhitePerspective, chosenPiece, validMoves);
    }

    drawHorizontalBorder(isWhitePerspective);
  }

  private static void drawHorizontalBorder(boolean isWhitePerspective) {
    OUT.print(EMPTY_BORDER_SQUARE);

    for (int col = 0; col < 8; col++) {
      char colLetter = (char) ('a' + (isWhitePerspective ? col : 7 - col));
      OUT.print(
          BORDER_BG_COLOR
              + BORDER_TEXT_COLOR
              + " "
              + colLetter
              + " "
              + RESET_BG_COLOR
              + RESET_TEXT_COLOR);
    }

    OUT.print(EMPTY_BORDER_SQUARE);

    OUT.println();
  }

  private static void drawRowNumber(int displayRow) {
    OUT.print(
        BORDER_BG_COLOR
            + BORDER_TEXT_COLOR
            + " "
            + displayRow
            + " "
            + RESET_BG_COLOR
            + RESET_TEXT_COLOR);
  }

  private static void drawRow(
      ChessBoard board,
      int arrayRow,
      int displayRow,
      boolean isWhitePerspective,
      ChessPosition chosenPiece,
      Collection<ChessMove> validMoves) {

    drawRowNumber(displayRow);

    for (int arrayCol = 0; arrayCol < 8; arrayCol++) {
      drawSquare(board, arrayRow, arrayCol, isWhitePerspective, chosenPiece, validMoves);
    }

    drawRowNumber(displayRow);

    OUT.println();
  }

  private static void drawSquare(
      ChessBoard board,
      int arrayRow,
      int arrayCol,
      boolean isWhitePerspective,
      ChessPosition chosenPiece,
      Collection<ChessMove> validMoves) {

    int displayRow = isWhitePerspective ? 8 - arrayRow : arrayRow + 1;
    int displayCol = isWhitePerspective ? arrayCol + 1 : 8 - arrayCol;

    String squareColor = getSquareColor(arrayRow, arrayCol);

    ChessPosition position = new ChessPosition(displayRow, displayCol);

    if (position.equals(chosenPiece)) {
      squareColor = HIGHLIGHT_CURRENT;
    } else if (validMoves != null) {
      for (ChessMove move : validMoves) {
        if (move.getEndPosition().equals(position)) {
          ChessPiece piece = board.getPiece(position);
          squareColor = piece != null ? HIGHLIGHT_CAPTURE : HIGHLIGHT_VALID_MOVE;
          break;
        }
      }
    }

    ChessPiece piece = board.getPiece(new ChessPosition(displayRow, displayCol));
    String pieceSymbol = getPieceSymbol(piece);

    OUT.print(squareColor + pieceSymbol + RESET_BG_COLOR);
  }

  public static void drawDividerLine() {
    OUT.println();
  }

  private static String getSquareColor(int arrayRow, int arrayCol) {
    return (arrayRow + arrayCol) % 2 == 0 ? SET_BG_COLOR_GRAY : SET_BG_COLOR_DARK_BLUE;
  }

  private static String getPieceSymbol(ChessPiece piece) {
    if (piece == null) {
      return EMPTY_SQUARE;
    }

    String colorCode =
        piece.getTeamColor() == ChessGame.TeamColor.WHITE
            ? SET_TEXT_COLOR_GREEN
            : SET_TEXT_COLOR_PURPLE;

    return SET_TEXT_BOLD + colorCode + " " + piece.toString().toUpperCase() + " ";
  }
}
