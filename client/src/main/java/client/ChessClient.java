package client;

import client.websocket.ServerMessageHandler;
import exception.ResponseException;
import java.util.*;
import model.GameData;
import result.ListGamesResult;
import serverfacade.ServerFacade;

public class ChessClient {
  private final String serverURL;
  private final ServerMessageHandler serverMessageHandler;
  private final ServerFacade serverFacade;
  private final Map<Integer, Integer> externalToInternalGameIDs = new HashMap<>();
  private final Map<Integer, Integer> internalToExternalGameIDs = new HashMap<>();
  private String authToken;
  private ClientInterface currentClient;

  public ChessClient(String serverURL, ServerMessageHandler serverMessageHandler) {
    this.serverURL = serverURL;
    this.serverMessageHandler = serverMessageHandler;
    this.serverFacade = new ServerFacade(serverURL);
    this.authToken = null;
    this.currentClient = new PreLoginClient(this, serverFacade);
  }

  public String getServerURL() {
    return serverURL;
  }

  public ServerMessageHandler getServerMessageHandler() {
    return serverMessageHandler;
  }

  public String getAuthToken() {
    return authToken;
  }

  public void setAuthToken(String authToken) {
    this.authToken = authToken;
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
}
