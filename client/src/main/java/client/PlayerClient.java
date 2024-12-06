package client;

import chess.*;
import client.websocket.WebSocketFacade;
import exception.ResponseException;
import java.util.Arrays;
import java.util.Scanner;
import serverfacade.ServerFacade;

public class PlayerClient extends InGameClient {
  public PlayerClient(ChessClient chessClient, ServerFacade serverFacade) {
    super(chessClient, serverFacade);
  }

  @Override
  public String eval(String input) {
    try {
      var tokens = input.toLowerCase().split(" ");
      var command = (tokens.length > 0) ? tokens[0] : "help";
      var params = Arrays.copyOfRange(tokens, 1, tokens.length);

      return switch (command) {
        case "highlight" -> highlightLegalMoves(params);
        case "move" -> makeMove(params);
        case "redraw" -> redrawBoard();
        case "leave" -> leaveGame();
        case "resign" -> resign();
        default -> help();
      };
    } catch (ResponseException ex) {
      return ex.getMessage();
    }
  }

  private String makeMove(String... params) throws ResponseException {
    int gameID = chessClient.getCurrentInternalGameID();
    ChessGame game = chessClient.getGame(gameID).game();

    ChessPosition startPosition = makeSurePieceIsNotNull(game, ChessPosition.fromString(params[0]));
    ChessPosition endPosition = ChessPosition.fromString(params[1]);

    ChessMove move;
    if (pawnIsUpForPromotion(chessClient.getCurrentTeamColor(), game, startPosition)) {
      ChessPiece.PieceType promotionPiece = getPromotionPiece();
      move = new ChessMove(startPosition, endPosition, promotionPiece);
    } else {
      move = new ChessMove(startPosition, endPosition, null);
    }

    WebSocketFacade ws = chessClient.getWebSocketFacade();
    ws.makeMove(chessClient.getAuthToken(), chessClient.getCurrentInternalGameID(), move);

    return "";
  }

  private String resign() throws ResponseException {
    System.out.println(
        "Are you sure you want to resign? Type 'yes' to confirm, or 'no' to cancel.");
    System.out.print("\n" + ">>> ");

    Scanner scanner = new Scanner(System.in);
    String confirmation = scanner.nextLine().trim().toLowerCase();

    WebSocketFacade ws = chessClient.getWebSocketFacade();
    if (confirmation.equals("yes")) {
      ws.resign(chessClient.getAuthToken(), chessClient.getCurrentInternalGameID());
    }
    return "";
  }

  @Override
  public String help() {
    return """
        While you play a game, here are your options:

        - highlight <piece position>: highlight legal moves
        - move <start position> <end position>: make a move (example: a7a8)
        - redraw: redraw the chess board
        - leave: remove yourself from the game
        - resign: forfeit the game, but must type 'leave' to leave it
        - help: see possible commands
        """;
  }

  private boolean pawnIsUpForPromotion(
      ChessGame.TeamColor teamColor, ChessGame game, ChessPosition startPosition) {
    ChessPiece piece = game.getBoard().getPiece(startPosition);
    if (piece == null) {
      return false;
    }

    ChessPiece.PieceType pieceType = piece.getPieceType();
    ChessPiece.PieceType pawn = ChessPiece.PieceType.PAWN;

    ChessGame.TeamColor white = ChessGame.TeamColor.WHITE;
    ChessGame.TeamColor black = ChessGame.TeamColor.BLACK;
    int row = startPosition.getRow();

    return (teamColor == white && row == 7 && pieceType == pawn)
        || (teamColor == black && row == 2 && pieceType == pawn);
  }

  private ChessPiece.PieceType getPromotionPiece() {
    Scanner scanner = new Scanner(System.in);
    ChessPiece.PieceType pieceType = null;

    while (pieceType == null) {
      System.out.println(
          """
              What do you want your pawn to be promoted to?

              - QUEEN
              - BISHOP
              - KNIGHT
              - ROOK
              """);
      System.out.print("\n>>> ");

      String response = scanner.nextLine().trim().toUpperCase();

      try {
        pieceType = ChessPiece.PieceType.valueOf(response);
      } catch (IllegalArgumentException e) {
        System.out.println("Invalid input. Please enter one of: QUEEN, BISHOP, KNIGHT, ROOK.");
      }
    }

    return pieceType;
  }
}
