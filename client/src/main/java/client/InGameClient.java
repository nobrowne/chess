package client;

import exception.ResponseException;
import serverfacade.ServerFacade;

public class InGameClient implements ClientInterface {
  protected final ChessClient chessClient;
  protected final ServerFacade serverFacade;

  public InGameClient(ChessClient chessClient, ServerFacade serverFacade) {
    this.chessClient = chessClient;
    this.serverFacade = serverFacade;
  }

  @Override
  public String eval(String input) {
    try {
      var tokens = input.toLowerCase().split(" ");
      var command = (tokens.length > 0) ? tokens[0] : "help";

      return switch (command) {
        case "highlight" -> highlightLegalMoves();
        // case "redraw" -> redrawBoard();
        case "leave" -> leaveGame();
        default -> help();
      };
    } catch (ResponseException ex) {
      return ex.getMessage();
    }
  }

  protected String highlightLegalMoves() throws ResponseException {
    throw new ResponseException(400, "wow, not implemented");
  }

  protected String leaveGame() {
    chessClient.setCurrentClient(new PostLoginClient(chessClient, serverFacade));

    return "You have left the game";
  }

  @Override
  public String help() {
    return """
        While you observe a game, here are your options:

        - highlight: highlight legal moves
        - redraw: redraw the chess board
        - leave: remove yourself from the game
        - help: see possible commands

        While you play a game, here are some additional options:

        - move <start position><end position>: make a move (example: a7a8)
        - resign: forfeit the game, but must type 'leave' to leave it
        """;
  }
}
