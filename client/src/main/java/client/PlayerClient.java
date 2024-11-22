package client;

import exception.ResponseException;
import java.util.Arrays;
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
        // case "redraw" -> redrawBoard(params);
        case "leave" -> leaveGame();
        // case "resign" -> resign();
        default -> help();
      };
    } catch (ResponseException ex) {
      return ex.getMessage();
    }
  }

  private String makeMove(String... params) {
    return null;
  }

  @Override
  public String help() {
    return """
        While you play a game, here are your options:

        - highlight: highlight legal moves
        - move <start position><end position>: make a move (example: a7a8)
        - redraw: redraw the chess board
        - leave: remove yourself from the game
        - resign: forfeit the game, but must type 'leave' to leave it
        - help: see possible commands
        """;
  }
}
