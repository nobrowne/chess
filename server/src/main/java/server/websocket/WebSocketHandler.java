package server.websocket;

import chess.*;
import com.google.gson.Gson;
import dataaccess.DataAccessException;
import dataaccess.auth.AuthDAO;
import dataaccess.game.GameDAO;
import java.io.IOException;
import java.util.ArrayList;
import model.AuthData;
import model.GameData;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import websocket.commands.MakeMoveCommand;
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

  public WebSocketHandler(AuthDAO authDAO, GameDAO gameDAO) {
    this.authDAO = authDAO;
    this.gameDAO = gameDAO;
  }

  @OnWebSocketMessage
  public void onMessage(Session session, String message) throws IOException {
    UserGameCommand command = new Gson().fromJson(message, UserGameCommand.class);
    UserGameCommand.CommandType commandType = command.getCommandType();
    ChessMove move = null;
    if (commandType == UserGameCommand.CommandType.MAKE_MOVE) {
      MakeMoveCommand makeMoveCommand = new Gson().fromJson(message, MakeMoveCommand.class);
      move = makeMoveCommand.move;
    }
    String authToken = command.getAuthToken();
    int gameID = command.getGameID();

    switch (commandType) {
      case CONNECT -> connectToGame(authToken, gameID, session);
      case MAKE_MOVE -> makeMove(authToken, gameID, move, session);
      case LEAVE -> leave(authToken, gameID, session);
      case RESIGN -> resign(authToken, gameID, session);
    }
  }

  private void connectToGame(String authToken, int gameID, Session session) throws IOException {
    String username = getUsername(session, authToken);
    if (username == null) {
      return;
    }
    connections.add(username, gameID, session);

    ChessGame.TeamColor teamColor = getTeamColor(session, gameID, username);
    if (teamColor == null) {
      sendNotificationToOthers(
          gameID, username, String.format("%s has joined as an observer", username));
    } else {
      sendNotificationToOthers(
          gameID,
          username,
          String.format("%s has joined as %s", username, teamColor.toString().toLowerCase()));
    }

    ChessGame game = getChessGame(session, gameID);
    sendLoadGameToSession(session, game, teamColor);
  }

  private void makeMove(String authToken, int gameID, ChessMove move, Session session)
      throws IOException {
    String username = getUsername(session, authToken);
    if (username == null) {
      return;
    }

    GameData gameData = getGameData(session, gameID);
    assert gameData != null;

    ChessGame game = gameData.game();

    if (userIsObserver(session, gameID, username)) {
      sendErrorToSession(session, "Error: you're just observing");
      return;
    }

    if (getGameState(session, gameID) == GameState.OVER) {
      sendErrorToSession(session, "Error: you can't move after the game is over");
      return;
    }

    if (!pieceBelongsToUser(session, gameID, username, move, game)) {
      sendErrorToSession(session, "Error: that's not one of your pieces");
      return;
    }

    try {
      game.makeMove(move);
    } catch (InvalidMoveException ex) {
      sendErrorToSession(session, ex.getMessage());
      return;
    }

    ChessGame.TeamColor teamColor = getTeamColor(session, gameID, username);

    sendLoadGameToSession(session, game, teamColor);
    sendLoadGameToOthers(session, gameID, game);

    String startPos = move.getStartPosition().toString();
    String endPos = move.getEndPosition().toString();
    sendNotificationToOthers(
        gameID, username, String.format("%s moved from %s to %s", username, startPos, endPos));

    GameData updatedGameData =
        new GameData(
            gameID, gameData.whiteUsername(), gameData.blackUsername(), gameData.gameName(), game);
    updateGame(session, updatedGameData);

    ChessGame.TeamColor otherTeamColor = getOtherTeamColor(session, gameID, username);

    if (game.isInCheckmate(otherTeamColor)) {
      sendNotificationToSession(session, "CHECKMATE! YOU WON!");
      sendNotificationToOthers(gameID, username, String.format("Checkmate. %s won", username));
      updateGameState(session, gameID, GameState.OVER);
      return;
    }

    if (game.isInStalemate(otherTeamColor)) {
      sendNotificationToSession(session, "Stalemate");
      sendNotificationToOthers(gameID, username, "Stalemate. It's a draw");
      updateGameState(session, gameID, GameState.OVER);
      return;
    }

    if (game.isInCheck(otherTeamColor)) {
      sendNotificationToSession(session, "Check");
      sendNotificationToOthers(gameID, username, "Check");
    }
  }

  private void leave(String authToken, int gameID, Session session) throws IOException {
    String username = getUsername(session, authToken);
    if (username == null) {
      return;
    }

    GameData currentGameData = getGameData(session, gameID);

    assert currentGameData != null;

    sendNotificationToOthers(gameID, username, String.format("%s has left", username));
    connections.remove(username, gameID);

    if (userIsObserver(session, gameID, username)) {
      return;
    }

    GameData updatedGameData = removeUserFromGameData(currentGameData, username);
    updateGame(session, updatedGameData);
  }

  private void resign(String authToken, int gameID, Session session) throws IOException {
    String username = getUsername(session, authToken);
    if (userIsObserver(session, gameID, username)) {
      sendErrorToSession(session, "Error: you can't resign as an observer");
      return;
    }

    if (getGameState(session, gameID) == GameState.OVER) {
      sendErrorToSession(session, "Error: you can't resign after the game is over");
      return;
    }

    updateGameState(session, gameID, GameState.OVER);
    sendNotificationToSession(session, "You have resigned");
    sendNotificationToOthers(gameID, username, String.format("%s has resigned", username));
  }

  private String getUsername(Session session, String authToken) throws IOException {
    String username = null;
    try {
      AuthData authData = authDAO.getAuth(authToken);
      if (authData == null) {
        throw new DataAccessException("");
      }
      username = authData.username();
      return username;
    } catch (DataAccessException ex) {
      sendErrorToSession(session, "Error: unauthorized user");
    }

    return null;
  }

  private GameData getGameData(Session session, int gameID) throws IOException {
    try {
      GameData gameData = gameDAO.getGame(gameID);
      if (gameData == null) {
        throw new DataAccessException("");
      }
      return gameData;
    } catch (DataAccessException ex) {
      sendErrorToSession(session, "Error: the game with that ID doesn't exist");
    }

    return null;
  }

  private void updateGame(Session session, GameData gameData) throws IOException {
    try {
      gameDAO.updateGame(gameData);
    } catch (DataAccessException ex) {
      sendErrorToSession(session, "Error: the game with that ID doesn't exist");
    }
  }

  private GameState getGameState(Session session, int gameID) throws IOException {
    try {
      return gameDAO.getGameState(gameID);
    } catch (DataAccessException e) {
      sendErrorToSession(session, "Error: the game with that ID doesn't exist");
    }
    return null;
  }

  private void updateGameState(Session session, int gameID, GameState gameState)
      throws IOException {
    try {
      gameDAO.updateGameState(gameID, gameState);
    } catch (DataAccessException ex) {
      sendErrorToSession(session, "Error: the game with that ID doesn't exist");
    }
  }

  private ChessGame.TeamColor getTeamColor(Session session, int gameID, String username)
      throws IOException {
    GameData gameData = getGameData(session, gameID);
    assert gameData != null;

    String whiteUsername = gameData.whiteUsername();
    String blackUsername = gameData.blackUsername();

    if (username.equals(whiteUsername)) {
      return ChessGame.TeamColor.WHITE;
    }
    if (username.equals(blackUsername)) {
      return ChessGame.TeamColor.BLACK;
    }
    return null;
  }

  private ChessGame.TeamColor getOtherTeamColor(Session session, int gameID, String username)
      throws IOException {
    ChessGame.TeamColor userTeamColor = getTeamColor(session, gameID, username);
    if (userTeamColor == ChessGame.TeamColor.WHITE) {
      return ChessGame.TeamColor.BLACK;
    }
    return ChessGame.TeamColor.WHITE;
  }

  private ChessGame getChessGame(Session session, int gameID) throws IOException {
    GameData gameData = getGameData(session, gameID);
    assert gameData != null;

    return gameData.game();
  }

  private boolean userIsObserver(Session session, int gameID, String username) throws IOException {
    GameData gameData = getGameData(session, gameID);
    assert gameData != null;

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

  private boolean pieceBelongsToUser(
      Session session, int gameID, String username, ChessMove move, ChessGame game)
      throws IOException {
    ChessGame.TeamColor userTeamColor = getTeamColor(session, gameID, username);
    ChessPosition piecePosition = move.getStartPosition();
    ChessBoard board = game.getBoard();
    ChessPiece piece = board.getPiece(piecePosition);
    ChessGame.TeamColor pieceColor = piece.getTeamColor();

    return userTeamColor == pieceColor;
  }

  private void sendErrorToSession(Session session, String message) throws IOException {
    connections.sendToSession(
        session, new ErrorMessage(ServerMessage.ServerMessageType.ERROR, message));
  }

  private void sendLoadGameToSession(Session session, ChessGame game, ChessGame.TeamColor teamColor)
      throws IOException {
    boolean isWhitePerspective = teamColor == null || teamColor.equals(ChessGame.TeamColor.WHITE);
    connections.sendToSession(
        session,
        new LoadGameMessage(ServerMessage.ServerMessageType.LOAD_GAME, game, isWhitePerspective));
  }

  private void sendLoadGameToOthers(Session userSession, int gameID, ChessGame game)
      throws IOException {
    ArrayList<Connection> sessionsInGame = connections.gameConnections.get(gameID);
    for (Connection connection : sessionsInGame) {
      ChessGame.TeamColor teamColor = getTeamColor(connection.session, gameID, connection.username);
      boolean isWhitePerspective = teamColor == null || teamColor.equals(ChessGame.TeamColor.WHITE);
      if (connection.session == userSession) {
        continue;
      }
      connections.sendToSession(
          connection.session,
          new LoadGameMessage(ServerMessage.ServerMessageType.LOAD_GAME, game, isWhitePerspective));
    }
  }

  private void sendNotificationToSession(Session session, String message) throws IOException {
    connections.sendToSession(
        session, new NotificationMessage(ServerMessage.ServerMessageType.NOTIFICATION, message));
  }

  private void sendNotificationToOthers(int gameID, String username, String message)
      throws IOException {
    connections.sendToOthers(
        gameID,
        username,
        new NotificationMessage(ServerMessage.ServerMessageType.NOTIFICATION, message));
  }
}
