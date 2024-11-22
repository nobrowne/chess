package client;

import exception.ResponseException;
import java.util.*;
import model.GameData;
import result.ListGamesResult;
import serverfacade.ServerFacade;

public class ChessClient {
  private final ServerFacade serverFacade;
  private final Map<Integer, Integer> externalToInternalGameIDs = new HashMap<>();
  private final Map<Integer, Integer> internalToExternalGameIDs = new HashMap<>();
  private String authToken;
  private ClientInterface currentClient;
  private State state;

  public ChessClient(String serverURL) {
    this.serverFacade = new ServerFacade(serverURL);
    this.authToken = null;
    this.currentClient = new PreLoginClient(this, serverFacade);
    this.state = State.SIGNEDOUT;
  }

  public String getAuthToken() {
    return authToken;
  }

  public void setAuthToken(String authToken) {
    this.authToken = authToken;
  }

  public State getState() {
    return state;
  }

  public void setState(State state) {
    this.state = state;
  }

  public void setCurrentClient(ClientInterface client) {
    this.currentClient = client;
  }

  public String eval(String input) {
    return currentClient.eval(input);
  }

  public void updateGameIdMappings() throws ResponseException {
    ListGamesResult listGamesResult = serverFacade.listGames(authToken);
    ArrayList<GameData> games = listGamesResult.games();
    Collections.shuffle(games);

    for (int i = 0; i < games.size(); i++) {
      int internalGameID = games.get(i).gameID();
      int externalGameID = i + 1;

      internalToExternalGameIDs.put(internalGameID, externalGameID);
      externalToInternalGameIDs.put(externalGameID, internalGameID);
    }
  }

  public int getInternalGameID(int externalID) throws ResponseException {
    try {
      return externalToInternalGameIDs.get(externalID);
    } catch (NullPointerException ex) {
      throw new ResponseException(400, "Error: invalid gameID");
    }
  }

  public int getExternalGameID(int internalID) {
    return internalToExternalGameIDs.get(internalID);
  }

  public GameData getGame(int gameID) throws ResponseException {
    ListGamesResult result = serverFacade.listGames(authToken);

    for (GameData game : result.games()) {
      if (game.gameID() == gameID) {
        return game;
      }
    }

    throw new ResponseException(400, "Error: invalid gameID");
  }

