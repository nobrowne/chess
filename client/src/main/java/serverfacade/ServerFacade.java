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
import model.AuthData;
import model.UserData;

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
        http.setRequestProperty("Authorization:", authToken);
      }

      writeBody(request, http);
      http.connect();
      throwIfNotSuccessful(http);
      return readBody(http, responseClass);
    } catch (Exception ex) {
      throw new ResponseException(500, ex.getMessage());
    }
  }

  private void throwIfNotSuccessful(HttpURLConnection http) throws IOException, ResponseException {
    var status = http.getResponseCode();
    if (!isSuccessful(status)) {
      throw new ResponseException(status, "failure: " + status);
    }
  }

  private boolean isSuccessful(int status) {
    return status / 100 == 2;
  }
}
