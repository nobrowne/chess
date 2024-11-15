package serverfacade;

import com.google.gson.Gson;
import exception.ResponseException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import model.AuthData;
import model.ExceptionDTO;
import model.GameData;
import model.UserData;
import request.JoinGameRequest;
import request.LoginRequest;
import result.LoginResult;

public class ServerFacade {
  private final String serverURL;

  public ServerFacade(String url) {
    this.serverURL = url;
  }

  private static void writeBody(Object request, HttpURLConnection http) throws IOException {
    if (request != null) {
      String reqData = new Gson().toJson(request);
      try (OutputStream reqBody = http.getOutputStream()) {
        reqBody.write(reqData.getBytes());
      }
    }
  }

  private static <T> T readBody(HttpURLConnection http, Class<T> responseClass) throws IOException {
    T response = null;
    if (http.getContentLength() < 0) {
      try (InputStream respBody = http.getInputStream()) {
        InputStreamReader reader = new InputStreamReader(respBody);
        if (responseClass != null) {
          response = new Gson().fromJson(reader, responseClass);
        }
      }
    }
    return response;
  }

  public AuthData register(UserData user) throws ResponseException {
    String path = "/user";
    return this.makeRequest("POST", path, user, AuthData.class, null);
  }

  public LoginResult login(LoginRequest loginRequest) throws ResponseException {
    String path = "/session";
    return this.makeRequest("POST", path, loginRequest, LoginResult.class, null);
  }

  public void logout(String authToken) throws ResponseException {
    String path = "/session";
    this.makeRequest("DELETE", path, null, null, authToken);
  }

  public ArrayList<GameData> listGames(String authToken) throws ResponseException {
    String path = "/game";

    record listGamesResponse(ArrayList<GameData> games) {}

    var response = this.makeRequest("GET", path, null, listGamesResponse.class, authToken);
    return response.games;
  }

  public int createGame(String gameName, String authToken) throws ResponseException {
    String path = "/game";

    record CreateGameRequest(String gameName) {}
    var createGameRequest = new CreateGameRequest(gameName);

    record CreateGameResponse(int gameID) {}
    var response =
        this.makeRequest("POST", path, createGameRequest, CreateGameResponse.class, authToken);

    return response.gameID();
  }

  public void joinGame(String authToken, JoinGameRequest joinGameRequest) throws ResponseException {
    String path = "/game";

    this.makeRequest("PUT", path, joinGameRequest, null, authToken);
  }

  public void clearApplication() throws ResponseException {
    String path = "/db";
    this.makeRequest("DELETE", path, null, null, null);
  }

  private <T> T makeRequest(
      String method, String path, Object request, Class<T> responseClass, String authToken)
      throws ResponseException {
    try {
      URL url = (new URI(serverURL + path)).toURL();
      HttpURLConnection http = (HttpURLConnection) url.openConnection();
      http.setRequestMethod(method);
      http.setDoOutput(true);

      http.setRequestProperty("Content-Type", "application/json");

      if (authToken != null && !authToken.isEmpty()) {
        http.setRequestProperty("Authorization", authToken);
      }

      writeBody(request, http);
      http.connect();
      throwIfNotSuccessful(http);
      return readBody(http, responseClass);
    } catch (ResponseException ex) {
      throw ex;
    } catch (Exception ex) {
      throw new ResponseException(500, ex.getMessage());
    }
  }

  private void throwIfNotSuccessful(HttpURLConnection http) throws IOException, ResponseException {
    var status = http.getResponseCode();
    if (isSuccessful(status)) {
      return;
    }
    try (InputStream respError = http.getErrorStream()) {
      if (respError != null) {
        InputStreamReader reader = new InputStreamReader(respError);
        ExceptionDTO exDTO = new Gson().fromJson(reader, ExceptionDTO.class);
        throw new ResponseException(status, exDTO.message());
      }
    }
  }

  private boolean isSuccessful(int status) {
    return status / 100 == 2;
  }
}
