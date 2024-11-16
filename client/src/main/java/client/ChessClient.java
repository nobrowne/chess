package client;

import chess.ChessGame;
import exception.ResponseException;
import java.util.*;
import model.GameData;
import request.CreateGameRequest;
import request.JoinGameRequest;
import request.LoginRequest;
import request.RegisterRequest;
import result.ListGamesResult;
import result.LoginResult;
import result.RegisterResult;
import serverfacade.ServerFacade;

public class ChessClient {
  private final ServerFacade server;
  private State state = State.SIGNEDOUT;
  private String authToken;

  public ChessClient(String serverUrl) {
    server = new ServerFacade(serverUrl);
  }

  public String eval(String input) {
    try {
      var tokens = input.toLowerCase().split(" ");
      var command = (tokens.length > 0) ? tokens[0] : "help";
      var params = Arrays.copyOfRange(tokens, 1, tokens.length);

      return switch (command) {
        case "register" -> register(params);
        case "login" -> login(params);
        case "logout" -> logout();
        case "create" -> createGame(params);
        case "list" -> listGames();
        case "join" -> joinGame(params);
        case "observe" -> observeGame(params);
        case "quit" -> "quit";
        default -> help();
      };
    } catch (ResponseException ex) {
      return ex.getMessage();
    }
  }

  public String register(String... params) throws ResponseException {
    if (params.length < 3) {
      throw new ResponseException(400, "Error: username, password, and email must all be filled");
    }

    String username = params[0];
    String password = params[1];
    String email = params[2];
    RegisterRequest request = new RegisterRequest(username, password, email);
    RegisterResult result = server.register(request);

    authToken = result.authToken();

    return String.format("You are registered as %s", username);
  }

  public String login(String... params) throws ResponseException {
    if (params.length < 2) {
      throw new ResponseException(400, "Error: username and password must be filled");
    }

    String username = params[0];
    String password = params[1];
    LoginRequest request = new LoginRequest(username, password);
    LoginResult result = server.login(request);

    state = State.SIGNEDIN;
    authToken = result.authToken();

    return String.format("You have logged in as %s%n", username) + help();
  }

  public String logout() throws ResponseException {
    if (state != State.SIGNEDIN) {
      throw new ResponseException(400, "Error: cannot log out if not logged in");
    }

    server.logout(authToken);
    state = State.SIGNEDOUT;

    return "You have logged out";
  }

  public String createGame(String... params) throws ResponseException {
    if (state != State.SIGNEDIN) {
      throw new ResponseException(400, "Error: cannot create game if not logged in");
    }
    if (params.length < 1) {
      throw new ResponseException(400, "Error: game name must be filled");
    }

    String gameName = String.join(" ", params);
    CreateGameRequest request = new CreateGameRequest(gameName);
    server.createGame(request, authToken);

    return String.format("You have created a new game called %s", gameName);
  }

  public String listGames() throws ResponseException {
    if (state != State.SIGNEDIN) {
      throw new ResponseException(400, "Error: cannot list games if not logged in");
    }

    ListGamesResult result = server.listGames(authToken);

    return formatGamesList(result);
  }

  public String joinGame(String... params) throws ResponseException {
    if (state != State.SIGNEDIN) {
      throw new ResponseException(400, "Error: cannot join games if not logged in");
    }
    if (params.length < 2) {
      throw new ResponseException(400, "Error: team color and gameID must be filled");
    }

    ChessGame.TeamColor teamColor;
    try {
      teamColor = ChessGame.TeamColor.valueOf(params[0].toUpperCase());
    } catch (IllegalArgumentException e) {
      throw new ResponseException(400, "Error: team color must be 'WHITE' or 'BLACK'");
    }

    int gameID; // TODO: make a mapping between real IDs and client-friendly IDs
    try {
      gameID = Integer.parseInt(params[1]);
    } catch (NumberFormatException e) {
      throw new ResponseException(400, "Error: gameID must be a valid integer");
    }

    JoinGameRequest request = new JoinGameRequest(teamColor, gameID);
    server.joinGame(request, authToken);

    return String.format("You have joined game %d", gameID);
  }

  public String observeGame(String... params) throws ResponseException {
    if (state != State.SIGNEDIN) {
      throw new ResponseException(400, "Error: cannot observe games if not logged in");
    }
    if (params.length < 1) {
      throw new ResponseException(400, "Error: gameID must be filled");
    }

    int gameID = Integer.parseInt(params[0]);

    return String.format("You have chosen to observe game %d", gameID);
  }

  public String help() {
    if (state == State.SIGNEDOUT) {
      return """
        - register <USERNAME> <PASSWORD> <EMAIL>: create an account
        - login <USERNAME> <PASSWORD>: play chess
        - quit: shut down the application
        - help: see possible commands
      """;
    }

    return """
      - create <GAME NAME>: create a new game
      - list: list all games
      - join <WHITE|BLACK> <GAME ID>: join a game as the white or black team
      - observe: join a game as a non-player observer
      - logout: leave the application
      - quit: shut down the application
      - help: see possible commands
    """;
  }

  public String formatGamesList(ListGamesResult result) {
    ArrayList<GameData> games = result.games();
    games.sort(Comparator.comparing(GameData::gameID));

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
              gameData.gameID(), // TODO: change it to the client-friendly ID
              gameData.gameName(),
              gameData.whiteUsername() != null ? gameData.whiteUsername() : "TBD",
              gameData.blackUsername() != null ? gameData.blackUsername() : "TBD"));
    }

    return sb.toString();
  }
}
