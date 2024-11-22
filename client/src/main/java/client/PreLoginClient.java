package client;

import exception.ResponseException;
import java.util.Arrays;
import request.RegisterRequest;
import result.RegisterResult;
import serverfacade.ServerFacade;

public class PreLoginClient implements ClientInterface {
  private final ChessClient chessClient;
  private final ServerFacade serverFacade;

  public PreLoginClient(ChessClient chessClient, ServerFacade serverFacade) {
    this.chessClient = chessClient;
    this.serverFacade = serverFacade;
  }

  @Override
  public String eval(String input) {
    try {
      var tokens = input.toLowerCase().split(" ");
      var command = tokens.length > 0 ? tokens[0] : "help";
      var params = Arrays.copyOfRange(tokens, 1, tokens.length);

      return switch (command) {
        case "register" -> register(params);
        //        case "login" -> login(params);
        //        case "quit" -> quit();
        default -> help();
      };
    } catch (ResponseException ex) {
      return ex.getMessage();
    }
  }

  public String register(String... params) throws ResponseException {
    if (params.length < 3) {
      throw new ResponseException(400, "Error: username, password, and email must all be filled");
    }

    String username = params[0];
    String password = params[1];
    String email = params[2];
    RegisterRequest request = new RegisterRequest(username, password, email);
    RegisterResult result = serverFacade.register(request);

    chessClient.setAuthToken(result.authToken());
    chessClient.setState(State.SIGNEDIN);
    chessClient.setCurrentClient(new PostLoginClient(chessClient, serverFacade));

    return String.format("You are registered as %s", username);
  }

  @Override
  public String help() {
    return """
            - register <USERNAME> <PASSWORD> <EMAIL>: create an account
            - login <USERNAME> <PASSWORD>: play chess
            - quit: shut down the application
            - help: see possible commands
          """;
  }
}
