package client.websocket;

import chess.ChessMove;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import exception.ResponseException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import javax.websocket.*;
import websocket.commands.MakeMoveCommand;
import websocket.commands.UserGameCommand;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;
import websocket.messages.ServerMessage;

public class WebSocketFacade extends Endpoint {
  Session session;
  ServerMessageHandler serverMessageHandler;

  public WebSocketFacade(String serverURL, ServerMessageHandler serverMessageHandler)
      throws ResponseException {
    try {
      serverURL = serverURL.replace("http", "ws");
      URI socketURI = new URI(serverURL + "/ws");
      this.serverMessageHandler = serverMessageHandler;

      WebSocketContainer container = ContainerProvider.getWebSocketContainer();
      this.session = container.connectToServer(this, socketURI);

      this.session.addMessageHandler(
          String.class,
          message -> {
            JsonObject jsonObject = JsonParser.parseString(message).getAsJsonObject();
            String type = jsonObject.get("serverMessageType").getAsString();

            Gson gson = new Gson();
            ServerMessage serverMessage =
                switch (type) {
                  case "LOAD_GAME" -> gson.fromJson(jsonObject, LoadGameMessage.class);
                  case "ERROR" -> gson.fromJson(jsonObject, ErrorMessage.class);
                  case "NOTIFICATION" -> gson.fromJson(jsonObject, NotificationMessage.class);
                  default -> gson.fromJson(jsonObject, ServerMessage.class);
                };

            serverMessageHandler.notify(serverMessage);
          });
    } catch (DeploymentException | IOException | URISyntaxException ex) {
      throw new ResponseException(500, ex.getMessage());
    }
  }

  @Override
  public void onOpen(Session session, EndpointConfig endpointConfig) {}

  public void connectToGame(String authToken, int gameID) throws ResponseException {
    try {
      UserGameCommand command =
          new UserGameCommand(UserGameCommand.CommandType.CONNECT, authToken, gameID);
      this.session.getBasicRemote().sendText(new Gson().toJson(command));
    } catch (IOException ex) {
      throw new ResponseException(500, ex.getMessage());
    }
  }

  public void makeMove(String authToken, int gameID, ChessMove move) throws ResponseException {
    try {
      MakeMoveCommand command =
          new MakeMoveCommand(UserGameCommand.CommandType.MAKE_MOVE, authToken, gameID, move);
      this.session.getBasicRemote().sendText(new Gson().toJson(command));
    } catch (IOException ex) {
      throw new ResponseException(500, ex.getMessage());
    }
  }

  public void leaveGame(String authToken, int gameID) throws ResponseException {
    try {
      UserGameCommand command =
          new UserGameCommand(UserGameCommand.CommandType.LEAVE, authToken, gameID);
      this.session.getBasicRemote().sendText(new Gson().toJson(command));
    } catch (IOException ex) {
      throw new ResponseException(500, ex.getMessage());
    }
  }

  public void resign(String authToken, int gameID) throws ResponseException {
    try {
      UserGameCommand command =
          new UserGameCommand(UserGameCommand.CommandType.RESIGN, authToken, gameID);
      this.session.getBasicRemote().sendText(new Gson().toJson(command));
    } catch (IOException ex) {
      throw new ResponseException(500, ex.getMessage());
    }
  }
}