  //  private final ServerFacade server;
  //  private final Map<Integer, Integer> externalToInternalGameIDs = new HashMap<>();
  //  private final Map<Integer, Integer> internalToExternalGameIDs = new HashMap<>();
  //  private State state = State.SIGNEDOUT;
  //  private String authToken;
  //
  //  public ChessClient(String serverUrl) {
  //    server = new ServerFacade(serverUrl);
  //  }
  //
  //  public String eval(String input) {
  //    try {
  //      var tokens = input.toLowerCase().split(" ");
  //      var command = (tokens.length > 0) ? tokens[0] : "help";
  //      var params = Arrays.copyOfRange(tokens, 1, tokens.length);
  //
  //      return switch (command) {
  //        case "register" -> register(params);
  //        case "login" -> login(params);
  //        case "logout" -> logout();
  //        case "create" -> createGame(params);
  //        case "list" -> listGames();
  //        case "join" -> joinGame(params);
  //        case "observe" -> observeGame(params);
  //        case "quit" -> quit();
  //        default -> help();
  //      };
  //    } catch (ResponseException ex) {
  //      return ex.getMessage();
  //    }
  //  }
  //
  //  public String register(String... params) throws ResponseException {
  //    if (params.length < 3) {
  //      throw new ResponseException(400, "Error: username, password, and email must all be
  // filled");
  //    }
  //
  //    String username = params[0];
  //    String password = params[1];
  //    String email = params[2];
  //    RegisterRequest request = new RegisterRequest(username, password, email);
  //    RegisterResult result = server.register(request);
  //
  //    state = State.SIGNEDIN;
  //    authToken = result.authToken();
  //
  //    return String.format("You are registered as %s%n", username) + help();
  //  }
  //
  //  public String login(String... params) throws ResponseException {
  //    if (params.length < 2) {
  //      throw new ResponseException(400, "Error: username and password must be filled");
  //    }
  //
  //    String username = params[0];
  //    String password = params[1];
  //    LoginRequest request = new LoginRequest(username, password);
  //    LoginResult result = server.login(request);
  //
  //    state = State.SIGNEDIN;
  //    authToken = result.authToken();
  //
  //    return String.format("You have logged in as %s%n", username) + help();
  //  }
  //
  //  public String logout() throws ResponseException {
  //    if (state != State.SIGNEDIN) {
  //      throw new ResponseException(400, "Error: cannot log out if not logged in");
  //    }
  //
  //    server.logout(authToken);
  //    state = State.SIGNEDOUT;
  //
  //    return "You have logged out";
  //  }
  //
  //  public String createGame(String... params) throws ResponseException {
  //    if (state != State.SIGNEDIN) {
  //      throw new ResponseException(400, "Error: cannot create game if not logged in");
  //    }
  //    if (params.length < 1) {
  //      throw new ResponseException(400, "Error: game name must be filled");
  //    }
  //
  //    String gameName = String.join(" ", params);
  //    CreateGameRequest request = new CreateGameRequest(gameName);
  //    server.createGame(request, authToken);
  //
  //    ListGamesResult listGamesResult = server.listGames(authToken);
  //    updateGameIdMappings(listGamesResult);
  //
  //    return String.format("You have created a new game called %s", gameName);
  //  }
  //
  //  public String listGames() throws ResponseException {
  //    if (state != State.SIGNEDIN) {
  //      throw new ResponseException(400, "Error: cannot list games if not logged in");
  //    }
  //
  //    ListGamesResult result = server.listGames(authToken);
  //    updateGameIdMappings(result);
  //
  //    return formatGamesList(result);
  //  }
  //
  //  public String joinGame(String... params) throws ResponseException {
  //    if (state != State.SIGNEDIN) {
  //      throw new ResponseException(400, "Error: cannot join games if not logged in");
  //    }
  //    if (params.length < 2) {
  //      throw new ResponseException(400, "Error: team color and gameID must be filled");
  //    }
  //
  //    ChessGame.TeamColor teamColor;
  //    try {
  //      teamColor = ChessGame.TeamColor.valueOf(params[0].toUpperCase());
  //    } catch (IllegalArgumentException e) {
  //      throw new ResponseException(400, "Error: team color must be 'WHITE' or 'BLACK'");
  //    }
  //
  //    int externalGameID;
  //    try {
  //      externalGameID = Integer.parseInt(params[1]);
  //    } catch (NumberFormatException e) {
  //      throw new ResponseException(400, "Error: gameID must be a valid integer");
  //    }
  //
  //    int internalGameID = getInternalGameID(externalGameID);
  //    if (isNotValidGameID(internalGameID)) {
  //      throw new ResponseException(400, "Error: invalid gameID");
  //    }
  //
  //    JoinGameRequest request = new JoinGameRequest(teamColor, internalGameID);
  //    server.joinGame(request, authToken);
  //
  //    GameData gameData = getGame(internalGameID);
  //    formatBoards(gameData);
  //
  //    return String.format("You have joined game %d", externalGameID);
  //  }
  //
  //  public String observeGame(String... params) throws ResponseException {
  //    if (state != State.SIGNEDIN) {
  //      throw new ResponseException(400, "Error: cannot observe games if not logged in");
  //    }
  //    if (params.length < 1) {
  //      throw new ResponseException(400, "Error: gameID must be filled");
  //    }
  //
  //    int externalGameID;
  //    try {
  //      externalGameID = Integer.parseInt(params[0]);
  //    } catch (NumberFormatException e) {
  //      throw new ResponseException(400, "Error: gameID must be a valid integer");
  //    }
  //
  //    int internalGameID = getInternalGameID(externalGameID);
  //    if (isNotValidGameID(internalGameID)) {
  //      throw new ResponseException(400, "Error: invalid gameID");
  //    }
  //
  //    GameData gameData = getGame(internalGameID);
  //    formatBoards(gameData);
  //
  //    return String.format("You have chosen to observe game %d", externalGameID);
  //  }
  //
  //  public String help() {
  //    if (state == State.SIGNEDOUT) {
  //      return """
  //        - register <USERNAME> <PASSWORD> <EMAIL>: create an account
  //        - login <USERNAME> <PASSWORD>: play chess
  //        - quit: shut down the application
  //        - help: see possible commands
  //      """;
  //    }
  //
  //    return """
  //      - create <GAME NAME>: create a new game
  //      - list: list all games
  //      - join <WHITE|BLACK> <GAME ID>: join a game as the white or black team
  //      - observe: join a game as a non-player observer
  //      - logout: leave the application
  //      - help: see possible commands
  //    """;
  //  }
  //
  //  public String quit() throws ResponseException {
  //    if (state == State.SIGNEDIN) {
  //      throw new ResponseException(400, "Error: cannot quit unless logged out");
  //    }
  //    return "quit";
  //  }
  //
  //  public void updateGameIdMappings(ListGamesResult listGamesResult) {
  //    ArrayList<GameData> games = listGamesResult.games();
  //    Collections.shuffle(games);
  //
  //    for (int i = 0; i < games.size(); i++) {
  //      int internalGameID = games.get(i).gameID();
  //      int externalGameID = i + 1;
  //
  //      internalToExternalGameIDs.put(internalGameID, externalGameID);
  //      externalToInternalGameIDs.put(externalGameID, internalGameID);
  //    }
  //  }
  //
  //  public int getInternalGameID(int externalID) throws ResponseException {
  //    try {
  //      return externalToInternalGameIDs.get(externalID);
  //    } catch (NullPointerException ex) {
  //      throw new ResponseException(400, "Error: invalid gameID");
  //    }
  //  }
  //
  //  public int getExternalGameID(int internalID) {
  //    return internalToExternalGameIDs.get(internalID);
  //  }
  //
  //  public GameData getGame(int gameID) throws ResponseException {
  //    ListGamesResult result = server.listGames(authToken);
  //
  //    for (GameData game : result.games()) {
  //      if (game.gameID() == gameID) {
  //        return game;
  //      }
  //    }
  //
  //    throw new ResponseException(400, "Error: invalid gameID");
  //  }
  //
  //    public boolean isNotValidGameID(int gameID) {
  //      try {
  //        getGame(gameID);
  //        return false;
  //      } catch (ResponseException ex) {
  //        return true;
  //      }
  //    }
  //
  //  public String formatGamesList(ListGamesResult result) {
  //    ArrayList<GameData> games = result.games();
  //
  //    StringBuilder sb = new StringBuilder();
  //
  //    sb.append("Available Games:\n");
  //    sb.append(
  //        String.format(
  //            "%-10s %-20s %-15s %-15s%n", "Game ID", "Game Name", "White Player", "Black
  // Player"));
  //    sb.append("=".repeat(60)).append("\n");
  //
  //    for (GameData gameData : games) {
  //      sb.append(
  //          String.format(
  //              "%-10d %-20s %-15s %-15s%n",
  //              getExternalGameID(gameData.gameID()),
  //              gameData.gameName(),
  //              gameData.whiteUsername() != null ? gameData.whiteUsername() : "TBD",
  //              gameData.blackUsername() != null ? gameData.blackUsername() : "TBD"));
  //    }
  //
  //    return sb.toString();
  //  }
  //
  //  public void formatBoards(GameData gameData) {
  //    ChessGame game = gameData.game();
  //    BoardDrawer.drawBoard(game, true);
  //    //    BoardDrawer.drawDividerLine();
  //    //    BoardDrawer.drawBoard(game, false);
  //  }
}
