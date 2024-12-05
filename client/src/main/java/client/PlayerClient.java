package client;

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
        case "highlight" -> highlightLegalMoves();
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

  private String makeMove(String... params) {
    return null;
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
}
