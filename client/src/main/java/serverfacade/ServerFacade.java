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
import model.ExceptionDTO;
import request.CreateGameRequest;
import request.JoinGameRequest;
import request.LoginRequest;
import request.RegisterRequest;
import result.CreateGameResult;
import result.ListGamesResult;
import result.LoginResult;
import result.RegisterResult;

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

  public RegisterResult register(RegisterRequest registerRequest) throws ResponseException {
    return this.makeRequest("POST", "/user", registerRequest, RegisterResult.class, null);
  }

  public LoginResult login(LoginRequest loginRequest) throws ResponseException {
    return this.makeRequest("POST", "/session", loginRequest, LoginResult.class, null);
  }

  public void logout(String authToken) throws ResponseException {
    this.makeRequest("DELETE", "/session", null, null, authToken);
  }

  public ListGamesResult listGames(String authToken) throws ResponseException {
    return this.makeRequest("GET", "/game", null, ListGamesResult.class, authToken);
  }

  public CreateGameResult createGame(CreateGameRequest createGameRequest, String authToken)
      throws ResponseException {
    return this.makeRequest("POST", "/game", createGameRequest, CreateGameResult.class, authToken);
  }

  public void joinGame(JoinGameRequest joinGameRequest, String authToken) throws ResponseException {
    this.makeRequest("PUT", "/game", joinGameRequest, null, authToken);
  }

  public void clearApplication() throws ResponseException {
    this.makeRequest("DELETE", "/db", null, null, null);
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
