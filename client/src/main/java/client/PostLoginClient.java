package client;

import chess.ChessGame;
import exception.ResponseException;
import java.util.ArrayList;
import java.util.Arrays;
import model.GameData;
import request.CreateGameRequest;
import request.JoinGameRequest;
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
        case "join" -> joinGame(params);
        case "observe" -> observeGame(params);
        default -> help();
      };
    } catch (ResponseException ex) {
      return ex.getMessage();
    }
  }

  private String logout() throws ResponseException {
    String authToken = chessClient.getAuthToken();
    serverFacade.logout(authToken);

    chessClient.setState(State.SIGNEDOUT);
    chessClient.setCurrentClient(new PreLoginClient(chessClient, serverFacade));

    return "You have logged out";
  }

  private String createGame(String... params) throws ResponseException {
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

  private String listGames() throws ResponseException {
    String authToken = chessClient.getAuthToken();
    ListGamesResult result = serverFacade.listGames(authToken);
    chessClient.updateGameIdMappings();

    return formatListOfGames(result);
  }

  private String joinGame(String... params) throws ResponseException {
    if (params.length < 2) {
      throw new ResponseException(400, "Error: team color and gameID must be filled");
    }

    ChessGame.TeamColor teamColor = parseTeamColor(params[0]);
    int externalGameID = parseExternalGameID(params[1]);
    int internalGameID = validateInternalGameID(externalGameID);

    JoinGameRequest request = new JoinGameRequest(teamColor, internalGameID);
    String authToken = chessClient.getAuthToken();
    serverFacade.joinGame(request, authToken);

    chessClient.setCurrentClient(new PlayerClient(chessClient, serverFacade));
    chessClient.setState(State.PLAYING);

    // We'll see how this changes with websocket. I'm not sure whether this block should be here
    // GameData gameData = chessClient.getGame(internalGameID);
    // formatBoards(gameData);

    return String.format("You have joined game %d", externalGameID);
  }

  public String observeGame(String... params) throws ResponseException {
    if (params.length < 1) {
      throw new ResponseException(400, "Error: gameID must be filled");
    }

    int externalGameID = parseExternalGameID(params[0]);
    validateInternalGameID(externalGameID);
    // int internalGameID = validateInternalGameID(externalGameID);

    // We'll see how this changes with websocket. I'm not sure whether this block should be here
    // GameData gameData = chessClient.getGame(internalGameID);
    // formatBoards(gameData);

    chessClient.setCurrentClient(new InGameClient(chessClient, serverFacade));
    chessClient.setState(State.OBSERVING);

    return String.format("You have chosen to observe game %d", externalGameID);
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

  private String formatListOfGames(ListGamesResult result) {
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

  private ChessGame.TeamColor parseTeamColor(String teamColorParam) throws ResponseException {
    try {
      return ChessGame.TeamColor.valueOf(teamColorParam.toUpperCase());
    } catch (IllegalArgumentException ex) {
      throw new ResponseException(400, "Error: team color must be 'WHITE' or 'BLACK'");
    }
  }

  private int parseExternalGameID(String externalGameIdParam) throws ResponseException {
    try {
      return Integer.parseInt(externalGameIdParam);
    } catch (NumberFormatException ex) {
      throw new ResponseException(400, "Error: gameID must be a valid integer");
    }
  }

  private int validateInternalGameID(int externalGameID) throws ResponseException {
    int internalGameID = chessClient.getInternalGameID(externalGameID);
    chessClient.getGame(internalGameID);

    return internalGameID;
  }
}
