package ui;

import static ui.EscapeSequences.*;

import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessPiece;
import chess.ChessPosition;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

public class BoardDrawer {
  private static final PrintStream OUT = new PrintStream(System.out, true, StandardCharsets.UTF_8);
  private static final String EMPTY_SQUARE = "   ";
  private static final String BORDER_BG_COLOR = SET_BG_COLOR_MAGENTA;
  private static final String BORDER_TEXT_COLOR = SET_TEXT_COLOR_WHITE;

  public static void drawBoard(ChessGame game, boolean isBlackPerspective) {
    ChessBoard board = game.getBoard();

    drawColLetters(isBlackPerspective);

    for (int row = 0; row < 8; row++) {
      int rowFromPerspective = isBlackPerspective ? row + 1 : 8 - row;
      drawRow(board, row, rowFromPerspective, isBlackPerspective);
    }

    drawColLetters(isBlackPerspective);
  }

  private static void drawColLetters(boolean isBlackPerspective) {
    OUT.print(BORDER_BG_COLOR + EMPTY_SQUARE + RESET_BG_COLOR);

    for (int col = 0; col < 8; col++) {
      char colLetter = (char) ('a' + (isBlackPerspective ? 7 - col : col));
      OUT.print(
          BORDER_BG_COLOR
              + BORDER_TEXT_COLOR
              + " "
              + colLetter
              + " "
              + RESET_BG_COLOR
              + RESET_TEXT_COLOR);
    }

    OUT.print(BORDER_BG_COLOR + EMPTY_SQUARE + RESET_BG_COLOR);

    OUT.println();
  }

  private static void drawRowNumber(int rowNumber) {
    OUT.print(
        BORDER_BG_COLOR
            + BORDER_TEXT_COLOR
            + " "
            + rowNumber
            + " "
            + RESET_BG_COLOR
            + RESET_TEXT_COLOR);
  }

  private static void drawRow(
      ChessBoard board, int row, int rowNumber, boolean isBlackPerspective) {

    drawRowNumber(rowNumber);

    for (int col = 0; col < 8; col++) {
      drawSquare(board, row, col, isBlackPerspective);
    }

    drawRowNumber(rowNumber);

    OUT.println();
  }

  private static void drawSquare(ChessBoard board, int row, int col, boolean isBlackPerspective) {
    int colFromPerspective = isBlackPerspective ? 7 - col : col;
    int rowFromPerspective = isBlackPerspective ? 7 - row : row;
    String squareColor = getSquareColor(colFromPerspective, rowFromPerspective);

    ChessPiece piece =
        board.getPiece(new ChessPosition(rowFromPerspective + 1, colFromPerspective + 1));
    String pieceSymbol = getPieceSymbol(piece);
    OUT.print(squareColor + pieceSymbol + RESET_BG_COLOR);
  }

  public static void drawDividerLine() {
    OUT.println();
  }

  private static String getSquareColor(int col, int row) {
    return (row + col) % 2 == 0 ? SET_BG_COLOR_LIGHT_GREY : SET_BG_COLOR_DARK_GREY;
  }

  private static String getPieceSymbol(ChessPiece piece) {
    if (piece == null) {
      return EMPTY_SQUARE;
    }

    String colorCode =
        piece.getTeamColor() == ChessGame.TeamColor.BLACK
            ? SET_TEXT_COLOR_WHITE
            : SET_TEXT_COLOR_BLACK;

    return colorCode + " " + piece.toString().toUpperCase() + " ";
  }
}
