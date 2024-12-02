package client.websocket;

import com.google.gson.Gson;
import exception.ResponseException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import javax.websocket.*;
import websocket.commands.UserGameCommand;
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
          new MessageHandler.Whole<String>() {
            @Override
            public void onMessage(String message) {
              ServerMessage serverMessage = new Gson().fromJson(message, ServerMessage.class);
              serverMessageHandler.notify(serverMessage);
            }
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
}
