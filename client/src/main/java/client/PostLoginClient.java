package client;

import exception.ResponseException;
import java.util.Arrays;
import serverfacade.ServerFacade;

public class PostLoginClient implements ClientInterface {
  private final ChessClient chessClient;
  private final ServerFacade serverFacade;

  public PostLoginClient(ChessClient chessClient, ServerFacade serverFacade) {
    this.chessClient = chessClient;
    this.serverFacade = serverFacade;
  }

  @Override
  public String eval(String input) {
    try {
      var tokens = input.toLowerCase().split(" ");
      var command = (tokens.length > 0) ? tokens[0] : "help";
      var params = Arrays.copyOfRange(tokens, 1, tokens.length);

      return switch (command) {
        case "logout" -> logout();
        //        case "create" -> createGame(params);
        //        case "list" -> listGames();
        //        case "join" -> joinGame(params);
        //        case "observe" -> observeGame(params);
        default -> help();
      };
    } catch (ResponseException ex) {
      return ex.getMessage();
    }
  }

  public String logout() throws ResponseException {
    String authToken = chessClient.getAuthToken();
    serverFacade.logout(authToken);

    chessClient.setState(State.SIGNEDOUT);
    chessClient.setCurrentClient(new PreLoginClient(chessClient, serverFacade));

    return "You have logged out";
  }

  @Override
  public String help() {
    return """
        Until you join or observe a game, here are your options:

        - create <GAME NAME>: create a new game
        - list: list all games
        - join <WHITE|BLACK> <GAME ID>: join a game as the white or black team
        - observe: join a game as a non-player observer
        - logout: leave the application
        - help: see possible commands
        """;
  }
}
