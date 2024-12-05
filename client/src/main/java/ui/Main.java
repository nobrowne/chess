package ui;

import chess.ChessGame;

public class Main {
  public static void main(String[] args) {
    // Initialize the chess game
    ChessGame game = new ChessGame();

    System.out.println("White's perspective:");
    BoardDrawer.drawBoard(game, true, null);

    BoardDrawer.drawDividerLine();

    System.out.println("Black's Perspective:");
    BoardDrawer.drawBoard(game, false, null);
  }
}
