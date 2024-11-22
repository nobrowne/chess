package client;

import static org.junit.jupiter.api.Assertions.*;

import exception.ResponseException;
import model.UserData;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.Server;
import serverfacade.ServerFacade;

public class ChessClientTests {
  private static ChessClient chessClient;
  private static ServerFacade serverFacade;
  private static Server server;
  private static UserData newUser;
  private static UserData existingUser;

  @BeforeAll
  public static void init() {
    server = new Server();
    int port = server.run(0);

    String serverUrl = "http://localhost:" + port;
    chessClient = new ChessClient(serverUrl);
    serverFacade = new ServerFacade(serverUrl);

    newUser = new UserData("user1", "password", "email@email.com");
    existingUser = new UserData("existingUser", "password", "email@email.com");
  }

  @AfterAll
  public static void stopServer() {
    server.stop();
  }

  @BeforeEach
  public void setUp() throws ResponseException {
    serverFacade.clearApplication();
  }

  public void register() {
    chessClient.eval(
        String.format(
            "register %s %s %s",
            existingUser.username(), existingUser.password(), existingUser.email()));
  }

  //  public void login() {
  //    String loginInfo =
  //        String.format("login %s %s", existingUser.username(), existingUser.password());
  //    chessClient.eval(loginInfo);
  //  }
  //
  public void logout() {
    chessClient.eval("logout");
  }

  //  public void createTestGame() {
  //    String gameName = "fun game";
  //    String createGameInfo = String.format("create %s", gameName);
  //    chessClient.eval(createGameInfo);
  //  }

  @Test
  public void successfulRegistration() {
    String registrationInfo =
        String.format("register %s %s %s", newUser.username(), newUser.password(), newUser.email());
    String registrationMessage = chessClient.eval(registrationInfo);

    assertEquals("You are registered as " + newUser.username(), registrationMessage);

    logout();
  }

  @Test
  public void registeringWithMissingInfo() {
    String registrationInfo =
        String.format("register %s %s", newUser.username(), newUser.password());
    String registrationMessage = chessClient.eval(registrationInfo);

    assertEquals("Error: username, password, and email must all be filled", registrationMessage);
  }

  @Test
  public void registeringWithTakenUsername() {
    register();
    logout();

    String registrationInfo =
        String.format(
            "register %s %s %s", existingUser.username(), newUser.password(), newUser.email());
    String registrationMessage = chessClient.eval(registrationInfo);

    assertEquals("Error: username already taken", registrationMessage);

    logout();
  }

  @Test
  public void successfulLogin() throws ResponseException {
    register();

    String loginInfo =
        String.format("login %s %s", existingUser.username(), existingUser.password());
    String loginMessage = chessClient.eval(loginInfo);

    assertFalse(loginMessage.contains("Error"));
    assertFalse(loginMessage.contains("missing"));

    logout();
  }

  @Test
  public void loggingInWithoutRegisteredUsername() {
    String loginInfo = String.format("login %s %s", newUser.username(), newUser.password());
    String loginMessage = chessClient.eval(loginInfo);

    assertEquals("Error: user has not registered an account yet", loginMessage);
  }

  @Test
  public void loggingInWithMissingInfo() {
    register();
    logout();

    String loginInfo = String.format("login %s", newUser.username());
    String loginMessage = chessClient.eval(loginInfo);

    assertEquals("Error: username and password must be filled", loginMessage);
  }

  @Test
  public void loggingInWithIncorrectPassword() {
    register();
    logout();

    String loginInfo = String.format("login %s %s", existingUser.username(), "badPassword");
    String loginMessage = chessClient.eval(loginInfo);

    assertEquals("Error: invalid password", loginMessage);

    logout();
  }

  @Test
  public void successfulLogout() {
    String loginInfo =
        String.format("login %s %s", existingUser.username(), existingUser.password());
    chessClient.eval(loginInfo);

    logout();

    assertEquals(
        chessClient.eval("help"),
        """
          - register <USERNAME> <PASSWORD> <EMAIL>: create an account
          - login <USERNAME> <PASSWORD>: play chess
          - quit: shut down the application
          - help: see possible commands
        """);
  }

  @Test
  public void successfulLogoutExtraParameters() {
    String loginInfo =
        String.format("login %s %s", existingUser.username(), existingUser.password());
    chessClient.eval(loginInfo);

    chessClient.eval("logout because I'm bored");

    assertEquals(
        """
              - register <USERNAME> <PASSWORD> <EMAIL>: create an account
              - login <USERNAME> <PASSWORD>: play chess
              - quit: shut down the application
              - help: see possible commands
            """,
        chessClient.eval("help"));
  }

