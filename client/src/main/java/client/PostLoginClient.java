package client;

import serverfacade.ServerFacade;

public class PostLoginClient implements ClientInterface {
  private final ChessClient chessClient;
  private final ServerFacade serverFacade;

  public PostLoginClient(ChessClient chessClient, ServerFacade serverFacade) {
    this.chessClient = chessClient;
    this.serverFacade = serverFacade;
  }

  @Override
  public String eval(String input) {
    return "";
  }

  @Override
  public String help() {
    return """
          - create <GAME NAME>: create a new game
          - list: list all games
          - join <WHITE|BLACK> <GAME ID>: join a game as the white or black team
          - observe: join a game as a non-player observer
          - logout: leave the application
          - help: see possible commands
        """;
  }
}
