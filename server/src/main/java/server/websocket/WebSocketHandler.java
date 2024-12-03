package server.websocket;

import chess.ChessGame;
import chess.GameState;
import com.google.gson.Gson;
import dataaccess.DataAccessException;
import dataaccess.auth.AuthDAO;
import dataaccess.game.GameDAO;
import dataaccess.user.UserDAO;
import java.io.IOException;
import model.AuthData;
import model.GameData;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import websocket.commands.UserGameCommand;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;
import websocket.messages.ServerMessage;

@WebSocket
public class WebSocketHandler {

  private final ConnectionManager connections = new ConnectionManager();
  private final AuthDAO authDAO;
  private final GameDAO gameDAO;
  private final UserDAO userDAO;

  public WebSocketHandler(AuthDAO authDAO, GameDAO gameDAO, UserDAO userDAO) {
    this.authDAO = authDAO;
    this.gameDAO = gameDAO;
    this.userDAO = userDAO;
  }

  @OnWebSocketMessage
  public void onMessage(Session session, String message) throws IOException {
    // TODO: convert the MakeMove class to a subclass of UserGameCommand, so I can pass in the move

    UserGameCommand command = new Gson().fromJson(message, UserGameCommand.class);
    UserGameCommand.CommandType commandType = command.getCommandType();
    String authToken = command.getAuthToken();
    int gameID = command.getGameID();

    switch (commandType) {
      case CONNECT -> connectToGame(authToken, gameID, session);
      case MAKE_MOVE -> makeMove();
      case LEAVE -> leave(authToken, gameID, session);
      case RESIGN -> resign(authToken, gameID);
    }
  }

  private void connectToGame(String authToken, int gameID, Session session) throws IOException {
    connections.add(authToken, gameID, session);

    String username = getUsername(authToken);
    String teamColor = getTeamColor(gameID, username, authToken);
    NotificationMessage notificationMessage =
        createNotificationMessage(
            String.format("%s has joined the game as %s", username, teamColor));
    connections.broadcastToGame(gameID, authToken, notificationMessage);

    ChessGame game = getChessGame(gameID, authToken);
    LoadGameMessage loadGameMessage = createLoadGameMessage(game);
    connections.broadcastToRootClient(authToken, loadGameMessage);
  }

  private void makeMove() {
    // Add move to parameters
  }

  private void leave(String authToken, int gameID, Session session) throws IOException {
    String username = getUsername(authToken);
    GameData currentGameData = getGameData(gameID, authToken);

    if (userIsObserver(gameID, username, authToken)) {
      NotificationMessage notificationMessage =
          createNotificationMessage(String.format("%s has left the game", username));
      connections.broadcastToGame(gameID, authToken, notificationMessage);
      connections.remove(authToken, gameID);
      return;
    }

    GameData updatedGameData = removeUserFromGameData(currentGameData, username);
    try {
      gameDAO.updateGame(updatedGameData);
    } catch (DataAccessException ex) {
      ErrorMessage errorMessage = createErrorMessage("Error: the game with that ID doesn't exist");
      connections.broadcastToRootClient(authToken, errorMessage);
    }

    NotificationMessage notificationMessage =
        createNotificationMessage(String.format("%s has left the game", username));
    connections.broadcastToGame(gameID, authToken, notificationMessage);
    connections.remove(authToken, gameID);
  }

  private void resign(String authToken, int gameID) throws IOException {
    String username = getUsername(authToken);
    if (userIsObserver(gameID, username, authToken)) {
      ErrorMessage errorMessage = createErrorMessage("Error: you can't resign as an observer");
      connections.broadcastToRootClient(authToken, errorMessage);
      return;
    }

    try {
      if (gameDAO.getGameState(gameID) == GameState.OVER) {
        ErrorMessage errorMessage = createErrorMessage("Error: you can't resign after the game");
        connections.broadcastToRootClient(authToken, errorMessage);
        return;
      }
      gameDAO.updateGameState(gameID, GameState.OVER);
    } catch (DataAccessException e) {
      ErrorMessage errorMessage = createErrorMessage("Error: the game with that ID doesn't exist");
      connections.broadcastToRootClient(authToken, errorMessage);
    }

    NotificationMessage notificationToOthers =
        createNotificationMessage(String.format("%s has resigned the game", username));
    connections.broadcastToGame(gameID, authToken, notificationToOthers);

    NotificationMessage notificationToRootClient =
        createNotificationMessage("You have resigned the game");
    connections.broadcastToRootClient(authToken, notificationToRootClient);
  }

  private AuthData getAuthData(String authToken) throws IOException {
    try {
      AuthData authData = authDAO.getAuth(authToken);
      if (authData == null) {
        throw new DataAccessException("");
      }
      return authData;
    } catch (DataAccessException ex) {
      ErrorMessage errorMessage = createErrorMessage("Error: unauthorized user");
      connections.broadcastToRootClient(authToken, errorMessage);
    }

    return null;
  }

  private GameData getGameData(int gameID, String authToken) throws IOException {
    try {
      GameData gameData = gameDAO.getGame(gameID);
      if (gameData == null) {
        throw new DataAccessException("");
      }
      return gameData;
    } catch (DataAccessException ex) {
      ErrorMessage errorMessage = createErrorMessage("Error: the game with that ID doesn't exist");
      connections.broadcastToRootClient(authToken, errorMessage);
    }

    return null;
  }

  private String getUsername(String authToken) throws IOException {
    AuthData authData = getAuthData(authToken);
    return authData.username();
  }

  private String getTeamColor(int gameID, String username, String authToken) throws IOException {
    GameData gameData = getGameData(gameID, authToken);
    if (username.equals(gameData.whiteUsername())) {
      return ChessGame.TeamColor.WHITE.toString().toLowerCase();
    }
    if (username.equals(gameData.blackUsername())) {
      return ChessGame.TeamColor.BLACK.toString().toLowerCase();
    }
    return "an observer";

    // TODO: add a way to send back a message if both are the same player
  }

  private ChessGame getChessGame(int gameID, String authToken) throws IOException {
    GameData gameData = getGameData(gameID, authToken);
    return gameData.game();
  }

  private boolean userIsObserver(int gameID, String username, String authToken) throws IOException {
    GameData gameData = getGameData(gameID, authToken);
    String whiteUsername = gameData.whiteUsername();
    String blackUsername = gameData.blackUsername();

    return !username.equals(whiteUsername) && !username.equals(blackUsername);
  }

  private GameData removeUserFromGameData(GameData gameData, String username) {
    int gameID = gameData.gameID();
    String whiteUsername = gameData.whiteUsername();
    String blackUsername = gameData.blackUsername();
    String gameName = gameData.gameName();
    ChessGame game = gameData.game();

    if (username.equals(whiteUsername)) {
      return new GameData(gameID, null, blackUsername, gameName, game);
    }
    return new GameData(gameID, whiteUsername, null, gameName, game);
  }

  private LoadGameMessage createLoadGameMessage(ChessGame game) {
    return new LoadGameMessage(ServerMessage.ServerMessageType.LOAD_GAME, game);
  }

  private ErrorMessage createErrorMessage(String message) {
    return new ErrorMessage(ServerMessage.ServerMessageType.ERROR, message);
  }

  private NotificationMessage createNotificationMessage(String message) {
    return new NotificationMessage(ServerMessage.ServerMessageType.NOTIFICATION, message);
  }
}
