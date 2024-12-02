package server.websocket;

import com.google.gson.Gson;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import org.eclipse.jetty.websocket.api.Session;
import websocket.messages.ServerMessage;

public class ConnectionManager {
  public final ConcurrentHashMap<String, Connection> connections = new ConcurrentHashMap<>();
  public final ConcurrentHashMap<Integer, ArrayList<Connection>> gameConnections =
      new ConcurrentHashMap<>();

  public void add(String authToken, int gameID, Session session) {
    Connection connection = new Connection(authToken, session);
    connections.put(authToken, connection);
    gameConnections.computeIfAbsent(gameID, key -> new ArrayList<>()).add(connection);
  }

  public void remove(String authToken, int gameID) {
    connections.remove(authToken);

    ArrayList<Connection> gameList = gameConnections.get(gameID);
    if (gameList != null) {
      gameList.removeIf(connection -> connection.authToken.equals(authToken));
      if (gameList.isEmpty()) {
        gameConnections.remove(gameID);
      }
    }
  }

  public void broadcastToRootClient(String authToken, ServerMessage serverMessage)
      throws IOException {
    Connection c = connections.get(authToken);
    if (c.session.isOpen()) {
      c.send(new Gson().toJson(serverMessage));
    }
  }

  public void broadcastToGame(int gameID, String excludeAuthToken, ServerMessage serverMessage)
      throws IOException {
    ArrayList<Connection> gameList = gameConnections.get(gameID);
    if (gameList == null) {
      return;
    }

    var removeList = new ArrayList<Connection>();
    for (Connection c : gameList) {
      if (c.session.isOpen()) {
        if (!c.authToken.equals(excludeAuthToken)) {
          c.send(new Gson().toJson(serverMessage));
        }
      } else {
        removeList.add(c);
      }
    }

    gameList.removeAll(removeList);
    if (gameList.isEmpty()) {
      gameConnections.remove(gameID);
    }
  }
}
