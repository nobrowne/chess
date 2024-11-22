package client;

import exception.ResponseException;
import java.util.ArrayList;
import java.util.Arrays;
import model.GameData;
import request.CreateGameRequest;
import result.ListGamesResult;
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
        case "create" -> createGame(params);
        case "list" -> listGames();
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

  public String createGame(String... params) throws ResponseException {
    if (params.length < 1) {
      throw new ResponseException(400, "Error: game name must be filled");
    }

    String gameName = String.join(" ", params);
    CreateGameRequest request = new CreateGameRequest(gameName);
    String authToken = chessClient.getAuthToken();
    serverFacade.createGame(request, authToken);

    chessClient.updateGameIdMappings();

    return String.format("You have created a new game called %s", gameName);
  }

  public String listGames() throws ResponseException {
    String authToken = chessClient.getAuthToken();
    ListGamesResult result = serverFacade.listGames(authToken);
    chessClient.updateGameIdMappings();

    return formatListOfGames(result);
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

  public String formatListOfGames(ListGamesResult result) {
    ArrayList<GameData> games = result.games();

    StringBuilder sb = new StringBuilder();

    sb.append("Available Games:\n");
    sb.append(
        String.format(
            "%-10s %-20s %-15s %-15s%n", "Game ID", "Game Name", "White Player", "Black Player"));
    sb.append("=".repeat(60)).append("\n");

    for (GameData gameData : games) {
      sb.append(
          String.format(
              "%-10d %-20s %-15s %-15s%n",
              chessClient.getExternalGameID(gameData.gameID()),
              gameData.gameName(),
              gameData.whiteUsername() != null ? gameData.whiteUsername() : "TBD",
              gameData.blackUsername() != null ? gameData.blackUsername() : "TBD"));
    }

    return sb.toString();
  }
}
