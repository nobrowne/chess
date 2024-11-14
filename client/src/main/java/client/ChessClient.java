package client;

import exception.ResponseException;
import java.util.Arrays;
import model.AuthData;
import model.UserData;
import serverfacade.ServerFacade;

public class ChessClient {
  private final ServerFacade server;
  private final String serverUrl;
  private final State state = State.SIGNEDOUT;
  private String username;
  private String authToken;

  public ChessClient(String serverUrl) {
    server = new ServerFacade(serverUrl);
    this.serverUrl = serverUrl;
  }

  public String eval(String input) {
    try {
      var tokens = input.toLowerCase().split(" ");
      var command = (tokens.length > 0) ? tokens[0] : "help";
      var params = Arrays.copyOfRange(tokens, 1, tokens.length);
      return switch (command) {
        case "register" -> register(params);
        case "login" -> login(params);
        case "quit" -> "quit";
        default -> help();
      };
    } catch (ResponseException ex) {
      return ex.getMessage();
    }
  }

  public String register(String... params) throws ResponseException {
    if (params.length == 3) {
      username = params[0];
      String password = params[1];
      String email = params[2];
      UserData user = new UserData(username, password, email);

      AuthData authData = server.register(user);
      authToken = authData.authToken();

      return String.format("You are registered as %s", username);
    }
    throw new ResponseException(
        400,
        "At least one of the required inputs is missing. Please provide username, password, and email");
  }

  public String login(String... params) {
    return null;
    // print the help statement for SIGNEDIN
  }

  public String help() {
    return null;
  }
}
