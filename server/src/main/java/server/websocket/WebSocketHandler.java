package server.websocket;

import chess.ChessGame;
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
      case LEAVE -> leave();
      case RESIGN -> resign();
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

  private void leave() {}

  private void resign() {}

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
      ErrorMessage errorMessage =
          createErrorMessage("Error: the game associated with that ID does not exist");
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
