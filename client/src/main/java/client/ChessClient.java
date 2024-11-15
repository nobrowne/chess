package client;

import exception.ResponseException;
import java.util.Arrays;
import request.LoginRequest;
import request.RegisterRequest;
import result.LoginResult;
import result.RegisterResult;
import serverfacade.ServerFacade;

public class ChessClient {
  private final ServerFacade server;
  private State state = State.SIGNEDOUT;
  private String authToken;

  public ChessClient(String serverUrl) {
    server = new ServerFacade(serverUrl);
  }

  public String eval(String input) {
    try {
      var tokens = input.toLowerCase().split(" ");
      var command = (tokens.length > 0) ? tokens[0] : "help";
      var params = Arrays.copyOfRange(tokens, 1, tokens.length);
      return switch (command) {
        case "register" -> register(params);
        case "login" -> login(params);
        case "logout" -> logout();
        case "quit" -> "quit";
        default -> help();
      };
    } catch (ResponseException ex) {
      return ex.getMessage();
    }
  }

  public String register(String... params) throws ResponseException {
    if (params.length < 3) {
      throw new ResponseException(400, "error: username, password, and email must all be filled");
    }

    String username = params[0];
    String password = params[1];
    String email = params[2];

    RegisterRequest request = new RegisterRequest(username, password, email);

    RegisterResult result = server.register(request);
    authToken = result.authToken();

    return String.format("You are registered as %s", username);
  }

  public String login(String... params) throws ResponseException {
    if (params.length < 2) {
      throw new ResponseException(400, "error: username and password must be filled");
    }

    String username = params[0];
    String password = params[1];

    LoginRequest request = new LoginRequest(username, password);

    LoginResult result = server.login(request);
    state = State.SIGNEDIN;
    authToken = result.authToken();

    return String.format("You have logged in as %s%n", username) + help();
  }

  public String logout() throws ResponseException {
    if (state != State.SIGNEDIN) {
      throw new ResponseException(400, "error: cannot log out if not logged in");
    }

    server.logout(authToken);
    state = State.SIGNEDOUT;

    return "You have logged out";
  }

  public String help() {
    if (state == State.SIGNEDOUT) {
      return """
        - register <USERNAME> <PASSWORD> <EMAIL>: create an account
        - login <USERNAME> <PASSWORD>: play chess
        - quit: shut down the application
        - help: see possible commands
      """;
    }

    return """
      - create <GAME NAME>: create a new game
      - list: list all games
      - join <GAME ID> <WHITE|BLACK>: join a game as the white or black team
      - observe: join a game as a non-player observer
      - logout: leave the application
      - quit: shut down the application
      - help: see possible commands
    """;
  }
}