  @Test
  public void loggingOutWithoutBeingLoggedIn() {
    String logoutMessage = chessClient.eval("logout because I'm bored");

    assertEquals(
        """
              - register <USERNAME> <PASSWORD> <EMAIL>: create an account
              - login <USERNAME> <PASSWORD>: play chess
              - quit: shut down the application
              - help: see possible commands
            """,
        logoutMessage);
  }
  //
  //  @Test
  //  public void successfulCreateGame() {
  //    register();
  //    login();
  //
  //    String gameName = "what a fun game";
  //    String createGameInfo = String.format("create %s", gameName);
  //    String createGameMessage = chessClient.eval(createGameInfo);
  //
  //    assertEquals("You have created a new game called " + gameName, createGameMessage);
  //
  //    logout();
  //  }
  //
  //  @Test
  //  public void creatingGameWithNoName() {
  //    register();
  //    login();
  //
  //    String createGameMessage = chessClient.eval("create");
  //
  //    assertEquals("Error: game name must be filled", createGameMessage);
  //
  //    logout();
  //  }
  //
  //  @Test
  //  public void creatingGameWithoutLoggingIn() {
  //    String gameName = "what a fun game";
  //    String createGameInfo = String.format("create %s", gameName);
  //    String createGameMessage = chessClient.eval(createGameInfo);
  //
  //    assertEquals("Error: cannot create game if not logged in", createGameMessage);
  //  }
  //
  //  @Test
  //  public void successfulListGames() {
  //    register();
  //    login();
  //
  //    ArrayList<String> gameNames = new ArrayList<>();
  //    gameNames.add("game1");
  //    gameNames.add("game2");
  //    gameNames.add("game3");
  //
  //    for (String gameName : gameNames) {
  //      String createGameInfo = String.format("create %s", gameName);
  //      chessClient.eval(createGameInfo);
  //    }
  //
  //    String listGamesMessage = chessClient.eval("list");
  //
  //    assertFalse(listGamesMessage.contains("Error"));
  //    assertFalse(listGamesMessage.contains("create <GAME NAME>: create a new game"));
  //
  //    logout();
  //  }
  //
  //  @Test
  //  public void listingGamesWithoutLoggingIn() {
  //    String listGamesMessage = chessClient.eval("list");
  //    assertEquals("Error: cannot list games if not logged in", listGamesMessage);
  //  }
  //
  //  @Test
  //  public void successfulJoinGame() {
  //    register();
  //    login();
  //    createTestGame();
  //
  //    String playerColor = "white";
  //    int gameID = 1;
  //    String joinGameMessage = chessClient.eval("join" + " " + playerColor + " " + gameID);
  //
  //    assertEquals("You have joined game " + gameID, joinGameMessage);
  //
  //    logout();
  //  }
  //
  //  @Test
  //  public void joiningGameWithoutLoggingIn() {
  //    register();
  //    login();
  //    createTestGame();
  //    logout();
  //
  //    String joinGameMessage = chessClient.eval("join white 1");
  //
  //    assertEquals("Error: cannot join games if not logged in", joinGameMessage);
  //  }
  //
  //  @Test
  //  public void joiningGameWithMissingInfo() {
  //    register();
  //    login();
  //    createTestGame();
  //
  //    String joinGameMessage = chessClient.eval("join 1");
  //
  //    assertEquals("Error: team color and gameID must be filled", joinGameMessage);
  //
  //    logout();
  //  }
  //
  //  @Test
  //  public void joiningGameWithInvalidTeamColor() {
  //    register();
  //    login();
  //    createTestGame();
  //
  //    String joinGameMessage = chessClient.eval("join green 1");
  //
  //    assertEquals("Error: team color must be 'WHITE' or 'BLACK'", joinGameMessage);
  //
  //    logout();
  //  }
  //
  //  @Test
  //  public void joiningGameWithInvalidGameID() {
  //    register();
  //    login();
  //    createTestGame();
  //
  //    String joinGameMessage = chessClient.eval("join white 7");
  //
  //    assertEquals("Error: invalid gameID", joinGameMessage);
  //
  //    logout();
  //  }
  //
  //  @Test
  //  public void successfulObserveGame() throws ResponseException {
  //    register();
  //    login();
  //    createTestGame();
  //
  //    int externalGameID = 1;
  //    String observeGameMessage = chessClient.eval(String.format("observe %d", externalGameID));
  //
  //    int internalGameID = chessClient.getInternalGameID(externalGameID);
  //    assertEquals("You have chosen to observe game " + internalGameID, observeGameMessage);
  //
  //    logout();
  //  }
  //
  //  @Test
  //  public void observingGameWithInvalidGameID() {
  //    register();
  //    login();
  //    createTestGame();
  //
  //    int gameID = 7;
  //    String observeGameMessage = chessClient.eval(String.format("observe %d", gameID));
  //
  //    assertEquals("Error: invalid gameID", observeGameMessage);
  //
  //    logout();
  //  }
}
