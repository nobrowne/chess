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

  public void add(String username, int gameID, Session session) {
    Connection connection = new Connection(username, session);
    connections.put(username, connection);
    gameConnections.computeIfAbsent(gameID, key -> new ArrayList<>()).add(connection);
  }

  public void remove(String username, int gameID) {
    connections.remove(username);

    ArrayList<Connection> gameList = gameConnections.get(gameID);
    if (gameList != null) {
      gameList.removeIf(connection -> connection.username.equals(username));
      if (gameList.isEmpty()) {
        gameConnections.remove(gameID);
      }
    }
  }

  public void sendToSession(Session session, ServerMessage serverMessage) throws IOException {
    if (session.isOpen()) {
      session.getRemote().sendString(new Gson().toJson(serverMessage));
    }
  }

  public void sendToOthers(int gameID, String excludedUsername, ServerMessage serverMessage)
      throws IOException {
    ArrayList<Connection> gameList = gameConnections.get(gameID);
    if (gameList == null) {
      return;
    }

    var removeList = new ArrayList<Connection>();
    for (Connection c : gameList) {
      if (c.session.isOpen()) {
        if (!c.username.equals(excludedUsername)) {
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
